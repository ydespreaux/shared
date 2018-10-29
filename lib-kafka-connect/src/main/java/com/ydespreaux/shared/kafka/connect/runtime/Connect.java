package com.ydespreaux.shared.kafka.connect.runtime;

import com.ydespreaux.shared.autoconfigure.kafka.connect.KafkaConnectProperties;
import com.ydespreaux.shared.kafka.connect.support.KafkaConnectTemplate;
import lombok.extern.slf4j.Slf4j;

import static java.lang.String.format;

/**
 *
 * @since 1.0.0
 */
@Slf4j
public class Connect {

    /**
     * Kafka connect template
     */
    private final KafkaConnectTemplate template;

    /**
     * Default constructor
     *
     * @param template
     */
    public Connect(KafkaConnectTemplate template) {
        if (log.isDebugEnabled()) {
            log.debug("Kafka Connect instance created");
        }
        this.template = template;
    }

    /**
     * Start cluster.
     * Create or update the connectors
     * @param properties
     */
    public void start(KafkaConnectProperties properties) {
        createOrUpdateConnectors(properties);
    }

    /**
     * Stop cluster
     */
    public void stop(){
        // Nothing to do
    }

    /**
     * Create or update connectors.
     *
     */
    protected void createOrUpdateConnectors(KafkaConnectProperties properties){
        if (properties == null || properties.getConnectorAction() == KafkaConnectProperties.ConnectorAction.NONE) {
            return;
        }
        properties.getAllConnectors().forEach(connectorConfig -> {
            try {
                if (properties.getConnectorAction() == KafkaConnectProperties.ConnectorAction.CREATE_OR_UPDATE) {
                    this.template.updateConnector(connectorConfig.getName(), connectorConfig.getConfig());
                    logCreateOrUpdateConnector(connectorConfig.getName());
                }else if (!this.template.connectorExists(connectorConfig.getName())){
                    this.template.createConnector(connectorConfig.getName(), connectorConfig.getConfig());
                    logCreateOrUpdateConnector(connectorConfig.getName());
                }
            }catch(Exception e) {
                log.error(format("Create connector [%s] failed:", connectorConfig.getName()), e);
            }
        });
    }

    /**
     *
     * @param connectorName
     */
    private void logCreateOrUpdateConnector(String connectorName) {
        if (log.isInfoEnabled()) {
            log.info("Connector {} created", connectorName);
        }
    }
}
