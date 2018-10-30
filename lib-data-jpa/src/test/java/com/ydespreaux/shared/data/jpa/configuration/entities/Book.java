package com.ydespreaux.shared.data.jpa.configuration.entities;

import com.ydespreaux.shared.data.jpa.criteria.SimpleCriteria;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "book")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class Book implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public enum Genre {
        THRILLER, FANTASTIQUE, FICTION
    }

    /**
     * Properties path for search
     */
    public enum BookPropertyPath implements SimpleCriteria.EnumPropertyPath {

        TITLE("title"),
        DESCRIPTION("description"),
        GENRE("genre"),
        PRICE("price"),
        AUTHOR("author"),
        AUTHOR_FIRSTNAME("author.firstName"),
        AUTHOR_LASTNAME("author.lastName"),
        EDITOR("editor"),
        PUBLICATION("publication");

        private final String propertyPath;

        BookPropertyPath(String propertyPath) {
            this.propertyPath = propertyPath;
        }

        public String getPropertyPath() {
            return this.propertyPath;
        }
    }

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "title", length = 100, nullable = false, unique = true)
    private String title;

    @Column(name = "description", length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "style", length = 20)
    private Genre genre;

    @Column(name = "price")
    private Double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    @Column(name = "editor", length = 255)
    private String editor;

    @Column(name = "publication")
    @Temporal(TemporalType.DATE)
    private Date publication;

    @Version
    private Integer version;

}
