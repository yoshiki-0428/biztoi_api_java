package com.biztoi.web.service;

import com.biztoi.Tables;
import com.biztoi.model.Toi;
import com.biztoi.tables.records.MstToiRecord;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DataQueryService {
    @NonNull DSLContext dsl;

    public Toi findToi() {
        MstToiRecord record = this.dsl.selectFrom(Tables.MST_TOI).fetchOne();
        return new Toi().title(record.getTitle()).detail(record.getDetail()).publishFlg(true);
    }
}
