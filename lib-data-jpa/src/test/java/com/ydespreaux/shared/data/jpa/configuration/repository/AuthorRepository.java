package com.ydespreaux.shared.data.jpa.configuration.repository;import com.ydespreaux.shared.data.jpa.configuration.entities.Author;import org.springframework.data.jpa.repository.JpaRepository;public interface AuthorRepository extends JpaRepository<Author, Long> {}