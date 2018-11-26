package com.ydespreaux.shared.data.jpa.configuration.entities;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class BookDTO implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String description;
    private Book.Genre genre;
    private Double price;
    private String author;
    private String editor;
    private LocalDate publication;

}
