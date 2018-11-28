package com.ydespreaux.shared.autoconfigure.kafka;

import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.KafkaException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xpax624
 * @since 7.0.0
 */
@Slf4j
@Validated
@Getter
@Setter
@EnableConfigurationProperties(KafkaProperties.class)
@ConfigurationProperties(prefix = "spring.kafka.extra")
public class KafkaExtraProperties {

    private static final String SECURITY_PROTOCOL_DEFAULT_VALUE = "PLAINTEXT";
    private static final String NO_KEY_TOPIC_MESSAGE = "No key topic defined for getTopicName()";

    /**
     * Security procole
     */
    private String securityProtocol = SECURITY_PROTOCOL_DEFAULT_VALUE;

    /**
     * Schema registry url
     */
    private String schemaRegistry;

    /**
     * Application id
     */
    private String applicationId;

    /**
     * Topic properties
     */
    @Valid
    private Map<String, TopicProperties> topics = new HashMap<>();

    /**
     * Default kafka properties
     */
    private final KafkaProperties kafkaProperties;

    /**
     * @param properties
     */
    public KafkaExtraProperties(KafkaProperties properties) {
        this.kafkaProperties = properties;
    }

    /**
     * @param keyTopic
     * @return
     */
    public String getTopicName(String keyTopic) {
        Assert.notNull(keyTopic, NO_KEY_TOPIC_MESSAGE);
        return getTopic(keyTopic).getName();
    }

    /**
     * @param keyTopic
     * @return
     */
    public Integer getMaxPollRecords(String keyTopic) {
        return getMaxPollRecordsOrDefault(keyTopic, null);
    }

    /**
     * @param keyTopic
     * @param defaultValue
     * @return
     */
    public Integer getMaxPollRecordsOrDefault(String keyTopic, Integer defaultValue) {
        Assert.notNull(keyTopic, NO_KEY_TOPIC_MESSAGE);
        TopicConsumer consumer = getConsumer(keyTopic);
        Integer maxPollRecords = consumer.getMaxPollRecords() == null ? this.kafkaProperties.getConsumer().getMaxPollRecords() : consumer.getMaxPollRecords();
        if (maxPollRecords == null) {
            return defaultValue;
        }
        return maxPollRecords;
    }

    /**
     * @param keyTopic
     * @return
     */
    public Integer getConcurrency(String keyTopic) {
        return getConcurrencyOrDefault(keyTopic, null);
    }

    /**
     * @param keyTopic
     * @param defaultValue
     * @return
     */
    public Integer getConcurrencyOrDefault(String keyTopic, Integer defaultValue) {
        Assert.notNull(keyTopic, NO_KEY_TOPIC_MESSAGE);
        Integer concurrency = getConsumer(keyTopic).getConcurrency();
        if (concurrency == null) {
            return defaultValue;
        }
        return concurrency;
    }

    /**
     * @param keyTopic
     * @return
     */
    private TopicProperties getTopic(String keyTopic) {
        Assert.notNull(keyTopic, NO_KEY_TOPIC_MESSAGE);
        TopicProperties properties = this.topics.get(keyTopic);
        Assert.notNull(properties, String.format("No topic key %s defined for getConsumer()", keyTopic));
        return properties;
    }

    /**
     * @param keyTopic
     * @return
     */
    public TopicConsumer getConsumer(String keyTopic) {
        return getTopic(keyTopic).getConsumer();
    }

    /**
     * Create an initial map of consumer properties from the state of this instance.
     *
     * @return
     */
    public Map<String, Object> buildConsumerProperties() {
        return buildConsumerProperties(null);
    }

    /**
     * Create an initial map of consumer properties from the state of this instance.
     *
     * @return
     */
    public Map<String, Object> buildConsumerProperties(String topicKey) {
        Map<String, Object> properties = this.kafkaProperties.buildConsumerProperties();
        properties.putAll(addExtraProperties());
        if (topicKey != null) {
            properties.putAll(buildSpecificConsumerProperties(topicKey));
        }
        return properties;
    }

    /**
     * @param keyTopic
     * @return
     */
    protected Map<String, Object> buildSpecificConsumerProperties(String keyTopic) {
        Assert.notNull(keyTopic, NO_KEY_TOPIC_MESSAGE);
        final Map<String, Object> props = new HashMap<>();
        TopicConsumer consumerConfig = this.getConsumer(keyTopic);
        if (!StringUtils.isEmpty(schemaRegistry)) {
            props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, consumerConfig.getReadSpecificAvro() == null ? "true" : consumerConfig.getReadSpecificAvro().toString());
        }
        props.putAll(consumerConfig.getProperties());
        return props;
    }

    /**
     * Create an initial map of producer properties from the state of this instance.
     *
     * @return
     */
    public Map<String, Object> buildProducerProperties() {
        Map<String, Object> properties = this.kafkaProperties.buildProducerProperties();
        properties.putAll(addExtraProperties());
        return properties;
    }

    /**
     * Create an initial map of stream properties from the state of this instance.
     *
     * @return
     */
    public StreamsConfig buildKafkaStreamProperties() {
        Map<String, Object> properties = this.kafkaProperties.buildConsumerProperties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, this.applicationId);
        properties.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);
        if (!StringUtils.isEmpty(System.getProperty(StreamsConfig.STATE_DIR_CONFIG))) {
            properties.put(StreamsConfig.STATE_DIR_CONFIG, System.getProperty(StreamsConfig.STATE_DIR_CONFIG));
        }
        // Add security protocol
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, StringUtils.isEmpty(securityProtocol) ? SECURITY_PROTOCOL_DEFAULT_VALUE : securityProtocol);
        // disable auto commit and throw exception if there is user overridden values,
        // this is necessary for streams commit semantics
        properties.remove(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG);
        //
        registerSSL(this.schemaRegistry, this.kafkaProperties);
        return new StreamsConfig(properties);
    }

    /**
     *
     */
    protected Map<String, Object> addExtraProperties() {
        Map<String, Object> properties = new HashMap<>();
        // Add schema registry url
        if (!StringUtils.isEmpty(schemaRegistry)) {
            properties.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistry);
            registerSSL(this.schemaRegistry, this.kafkaProperties);
        }
        // Add security protocol
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, StringUtils.isEmpty(securityProtocol) ? SECURITY_PROTOCOL_DEFAULT_VALUE : securityProtocol);
        return properties;
    }

    /**
     * Register ssl environnement for https schema registry.
     */
    private static void registerSSL(String schemaRegistryUrl, KafkaProperties properties){
        if (!StringUtils.isEmpty(schemaRegistryUrl) && schemaRegistryUrl.startsWith("https://")) {
            try {
                if (System.getProperty("javax.net.ssl.keyStore") == null){
                    if (properties.getSsl() == null || properties.getSsl().getKeyStoreLocation() == null) {
                        if (log.isWarnEnabled()) {
                            log.warn("The keystore location is not configured !!");
                        }
                    }else {
                        System.setProperty("javax.net.ssl.keyStore", properties.getSsl().getKeyStoreLocation().getFile().getPath());
                        System.setProperty("javax.net.ssl.keyStorePassword", properties.getSsl().getKeyStorePassword());
                    }
                }
                if (System.getProperty("javax.net.ssl.trustStore") == null){
                    if (properties.getSsl() == null || properties.getSsl().getTrustStoreLocation() == null) {
                        if (log.isWarnEnabled()) {
                            log.warn("The truststore location is not configured !!");
                        }
                    } else {
                        System.setProperty("javax.net.ssl.trustStore", properties.getSsl().getTrustStoreLocation().getFile().getPath());
                        System.setProperty("javax.net.ssl.trustStorePassword", properties.getSsl().getTrustStorePassword());
                    }
                }
            }catch(IOException e) {
                throw new KafkaException("Invalid configuration", e);
            }
        }
    }

    @Getter
    @Setter
    public static class TopicProperties {

        /**
         *
         */
        private String name;
        /**
         *
         */
        @Valid
        private TopicConsumer consumer = new TopicConsumer();
        /**
         *
         */
        @Valid
        private TopicProducer producer = new TopicProducer();
    }


    @Getter
    @Setter
    public static class TopicConsumer {

        public static final Integer MAX_POOL_RECORDS_DEFAULT_VALUE = 50;

        /**
         *
         */
        private Integer concurrency = 3;
        /**
         *
         */
        @NotNull
        private Boolean batchListener = Boolean.FALSE;
        /**
         * Maximum number of records returned in a single call to poll().
         */
        private Integer maxPollRecords = null;
        /**
         *
         */
        @NotNull
        private Boolean readSpecificAvro = Boolean.TRUE;
        /**
         *
         */
        private Map<String, String> properties = new HashMap<>();
    }

    @Getter
    @Setter
    public static class TopicProducer {

    }
}
