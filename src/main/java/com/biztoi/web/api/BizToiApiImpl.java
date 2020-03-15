package com.biztoi.web.api;

import com.biztoi.api.ApiApi;
import com.biztoi.model.*;
import com.biztoi.web.service.DataQueryService;
import com.biztoi.web.service.RakutenApiService;
import com.biztoi.web.utils.BooksUtils;
import com.biztoi.web.utils.PrincipalUtils;
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
        return exchange.getPrincipal()
                .map(PrincipalUtils::getUserId)
                .map(userId -> {
                    List<Item> items = this.rakutenApiService.getSalesBooks();
                    List<String> bookFavList = this.queryService.isFavoriteBooks(userId);
                    return items.stream()
                            .map(BooksUtils::to)
                            .map(b -> b.favorite(bookFavList.contains(b.getIsbn())))
                            .collect(Collectors.toList());
                })
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Void> favoriteBooks(@Valid SendLikeInfo sendLikeInfo, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return exchange.getPrincipal()
                .map(PrincipalUtils::getUserId)
                .map(userId -> this.queryService.createLike(sendLikeInfo.getId(), "book", userId))
                .then();
    }

    @Override
    public Mono<AnswerHead> getAnswerHead(String bookId, String answerHeadId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return exchange.getPrincipal()
                .map(PrincipalUtils::getUserId)
                .map(userId -> this.queryService.getAnswerHeadList(userId, bookId, null, false).stream()
                            .filter(answerHead -> answerHead.getId().equals(answerHeadId)).findFirst().orElse(null));
    }

    @Override
    public Flux<AnswerHead> getAnswerHeadList(String bookId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return exchange.getPrincipal()
                .map(PrincipalUtils::getUserId)
                .map(userId ->
                        this.queryService.getAnswerHeadList(userId, bookId, 50, false))
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<AnswerHead> getAnswerHeadMe(String bookId, String answerHeadId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return exchange.getPrincipal()
                .map(PrincipalUtils::getUserId)
                .map(userId -> this.queryService.getAnswerHeadList(userId, bookId, null, true).stream()
                                .filter(answerHead -> answerHead.getId().equals(answerHeadId)).findFirst().orElse(null));
    }

    @Override
    public Mono<Void> deleteFavoriteBooks(@Valid SendLikeInfo sendLikeInfo, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return exchange.getPrincipal()
                .map(PrincipalUtils::getUserId)
                .map(userId -> this.queryService.deleteLike(sendLikeInfo.getId(), "book", userId))
                .then();
    }

    @Override
    public Mono<Void> likesAnswers(@Valid SendLikeInfo sendLikeInfo, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return exchange.getPrincipal()
                .map(PrincipalUtils::getUserId)
                .map(userId -> this.queryService.createLike(sendLikeInfo.getId(), "answer", userId))
                .then();
    }

    @Override
    public Mono<Void> deleteLikesAnswers(@Valid SendLikeInfo sendLikeInfo, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return exchange.getPrincipal()
                .map(PrincipalUtils::getUserId)
                .map(userId -> this.queryService.deleteLike(sendLikeInfo.getId(), "answer", userId))
                .then();
    }

    @Override
    public Flux<AnswerHead> getAnswerHeadMeList(String bookId, ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .map(PrincipalUtils::getUserId)
                .map(userId ->
                        this.queryService.getAnswerHeadList(userId, bookId, 50, true))
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Flux<Answer> getAnswerMeByQuestion(String bookId, String answerHeadId, String questionId, ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .map(PrincipalUtils::getUserId)
                .map(userId ->
                        this.queryService.getAnswerMeByQuestion(answerHeadId, questionId, userId))
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Book> getBookId(String bookId, ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .map(PrincipalUtils::getUserId)
                .map(userId -> {
                    List<String> bookFavList = this.queryService.isFavoriteBooks(userId);
                    return BooksUtils.to(this.rakutenApiService.findBook(bookId))
                            .favorite(bookFavList.contains(bookId));
                });
    }

    @Override
    public Mono<Question> getBookQuestion(String bookId, String questionId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
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
    public Mono<AnswerHead> postAnswerHead(String bookId, @Valid AnswerHead answerHead, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return exchange.getPrincipal()
                .map(PrincipalUtils::getUserId)
                .map(userId -> this.queryService.insertAnswerHead(bookId, userId));
    }

    @Override
    public Flux<Answer> postAnswerMeByQuestion(String bookId, String answerHeadId, String questionId, @Valid AnswerList answerList, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return Flux.fromIterable(this.queryService.insertAnswers(questionId, answerList));
    }

    // 未使用
    @Override
    public Mono<Void> booksPost(@Valid Book book, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return null;
    }

    // 未使用
    @Override
    public Mono<Void> postQuestion(String bookId, @Valid Question question, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return null;
    }

    // 未使用
    @Override
    public Mono<Toi> postToi(String bookId, @Valid Toi toi, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return null;
    }

    @Override
    public Mono<BizToiUser> userInfo(ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .map(PrincipalUtils::getBizToiUser);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ FeignException.class })
    public void feignExceptionHandler(FeignException fe) {
        log.error(fe.getMessage());
    }

}
