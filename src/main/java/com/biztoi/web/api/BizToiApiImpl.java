package com.biztoi.web.api;

import com.biztoi.api.ApiApi;
import com.biztoi.model.*;
import com.biztoi.web.service.DataQueryService;
import com.biztoi.web.service.RakutenApiService;
import com.biztoi.web.utils.BooksUtils;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RestControllerAdvice
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BizToiApiImpl implements ApiApi {

    @NonNull
    RakutenApiService rakutenApiService;

    @NonNull
    DataQueryService queryService;

    private static final Logger log = LoggerFactory.getLogger(BizToiApiImpl.class);

    private static String userId = "a8554f4c-569c-414c-bddd-c47707e241e1";

    @Override
    public ResponseEntity<Authorize> authGetToken(@NotNull @Valid String code, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public ResponseEntity<Void> authLogin(@NotNull @Valid String redirectUri, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public ResponseEntity<Flux<Book>> books(ServerWebExchange exchange) {
        List<Item> items = this.rakutenApiService.getSalesBooks();
        if (items == null) {
            return ResponseEntity.notFound().build();
        }

        List<String> bookFavList = this.queryService.isFavoriteBooks(userId);
        List<Book> books = items.stream()
                .map(BooksUtils::to)
                .map(b -> b.favorite(bookFavList.contains(b.getIsbn())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(Flux.fromIterable(books));
    }

    @Override
    public ResponseEntity<Void> favoriteBooks(@Valid Mono<SendLikeInfo> sendLikeInfo, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        sendLikeInfo.subscribe(likeInfo -> this.queryService.createLike(likeInfo.getId(), "book", userId));
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<AnswerHead> getAnswerHead(String bookId, String answerHeadId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        AnswerHead result = this.queryService.getAnswerHeadList(userId, bookId, null, false).stream()
                .filter(answerHead -> answerHead.getId().equals(answerHeadId)).findFirst().orElse(null);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Flux<AnswerHead>> getAnswerHeadList(String bookId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return ResponseEntity.ok(Flux.fromIterable(this.queryService.getAnswerHeadList(userId, bookId, 50, false)));
    }

    @Override
    public ResponseEntity<AnswerHead> getAnswerHeadMe(String bookId, String answerHeadId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        AnswerHead result = this.queryService.getAnswerHeadList(userId, bookId, null, true).stream()
                .filter(answerHead -> answerHead.getId().equals(answerHeadId)).findFirst().orElse(null);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Void> deleteFavoriteBooks(@Valid Mono<SendLikeInfo> sendLikeInfo, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        sendLikeInfo.subscribe(likeInfo -> this.queryService.deleteLike(likeInfo.getId(), "book", userId));
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> likesAnswers(@Valid Mono<SendLikeInfo> sendLikeInfo, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        sendLikeInfo.subscribe(likeInfo -> this.queryService.createLike(likeInfo.getId(), "answer", userId));
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteLikesAnswers(@Valid Mono<SendLikeInfo> sendLikeInfo, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        sendLikeInfo.subscribe(likeInfo -> this.queryService.deleteLike(likeInfo.getId(), "answer", userId));
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Flux<AnswerHead>> getAnswerHeadMeList(String bookId, ServerWebExchange exchange) {
        return ResponseEntity.ok(Flux.fromIterable(this.queryService.getAnswerHeadList(userId, bookId, 50, true)));
    }

    @Override
    public ResponseEntity<Flux<Answer>> getAnswerMeByQuestion(String bookId, String answerHeadId, String questionId, ServerWebExchange exchange) {
        return ResponseEntity.ok(Flux.fromIterable(this.queryService.getAnswerMeByQuestion(answerHeadId, questionId, userId)));
    }

    @Override
    public ResponseEntity<Book> getBookId(String bookId, ServerWebExchange exchange) {
        Item item = this.rakutenApiService.findBook(bookId);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }

        List<String> bookFavList = this.queryService.isFavoriteBooks(userId);
        return ResponseEntity.ok(BooksUtils.to(item)
                .favorite(bookFavList.contains(bookId)));
    }

    @Override
    public ResponseEntity<Question> getBookQuestion(String bookId, String questionId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        log.info("questionId {}", questionId);
        return ResponseEntity.ok(this.queryService.findQuestion(questionId));
    }

    @Override
    public ResponseEntity<Flux<Question>> getBookQuestions(String bookId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return ResponseEntity.ok(Flux.fromIterable(this.queryService.findQuestionsAll()));
    }

    @Override
    public ResponseEntity<Toi> getBookToi(String bookId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return ResponseEntity.ok(this.queryService.findToi());
    }

    @Override
    public ResponseEntity<AnswerHead> postAnswerHead(String bookId, @Valid Mono<AnswerHead> answerHead, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return ResponseEntity.ok(this.queryService.insertAnswerHead(bookId, userId));
    }

    @Override
    public ResponseEntity<Flux<Answer>> postAnswerMeByQuestion(String bookId, String answerHeadId, String questionId, @Valid Mono<AnswerList> answerList, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return ResponseEntity.ok(Flux.fromIterable(this.queryService.insertAnswers(questionId, answerList.block())));
    }

    // 未使用
    @Override
    public ResponseEntity<Void> booksPost(@Valid Mono<Book> book, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return null;
    }

    // 未使用
    @Override
    public ResponseEntity<Void> postQuestion(String bookId, @Valid Mono<Question> question, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return null;
    }

    // 未使用
    @Override
    public ResponseEntity<Toi> postToi(String bookId, @Valid Mono<Toi> toi, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return null;
    }

    // TODO stub
    @Override
    public ResponseEntity<BizToiUser> userInfo(ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return ResponseEntity.ok(new BizToiUser().id(userId)
                .country("ja").email("biztoi@biztoi.com")
                .nickname("biz").pictureUrl("https://picsum.photos/100/100"));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ FeignException.class })
    public void feignExceptionHandler(FeignException fe) {
        log.error(fe.getMessage());
    }

}
