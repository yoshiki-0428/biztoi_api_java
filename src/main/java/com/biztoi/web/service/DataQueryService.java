package com.biztoi.web.service;

import com.biztoi.model.*;
import com.biztoi.tables.records.MstQuestionRecord;
import com.biztoi.tables.records.MstToiRecord;
import com.biztoi.web.utils.BooksUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

import static com.biztoi.Tables.*;
import static com.biztoi.web.config.ApplicationConst.RAKUTEN_GENRE_ID;
import static java.util.stream.Collectors.toList;

// TODO 役割に応じてサービスを分離
@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DataQueryService {

    @NonNull
    DSLContext dsl;

    @NonNull
    Environment env;

    @NonNull
    AWSCognitoUserPoolService userPoolService;

    private static final Logger log = LoggerFactory.getLogger(DataQueryService.class);

    public Toi findToi() {
        MstToiRecord record = this.dsl.selectFrom(MST_TOI).fetchOne();
        return new Toi().title(record.getTitle()).detail(record.getDetail()).publishFlg(true);
    }

    public List<Question> findQuestionsAll(final String pattern) {
        var patternCond = pattern != null ? MST_QUESTION.PATTERN_ID.eq(Integer.valueOf(pattern)) : DSL.noCondition();
        return this.dsl.selectFrom(MST_QUESTION).where(patternCond).orderBy(MST_QUESTION.ORDER_ID)
                .fetch().stream().map(record -> new Question()
                        .title(record.getTitle()).detail(record.getDetail()).id(record.getId())
                        .orderId(record.getOrderId()).patternId(record.getPatternId()).example(record.getExample())
                        .required(record.getRequired().equals("1")).step(record.getStep()))
                .collect(toList());
    }

    public Question findQuestion(String questionId, String pattern) {
        var patternCond = pattern != null ? MST_QUESTION.PATTERN_ID.eq(Integer.valueOf(pattern)) : DSL.noCondition();
        MstQuestionRecord record = this.dsl.selectFrom(MST_QUESTION).where(patternCond
                .and(MST_QUESTION.ID.eq(questionId))).fetchOne();
        return new Question().title(record.getTitle()).detail(record.getDetail()).id(record.getId())
                .orderId(record.getOrderId()).patternId(record.getPatternId())
                .example(record.getExample()).required(record.getRequired().equals("1"))
                .step(record.getStep());
    }

    public List<String> isFavoriteBooks(String userId) {
        return this.dsl.select(LIKES.FOREIGN_ID).from(LIKES)
                .where(LIKES.USER_ID.eq(userId).and(LIKES.LIKES_TYPE.eq("book"))).fetch().stream()
                .map(r -> r.get(LIKES.FOREIGN_ID)).collect(toList());
    }

    public List<Book> getBookFavoriteListMe(String userId) {
        return this.dsl.select(BOOK.TITLE, BOOK.ISBN, BOOK.DETAIL, BOOK.LINK_URL, BOOK.PICTURE_URL, BOOK.AUTHORS, BOOK.CATEGORIES)
                .from(LIKES).join(BOOK).on(BOOK.ISBN.eq(LIKES.FOREIGN_ID))
                .where(LIKES.LIKES_TYPE.eq("book").and(LIKES.USER_ID.eq(userId)))
                .fetch().stream().map(BooksUtils::to).collect(toList());
    }

    public int createLike(String id, String type, String userId) {
        return this.dsl
                .insertInto(LIKES, LIKES.FOREIGN_ID, LIKES.LIKES_TYPE, LIKES.USER_ID)
                .values(id, type, userId)
                .onDuplicateKeyIgnore().execute();
    }

    public int deleteLike(String id, String type, String userId) {
        return this.dsl.deleteFrom(LIKES)
                .where(LIKES.FOREIGN_ID.eq(id)
                        .and(LIKES.LIKES_TYPE.eq(type)
                        .and(LIKES.USER_ID.cast(String.class).eq(userId))))
                .execute();
    }

    public List<Book> summaryFavoriteBook() {
        return this.dsl.select(DSL.count(), BOOK.TITLE, BOOK.ISBN, BOOK.DETAIL, BOOK.LINK_URL, BOOK.PICTURE_URL, BOOK.AUTHORS, BOOK.CATEGORIES)
                .from(BOOK).join(LIKES).on(BOOK.ISBN.eq(LIKES.FOREIGN_ID))
                .groupBy(BOOK.ISBN).orderBy(DSL.count().desc())
                .fetch().stream().map(BooksUtils::to).collect(Collectors.toList());
    }

    public List<Book> summaryLikesAnswer() {
        return this.dsl.select(DSL.count(), BOOK.TITLE, BOOK.ISBN, BOOK.DETAIL, BOOK.LINK_URL, BOOK.PICTURE_URL, BOOK.AUTHORS, BOOK.CATEGORIES)
                .from(BOOK)
                .join(LIKES).on(BOOK.ISBN.eq(LIKES.FOREIGN_ID))
                .groupBy(BOOK.ISBN).orderBy(DSL.count().desc())
                .fetch().stream().map(BooksUtils::to).collect(Collectors.toList());
    }

    public List<String> selectAllLikesBook() {
        return this.dsl.select(LIKES.FOREIGN_ID, DSL.count()).from(LIKES)
                .where(LIKES.LIKES_TYPE.eq("book"))
                .groupBy(LIKES.FOREIGN_ID).orderBy(DSL.count().desc())
                .fetch().stream().map(r -> r.get(LIKES.FOREIGN_ID)).collect(toList());
    }

    /**
     * AnswerHeadから本の情報を取得し最も多いジャンルを集計
     * 取得したジャンルでRakutenAPIでジャンル絞り込みで検索
     *
     * @param userId
     * @return
     */
    public String bookRecommendList(String userId) {
        List<String> userCategories = new ArrayList<>();
        this.dsl.select(BOOK.CATEGORIES).from(BOOK).join(LIKES).on(BOOK.ISBN.eq(LIKES.FOREIGN_ID))
                .where(LIKES.USER_ID.eq(userId)).fetch().map(r -> userCategories.addAll(Arrays.asList(r.get(BOOK.CATEGORIES).split(","))));
        var groupingEntrySet = userCategories.stream()
                .collect(Collectors.groupingBy(x -> x, Collectors.counting())).entrySet();
        var recommendGenre = groupingEntrySet.stream().max(Map.Entry.comparingByValue());

        return recommendGenre.isEmpty() ? BooksGenre.map.get(env.getProperty(RAKUTEN_GENRE_ID)) : recommendGenre.get().getKey();
    }

    // TODO リファクタ
    public List<Book> bookUnfinishedList(String userId) {
        // 質問の必須数を取得
        var requiredCnt = this.dsl.selectCount().from(MST_QUESTION)
                .where(MST_QUESTION.PATTERN_ID.eq(0)).and(MST_QUESTION.REQUIRED.eq("1")).fetchOne();

        // 回答したAnswerHeadIdを取得
        var answerHeadIds = this.dsl.select(DSL.count(), ANSWER.ANSWER_HEAD_ID).from(ANSWER)
                .join(MST_QUESTION).on(MST_QUESTION.ID.eq(ANSWER.QUESTION_ID))
                .where(MST_QUESTION.REQUIRED.eq("1")).and(ANSWER.ORDER_ID.eq(1))
                .groupBy(ANSWER.ANSWER_HEAD_ID)
                .fetch().stream().filter(r -> (int) r.get(0) < (int) requiredCnt.get(0)).map(r -> r.get(ANSWER.ANSWER_HEAD_ID)).collect(Collectors.toList());

        if (answerHeadIds.size() == 0) {
            return Collections.emptyList();
        }

        // AnswerHeadIdsで本情報を取得
        return this.dsl.select(BOOK.TITLE, BOOK.ISBN, BOOK.DETAIL, BOOK.LINK_URL, BOOK.PICTURE_URL, BOOK.AUTHORS, BOOK.CATEGORIES).distinctOn(BOOK.ISBN)
                .from(BOOK).join(ANSWER_HEAD).on(BOOK.ISBN.eq(ANSWER_HEAD.BOOK_ID))
                .where(ANSWER_HEAD.ID.in(answerHeadIds).and(ANSWER_HEAD.USER_ID.eq(userId)))
                .fetch().stream().map(BooksUtils::to).collect(Collectors.toList());
    }


    // 多い順から各AnswerHeadのBookIdを取得する(重複削除する) this.queryService.selectBook(ids)
    // TODO 正確に多い順ではないためプログラムで頑張る
    public List<Book> bookLikesList() {
        var subQuery = this.dsl.select(LIKES.FOREIGN_ID).from(LIKES).where(LIKES.LIKES_TYPE.eq("answer")).groupBy(LIKES.FOREIGN_ID).orderBy(DSL.count().desc());
        return this.dsl.select(BOOK.TITLE, BOOK.ISBN, BOOK.DETAIL, BOOK.LINK_URL, BOOK.PICTURE_URL, BOOK.AUTHORS, BOOK.CATEGORIES)
                .distinctOn(BOOK.ISBN)
                .from(BOOK).join(ANSWER_HEAD).on(BOOK.ISBN.eq(ANSWER_HEAD.BOOK_ID))
                .where(ANSWER_HEAD.ID.in(subQuery))
                .fetch().stream().map(BooksUtils::to).collect(Collectors.toList());
    }

    public Map<String, AnswerLikes> selectAllLikesAnswer(String userId) {
        List<String> userHasLikes = this.dsl.select(LIKES.FOREIGN_ID).from(LIKES)
                .where(LIKES.USER_ID.eq(userId).and(LIKES.LIKES_TYPE.eq("answer"))).fetch().stream()
                .map(r -> r.get(LIKES.FOREIGN_ID)).collect(toList());

        return this.dsl.select(LIKES.FOREIGN_ID, DSL.count()).from(LIKES)
                .where(LIKES.LIKES_TYPE.eq("answer"))
                .groupBy(LIKES.FOREIGN_ID).orderBy(DSL.count().desc())
                .fetch().stream().collect(Collectors.toMap(
                        r -> r.get(LIKES.FOREIGN_ID),
                        r -> new AnswerLikes()
                                .active(userHasLikes.contains(r.get(LIKES.FOREIGN_ID)))
                                .sum(r.get(DSL.count()))
                                .id(r.get(LIKES.FOREIGN_ID))));
    }

    public void deleteAnswers(AnswerList answers) {
        answers.getAnswers().forEach(answer ->
                this.dsl.deleteFrom(ANSWER).where(ANSWER.ID.eq(answer.getId())).execute());
    }

    public List<Answer> insertAnswers(String questionId, AnswerList answers) {
        answers.getAnswers().forEach(answer -> {
            var answerId = answer.getId() == null || answer.getId().isEmpty() ? UUID.randomUUID().toString() : answer.getId();
            Record record = this.dsl.fetchOne("insert into ANSWER (ID, ANSWER_HEAD_ID, QUESTION_ID, ANSWER, ORDER_ID) VALUES (?, ?, ?, ?, ?)" +
                    "on conflict (id) do update set answer = ?" +
                    " returning id, inserted;",
                    answerId, answer.getAnswerHeadId(), questionId, answer.getAnswer(), answer.getOrderId(), answer.getAnswer());
            log.info(record.toString());
            answer.setId((String) record.get("id"));
            answer.setInserted(record.get("inserted").toString());
        });
        return answers.getAnswers();
    }

    public int insertBook(Book book) {
        return this.dsl.insertInto(BOOK, BOOK.TITLE, BOOK.ISBN, BOOK.DETAIL, BOOK.LINK_URL, BOOK.PICTURE_URL, BOOK.AUTHORS, BOOK.CATEGORIES)
                .values(book.getTitle(), book.getIsbn(), book.getDetail(), book.getLinkUrl(), book.getPictureUrl(), String.join(",", book.getAuthors()), String.join(",", book.getCategories()))
                .onDuplicateKeyIgnore().execute();
    }

    public AnswerHead insertAnswerHead(String bookId, String userId) {
        Record record = this.dsl.fetchOne("insert into ANSWER_HEAD (ID, BOOK_ID, USER_ID, PUBLISH_FLG) VALUES (?, ?, ?, ?) returning id, inserted;",
                UUID.randomUUID().toString(), bookId, userId, "1");
        log.info(record.toString());
        return new AnswerHead()
                .id((String) record.getValue("id")).bookId(bookId).userId(userId)
                .publishFlg(true).inserted(record.getValue("inserted").toString());
    }

    public Mono<AnswerHead> findAnswerHead(String answerHeadId) {
        Result<Record> records = this.dsl.select().from(ANSWER_HEAD).leftJoin(ANSWER).on(ANSWER_HEAD.ID.eq(ANSWER.ANSWER_HEAD_ID))
                .where(ANSWER_HEAD.ID.eq(answerHeadId)).fetch();
        var result = records.stream().collect(Collectors.groupingBy(r -> r.get(ANSWER_HEAD.ID))).values().stream()
                .map(recordList -> {
                    final AnswerHead entity = this.mapToAnswerHead(recordList.get(0));
                    entity.setAnswers(recordList.stream().filter(r -> r.get(ANSWER.ID) != null).map(this::mapToAnswer).collect(toList()));
                    return entity;
                }).findFirst().orElse(null);
        return (result != null) ? Mono.just(result) : Mono.empty();

    }

    public Mono<AnswerHead> getAnswerHead(final String answerHeadId, final String userId) {
        final Map<String, AnswerLikes> answerLikesMap = this.selectAllLikesAnswer(userId);

        Result<Record> records = this.dsl.select().from(ANSWER_HEAD).leftJoin(ANSWER).on(ANSWER_HEAD.ID.eq(ANSWER.ANSWER_HEAD_ID))
                .where(ANSWER_HEAD.ID.eq(answerHeadId)
                .and(ANSWER_HEAD.USER_ID.eq(userId))).fetch();
        var result = records.stream().collect(Collectors.groupingBy(r -> r.get(ANSWER_HEAD.ID))).values().stream()
                .map(recordList -> {
                    final AnswerHead entity = this.mapToAnswerHead(recordList.get(0));
                    entity.setAnswers(recordList.stream().filter(r -> r.get(ANSWER.ID) != null).map(this::mapToAnswer).collect(toList()));
                    entity.setLikeInfo(answerLikesMap.getOrDefault(entity.getId(), new AnswerLikes().active(false).sum(0)));
                    entity.setUserInfo(userPoolService.getUser(recordList.get(0).get(ANSWER_HEAD.USER_ID)));
                    return entity;
                }).findFirst().orElse(null);
        return (result != null) ? Mono.just(result) : Mono.empty();

    }

    public Mono<AnswerHead> getAnswerHead(String answerHeadId, String userId, String bookId, Integer limit, boolean hasUser) {
        var answerHead = this.getAnswerHeadList(userId, bookId, limit, hasUser).stream()
                .filter(a -> a.getId().equals(answerHeadId)).findFirst().orElse(null);
        return (answerHead != null) ? Mono.just(answerHead) : Mono.empty();
    }

    public List<AnswerHead> getAnswerHeadList(String userId, String bookId, Integer limit, boolean hasUser) {
        final Map<String, AnswerLikes> answerLikesMap = this.selectAllLikesAnswer(userId);

        Result<Record> records = this.dsl.select().from(ANSWER_HEAD).join(ANSWER).on(ANSWER_HEAD.ID.eq(ANSWER.ANSWER_HEAD_ID))
                .where(ANSWER_HEAD.BOOK_ID.eq(bookId).and(hasUser ? ANSWER_HEAD.USER_ID.eq(userId) : DSL.noCondition()))
                .limit(limit).fetch();
        return records.stream().collect(Collectors.groupingBy(r -> r.get(ANSWER_HEAD.ID))).values().stream()
                .map(recordList -> {
                    final AnswerHead entity = this.mapToAnswerHead(recordList.get(0));
                    entity.setAnswers(recordList.stream().filter(r -> r.get(ANSWER.ID) != null).map(this::mapToAnswer).collect(toList()));
                    entity.setLikeInfo(answerLikesMap.getOrDefault(entity.getId(), new AnswerLikes().active(false).sum(0)));
                    entity.setUserInfo(userPoolService.getUser(recordList.get(0).get(ANSWER_HEAD.USER_ID)));
                    return entity;
                }).collect(toList());
    }

    public List<Answer> getAnswerMeByQuestion(String answerHeadId, String questionId, String userId) {
        return this.dsl.selectFrom(ANSWER)
                .where(ANSWER.ANSWER_HEAD_ID.eq(answerHeadId).and(ANSWER.QUESTION_ID.eq(questionId)))
                .fetch().stream().map(record ->
                    new Answer().id(record.get(ANSWER.ID)).answer(record.get(ANSWER.ANSWER_))
                            .answerHeadId(record.get(ANSWER.ANSWER_HEAD_ID)).inserted(record.get(ANSWER.INSERTED).toString()).modified(record.get(ANSWER.MODIFIED).toString())
                            .orderId(record.get(ANSWER.ORDER_ID))
                ).collect(toList());
    }

    private AnswerHead mapToAnswerHead(Record record) {
        return new AnswerHead().bookId(record.get(ANSWER_HEAD.BOOK_ID))
                .id(record.get(ANSWER_HEAD.ID)).userId(record.get(ANSWER_HEAD.USER_ID))
                .publishFlg(record.get(ANSWER_HEAD.PUBLISH_FLG).equals("1"))
                .inserted(record.get(ANSWER_HEAD.INSERTED).toString())
                .modified(record.get(ANSWER_HEAD.MODIFIED).toString());
    }

    private Answer mapToAnswer(Record record) {
        return new Answer()
                .id(record.get(ANSWER.ID)).answerHeadId(record.get(ANSWER.ANSWER_HEAD_ID))
                .orderId(record.get(ANSWER.ORDER_ID)).questionId(record.get(ANSWER.QUESTION_ID))
                .answer(record.get(ANSWER.ANSWER_))
                .inserted(record.get(ANSWER.INSERTED).toString())
                .modified(record.get(ANSWER.MODIFIED).toString());
    }
}
