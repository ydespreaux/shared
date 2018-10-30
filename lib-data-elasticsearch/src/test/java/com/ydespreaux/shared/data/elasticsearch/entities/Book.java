package com.ydespreaux.shared.data.elasticsearch.entities;

import com.ydespreaux.shared.data.elasticsearch.annotations.Document;
import io.searchbox.annotations.JestId;
import io.searchbox.annotations.JestVersion;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document(aliasOrIndex = "books", type = "book", indexPath = "classpath:indices/book.index")
public class Book {

    @JestId
    private String documentId;
    @JestVersion
    private Integer version;
    private String title;
    private String description;
    private Double price;
}
