package com.ydespreaux.shared.testcontainers.cassandra;

import com.ydespreaux.shared.testcontainers.cassandra.CassandraContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@Slf4j
@RunWith(SpringRunner.class)
public class CassandraContainerConfigTest {

    @Test
    public void withCqlScriptDirectory() {
        CassandraContainer container = new CassandraContainer()
                .withCqlScriptDirectory("db-schema");
        List<String> scripts = container.getCqlScripts();
        assertThat(scripts.size(), is(equalTo(2)));
        assertThat(scripts.get(0), is(equalTo("/tmp/init-schema/1-schema.cql")));
        assertThat(scripts.get(1), is(equalTo("/tmp/init-schema/2-data.cql")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void withCqlScriptDirectoryWithResourceNotFound() {
        new CassandraContainer()
                .withCqlScriptDirectory("db-schema-notfound");
    }

    @Test(expected = IllegalArgumentException.class)
    public void withCqlScriptDirectoryWithFileResource() {
        new CassandraContainer()
                .withCqlScriptDirectory("db-schema/1-schema.cql");
    }
}
