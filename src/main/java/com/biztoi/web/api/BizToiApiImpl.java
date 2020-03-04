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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@CrossOrigin
@RestController
@RestControllerAdvice
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BizToiApiImpl implements ApiApi {
    @NonNull
    RakutenApiService rakutenApiService;

    @NonNull DataQueryService queryService;

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
    public ResponseEntity<Void> booksPost(@Valid Mono<Book> book, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return null;
    }

    @Override
    public ResponseEntity<Void> favoriteBooks(@Valid Mono<SendLikeInfo> sendLikeInfo, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        sendLikeInfo.subscribe(likeInfo -> this.queryService.createLike(likeInfo.getId(), "book", userId));
        return ResponseEntity.ok().build();
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

    // TODO stub
    @Override
    public ResponseEntity<AnswerHead> getAnswer(String bookId, String answerId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return ResponseEntity.ok(this.getStubAnswerHead(answerId, bookId));
    }

    // TODO stub
    @Override
    public ResponseEntity<Flux<Answer>> getAnswerByQuestion(String bookId, String questionId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return ResponseEntity.ok(Flux.fromIterable(
                this.getStubAnswerList().stream()
                .filter(ans -> ans.getQuestionId().equals(questionId))
                .collect(Collectors.toList())));
    }

    @Override
    public ResponseEntity<Flux<AnswerHead>> getAnswers(String bookId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return ResponseEntity.ok(Flux.fromIterable(this.queryService.getAnswers(userId, bookId, 50)));
    }

    @Override
    public ResponseEntity<AnswerHead> getAnswerHeadsMe(String bookId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return ResponseEntity.ok(this.queryService.getAnswerHeadMe(bookId, userId));
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
    public ResponseEntity<Flux<Answer>> postAnswer(String bookId, String questionId, @Valid Mono<AnswerList> answerList, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return ResponseEntity.ok(Flux.fromIterable(this.queryService.insertAnswers(questionId, answerList.block())));
    }

    @Override
    public ResponseEntity<AnswerHead> postAnswerHead(String bookId, @Valid Mono<AnswerHead> answerHead, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return ResponseEntity.ok(this.queryService.insertAnswerHead(bookId, userId));
    }

    @Override
    public ResponseEntity<Void> postQuestion(String bookId, @Valid Mono<Question> question, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return null;
    }

    @Override
    public ResponseEntity<Toi> postToi(String bookId, @Valid Mono<Toi> toi, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return null;
    }

    @Override
    public ResponseEntity<BizToiUser> userInfo(ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return ResponseEntity.ok(new BizToiUser().id(UUID.randomUUID().toString())
                .country("ja").email("biztoi@biztoi.com")
                .nickname("biz").pictureUrl("https://picsum.photos/100/100"));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ FeignException.class })
    public void feignExceptionHandler(FeignException fe) {
        log.error(fe.getMessage());
    }



    static Random random = new Random();
    static LocalDateTime date = LocalDateTime.now();

    private Answer getStubAnswer(int orderId, String answerType, String questionId) {
        return new Answer()
                .id(UUID.randomUUID().toString())
                .answer(random.nextBoolean() ? "チームで力を発揮する" : "朝礼後にチームメンバーを集めて共通目標の有用性について説明する。\n3日後までに目標を考えてくるようお願いする、\n3日後の朝礼後に再度集まり30分間チームメンバーで目標案を検討し決定する")
                .answerHeadId(UUID.randomUUID().toString())
                .questionId(questionId)
                .orderId(orderId)
                .inserted(date.toString())
                .modified(date.plusDays(5).toString());
    }

    private AnswerHead getStubAnswerHead(String answerId, String bookId) {
        return new AnswerHead()
                .id(answerId)
                .bookId(bookId)
                .publishFlg(true)
                .userId(UUID.randomUUID().toString())
                .answers(this.getStubAnswerList())
                .userInfo(new BizToiUser()
                        .id(UUID.randomUUID().toString())
                        .pictureUrl("https://picsum.photos/30/30")
                        .nickname("Biztoi Nick")
                        .country("jp")
                        .email("biztoi.tool@gmail.com")
                )
                .likeInfo(new AnswerLikes()
                        .active(random.nextBoolean())
                        .sum(random.nextInt(100))
                )
                .inserted(date.toString())
                .modified(date.plusDays(5).toString());
    }

    private List<Answer> getStubAnswerList() {
        return Arrays.asList(
                this.getStubAnswer(1, "OBJECTIVE", "00000-00000-11111"),
                this.getStubAnswer(2, "OBJECTIVE", "00000-00000-11111"),
                this.getStubAnswer(1, "GIST", "00000-00000-22222"),
                this.getStubAnswer(2, "GIST", "00000-00000-22222"),
                this.getStubAnswer(1, "GIST", "00000-00000-33333"),
                this.getStubAnswer(1, "GIST", "00000-00000-44444"),
                this.getStubAnswer(1, "ACTION_PLAN", "00000-00000-55555")
        );
    }

}
