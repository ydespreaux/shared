package com.ydespreaux.shared.testcontainers.kafka.rule;

import com.ydespreaux.shared.testcontainers.kafka.config.TopicConfiguration;
import com.ydespreaux.shared.testcontainers.kafka.containers.KafkaContainer;
import com.ydespreaux.shared.testcontainers.kafka.containers.SchemaRegistryContainer;
import com.ydespreaux.shared.testcontainers.kafka.containers.ZookeeperContainer;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.rules.ExternalResource;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.containers.Network;

import java.util.*;

import static com.ydespreaux.shared.testcontainers.common.utils.ContainerUtils.execCmd;
import static com.ydespreaux.shared.testcontainers.common.utils.ContainerUtils.getContainerHostname;

/**
 * This class is used to start a zookeeper container, a kafka container, and a schema registry container
 * if it is enabled.
 * @since 1.0.0
 * @param <SELF>
 */
@Slf4j
public class ConfluentKafkaContainer<SELF extends ConfluentKafkaContainer<SELF>> extends ExternalResource implements ConfluentContainer<SELF> {

    private static final String CONFLUENT_DEFAULT_VERSION = "3.3.1";

    /**
     *
     */
    private static final String BROKER_SERVERS_SYSTEM_PROPERTY = "spring.kafka.bootstrap-servers";

    /**
     *
     */
    private static final String SCHEMA_REGISTRY_SYSTEM_PROPERTY = "spring.kafka.properties.schema.registry.url";

    /**
     * Define the confluent version.
     */
    @Getter
    private final String confluentVersion;

    /**
     * Define the network for all containers.
     */
    @Getter
    private Network network;
    /**
     * Define the zookeeper container.
     */
    @Getter
    private ZookeeperContainer zookeeperContainer;
    /**
     * Dfine the kafka container
     */
    @Getter
    private KafkaContainer kafkaContainer;
    /**
     * Define the schema registry container.
     */
    @Getter
    private SchemaRegistryContainer schemaRegistryContainer;
    /**
     * Enable the schema registry container.
     */
    @Getter
    private boolean schemaRegistryEnabled;

    private String brokerServersSystemProperty = BROKER_SERVERS_SYSTEM_PROPERTY;
    private String schemaRegistrySystemProperty = SCHEMA_REGISTRY_SYSTEM_PROPERTY;

    /**
     * Register springboot properties in environment
     */
    private boolean registerSpringbootProperties = true;
    /**
     *
     */
    private String formatMessageVersion;

    /**
     * Default topics list
     */
    private final List<TopicConfiguration> topics = new ArrayList<>();


    /**
     *
     */
    public ConfluentKafkaContainer() {
        this(CONFLUENT_DEFAULT_VERSION);
    }

    /**
     * @param confluentVersion
     */
    public ConfluentKafkaContainer(final String confluentVersion) {
        this.confluentVersion = confluentVersion;
    }

    /**
     * Enable the schema registry.
     *
     * @param schemaRegistryEnabled
     * @return
     */
    public SELF withSchemaRegistry(boolean schemaRegistryEnabled) {
        this.schemaRegistryEnabled = schemaRegistryEnabled;
        return this.self();
    }

    /**
     * Register the spring boot properties.
     *
     * @param registerProperties
     * @return
     */
    public SELF withRegisterSpringbootProperties(boolean registerProperties) {
        this.registerSpringbootProperties = registerProperties;
        return this.self();
    }

    /**
     *
     * @return
     */
    protected boolean isRegisterSpringbootProperties(){
        return this.registerSpringbootProperties;
    }

    /**
     * Set the network.
     *
     * @param network
     * @return
     */
    public SELF withNetwork(Network network) {
        this.network = network;
        return this.self();
    }

    /**
     * @param brokerServersSystemProperty
     * @return
     */
    public SELF withBrokerServersSystemProperty(String brokerServersSystemProperty) {
        this.brokerServersSystemProperty = brokerServersSystemProperty;
        return this.self();
    }

    /**
     * @param schemaRegistrySystemProperty
     * @return
     */
    public SELF withSchemaRegistrySystemProperty(String schemaRegistrySystemProperty) {
        this.schemaRegistrySystemProperty = schemaRegistrySystemProperty;
        return this.self();
    }

    /**
     *
     * @param version
     * @return
     */
    public SELF withFormatMessageVersion(String version) {
        this.formatMessageVersion = version;
        return this.self();
    }

    /**
     *
     * @param topicName
     * @param partitions
     * @param compact
     * @return
     */
    public SELF withTopic(String topicName, int partitions, boolean compact) {
        return withTopic(new TopicConfiguration(topicName, partitions, compact));
    }

    /**
     *
     * @param topic
     * @return
     */
    public SELF withTopic(TopicConfiguration topic) {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(topic.getName());
        this.topics.add(topic);
        return this.self();
    }

    /**
     * Get the local zookeeper url.
     *
     * @return
     */
    public String getZookeeperConnect() {
        return this.zookeeperContainer.getInternalURL();
    }

    /**
     * Get the zookeeper url.
     *
     * @return
     */
    public String getZookeeperServer() {
        return this.zookeeperContainer.getURL();
    }

    /**
     * Get the kafka url.
     *
     * @return
     */
    public String getBootstrapServers() {
        return this.kafkaContainer.getURL();
    }

    /**
     * @return
     */
    public String getSchemaRegistryServers() {
        if (!this.schemaRegistryEnabled) {
            throw new IllegalArgumentException("Schema registry not started");
        }
        return schemaRegistryContainer.getURL();
    }

    /**
     * Destroy all containers.
     *
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        after();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        before();
    }

    /**
     * Start all containers.
     *
     * @throws Exception
     */
    @Override
    public void before() throws Exception {
        if (this.network == null) {
            withNetwork(Network.newNetwork());
        }

        zookeeperContainer = new ZookeeperContainer<>(this.confluentVersion)
                .withNetwork(network);
        zookeeperContainer.start();

        kafkaContainer = new KafkaContainer<>(this.confluentVersion)
                .withZookeeperHostname(getContainerHostname(zookeeperContainer))
                .withZookeeperPort(zookeeperContainer.getMappingPort())
                .withRegisterSpringbootProperties(this.registerSpringbootProperties)
                .withBrokerServersSystemProperty(this.brokerServersSystemProperty)
                .withFormatMessageVersion(this.formatMessageVersion)
                .withNetwork(network);
        kafkaContainer.start();
        // Create default topics
        if (!this.topics.isEmpty()) {
            this.createTopics(this.topics);
        }

        if (this.schemaRegistryEnabled) {
            schemaRegistryContainer = new SchemaRegistryContainer<>(this.confluentVersion)
                    .withZookeeperInternalURL(zookeeperContainer.getInternalURL())
                    .withBootstrapServersInternalURL(kafkaContainer.getInternalURL())
                    .withRegisterSpringbootProperties(this.registerSpringbootProperties)
                    .withSchemaRegistrySystemProperty(this.schemaRegistrySystemProperty)
                    .withNetwork(network);
            schemaRegistryContainer.start();
        }
    }

    /**
     * Stop and remove all containers.
     */
    @Override
    public void after() {
        if (schemaRegistryContainer != null && schemaRegistryContainer.isRunning()) {
            this.schemaRegistryContainer.stop();
        }
        if (kafkaContainer != null && kafkaContainer.isRunning()) {
            kafkaContainer.stop();
        }
        if (zookeeperContainer != null && zookeeperContainer.isRunning()) {
            zookeeperContainer.stop();
        }
    }

    @Override
    public SELF self() {
        return (SELF) this;
    }

    /**
     *
     * @param <K>
     * @param <V>
     * @return
     */
    public <K, V> KafkaTemplate<K, V> createKafkaTemplate() {
        return createKafkaTemplate("org.apache.kafka.common.serialization.StringSerializer",
                "io.confluent.kafka.serializers.KafkaAvroSerializer");
    }

    /**
     * @param keySerializerClass
     * @param valueSerializerClass
     * @param <K>
     * @param <V>
     * @return
     */
    public <K, V> KafkaTemplate<K, V> createKafkaTemplate(String keySerializerClass, String valueSerializerClass) {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerConfigs(keySerializerClass, valueSerializerClass)));
    }

    /**
     * @param keySerializer
     * @param valueSerializer
     * @param <K>
     * @param <V>
     * @return
     */
    public <K, V> KafkaTemplate<K, V> createKafkaTemplate(Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerConfigs(null, null), keySerializer, valueSerializer));
    }

    /**
     * Build properties
     *
     * @return
     */
    private Map<String, Object> producerConfigs(String keySerializerClass, String valueSerializerClass) {
        Map<String, Object> props = new HashMap<>();
        if (this.isSchemaRegistryEnabled()) {
            props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, this.getSchemaRegistryServers());
        }
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.getBootstrapServers());
        if (keySerializerClass != null) {
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializerClass);
        }
        if (valueSerializerClass != null) {
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializerClass);
        }
        return props;
    }

    /**
     *
     * @param group
     * @param keyDeserializer
     * @param valueDeserializer
     * @param <K>
     * @param <V>
     * @return
     */
    public <K, V> DefaultKafkaConsumerFactory<K, V> createKafkaConsumerFactory(final String group,
                                                                               final Deserializer<K> keyDeserializer,
                                                                               final Deserializer<V> valueDeserializer) {
        Map<String, Object> properties = org.springframework.kafka.test.utils.KafkaTestUtils.consumerProps(this.getBootstrapServers(), group, "true");
        return new DefaultKafkaConsumerFactory<>(properties, keyDeserializer, valueDeserializer);
    }

    /**
     * @param group
     * @return
     */
    public DefaultKafkaConsumerFactory<String, ?> createKafkaAvroConsumerFactory(String group) {
        Map<String, String> config = new HashMap<>();
        config.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, this.getSchemaRegistryServers());
        config.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
        KafkaAvroDeserializer valueDeserializer = new KafkaAvroDeserializer();
        valueDeserializer.configure(config, false);
        return createKafkaConsumerFactory(group, new StringDeserializer(), valueDeserializer);
    }

    /**
     *
     * @param topic
     */
    public void createTopic(TopicConfiguration topic) {
        createTopics(Collections.singletonList(topic));
    }

    /**
     *
     * @param topics
     */
    public void createTopics(List<TopicConfiguration> topics) {
        if (!topics.isEmpty()) {
            final String zookeeper = this.getZookeeperConnect();
            final KafkaContainer kafkaContainer = this.getKafkaContainer();
            topics.forEach(topic -> {
                execCmd(kafkaContainer.getDockerClient(),
                        kafkaContainer.getContainerId(),
                        getCreateTopicCmd(topic.getName(), topic.getPartitions(), topic.isCompact(), zookeeper, 1));
            });
        }
    }

    /**
     * @param topicName
     * @param partitions
     * @param kafkaZookeeperConnect
     * @param brokersCount
     * @return
     */
    private String[] getCreateTopicCmd(String topicName, int partitions, boolean compact, String kafkaZookeeperConnect, int brokersCount) {
        String[] args = new String[]{
                "kafka-topics",
                "--create", "--topic", topicName,
                "--partitions", String.valueOf(partitions),
                "--replication-factor", String.valueOf(brokersCount),
                "--if-not-exists",
                "--zookeeper", kafkaZookeeperConnect
        };
        if (compact) {
            int orignalLength = args.length;
            args = Arrays.copyOf(args, orignalLength + 2);
            args[orignalLength] = "--config";
            args[orignalLength + 1] = "cleanup.policy=compact";
        }

        return args;
    }
}
