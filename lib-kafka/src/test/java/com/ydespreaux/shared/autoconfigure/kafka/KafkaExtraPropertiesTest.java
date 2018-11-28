package com.ydespreaux.shared.autoconfigure.kafka;

import com.ydespreaux.shared.commons.io.support.YamlPropertySourceFactory;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@PropertySource(factory = YamlPropertySourceFactory.class, value = "classpath:kafka-test.yml")
@EnableConfigurationProperties(KafkaExtraProperties.class)
public class KafkaExtraPropertiesTest {

    @Autowired
    private KafkaExtraProperties properties;

    @Test
    public void kafkaExtraPropertiesTest() {
        assertEquals("PLAINTEXT", properties.getSecurityProtocol());
        assertEquals("an_dev_admin_priv_v1", properties.getTopicName("topic-admin"));
        assertEquals("an_dev_simpletweet_priv_v1", properties.getTopicName("topic-simple-tweet"));
        assertEquals("kstreamId", this.properties.getApplicationId());
        assertEquals("http://localhost:8081", this.properties.getSchemaRegistry());

        KafkaProperties.Producer producer = properties.getKafkaProperties().getProducer();
        assertEquals(StringSerializer.class, producer.getKeySerializer());
        assertEquals(KafkaAvroSerializer.class, producer.getValueSerializer());

        KafkaProperties.Consumer consumer = properties.getKafkaProperties().getConsumer();
        assertEquals(StringDeserializer.class, consumer.getKeyDeserializer());
        assertEquals(KafkaAvroDeserializer.class, consumer.getValueDeserializer());
    }

    @Test
    public void buildConsumerPropertiesTest() {
        Map<String, Object> consumerProperties = properties.buildConsumerProperties();
        assertEquals("PLAINTEXT", consumerProperties.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
        assertEquals(StringDeserializer.class, consumerProperties.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
        assertEquals(KafkaAvroDeserializer.class, consumerProperties.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG));
    }

    @Test
    public void buildProducerPropertiesTest() {
        Map<String, Object> producerProperties = properties.buildProducerProperties();
        assertEquals("PLAINTEXT", producerProperties.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
        assertEquals(StringSerializer.class, producerProperties.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertEquals(KafkaAvroSerializer.class, producerProperties.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
    }

    @Test
    public void buildKafkaStreamPropertiesTest() {
        StreamsConfig streamProperties = this.properties.buildKafkaStreamProperties();


        Properties props = new Properties();
        streamProperties.values().forEach((k,v) -> {
            if (v != null){
                props.put(k,v);
            }
        });

        assertEquals("PLAINTEXT", props.getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
        assertEquals("kstreamId", props.getProperty(StreamsConfig.APPLICATION_ID_CONFIG));

        assertNull(props.getProperty(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG));
        assertNull(props.getProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
        assertNull(props.getProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG));
        assertNull(props.getProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG));
    }

    @Test
    public void getMaxPollRecords() {
        assertEquals(new Integer(10), this.properties.getMaxPollRecords("topic-admin"));
    }

    @Test
    public void getDefaultMaxPollRecords() {
        assertEquals(new Integer(100), this.properties.getMaxPollRecords("topic-simple-tweet"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMaxPollRecordsInvalid() {
        this.properties.getMaxPollRecords(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMaxPollRecordsUndefined() {
        this.properties.getMaxPollRecords("topic-undefined");
    }

    @Test
    public void getConcurrency() {
        assertEquals(new Integer(9), properties.getConcurrency("topic-simple-tweet"));
    }

    @Test
    public void getConcurrencyOrDefault() {
        assertEquals(new Integer(5), properties.getConcurrencyOrDefault("topic-admin", 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getConcurrencyInvalid() {
        this.properties.getConcurrency(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getConcurrencyUndefined() {
        this.properties.getConcurrency("topic-undefined");
    }
}
