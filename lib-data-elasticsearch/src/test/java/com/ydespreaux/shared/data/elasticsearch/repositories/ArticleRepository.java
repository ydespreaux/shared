package com.ydespreaux.shared.data.elasticsearch.repositories;import com.ydespreaux.shared.data.elasticsearch.entities.Article;import com.ydespreaux.shared.data.elasticsearch.repository.support.ElasticsearchRepository;public interface ArticleRepository extends ElasticsearchRepository<Article, String> {}