package com.biztoi.web.utils;

import com.biztoi.model.Book;
import com.biztoi.model.Item;
import com.biztoi.web.service.BooksGenre;
import org.jooq.Record;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.biztoi.Tables.BOOK;

public class BooksUtils {
    public static Book to(Item item) {
        return new Book()
                .isbn(item.getIsbn())
                .title(item.getTitle())
                .detail(item.getItemCaption())
                .pictureUrl(item.getMediumImageUrl())
                .linkUrl(item.getAffiliateUrl())
                .authors(item.getAuthor() != null ? Arrays.asList(item.getAuthor().split("/")) : null)
                .categories((item.getBooksGenreId() != null) ?
                        Arrays.stream(item.getBooksGenreId().split("/")).map(BooksGenre.map::get).collect(Collectors.toList()) : null)
                .favorite(false);
    }

    public static Book to(Record record) {
        return new Book()
                .isbn(record.get(BOOK.ISBN))
                .title(record.get(BOOK.TITLE))
                .detail(record.get(BOOK.DETAIL))
                .pictureUrl(record.get(BOOK.PICTURE_URL))
                .linkUrl(record.get(BOOK.LINK_URL))
                .authors(record.get(BOOK.AUTHORS) != null ? Arrays.asList(record.get(BOOK.AUTHORS).split(",")) : null)
                .categories(record.get(BOOK.CATEGORIES) != null ? Arrays.asList(record.get(BOOK.CATEGORIES).split(",")) : null)
                .favorite(false);
    }

}
