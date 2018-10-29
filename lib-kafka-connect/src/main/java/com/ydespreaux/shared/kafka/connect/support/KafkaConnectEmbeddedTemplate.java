package com.ydespreaux.shared.kafka.connect.support;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.connector.Connector;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.errors.NotFoundException;
import org.apache.kafka.connect.runtime.ConnectorConfig;
import org.apache.kafka.connect.runtime.Herder;
import org.apache.kafka.connect.runtime.distributed.RebalanceNeededException;
import org.apache.kafka.connect.runtime.rest.entities.*;
import org.apache.kafka.connect.runtime.rest.errors.ConnectRestException;
import org.apache.kafka.connect.tools.*;
import org.apache.kafka.connect.util.ConnectorTaskId;
import org.apache.kafka.connect.util.FutureCallback;

import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * KafkaConnectTemplate implementation for a embedded cluster.
 *
 * @since 1.0.0
 */
@Slf4j
public class KafkaConnectEmbeddedTemplate implements KafkaConnectTemplate {

    private static final long REQUEST_TIMEOUT_MS = 90000L;

    @Getter
    private final Herder herder;

    private final List<ConnectorPluginInfo> connectorPlugins;
    private static final List<Class<? extends Connector>> CONNECTOR_EXCLUDES = Arrays.asList(VerifiableSourceConnector.class, VerifiableSinkConnector.class, MockConnector.class, MockSourceConnector.class, MockSinkConnector.class, SchemaSourceConnector.class);

    public KafkaConnectEmbeddedTemplate(final Herder herder) {
        this.herder = herder;
        this.connectorPlugins = new ArrayList();
    }

    /**
     *
     * @return
     */
    @Override
    public ServerInfo getServerInfo(){
        return new ServerInfo(this.herder.kafkaClusterId());
    }

    /**
     * Get all connectors name
     * @return
     */
    @Override
    public Collection<String> getConnectors() {
        FutureCallback<Collection<String>> cb = new FutureCallback();
        this.herder.connectors(cb);
        return this.completeRequest(cb);
    }

    /**
     * Create a connector
     * @param connector connector name
     * @param connectorConfig connector configuration
     * @return
     */
    @Override
    public ConnectorInfo createConnector(String connector, Map<String, String> connectorConfig) {
        if (!connectorConfig.containsKey(ConnectorConfig.NAME_CONFIG)) {
            connectorConfig.put(ConnectorConfig.NAME_CONFIG, connector);
        }
        FutureCallback<Herder.Created<ConnectorInfo>> cb = new FutureCallback();
        this.herder.putConnectorConfig(connector, connectorConfig, false, cb);
        return this.completeRequest(cb).result();
    }

    /**
     * Update the configuration of a connector
     * @param connector connector name
     * @param connectorConfig connecotr configuration
     * @return
     */
    @Override
    public ConnectorInfo updateConnector(String connector, Map<String, String> connectorConfig) {
        if (!connectorConfig.containsKey(ConnectorConfig.NAME_CONFIG)) {
            connectorConfig.put(ConnectorConfig.NAME_CONFIG, connector);
        }
        FutureCallback<Herder.Created<ConnectorInfo>> cb = new FutureCallback();
        this.herder.putConnectorConfig(connector, connectorConfig, true, cb);
        return this.completeRequest(cb).result();
    }

    /**
     * Get informations of a connector
     * @param connector connector name
     * @return
     */
    @Override
    public ConnectorInfo getConnector(String connector) {
        FutureCallback<ConnectorInfo> cb = new FutureCallback();
        this.herder.connectorInfo(connector, cb);
        return this.completeRequest(cb);
    }

    /**
     * Get the configuration of a connector
     * @param connectorName connector name
     * @return
     */
    @Override
    public Map<String, String> getConnectorConfig(String connectorName) {
        FutureCallback<Map<String, String>> cb = new FutureCallback();
        this.herder.connectorConfig(connectorName, cb);
        return this.completeRequest(cb);
    }

    /**
     * Get the status of a connector
     * @param connectorName connector name
     * @return
     */
    @Override
    public ConnectorStateInfo getConnectorStatus(String connectorName) {
        try {
            return this.herder.connectorStatus(connectorName);
        }catch(NotFoundException e) {
            return null;
        }
    }

    /**
     * Restart a connector
     * @param connector connector name
     */
    @Override
    public void restartConnector(String connector) {
        FutureCallback<Void> cb = new FutureCallback();
        this.herder.restartConnector(connector, cb);
        this.completeRequest(cb);
    }

    /**
     * Pause a connector
     * @param connector connector name
     */
    @Override
    public void pauseConnector(String connector) {
        this.herder.pauseConnector(connector);
    }

    /**
     * Resume a connector
     * @param connector conenctor name
     */
    @Override
    public void resumeConnector(String connector) {
        this.herder.resumeConnector(connector);
    }

    /**
     * Get all tasks configuration for a connector
     * @param connector connector name
     * @return
     */
    @Override
    public List<TaskInfo> getTaskConfigs(String connector) {
        FutureCallback<List<TaskInfo>> cb = new FutureCallback();
        this.herder.taskConfigs(connector, cb);
        return this.completeRequest(cb);
    }

    /**
     * Update all tasks configuration for a connector
     * @param connector connector name
     * @param taskConfigs tasks configuration
     */
    @Override
    public void updateTaskConfigs(String connector, List<Map<String, String>> taskConfigs) {
        FutureCallback<Void> cb = new FutureCallback();
        this.herder.putTaskConfigs(connector, taskConfigs, cb);
        this.completeRequest(cb);
    }


    /**
     * Get task status
     * @param connector conenctor name
     * @param task task identifier
     * @return
     */
    @Override
    public ConnectorStateInfo.TaskState getTaskStatus(String connector, Integer task) {
        return this.herder.taskStatus(new ConnectorTaskId(connector, task));
    }

    /**
     * Restart a task
     * @param connector connector name
     * @param task task identifier
     */
    @Override
    public void restartTask(String connector, Integer task) {
        FutureCallback<Void> cb = new FutureCallback();
        ConnectorTaskId taskId = new ConnectorTaskId(connector, task);
        this.herder.restartTask(taskId, cb);
        this.completeRequest(cb);
    }

    /**
     * Check connector exists
     * @param connector connector name
     * @return
     */
    @Override
    public Boolean connectorExists(String connector) {
        return this.getConnectors().contains(connector);
    }


    /**
     * Delete a connector
     * @param connector connector name
     * @return
     */
    @Override
    public boolean deleteConnector(String connector) {
        try {
            FutureCallback<Herder.Created<ConnectorInfo>> cb = new FutureCallback();
            this.herder.deleteConnectorConfig(connector, cb);
            this.completeRequest(cb);
            return true;
        }catch(NotFoundException error) {
            return false;
        }
    }

    /**
     * Get all plugins
     * @return
     */
    @Override
    public synchronized List<ConnectorPluginInfo> getConnectorPlugins() {
        if (this.connectorPlugins.isEmpty()) {
            this.herder.plugins().connectors().forEach(plugin -> {
                if (!CONNECTOR_EXCLUDES.contains(plugin.pluginClass())) {
                    this.connectorPlugins.add(new ConnectorPluginInfo(plugin));
                }
            });
        }
        return Collections.unmodifiableList(this.connectorPlugins);
    }

    /**
     * Validate the connector configuration
     *
     * @param connectorType   connector type
     * @param connectorConfig connector configuration
     * @return
     */
    @Override
    public ConfigInfos validateConnectorConfigs(String connectorType, Map<String, String> connectorConfig) {
        return this.herder.validateConnectorConfig(connectorConfig);
    }


    /**
     *
     * @param cb
     * @param <T>
     * @return
     * @throws Throwable
     */
    private <T> T completeRequest(FutureCallback<T> cb) {
        try {
            return cb.get(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (ExecutionException error) {
            Throwable cause = error.getCause();
            if (cause instanceof RebalanceNeededException) {
                throw new ConnectRestException(Response.Status.CONFLICT.getStatusCode(), "Cannot complete request momentarily due to stale configuration (typically caused by a concurrent config change)");
            }else if (cause instanceof ConnectException) {
                throw (ConnectException)cause;
            } else {
                throw new ConnectRestException(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), cause.getMessage(), cause);
            }
        } catch (TimeoutException error) {
            throw new ConnectRestException(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Request timed out");
        } catch (InterruptedException error) {
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            throw new ConnectRestException(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Request interrupted");
        }
    }

}
