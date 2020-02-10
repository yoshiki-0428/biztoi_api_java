package com.biztoi.web.api;

import com.biztoi.api.BooksApi;
import com.biztoi.model.*;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RestControllerAdvice
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${openapi.bizToi.base-path:/api}")
public class BooksApiImpl implements BooksApi {
    @NonNull
    RakutenApiService rakutenApiService;

    private static final Logger log = LoggerFactory.getLogger(BooksApiImpl.class);

    @Override
    public ResponseEntity<Flux<Book>> books(ServerWebExchange exchange) {
        List<Item> items = this.rakutenApiService.getSalesBooks();
        if (items == null) {
            return ResponseEntity.notFound().build();
        }

        List<Book> books = items.stream()
                .map(BooksUtils::to)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Flux.fromIterable(books));
    }

    @Override
    public ResponseEntity<Void> booksPost(@Valid Mono<Book> book, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public ResponseEntity<AnswerHead> getAnswer(String bookId, String answerId, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public ResponseEntity<Flux<Answer>> getAnswerByQuestion(String bookId, String questionId, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public ResponseEntity<Flux<AnswerHead>> getAnswers(String bookId, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public ResponseEntity<AnswerHead> getAnswersMe(String bookId, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public ResponseEntity<Book> getBookId(String bookId, ServerWebExchange exchange) {
        Item item = this.rakutenApiService.findBook(bookId);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(BooksUtils.to(item));
    }

    @Override
    public ResponseEntity<Question> getBookQuestion(String bookId, String questionId, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public ResponseEntity<Flux<Question>> getBookQuestions(String bookId, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public ResponseEntity<Toi> getBookToi(String bookId, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public ResponseEntity<Flux<Answer>> postAnswer(String bookId, String questionId, @Valid Mono<AnswerList> answerList, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public ResponseEntity<AnswerHead> postAnswerHead(String bookId, @Valid Mono<AnswerHead> answerHead, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public ResponseEntity<Void> postQuestion(String bookId, @Valid Mono<Question> question, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public ResponseEntity<Toi> postToi(String bookId, @Valid Mono<Toi> toi, ServerWebExchange exchange) {
        return null;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ FeignException.class })
    public void feignExceptionHandler(FeignException fe) {
        log.error(fe.getMessage());
    }
}


