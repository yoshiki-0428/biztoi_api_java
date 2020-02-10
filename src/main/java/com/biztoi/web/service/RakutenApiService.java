package com.biztoi.web.service;

import com.biztoi.model.Item;
import com.biztoi.model.ItemMap;
import com.biztoi.model.SearchInfo;
import com.biztoi.web.client.RakutenBooksApiClient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class RakutenApiService {
    @NonNull
    RakutenBooksApiClient booksApiClient;

    private static final Logger log = LoggerFactory.getLogger(RakutenApiService.class);

    private String appId = "1035252894012359396";

    public List<Item> getSalesBooks() {
        SearchInfo searchInfo = this.booksApiClient.getBooksTotal(
                this.appId, "001006", null, null, null, null,
                null, "sales", null, null, null).getBody();
        if (searchInfo == null || searchInfo.getItems() == null) {
            return null;
        }
        log.debug(searchInfo.getItems().get(0).getItem().toString());
        return searchInfo.getItems().stream()
                .map(ItemMap::getItem)
                .collect(Collectors.toList());
    }

    public Item findBook(String isbn) {
        SearchInfo searchInfo = this.booksApiClient.getBooksTotal(
                this.appId, "001", null, null, null, isbn,
                null, null, null, null, null).getBody();
        if (searchInfo == null || searchInfo.getItems() == null || searchInfo.getItems().size() == 0) {
            return null;
        }
        log.debug(searchInfo.getItems().toString());
        return searchInfo.getItems().get(0).getItem();
    }
}
