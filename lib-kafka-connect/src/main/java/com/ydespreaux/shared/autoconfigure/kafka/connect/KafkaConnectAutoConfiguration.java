package com.ydespreaux.shared.autoconfigure.kafka.connect;

import com.ydespreaux.shared.kafka.connect.support.KafkaConnectEmbeddedTemplate;
import com.ydespreaux.shared.kafka.connect.support.KafkaConnectRestTemplate;
import com.ydespreaux.shared.kafka.connect.support.KafkaConnectTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.connect.runtime.Herder;
import org.apache.kafka.connect.runtime.Worker;
import org.apache.kafka.connect.runtime.WorkerConfigTransformer;
import org.apache.kafka.connect.runtime.WorkerInfo;
import org.apache.kafka.connect.runtime.distributed.DistributedConfig;
import org.apache.kafka.connect.runtime.distributed.DistributedHerder;
import org.apache.kafka.connect.runtime.isolation.Plugins;
import org.apache.kafka.connect.runtime.standalone.StandaloneConfig;
import org.apache.kafka.connect.runtime.standalone.StandaloneHerder;
import org.apache.kafka.connect.storage.*;
import org.apache.kafka.connect.util.ConnectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(KafkaConnectProperties.class)
public class KafkaConnectAutoConfiguration {

    @Configuration
    @ConditionalOnMissingBean(KafkaConnectTemplate.class)
    @ConditionalOnProperty(prefix = "spring.kafka-connect.cluster", name = "embedded", havingValue = "true", matchIfMissing = true)
    public static class EmbeddedKafkaConnectAutoConfiguration {

        /**
         * Kafka connect properties
         */
        private final KafkaConnectProperties properties;

        /**
         * Default constructor
         * @param properties
         */
        public EmbeddedKafkaConnectAutoConfiguration(final KafkaConnectProperties properties){
            this.properties = properties;
            this.properties.afterPropertiesSet();
        }

        /**
         * Create the kafka connect template
         * @return
         * @throws IOException
         */
        @Bean
        public KafkaConnectEmbeddedTemplate kafkaConnectEmbeddedTemplate() throws IOException {
            return new KafkaConnectEmbeddedTemplate(createHerder());
        }

        /**
         * Create a herder
         * @return
         * @throws IOException
         */
        @Bean
        Herder createHerder() throws IOException {
            KafkaConnectProperties.ClusterConfig.KafkaConnectMode mode = this.properties.getCluster().getMode();
            return mode == KafkaConnectProperties.ClusterConfig.KafkaConnectMode.DISTRIBUED || mode == KafkaConnectProperties.ClusterConfig.KafkaConnectMode.DISTRIBUTED ?
                    createDistributedHerder() : createStandaloneHerder();
        }

        /**
         * Create a herder distributed
         * @return
         */
        private Herder createDistributedHerder() throws IOException {
            Time time = Time.SYSTEM;
            if (log.isInfoEnabled()) {
                log.info("Kafka Connect distributed worker initializing ...");
            }
            long initStart = time.hiResClockMs();
            WorkerInfo initInfo = new WorkerInfo();
            initInfo.logAll();
            Map<String, String> workerProps = this.properties.getWorker();

            if (log.isInfoEnabled()) {
                log.info("Scanning for plugin classes. This might take a moment ...");
            }
            Plugins plugins = new Plugins(workerProps);
            plugins.compareAndSwapWithDelegatingLoader();
            DistributedConfig config = new DistributedConfig(workerProps);
            String kafkaClusterId = ConnectUtils.lookupKafkaClusterId(config);
            if (log.isDebugEnabled()) {
                log.debug("Kafka cluster ID: {}", kafkaClusterId);
            }

            String workerId = UUID.randomUUID().toString();

            KafkaOffsetBackingStore offsetBackingStore = new KafkaOffsetBackingStore();
            offsetBackingStore.configure(config);
            Worker worker = new Worker(workerId, time, plugins, config, offsetBackingStore);
            WorkerConfigTransformer configTransformer = worker.configTransformer();
            Converter internalValueConverter = worker.getInternalValueConverter();
            StatusBackingStore statusBackingStore = new KafkaStatusBackingStore(time, internalValueConverter);
            statusBackingStore.configure(config);
            ConfigBackingStore configBackingStore = new KafkaConfigBackingStore(internalValueConverter, config, configTransformer);
            Herder herder = new DistributedHerder(config, time, worker, kafkaClusterId, statusBackingStore, configBackingStore, "");
            if (log.isInfoEnabled()) {
                log.info("Kafka Connect distributed worker initialization took {}ms", time.hiResClockMs() - initStart);
            }
            return herder;
        }

        /**
         * Create a herder standalone
         * @return
         */
        private Herder createStandaloneHerder(){
            if (log.isInfoEnabled()) {
                log.info("Kafka Connect standalone worker initializing ...");
            }
            Time time = Time.SYSTEM;
            long initStart = time.hiResClockMs();
            WorkerInfo initInfo = new WorkerInfo();
            initInfo.logAll();
            Map<String, String> workerProps = this.properties.getWorker();
            if (log.isInfoEnabled()) {
                log.info("Scanning for plugin classes. This might take a moment ...");
            }
            Plugins plugins = new Plugins(workerProps);
            plugins.compareAndSwapWithDelegatingLoader();
            StandaloneConfig config = new StandaloneConfig(workerProps);

            String kafkaClusterId = ConnectUtils.lookupKafkaClusterId(config);
            if (log.isDebugEnabled()) {
                log.debug("Kafka cluster ID: {}", kafkaClusterId);
            }

            String workerId = UUID.randomUUID().toString();
            Worker worker = new Worker(workerId, time, plugins, config, new FileOffsetBackingStore());
            Herder herder = new StandaloneHerder(worker, kafkaClusterId);
            if (log.isInfoEnabled()) {
                log.info("Kafka Connect standalone worker initialization took {}ms", time.hiResClockMs() - initStart);
            }
            return herder;
        }

    }

    @Configuration
    @ConditionalOnMissingBean(KafkaConnectTemplate.class)
    @ConditionalOnProperty(prefix = "spring.kafka-connect.cluster", name = "embedded", havingValue = "false")
    public static class RestKafkaConnectAutoConfiguration {

        private final KafkaConnectProperties properties;

        public RestKafkaConnectAutoConfiguration(final KafkaConnectProperties properties){
            Objects.requireNonNull(properties.getCluster().getRestApi().getUrl(), "Kafka connect rest url must not be null !!!");
            this.properties = properties;
            this.properties.afterPropertiesSet();
        }


        /**
         * Create kafka connect template
         * @return
         */
        @Bean
        public KafkaConnectRestTemplate kafkaConnectRestTemplate(){
            return new KafkaConnectRestTemplate(createRestTemplate());
        }

        /**
         * Create rest template
         * @return
         */
        private RestTemplate createRestTemplate(){
            RestTemplateBuilder builder = new RestTemplateBuilder();
            builder = builder.rootUri(properties.getCluster().getRestApi().getUrl());
            return builder.build();
        }
    }
}
