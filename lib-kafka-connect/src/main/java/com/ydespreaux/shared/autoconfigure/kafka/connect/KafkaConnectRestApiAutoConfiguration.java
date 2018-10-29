package com.ydespreaux.shared.autoconfigure.kafka.connect;

import com.ydespreaux.shared.kafka.connect.rest.ConnectorPluginsRestController;
import com.ydespreaux.shared.kafka.connect.rest.ConnectorsRestController;
import com.ydespreaux.shared.kafka.connect.rest.RootRestController;
import com.ydespreaux.shared.kafka.connect.support.KafkaConnectEmbeddedTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter({KafkaConnectAutoConfiguration.class})
@ConditionalOnBean({KafkaConnectEmbeddedTemplate.class})
@ConditionalOnProperty(prefix = "spring.kafka-connect.cluster", name = "rest-api.enabled", matchIfMissing = true)
public class KafkaConnectRestApiAutoConfiguration {

    /**
     * Create root rest controller
     * @param template
     * @return
     */
    @Bean
    RootRestController rootRestController(final KafkaConnectEmbeddedTemplate template){
        return new RootRestController(template);
    }

    /**
     * Create connectors rest controller
     * @param template
     * @return
     */
    @Bean
    ConnectorsRestController connectorsRestController(final KafkaConnectEmbeddedTemplate template) {
        return new ConnectorsRestController(template);
    }

    /**
     * Create plugins rest controller
     * @param template
     * @return
     */
    @Bean
    ConnectorPluginsRestController connectorPluginsRestController(final KafkaConnectEmbeddedTemplate template){
        return new ConnectorPluginsRestController(template);
    }
}
