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
    public Mono<Authorize> authGetToken(@NotNull @Valid String code, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<Void> authLogin(@NotNull @Valid String redirectUri, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Flux<Book> books(ServerWebExchange exchange) {
        List<Item> items = this.rakutenApiService.getSalesBooks();
        if (items == null) {
            return Flux.empty();
        }

        List<String> bookFavList = this.queryService.isFavoriteBooks(userId);
        List<Book> books = items.stream()
                .map(BooksUtils::to)
                .map(b -> b.favorite(bookFavList.contains(b.getIsbn())))
                .collect(Collectors.toList());
        return Flux.fromIterable(books);
    }

    @Override
    public Mono<Void> favoriteBooks(@Valid Mono<SendLikeInfo> sendLikeInfo, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return sendLikeInfo
                .map(likeInfo -> this.queryService.createLike(likeInfo.getId(), "book", userId))
                .then();
    }

    @Override
    public Mono<AnswerHead> getAnswerHead(String bookId, String answerHeadId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        AnswerHead result = this.queryService.getAnswerHeadList(userId, bookId, null, false).stream()
                .filter(answerHead -> answerHead.getId().equals(answerHeadId)).findFirst().orElse(null);
        return Mono.just(result);
    }

    @Override
    public Flux<AnswerHead> getAnswerHeadList(String bookId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return Flux.fromIterable(this.queryService.getAnswerHeadList(userId, bookId, 50, false));
    }

    @Override
    public Mono<AnswerHead> getAnswerHeadMe(String bookId, String answerHeadId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        AnswerHead result = this.queryService.getAnswerHeadList(userId, bookId, null, true).stream()
                .filter(answerHead -> answerHead.getId().equals(answerHeadId)).findFirst().orElse(null);
        return Mono.just(result);
    }

    @Override
    public Mono<Void> deleteFavoriteBooks(@Valid Mono<SendLikeInfo> sendLikeInfo, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return sendLikeInfo
                .map(likeInfo -> this.queryService.deleteLike(likeInfo.getId(), "book", userId))
                .then();
    }

    @Override
    public Mono<Void> likesAnswers(@Valid Mono<SendLikeInfo> sendLikeInfo, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return sendLikeInfo
                .map(likeInfo -> this.queryService.createLike(likeInfo.getId(), "answer", userId))
                .then();
    }

    @Override
    public Mono<Void> deleteLikesAnswers(@Valid Mono<SendLikeInfo> sendLikeInfo, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return sendLikeInfo
                .map(likeInfo -> this.queryService.deleteLike(likeInfo.getId(), "answer", userId))
                .then();
    }

    @Override
    public Flux<AnswerHead> getAnswerHeadMeList(String bookId, ServerWebExchange exchange) {
        return Flux.fromIterable(this.queryService.getAnswerHeadList(userId, bookId, 50, true));
    }

    @Override
    public Flux<Answer> getAnswerMeByQuestion(String bookId, String answerHeadId, String questionId, ServerWebExchange exchange) {
        return Flux.fromIterable(this.queryService.getAnswerMeByQuestion(answerHeadId, questionId, userId));
    }

    @Override
    public Mono<Book> getBookId(String bookId, ServerWebExchange exchange) {
        Item item = this.rakutenApiService.findBook(bookId);
        if (item == null) {
            return Mono.empty();
        }

        List<String> bookFavList = this.queryService.isFavoriteBooks(userId);
        return Mono.just(BooksUtils.to(item)
                .favorite(bookFavList.contains(bookId)));
    }

    @Override
    public Mono<Question> getBookQuestion(String bookId, String questionId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        log.info("questionId {}", questionId);
        return Mono.just(this.queryService.findQuestion(questionId));
    }

    @Override
    public Flux<Question> getBookQuestions(String bookId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return Flux.fromIterable(this.queryService.findQuestionsAll());
    }

    @Override
    public Mono<Toi> getBookToi(String bookId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return Mono.just(this.queryService.findToi());
    }

    @Override
    public Mono<AnswerHead> postAnswerHead(String bookId, @Valid Mono<AnswerHead> answerHead, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return Mono.just(this.queryService.insertAnswerHead(bookId, userId));
    }

    @Override
    public Flux<Answer> postAnswerMeByQuestion(String bookId, String answerHeadId, String questionId, @Valid Mono<AnswerList> answerList, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return answerList
                .map(answer -> this.queryService.insertAnswers(questionId, answer))
                .flatMapMany(Flux::fromIterable);
    }

    // 未使用
    @Override
    public Mono<Void> booksPost(@Valid Mono<Book> book, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return null;
    }

    // 未使用
    @Override
    public Mono<Void> postQuestion(String bookId, @Valid Mono<Question> question, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return null;
    }

    // 未使用
    @Override
    public Mono<Toi> postToi(String bookId, @Valid Mono<Toi> toi, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return null;
    }

    // TODO stub
    @Override
    public Mono<BizToiUser> userInfo(ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return Mono.just(new BizToiUser().id(userId)
                .country("ja").email("biztoi@biztoi.com")
                .nickname("biz").pictureUrl("https://picsum.photos/100/100"));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ FeignException.class })
    public void feignExceptionHandler(FeignException fe) {
        log.error(fe.getMessage());
    }

}
