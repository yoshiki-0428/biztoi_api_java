package com.biztoi.web.api;

import com.biztoi.api.BooksApi;
import com.biztoi.web.client.VolumesApiClient;
import com.biztoi.model.*;
import feign.FeignException;
import io.swagger.v3.oas.models.servers.Server;
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
    VolumesApiClient volumesApiClient;

    private static final Logger log = LoggerFactory.getLogger(BooksApiImpl.class);

    @Override
    public ResponseEntity<Flux<Book>> books(ServerWebExchange exchange) {
        Volumes v = volumesApiClient.booksVolumesList("daigo", null, null, null,
            null, null, null, null,
            null, null, null, null,
            null, null, null, null,
            null, null, null, null).getBody();
        log.info(v.toString());

        List<Book> books = v.getItems().stream()
                .filter(item -> item.getVolumeInfo().getImageLinks() != null)
                .map(item -> {
            return new Book()
                   .id(item.getId())
                   .title(item.getVolumeInfo().getTitle())
                   .detail(item.getVolumeInfo().getDescription())
                   .pictureUrl(item.getVolumeInfo().getImageLinks().getSmallThumbnail())
                   .linkUrl(item.getSelfLink())
                   .isbn(item.getVolumeInfo().getIndustryIdentifiers().get(0).getIdentifier())
                   .author(item.getVolumeInfo().getAuthors())
                   .category(item.getVolumeInfo().getCategories())
                   .favorite(false);
        }).collect(Collectors.toList());

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
        Volume item = volumesApiClient.booksVolumesGet(bookId, null).getBody();
        log.info(item.toString());
        Book b = new Book()
                .id(item.getId())
                .title(item.getVolumeInfo().getTitle())
                .detail(item.getVolumeInfo().getDescription())
                .pictureUrl(item.getVolumeInfo().getImageLinks().getSmallThumbnail())
                .linkUrl(item.getSelfLink())
                .isbn(item.getVolumeInfo().getIndustryIdentifiers().get(0).getIdentifier())
                .author(item.getVolumeInfo().getAuthors())
                .category(item.getVolumeInfo().getCategories())
                .favorite(false);

        return ResponseEntity.ok(b);
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


