package com.biztoi.web.api;

import com.biztoi.api.ApiApi;
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

    private static final Logger log = LoggerFactory.getLogger(BizToiApiImpl.class);

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

        List<Book> books = items.stream()
                .map(BooksUtils::to)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Flux.fromIterable(books));
    }

    @Override
    public ResponseEntity<Void> booksPost(@Valid Mono<Book> book, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteFavoriteBooks(@Valid Mono<SendLikeInfo> sendLikeInfo, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteLikesAnswers(@Valid Mono<SendLikeInfo> sendLikeInfo, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return null;
    }

    @Override
    public ResponseEntity<Void> favoriteBooks(@Valid Mono<SendLikeInfo> sendLikeInfo, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return null;
    }

    @Override
    public ResponseEntity<AnswerHead> getAnswer(String bookId, String answerId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        return ResponseEntity.ok(this.getStubAnswerHead(answerId, bookId));
    }

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
        return ResponseEntity.ok(Flux.fromIterable(IntStream.range(0, 5).mapToObj(i -> {
            return this.getStubAnswerHead(UUID.randomUUID().toString(), bookId);
        }).collect(Collectors.toList())));
    }

    @Override
    public ResponseEntity<AnswerHead> getAnswersMe(String bookId, ServerWebExchange exchange) {

        log.info("path: {}", exchange.getRequest().getPath().toString());
        return ResponseEntity.ok(this.getStubAnswerHead(UUID.randomUUID().toString(), bookId));
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
        log.info("path: {}", exchange.getRequest().getPath().toString());
        log.info("bookId {}", bookId);
        log.info("questionId {}", questionId);
        return ResponseEntity.ok(this.getStubQuestionMap().get(questionId));
    }

    @Override
    public ResponseEntity<Flux<Question>> getBookQuestions(String bookId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        log.info("bookId {}", bookId);
        return ResponseEntity.ok(Flux.fromIterable(this.getStubQuestionMap().values().stream().sorted(Comparator.comparing(Question::getId)).collect(Collectors.toList())));
    }

    @Override
    public ResponseEntity<Toi> getBookToi(String bookId, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        log.info("bookId {}", bookId);
        return ResponseEntity.ok(new Toi().title("a").detail("aa").publishFlg(true));
    }

    @Override
    public ResponseEntity<Void> likesAnswers(@Valid Mono<SendLikeInfo> sendLikeInfo, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        log.info("sendLikeInfo {}", sendLikeInfo.toString());
        return null;
    }

    @Override
    public ResponseEntity<Flux<Answer>> postAnswer(String bookId, String questionId, @Valid Mono<AnswerList> answerList, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        log.info("bookId {}", bookId);
        log.info("questionId {}", questionId);
        return null;
    }

    @Override
    public ResponseEntity<AnswerHead> postAnswerHead(String bookId, @Valid Mono<AnswerHead> answerHead, ServerWebExchange exchange) {
        log.info("path: {}", exchange.getRequest().getPath().toString());
        log.info("bookId {}", bookId);
        return null;
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

    private Answer getStubAnswer(String orderId, String answerType, String questionId) {
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
                        .sum(new BigDecimal(random.nextInt(100)))
                )
                .inserted(date.toString())
                .modified(date.plusDays(5).toString());
    }

    private List<Answer> getStubAnswerList() {
        return Arrays.asList(
                this.getStubAnswer("1", "OBJECTIVE", "00000-00000-11111"),
                this.getStubAnswer("2", "OBJECTIVE", "00000-00000-11111"),
                this.getStubAnswer("1", "GIST", "00000-00000-22222"),
                this.getStubAnswer("2", "GIST", "00000-00000-22222"),
                this.getStubAnswer("1", "GIST", "00000-00000-33333"),
                this.getStubAnswer("1", "GIST", "00000-00000-44444"),
                this.getStubAnswer("1", "ACTION_PLAN", "00000-00000-55555")
        );
    }

    private Map<String, Question> getStubQuestionMap() {
        Map<String, Question> map = new HashMap<String, Question>() {
            {
                put("00000-00000-11111", getStubQuestion("00000-00000-11111", "00000-00000-22222", "この本を読んだ目的を設定してみましょう", "OBJECTIVE", 1, 1, "リーダーシップを身につけたい"));
                put("00000-00000-22222", getStubQuestion("00000-00000-22222", "00000-00000-33333", "目的を達成することでどのようなメリットがありますか？( 悩みの解決・収入UP etc… )", "OBJECTIVE", 2, 2, "チームをまとめる実力を身につけ、会社の幹部へ昇格する, 後輩から慕われる存在になりたい"));
                put("00000-00000-33333", getStubQuestion("00000-00000-33333", "00000-00000-44444", "この本を読んで得られた知識を書き出してみましょう", "GIST", 3, 2, "チームで共通の目標を持つ"));
                put("00000-00000-44444", getStubQuestion("00000-00000-44444", "00000-00000-55555", "「この本から得た知識」×「自らの経験・知識」から「気付き」があれば書いてみよう！", "GIST", 4, 2, "前に読んだ本と内容が似ているが、結論が違うので、自分なりに行動をして実際に確かめてみる"));
                put("00000-00000-55555", getStubQuestion("00000-00000-55555", "00000-00000-66666", "本から得た知識を使って、どのような行動ができそうか？(すぐに書けない時は5W1Hで考えてみよう！)", "ACTION_PLAN", 5, 3, "チームで話し合って共通の目標を作る"));
                put("00000-00000-66666", getStubQuestion("00000-00000-66666", null, "行動プランを「自分の環境」でのやり方として、具体的に書いてみよう！", "ACTION_PLAN", 6, 3,
                        "朝礼後にチームメンバーを集めて共通目標の有用性について説明する。\n" +
                                "3日後までに目標を考えてくるようお願いする。\n" +
                                "3日後の朝礼後に再度集まり30分間チームメンバーで目標案を検討し決定する"));
            }
        };
        return map;
    }

    private Question getStubQuestion(String id, String nextId, String title, String answerType, int orderId, int step, String example) {
        return new Question()
                .id(id)
//                .toiId(UUID.randomUUID().toString())
                .nextQuestionId(nextId)
                .title(title).answerType(answerType).example(example).required(true)
                .orderId(BigDecimal.valueOf(orderId)).step(BigDecimal.valueOf(step));
    }

}
