package com.biztoi.web.utils;

import com.biztoi.model.Book;
import com.biztoi.model.Item;

import java.util.Arrays;

public class BooksUtils {
    public static Book to(Item item) {
        return new Book()
                .id(item.getIsbn())
                .title(item.getTitle())
                .detail(item.getItemCaption())
                .pictureUrl(item.getMediumImageUrl())
                .linkUrl(item.getItemUrl())
                .isbn(item.getIsbn())
                .author(item.getAuthor() != null ? Arrays.asList(item.getAuthor().split("/")) : null)
                .category(item.getBooksGenreId() != null ? Arrays.asList(item.getBooksGenreId().split("/")) : null)
                .favorite(false);
    }
}