package com.ydespreaux.shared.data.elasticsearch;

import com.ydespreaux.shared.data.elasticsearch.repository.support.ITArticleRepositoryContextTest;
import com.ydespreaux.shared.data.elasticsearch.repository.support.ITArticleRepositoryTest;
import com.ydespreaux.shared.data.elasticsearch.repository.support.ITBookRepositoryContextTest;
import com.ydespreaux.shared.data.elasticsearch.repository.support.ITBookRepositoryTest;
import com.ydespreaux.shared.testcontainers.elasticsearch.ElasticsearchContainer;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ITElasticsearchTemplateTest.class,
        ITArticleRepositoryContextTest.class,
        ITArticleRepositoryTest.class,
        ITBookRepositoryContextTest.class,
        ITBookRepositoryTest.class
})
public class ITSuiteTest {

    @ClassRule
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer("6.5.0")
            .withUrisSystemProperty("spring.elasticsearch.rest.uris");
}
