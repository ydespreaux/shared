package com.ydespreaux.shared.autoconfigure.kafka.actuator;

import com.ydespreaux.shared.kafka.clients.ConfluentClientConfig;
import org.apache.kafka.clients.KafkaClient;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.HealthIndicatorProperties;
import org.springframework.boot.actuate.health.*;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@AutoConfigureBefore({ EndpointAutoConfiguration.class })
@EnableConfigurationProperties({ HealthIndicatorProperties.class })
public class KafkaHealthIndicatorAutoConfiguration {


    @Configuration
    @ConditionalOnClass({KafkaClient.class})
    @ConditionalOnEnabledHealthIndicator("kafka")
    @EnableConfigurationProperties(KafkaProperties.class)
    public static class KafkaHealthIndicatorConfiguration {

        private final KafkaProperties config;

        public KafkaHealthIndicatorConfiguration(KafkaProperties config) {
            this.config = config;
        }

        @Bean
        public HealthIndicator kafka(){
            OrderedHealthAggregator healthAggregator = new OrderedHealthAggregator();
            Map<String, HealthIndicator> indicators = new LinkedHashMap<>();
            indicators.put("Cluster Kafka", new KafkaClientHealthIndicator(config));
            if (config.getProperties().containsKey(ConfluentClientConfig.SCHEMA_REGISTRY_CONFIG)) {
                indicators.put("Schema Registry", new RemoteConnectionHealthIndicator(config.getProperties().get(ConfluentClientConfig.SCHEMA_REGISTRY_CONFIG)));
            }
            return new CompositeHealthIndicator(healthAggregator, indicators);
        }
    }

    /**
     *
     */
    public static class KafkaClientHealthIndicator extends AbstractHealthIndicator {

        private final KafkaProperties properties;
        private AdminClient adminClient;

        public KafkaClientHealthIndicator(KafkaProperties properties) {
            this.properties = properties;
        }

        @Override
        protected void doHealthCheck(Health.Builder builder) throws Exception {
            try {
                if (this.adminClient == null) {
                    this.adminClient = AdminClient.create(this.properties.buildAdminProperties());
                }
                DescribeClusterResult response = adminClient.describeCluster();
                KafkaFuture<String> clusterId = response.clusterId();
                Collection<Node> nodes = response.nodes().get();
                if (clusterId != null && !nodes.isEmpty()) {
                    builder.withDetail("Cluster Id", clusterId.get());
                    builder.withDetail("Nodes available", nodes.size());
                    builder.up();
                } else {
                    builder.down();
                }
            } catch (Exception e) {
                builder.down();
            }
        }
    }
}
