package com.biztoi.web.service;

import com.biztoi.model.*;
import com.biztoi.tables.records.AnswerHeadRecord;
import com.biztoi.tables.records.MstQuestionRecord;
import com.biztoi.tables.records.MstToiRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.biztoi.Tables.*;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DataQueryService {

    @NonNull DSLContext dsl;

    @NonNull ObjectMapper mapper;

    private static final Logger log = LoggerFactory.getLogger(DataQueryService.class);

    public Toi findToi() {
        MstToiRecord record = this.dsl.selectFrom(MST_TOI).fetchOne();
        return new Toi().title(record.getTitle()).detail(record.getDetail()).publishFlg(true);
    }

    public List<Question> findQuestionsAll() {
        return this.dsl.selectFrom(MST_QUESTION).where(MST_QUESTION.PATTERN_ID.eq(0)).orderBy(MST_QUESTION.ORDER_ID)
                .fetch().stream().map(record -> new Question()
                        .title(record.getTitle()).detail(record.getDetail()).id(record.getId())
                        .orderId(record.getOrderId()).patternId(record.getPatternId()).example(record.getExample())
                        .required(record.getRequired().equals("1")).step(record.getStep()))
                .collect(Collectors.toList());
    }

    public Question findQuestion(String questionId) {
        MstQuestionRecord record = this.dsl.selectFrom(MST_QUESTION).where(MST_QUESTION.PATTERN_ID.eq(0)
                .and(MST_QUESTION.ID.eq(questionId))).fetchOne();
        return new Question().title(record.getTitle()).detail(record.getDetail()).id(record.getId())
                .orderId(record.getOrderId()).patternId(record.getPatternId())
                .example(record.getExample()).required(record.getRequired().equals("1"))
                .step(record.getStep());
    }

    public List<String> isFavoriteBooks(String userId) {
        return this.dsl.select(LIKES.FOREIGN_ID).from(LIKES)
                .where(LIKES.USER_ID.eq(userId).and(LIKES.TYPE.eq("book"))).fetch().stream()
                .map(r -> r.get(LIKES.FOREIGN_ID)).collect(Collectors.toList());
    }

    public int createLike(String id, String type, String userId) {
        return this.dsl
                .insertInto(LIKES, LIKES.FOREIGN_ID, LIKES.TYPE, LIKES.USER_ID)
                .values(id, type, userId).execute();
    }
    public int deleteLike(String id, String type, String userId) {
        return this.dsl.deleteFrom(LIKES)
                .where(LIKES.FOREIGN_ID.eq(id)
                        .and(LIKES.TYPE.eq(type)
                        .and(LIKES.USER_ID.cast(String.class).eq(userId))))
                .execute();
    }

//    public List<SendLikeInfo> selectAllLikesBook(String userId) {
//        final List<SendLikeInfo> list = new ArrayList<>();
//        this.dsl.selectFrom(LIKES).where(LIKES.TYPE.eq("book").and(LIKES.USER_ID.eq(userId)))
//                .fetch().forEach(likesRecord -> list.add(new SendLikeInfo().id(likesRecord.getForeignId()).active(true)));
//        return list;
//    }
//

    public Map<String, AnswerLikes> selectAllLikesAnswer(String userId) {
        List<String> userHasLikes = this.dsl.select(LIKES.FOREIGN_ID).from(LIKES)
                .where(LIKES.USER_ID.eq(userId).and(LIKES.TYPE.eq("answer"))).fetch().stream()
                .map(r -> r.get(LIKES.FOREIGN_ID)).collect(Collectors.toList());

        return this.dsl.select(LIKES.FOREIGN_ID, DSL.count()).from(LIKES)
                .where(LIKES.TYPE.eq("answer"))
                .groupBy(LIKES.FOREIGN_ID).orderBy(DSL.count().desc())
                .fetch().stream().collect(Collectors.toMap(
                        r -> r.get(LIKES.FOREIGN_ID),
                        r -> new AnswerLikes()
                                .active(userHasLikes.contains(r.get(LIKES.FOREIGN_ID)))
                                .sum(r.get(DSL.count()))
                                .id(r.get(LIKES.FOREIGN_ID))));
    }

    // TODO stub
    public Map<String, BizToiUser> selectAllBizToiUserMock() {
        final Map<String, BizToiUser> bizToiUserMap = new HashMap<>();
        IntStream.range(0, 10).forEach(i -> bizToiUserMap.put(String.valueOf(i), new BizToiUser()
                .id(UUID.randomUUID().toString())
                .nickname("User NickName" + new Random().nextInt(11)).country("ja").pictureUrl("https://picsum.photos/20" + new Random().nextInt(9))
        ));
        return bizToiUserMap;
    }

    // TODO upsert
    public List<Answer> insertAnswers(String questionId, AnswerList answers) {
        answers.getAnswers().forEach(answer -> {
            Record record = this.dsl.fetchOne("insert into ANSWER (ID, ANSWER_HEAD_ID, QUESTION_ID, ANSWER, ORDER_ID) VALUES (?, ?, ?, ?, ?) returning id, inserted;",
                    UUID.randomUUID().toString(), answer.getAnswerHeadId(), questionId, answer.getAnswer(), answer.getOrderId());
            log.info(record.toString());
            answer.setId((String) record.get("id"));
            answer.setInserted(record.get("inserted").toString());
        });
        return answers.getAnswers();
    }

    public AnswerHead insertAnswerHead(String bookId, String userId) {
        Record record = this.dsl.fetchOne("insert into ANSWER_HEAD (ID, BOOK_ID, USER_ID, PUBLISH_FLG) VALUES (?, ?, ?, ?) returning id, inserted;",
                UUID.randomUUID().toString(), bookId, userId, "1");
        log.info(record.toString());
        return new AnswerHead()
                .id((String) record.getValue("id")).bookId(bookId).userId(userId)
                .publishFlg(true).inserted(record.getValue("inserted").toString());
    }

    public List<AnswerHead> getAnswerHeadList(String userId, String bookId, Integer limit, boolean hasUser) {
        final Map<String, AnswerLikes> answerLikesMap = this.selectAllLikesAnswer(userId);
        final Map<String, BizToiUser> bizToiUserMap = this.selectAllBizToiUserMock();

        Result<Record> records = this.dsl.select().from(ANSWER_HEAD).join(ANSWER).on(ANSWER_HEAD.ID.eq(ANSWER.ANSWER_HEAD_ID))
                .where(ANSWER_HEAD.BOOK_ID.eq(bookId).and(hasUser ? ANSWER_HEAD.USER_ID.eq(userId) : DSL.noCondition()))
                .limit(limit).fetch();
        // TODO AnswerHeadIdでグルーピングし、それぞれの回答情報をAnswerHeadにつける
        records.stream().collect(Collectors.groupingBy(r -> r.get(ANSWER_HEAD.ID))).entrySet().stream()
                .forEach(r -> {
                    log.info("key =" + r.getKey() + "value =");
                    r.getValue().forEach(v -> log.info(v.get(ANSWER.ANSWER_)));
                } );

//        final AnswerHead entity = this.mapToAnswerHead(records.get(0));
//        entity.setAnswers(records.map(this::mapToAnswer));
//        entity.setLikeInfo(answerLikesMap.getOrDefault(entity.getId(), new AnswerLikes().active(false).sum(0)));
        // TODO userInfoがStubなので修正
//        entity.setUserInfo(bizToiUserMap.getOrDefault(String.valueOf(new Random().nextInt(11)), null));
        return this.dsl.selectFrom(ANSWER_HEAD)
                .where(ANSWER_HEAD.BOOK_ID.eq(bookId).and(hasUser ? ANSWER_HEAD.USER_ID.eq(userId) : DSL.noCondition()))
                .limit(limit).fetch().stream().map(record -> new com.biztoi.model.AnswerHead().bookId(bookId)
                            .id(record.getId()).userId(record.getUserId()).publishFlg(record.getPublishFlg().equals("1"))
                            .inserted(record.getInserted().toString()).modified(record.getModified().toString())
                            .likeInfo(answerLikesMap.getOrDefault(record.getId(), new AnswerLikes().active(false).sum(0)))
                            .userInfo(bizToiUserMap.getOrDefault(String.valueOf(new Random().nextInt(11)), null)))
                .sorted(Comparator.comparing(o -> o.getLikeInfo().getSum(), Comparator.reverseOrder()))
                .collect(Collectors.toList());
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
                .answer(record.get(ANSWER.ANSWER_))
                .inserted(record.get(ANSWER.INSERTED).toString())
                .modified(record.get(ANSWER.MODIFIED).toString());
    }

    public List<Answer> getAnswerMeByQuestion(String answerHeadId, String questionId, String userId) {
        return this.dsl.selectFrom(ANSWER)
                .where(ANSWER.ANSWER_HEAD_ID.eq(answerHeadId).and(ANSWER.QUESTION_ID.eq(questionId)))
                .fetch().stream().map(record ->
                    new Answer().id(record.get(ANSWER.ID)).answer(record.get(ANSWER.ANSWER_))
                            .answerHeadId(record.get(ANSWER.ANSWER_HEAD_ID)).inserted(record.get(ANSWER.INSERTED).toString()).modified(record.get(ANSWER.MODIFIED).toString())
                            .orderId(record.get(ANSWER.ORDER_ID))
                ).collect(Collectors.toList());
    }

}
