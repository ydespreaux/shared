package com.ydespreaux.shared.autoconfigure.kafka.connect;

import org.apache.kafka.common.config.SslConfigs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(SpringJUnit4ClassRunner.class)
public class KafkaConnectPropertiesTest {

    @Test
    public void afterPropertiesSetWithSSLAndEmbedded() throws Exception {
        final String protocol = "SSL";
        final String keyPassword = "key_password";
        final String trutstorePassword = "trutstore_password";
        final String keystorePassword = "keystore_password";
        final ClassPathResource trutstoreLocation = new ClassPathResource("/jks/trutstore.jks", KafkaConnectPropertiesTest.class);
        final ClassPathResource keystoreLocation = new ClassPathResource("/jks/keystore.jks", KafkaConnectPropertiesTest.class);

        KafkaConnectProperties properties = new KafkaConnectProperties();
        properties.getCluster().setEmbedded(true);
        properties.getCluster().setSecurityProtocol(protocol);
        KafkaConnectProperties.SslConfig ssl = properties.getCluster().getSsl();
        ssl.setKeyPassword(keyPassword);
        ssl.setKeystorePassword(keystorePassword);
        ssl.setTruststorePassword(trutstorePassword);
        ssl.setKeystoreLocation(keystoreLocation);
        ssl.setTruststoreLocation(trutstoreLocation);

        properties.afterPropertiesSet();

        Map<String, String> worker = properties.getWorker();
        assertThat(worker.get("security.protocol"), is(equalTo(protocol)));
        assertThat(worker.get("producer.security.protocol"), is(equalTo(protocol)));
        assertThat(worker.get("consumer.security.protocol"), is(equalTo(protocol)));
        assertThat(worker.get(SslConfigs.SSL_KEY_PASSWORD_CONFIG), is(equalTo(keyPassword)));
        assertThat(worker.get("producer." + SslConfigs.SSL_KEY_PASSWORD_CONFIG), is(equalTo(keyPassword)));
        assertThat(worker.get("consumer." + SslConfigs.SSL_KEY_PASSWORD_CONFIG), is(equalTo(keyPassword)));

        assertThat(worker.get(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG), is(equalTo(keystoreLocation.getFile().getAbsolutePath())));
        assertThat(worker.get("producer." + SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG), is(equalTo(keystoreLocation.getFile().getAbsolutePath())));
        assertThat(worker.get("consumer." + SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG), is(equalTo(keystoreLocation.getFile().getAbsolutePath())));

        assertThat(worker.get(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG), is(equalTo(keystorePassword)));
        assertThat(worker.get("producer." + SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG), is(equalTo(keystorePassword)));
        assertThat(worker.get("consumer." + SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG), is(equalTo(keystorePassword)));

        assertThat(worker.get(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG), is(equalTo(trutstoreLocation.getFile().getAbsolutePath())));
        assertThat(worker.get("producer." + SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG), is(equalTo(trutstoreLocation.getFile().getAbsolutePath())));
        assertThat(worker.get("consumer." + SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG), is(equalTo(trutstoreLocation.getFile().getAbsolutePath())));

        assertThat(worker.get(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG), is(equalTo(trutstorePassword)));
        assertThat(worker.get("producer." + SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG), is(equalTo(trutstorePassword)));
        assertThat(worker.get("consumer." + SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG), is(equalTo(trutstorePassword)));

    }

    @Test
    public void afterPropertiesSetWithArrowbaseFormatter() throws Exception {
        KafkaConnectProperties properties = new KafkaConnectProperties();

        // Init connector configuration
        Map<String, String> config = new HashMap<>();
        config.put("name", "tweet-elasticsearch-sink");
        config.put("tasks.max", "1");
        config.put("connector.class", "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector");
        config.put("connection.url", "http://localhost:9200");
        config.put("connection.username", "elastic");
        config.put("connection.password", "changeme");
        config.put("type.name", "tweet");
        config.put("topics", "cdtjava-tweet-out-v1");
        config.put("key.ignore", "false");
        config.put("schema.ignore", "true");
        config.put("transforms", "RouteTS");
        config.put("transforms.RouteTS.type", "org.apache.kafka.connect.transforms.TimestampRouter");
        config.put("transforms.RouteTS.topic.format", "tweeter-@{timestamp}");
        config.put("transforms.RouteTS.timestamp.format", "yyyy-MM-dd");
        KafkaConnectProperties.ConnectorConfiguration connector = new KafkaConnectProperties.ConnectorConfiguration("elasticsearch-sink", config);
        //
        properties.getConnectors().put("elasticsearch-sink", connector);

        properties.afterPropertiesSet();

        assertThat(connector.getConfig().get("transforms.RouteTS.topic.format"), is(equalTo("tweeter-${timestamp}")));

    }

    @Test
    public void afterPropertiesSetWithSSLAndDistant() throws Exception {
        final String protocol = "SSL";
        final String keyPassword = "key_password";
        final String trutstorePassword = "trutstore_password";
        final String keystorePassword = "keystore_password";
        final ClassPathResource trutstoreLocation = new ClassPathResource("classpath:/jks/trutstore.jks");
        final ClassPathResource keystoreLocation = new ClassPathResource("classpath:/jks/keystore.jks");

        KafkaConnectProperties properties = new KafkaConnectProperties();
        properties.getCluster().setEmbedded(false);
        properties.getCluster().setSecurityProtocol(protocol);
        KafkaConnectProperties.SslConfig ssl = properties.getCluster().getSsl();
        ssl.setKeyPassword(keyPassword);
        ssl.setKeystorePassword(keystorePassword);
        ssl.setTruststorePassword(trutstorePassword);
        ssl.setKeystoreLocation(keystoreLocation);
        ssl.setTruststoreLocation(trutstoreLocation);

        properties.afterPropertiesSet();

        Map<String, String> worker = properties.getWorker();
        assertThat(worker.isEmpty(), is(true));
    }
}
