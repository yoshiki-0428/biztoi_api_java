package com.biztoi.web.service;

import com.biztoi.Tables;
import com.biztoi.model.Question;
import com.biztoi.model.Toi;
import com.biztoi.tables.records.MstQuestionRecord;
import com.biztoi.tables.records.MstToiRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.biztoi.Tables.MST_QUESTION;
import static com.biztoi.Tables.MST_TOI;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DataQueryService {
    @NonNull DSLContext dsl;
    @NonNull ObjectMapper mapper;

    public Toi findToi() {
        MstToiRecord record = this.dsl.selectFrom(MST_TOI).fetchOne();
        return new Toi().title(record.getTitle()).detail(record.getDetail()).publishFlg(true);
    }

    public List<Question> findQuestionsAll() {
        List<Question> list = new ArrayList<>();
        this.dsl.selectFrom(MST_QUESTION).where(MST_QUESTION.PATTERN_ID.eq(0)).orderBy(MST_QUESTION.ORDER_ID)
                .fetch().forEach(record -> {
                    list.add(
                            new Question().title(record.getTitle()).detail(record.getDetail()).id(record.getId().toString())
                                    .orderId(record.getOrderId()).patternId(record.getPatternId())
                                    .example(record.getExample()).required(record.getRequired().equals("1"))
                                    .step(record.getStep()));
                    });
        return list;
    }

    public Question findQuestion(String questionId) {
        MstQuestionRecord record = this.dsl.selectFrom(MST_QUESTION).where(MST_QUESTION.PATTERN_ID.eq(0)
                .and(MST_QUESTION.ID.cast(String.class).eq(questionId))).fetchOne();
        return new Question().title(record.getTitle()).detail(record.getDetail()).id(record.getId().toString())
                .orderId(record.getOrderId()).patternId(record.getPatternId())
                .example(record.getExample()).required(record.getRequired().equals("1"))
                .step(record.getStep());
    }
}
