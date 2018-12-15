package com.ydespreaux.shared.data.elasticsearch.repositories;

import com.ydespreaux.shared.data.elasticsearch.entities.Book;
import com.ydespreaux.shared.data.elasticsearch.repository.ElasticsearchRepository;

public interface BookRepository extends ElasticsearchRepository<Book, String> {
}
