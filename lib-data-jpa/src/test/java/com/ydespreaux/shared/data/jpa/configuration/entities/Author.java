package com.ydespreaux.shared.data.jpa.configuration.entities;import lombok.Getter;import lombok.Setter;import javax.persistence.*;@Entity@Table(name = "author")@Getter@Setterpublic class Author {    @Id    @GeneratedValue    private Long id;    @Version    private Integer version;    private String firstName;    private String lastName;}