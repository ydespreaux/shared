package com.ydespreaux.shared.data.elasticsearch.configuration;import com.ydespreaux.shared.data.elasticsearch.repository.config.EnableElasticsearchRepositories;import org.springframework.boot.autoconfigure.EnableAutoConfiguration;import org.springframework.context.annotation.Configuration;@Configuration@EnableAutoConfiguration@EnableElasticsearchRepositories(basePackages = "com.ydespreaux.shared.data.elasticsearch.repositories")public class ElasticsearchConfiguration {}