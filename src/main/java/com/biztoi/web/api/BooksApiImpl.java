package com.biztoi.web.api;

import com.biztoi.api.BooksApi;
import com.biztoi.api.VolumesApi;
import com.biztoi.model.*;
import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Arrays;

@RestController
@RequestMapping("${openapi.bizToi.base-path:/api}")
@CrossOrigin
public class BooksApiImpl implements BooksApi {
    @Override
    public ResponseEntity<Flux<com.biztoi.model.Book>> books(ServerWebExchange exchange) {
//        Volumes v = volumesApi.booksVolumesList("aaa", null, null, null,
//                null, null, null, null,
//                null, null, null, null,
//                null, null, null, null,
//                null, null, null, null).getBody();
        return ResponseEntity.ok(Flux.fromIterable(Arrays.asList(new Book().id("a").detail("detail").title("title").favorite(true).pictureUrl("http://www.henobu.com/wp-content/uploads/2016/05/test.jpg"))));
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
        return null;
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
}
