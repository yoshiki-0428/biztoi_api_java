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
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class RakutenApiService {
    @NonNull
    RakutenBooksApiClient booksApiClient;

    @NonNull
    Environment env;

    private static final Logger log = LoggerFactory.getLogger(RakutenApiService.class);

    public List<Item> getSalesBooks() {
        SearchInfo searchInfo = this.booksApiClient.getBooksTotal(
                env.getProperty("application.rakuten.app-id"), env.getProperty("application.rakuten.genre-id"), null, null, null, null,
                null, "sales", null, null, null).getBody();

        return filter(searchInfo);
    }

    public List<Item> getBooksForGenre(String genre) {
        SearchInfo searchInfo = this.booksApiClient.getBooksTotal(
                env.getProperty("application.rakuten-app-id"), genre, null, null, null, null,
                null, "sales", null, null, null).getBody();

        return filter(searchInfo);
    }


    public Item findBook(String isbn) {
        SearchInfo searchInfo = this.booksApiClient.getBooksTotal(
                env.getProperty("application.rakuten-app-id"), "001", null, null, null, isbn,
                null, null, null, null, null).getBody();
        if (searchInfo == null || searchInfo.getItems() == null || searchInfo.getItems().size() == 0) {
            return null;
        }

        return searchInfo.getItems().get(0).getItem();
    }

    private static List<Item> filter(SearchInfo searchInfo) {
        if (searchInfo == null || searchInfo.getItems() == null) {
            return Collections.emptyList();
        }

        return searchInfo.getItems().stream()
                .map(ItemMap::getItem)
                .filter(item -> !item.getIsbn().isEmpty())
                .collect(Collectors.toList());
    }

}
