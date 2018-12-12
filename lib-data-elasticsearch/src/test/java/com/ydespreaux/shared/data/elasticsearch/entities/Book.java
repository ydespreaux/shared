package com.ydespreaux.shared.data.elasticsearch.entities;

import com.ydespreaux.shared.data.elasticsearch.annotations.Document;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document(aliasOrIndex = "books", type = "book", indexPath = "classpath:indices/book.index")
public class Book {

    @Id
    private String documentId;
    @Version
    private Long version;
    private String title;
    private String description;
    private Double price;
    private LocalDate publication;
    private LocalDateTime lastUpdated;
}
