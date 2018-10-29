package com.ydespreaux.shared.testcontainers.kafka.utils;

import com.ydespreaux.shared.testcontainers.kafka.config.TopicConfiguration;
import com.ydespreaux.shared.testcontainers.kafka.containers.KafkaContainer;
import com.ydespreaux.shared.testcontainers.kafka.rule.ConfluentKafkaContainer;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.config.ContainerProperties;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ydespreaux.shared.testcontainers.common.utils.ContainerUtils.execCmd;

/**
 *
 * @since 1.0.0
 */
public final class KafkaTestUtils {

    /**
     *
     */
    private KafkaTestUtils(){
        // Nothing to do
    }

    /**
     * @param container
     * @param topic
     * @param partitions
     * @param compact
     */
    public static void createTopic(ConfluentKafkaContainer container, String topic, int partitions, boolean compact) {
        createTopics(container, new String[]{topic}, new int[]{partitions}, new boolean[]{compact});
    }

    /**
     * @param container
     * @param topics
     * @param partitions
     * @param compact
     */
    public static void createTopics(ConfluentKafkaContainer container, String[] topics, int[] partitions, boolean[] compact) {
        if (topics.length > 0) {
            final String zookeeper = container.getZookeeperConnect();
            final KafkaContainer kafkaContainer = container.getKafkaContainer();
            for (int i = 0; i < topics.length; i++) {
                execCmd(kafkaContainer.getDockerClient(),
                        kafkaContainer.getContainerId(),
                        getCreateTopicCmd(topics[i], partitions[i], compact[i], zookeeper, 1));
            }
        }
    }

    /**
     * @param container
     * @param topic
     */
    public static void createTopic(ConfluentKafkaContainer container, TopicConfiguration topic) {
        final String zookeeper = container.getZookeeperConnect();
        final KafkaContainer kafkaContainer = container.getKafkaContainer();
        execCmd(kafkaContainer.getDockerClient(),
                kafkaContainer.getContainerId(),
                getCreateTopicCmd(topic.getName(), topic.getPartitions(), topic.isCompact(), zookeeper, 1));
    }

    /**
     * @param container
     * @param topics
     */
    public static void createTopics(ConfluentKafkaContainer container, List<TopicConfiguration> topics) {
        if (!topics.isEmpty()) {
            final String zookeeper = container.getZookeeperConnect();
            final KafkaContainer kafkaContainer = container.getKafkaContainer();
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
    public static String[] getCreateTopicCmd(String topicName, int partitions, boolean compact, String kafkaZookeeperConnect, int brokersCount) {
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


    /**
     * @param container
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> KafkaTemplate<K, V> createKafkaTemplate(final ConfluentKafkaContainer container) {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerConfigs(container,
                "org.apache.kafka.common.serialization.StringSerializer",
                "io.confluent.kafka.serializers.KafkaAvroSerializer")));
    }

    /**
     * @param container
     * @param keySerializerClass
     * @param valueSerializerClass
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> KafkaTemplate<K, V> createKafkaTemplate(final ConfluentKafkaContainer container, String keySerializerClass, String valueSerializerClass) {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerConfigs(container, keySerializerClass, valueSerializerClass)));
    }

    /**
     * @param container
     * @param keySerializer
     * @param valueSerializer
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> KafkaTemplate<K, V> createKafkaTemplate(final ConfluentKafkaContainer container, Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerConfigs(container, null, null), keySerializer, valueSerializer));
    }


    /**
     * Build properties
     *
     * @return
     */
    private static Map<String, Object> producerConfigs(final ConfluentKafkaContainer container, String keySerializerClass, String valueSerializerClass) {
        Map<String, Object> props = new HashMap<>();
        if (container.isSchemaRegistryEnabled()) {
            props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, container.getSchemaRegistryServers());
        }
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, container.getBootstrapServers());
        if (keySerializerClass != null) {
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializerClass);
        }
        if (valueSerializerClass != null) {
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializerClass);
        }
        return props;
    }

    /**
     * @param group
     * @param topicName
     * @param container
     * @param listener
     * @return
     */
    public static KafkaMessageListenerContainer<String, ?> createListenerContainer(
            ConfluentKafkaContainer container,
            String group,
            String topicName,
            final MessageListener<String, ?> listener) {
        Map<String, String> config = new HashMap<>();
        config.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, container.getSchemaRegistryServers());
        config.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
        KafkaAvroDeserializer valueDeserializer = new KafkaAvroDeserializer();
        valueDeserializer.configure(config, false);
        return createListenerContainer(container, group, topicName, new StringDeserializer(), valueDeserializer, listener, true);
    }

    /**
     * @param container
     * @param group
     * @param topicName
     * @param keyDeserializer
     * @param valueDeserializer
     * @param listener
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> KafkaMessageListenerContainer<K, V> createListenerContainer(final ConfluentKafkaContainer container,
                                                                                     final String group,
                                                                                     final String topicName,
                                                                                     final Deserializer<K> keyDeserializer,
                                                                                     final Deserializer<V> valueDeserializer,
                                                                                     final MessageListener<K, V> listener) {
        return createListenerContainer(container, group, topicName, keyDeserializer, valueDeserializer, listener, true);
    }

    /**
     * @param group
     * @param topicName
     * @param keyDeserializer
     * @param valueDeserializer
     * @param listener
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> KafkaMessageListenerContainer<K, V> createListenerContainer(final ConfluentKafkaContainer container,
                                                                                     final String group,
                                                                                     final String topicName,
                                                                                     final Deserializer<K> keyDeserializer,
                                                                                     final Deserializer<V> valueDeserializer,
                                                                                     final MessageListener<K, ?> listener,
                                                                                     final boolean start) {

        Map<String, Object> properties = org.springframework.kafka.test.utils.KafkaTestUtils.consumerProps(container.getBootstrapServers(), group, "true");
        DefaultKafkaConsumerFactory<K, V> cf = new DefaultKafkaConsumerFactory<>(properties, keyDeserializer, valueDeserializer);
        KafkaMessageListenerContainer<K, V> adminContainer = new KafkaMessageListenerContainer<>(cf, new ContainerProperties(topicName));
        adminContainer.setupMessageListener(listener);
        if (start) {
            adminContainer.start();
        }
        return adminContainer;
    }


}
