package com.ydespreaux.shared.autoconfigure.kafka.connect.actuator;

import com.ydespreaux.shared.autoconfigure.kafka.connect.KafkaConnectDataAutoConfiguration;
import com.ydespreaux.shared.autoconfigure.kafka.connect.KafkaConnectProperties;
import com.ydespreaux.shared.kafka.connect.KafkaConnectException;
import com.ydespreaux.shared.kafka.connect.support.KafkaConnectTemplate;
import org.apache.kafka.connect.runtime.AbstractStatus;
import org.apache.kafka.connect.runtime.rest.entities.ConnectorStateInfo;
import org.apache.kafka.connect.runtime.rest.entities.ServerInfo;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.springframework.boot.actuate.health.Status.DOWN;
import static org.springframework.boot.actuate.health.Status.UP;

/**
 *
 *  @since 1.0.0
 */
@Configuration
@ConditionalOnClass({Health.class})
@AutoConfigureBefore({ EndpointAutoConfiguration.class })
@AutoConfigureAfter({KafkaConnectDataAutoConfiguration.class})
public class KafkaConnectHealthIndicatorAutoConfiguration {


    @Configuration
    @ConditionalOnEnabledHealthIndicator("kafka-connect")
    @EnableConfigurationProperties(KafkaConnectProperties.class)
    public static class KafkaHealthIndicatorConfiguration {

        /**
         * Kakfa connect properties
         */
        private final KafkaConnectProperties config;
        /**
         * Kafka connect template
         */
        private final KafkaConnectTemplate template;

        public KafkaHealthIndicatorConfiguration(final KafkaConnectTemplate template, KafkaConnectProperties config) {
            this.template = template;
            this.config = config;
        }

        @Bean
        public HealthIndicator kafkaConnectHealthIndicator(){
            return new ConnectorHealthIndicator(template, config);
        }

    }

    /**
     *
     */
    public static class ConnectorHealthIndicator extends AbstractHealthIndicator {

        /**
         * Kafka connect template
         */
        private final KafkaConnectTemplate template;
        /**
         * Kafka connect properties
         */
        private final KafkaConnectProperties properties;

        /**
         * Default constructor
         * @param template
         * @param properties
         */
        public ConnectorHealthIndicator(KafkaConnectTemplate template, KafkaConnectProperties properties) {
            this.template = template;
            this.properties = properties;
        }

        /**
         * Check cluster and connectors state
         * @param builder
         * @throws Exception
         */
        @Override
        protected void doHealthCheck(Health.Builder builder) throws Exception {

            // Check worker
            Status globalStatus = doHealthWorker(builder);
            if (globalStatus == DOWN) {
                builder.down();
                return;
            }
            Stream<KafkaConnectProperties.ConnectorConfiguration> connectors = this.properties.getAllConnectors();
            // Check connectors
            Set<Status> status = new HashSet<>();
            connectors.forEach(connector -> {
                Status connectorStatus = doHealthConnector(connector.getName());
                builder.withDetail(format("Connector : %s", connector.getName()), connectorStatus.getCode());
                status.add(connectorStatus);
            });

            if (status.contains(DOWN)) {
                builder.down();
            }else {
                builder.up();
            }
        }

        /**
         * Check cluster state
         * @param builder
         * @return
         */
        private Status doHealthWorker(Health.Builder builder){
            try {
                ServerInfo info = this.template.getServerInfo();
                builder.withDetail("version", info.version());
                return UP;
            }catch(KafkaConnectException e) {
                builder.withDetail("version", "N/A");
                return DOWN;
            }
        }

        /**
         * Check connector status
         * @param connectorName connector name
         * @return
         */
        private Status doHealthConnector(final String connectorName) {
            try {
                ConnectorStateInfo info = this.template.getConnectorStatus(connectorName);
                if (info == null) {
                    return DOWN;
                }
                return AbstractStatus.State.valueOf(info.connector().state().toUpperCase()) == AbstractStatus.State.RUNNING ? UP : DOWN;
            } catch (Exception e) {
                return DOWN;
            }
        }
    }

}
