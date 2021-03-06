package com.ydespreaux.shared.testcontainers.elasticsearch;

import com.ydespreaux.shared.testcontainers.elasticsearch.ElasticsearchContainer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
public class ITElasticsearchContainer_568_Test {

    @ClassRule
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer("5.6.8");

    private static String testUrl(String path) {
        return elasticContainer.getURL() + path;
    }

    private static OkHttpClient createHttpClient() {
        return createHttpClient(10, TimeUnit.SECONDS);
    }

    private static OkHttpClient createHttpClient(long timeout, TimeUnit timeUnit) {
        return new OkHttpClient.Builder().connectTimeout(timeout, timeUnit)
                .writeTimeout(timeout, timeUnit).readTimeout(timeout, timeUnit).build();
    }

    @Test
    public void environmentSystemProperty() {
        assertThat(System.getProperty(elasticContainer.getUrisSystemProperty()), is(equalTo("http://" + elasticContainer.getContainerIpAddress() + ":" + elasticContainer.getHttpPort())));
    }

    @Test
    public void getURL() {
        assertThat(elasticContainer.getURL(), is(equalTo("http://" + elasticContainer.getContainerIpAddress() + ":" + elasticContainer.getHttpPort())));
    }

    @Test
    public void getInternalURL() {
        assertThat(elasticContainer.getInternalURL(), is(equalTo("http://" + elasticContainer.getNetworkAliases().get(0) + ":" + 9200)));
    }

    @Test
    public void health() throws IOException {
        Response response = call("/_cluster/health");
        assertThat(response.isSuccessful(), is(true));
    }

    private Response call(String path) throws IOException {
        OkHttpClient client = createHttpClient();
        Request request = new Request.Builder().get().url(testUrl(path)).build();
        return client.newCall(request).execute();
    }

}
