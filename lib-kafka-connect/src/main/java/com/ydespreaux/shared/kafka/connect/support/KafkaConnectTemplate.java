package com.ydespreaux.shared.kafka.connect.support;

import org.apache.kafka.connect.runtime.rest.entities.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Kafka connect template interface
 *
 * @since 1.0.0
 */
public interface KafkaConnectTemplate {

    /**
     * Get informations of server
     * @return
     */
    ServerInfo getServerInfo();

    /**
     * Get all connectors name
     * @return
     */
    Collection<String> getConnectors();

    /**
     * Create a connector
     * @param name connector name
     * @param configs connector configuration
     * @return
     */
    ConnectorInfo createConnector(String name, Map<String, String> configs);

    /**
     * Update the configuration of a connector
     * @param connector connector name
     * @param connectorConfig connecotr configuration
     * @return
     */
    ConnectorInfo updateConnector(String connector, Map<String, String> connectorConfig);

    /**
     * Get informations of a connector
     * @param connector connector name
     * @return
     */
    ConnectorInfo getConnector(String connector);

    /**
     * Get the configuration of a connector
     * @param connectorName connector name
     * @return
     */
    Map<String, String> getConnectorConfig(String connectorName);

    /**
     * Get the status of a connector
     * @param connectorName connector name
     * @return
     */
    ConnectorStateInfo getConnectorStatus(String connectorName);

    /**
     * Restart a connector
     * @param connector connector name
     */
    void restartConnector(String connector);

    /**
     * Pause a connector
     * @param connector connector name
     */
    void pauseConnector(String connector);

    /**
     * Resume a connector
     * @param connector conenctor name
     */
    void resumeConnector(String connector);

    /**
     * Get all tasks configuration for a connector
     * @param connector connector name
     * @return
     */
    List<TaskInfo> getTaskConfigs(String connector);

    /**
     * Update all tasks configuration for a connector
     * @param connector connector name
     * @param taskConfigs tasks configuration
     */
    void updateTaskConfigs(String connector, List<Map<String, String>> taskConfigs);

    /**
     * Get task status
     * @param connector conenctor name
     * @param task task identifier
     * @return
     */
    ConnectorStateInfo.TaskState getTaskStatus(String connector, Integer task);

    /**
     * Restart a task
     * @param connector connector name
     * @param task task identifier
     */
    void restartTask(String connector, Integer task);

    /**
     * Check connector exists
     * @param connector connector name
     * @return
     */
    Boolean connectorExists(String connector);

    /**
     * Delete a connector
     * @param connector connector name
     * @return
     */
    boolean deleteConnector(String connector);

    /**
     * Get all plugins
     * @return
     */
    List<ConnectorPluginInfo> getConnectorPlugins();

    /**
     * Validate the connector configuration
     * @param connectorType connector type
     * @param connectorConfig connector configuration
     * @return
     */
    ConfigInfos validateConnectorConfigs(String connectorType, Map<String, String> connectorConfig);
}
