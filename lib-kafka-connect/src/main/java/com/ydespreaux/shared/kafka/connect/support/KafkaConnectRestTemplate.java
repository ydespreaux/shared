package com.ydespreaux.shared.kafka.connect.support;

import com.ydespreaux.shared.kafka.connect.KafkaConnectException;
import com.ydespreaux.shared.kafka.connect.dto.ConnectorStateInfoDTO;
import com.ydespreaux.shared.kafka.connect.dto.ServerInfoDTO;
import com.ydespreaux.shared.kafka.connect.utils.KafkaConnectUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.runtime.rest.entities.*;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

/**
 * KafkaConnectTemplate implementation for a remote cluster
 *
 * @since 1.0.0
 */
@Slf4j
public class KafkaConnectRestTemplate implements KafkaConnectTemplate{

    private static final String CONNECTORS_PATH = "/connectors";
    private static final String CONNECTOR_PLUGINS_PATH = "/connector-plugins";

    private final RestTemplate restTemplate;

    /**
     *
     * @param restTemplate
     */
    public KafkaConnectRestTemplate(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Get informations of server
     * @return
     */
    @Override
    public ServerInfo getServerInfo(){
        try {
            return this.restTemplate.getForObject("/", ServerInfoDTO.class);
        }catch(Exception e) {
            throw new KafkaConnectException(e);
        }
    }

    /**
     * Get all connectors name
     * @return
     */
    @Override
    public Collection<String> getConnectors(){
        try {
            return this.restTemplate.getForObject(CONNECTORS_PATH, Collection.class);
        }catch(Exception e) {
            throw new KafkaConnectException(e);
        }
    }

    /**
     * Create a connector
     * @param name connector name
     * @param configs connector configuration
     * @return
     */
    @Override
    public ConnectorInfo createConnector(String name, Map<String, String> configs) {
        ConnectorInfo info = exchangeRequest(CONNECTORS_PATH, HttpMethod.POST, ConnectorInfo.class, new CreateConnectorRequest(name, configs));
        if (log.isDebugEnabled()) {
            log.debug("Connector {} created", name);
        }
        return info;
    }

    /**
     * Update the configuration of a connector
     * @param name connector name
     * @param configs connecotr configuration
     * @return
     */
    @Override
    public ConnectorInfo updateConnector(String name, Map<String, String> configs) {
        ConnectorInfo info = exchangeRequest(CONNECTORS_PATH + "/{connectorName}/config", HttpMethod.PUT, ConnectorInfo.class, configs, name);
        if (log.isDebugEnabled()) {
            log.debug("Connector {} updated", name);
        }
        return info;
    }

    /**
     * Get informations of a connector
     * @param connectorName connector name
     * @return
     */
    @Override
    public ConnectorInfo getConnector(String connectorName) {
        try {
            return this.restTemplate.getForObject(CONNECTORS_PATH + "/{connectorName}", ConnectorInfo.class, connectorName);
        }catch(HttpClientErrorException e) {
            throw new KafkaConnectException(e.getStatusCode(), e);
        }catch(Exception e) {
            throw new KafkaConnectException(e);
        }
    }

    /**
     * Get the configuration of a connector
     * @param connectorName connector name
     * @return
     */
    @Override
    public Map<String, String> getConnectorConfig(String connectorName) {
        try {
            return this.restTemplate.getForObject(CONNECTORS_PATH + "/{connectorName}/config", Map.class, connectorName);
        }catch(HttpClientErrorException e) {
            throw new KafkaConnectException(e.getStatusCode(), e);
        }catch(Exception e) {
            throw new KafkaConnectException(e);
        }
    }

    /**
     * Get the status of a connector
     * @param connectorName connector name
     * @return
     */
    @Override
    public ConnectorStateInfo getConnectorStatus(String connectorName) {
        try {
            ConnectorStateInfoDTO infoDTO = this.restTemplate.getForObject(CONNECTORS_PATH + "/{connectorName}/status", ConnectorStateInfoDTO.class, connectorName);
            return infoDTO.build();
        }catch(HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                if (log.isDebugEnabled()) {
                    log.debug("Connector {} not found", connectorName);
                }
                return null;
            }else {
                throw new KafkaConnectException(e);
            }
        }catch(Exception e) {
            throw new KafkaConnectException(e);
        }
    }

    /**
     * Restart a connector
     * @param connectorName connector name
     */
    @Override
    public void restartConnector(String connectorName) {
        exchangeRequest(CONNECTORS_PATH + "/{connector}/restart", HttpMethod.POST, ConnectorInfo.class, null, connectorName);
    }

    /**
     * Pause a connector
     * @param connectorName connector name
     */
    @Override
    public void pauseConnector(String connectorName) {
        exchangeRequest(CONNECTORS_PATH + "/{connector}/pause", HttpMethod.PUT, ConnectorInfo.class, null, connectorName);
    }

    /**
     * Resume a connector
     * @param connectorName conenctor name
     */
    @Override
    public void resumeConnector(String connectorName) {
        exchangeRequest(CONNECTORS_PATH + "/{connector}/resume", HttpMethod.PUT, ConnectorInfo.class, null, connectorName);
    }

    /**
     * Get all tasks configuration for a connector
     * @param connector connector name
     * @return
     */
    @Override
    public List<TaskInfo> getTaskConfigs(String connector) {
        try {
            return this.restTemplate.getForObject(CONNECTORS_PATH + "/{connector}/tasks", List.class, connector);
        }catch(HttpClientErrorException e) {
            throw new KafkaConnectException(e.getStatusCode(), e);
        }catch(Exception e) {
            throw new KafkaConnectException(e);
        }
    }

    /**
     * Update all tasks configuration for a connector
     * @param connector connector name
     * @param taskConfigs tasks configuration
     */
    @Override
    public void updateTaskConfigs(String connector, List<Map<String, String>> taskConfigs) {
        exchangeRequest(CONNECTORS_PATH + "/{connector}/tasks", HttpMethod.POST, Void.class, taskConfigs, connector);
    }

    /**
     * Get task status
     * @param connector conenctor name
     * @param task task identifier
     * @return
     */
    @Override
    public ConnectorStateInfo.TaskState getTaskStatus(String connector, Integer task) {
        try {
            ConnectorStateInfoDTO.TaskStateDTO dto = this.restTemplate.getForObject(CONNECTORS_PATH + "/{connector}/tasks/{task}/status", ConnectorStateInfoDTO.TaskStateDTO.class, connector, task);
            return dto.build();
        }catch(HttpClientErrorException e) {
            throw new KafkaConnectException(e.getStatusCode(), e);
        }catch(Exception e) {
            throw new KafkaConnectException(e);
        }
    }

    /**
     * Restart a task
     * @param connector connector name
     * @param task task identifier
     */
    @Override
    public void restartTask(String connector, Integer task) {
        exchangeRequest(CONNECTORS_PATH + "/{connector}/tasks/{task}/restart", HttpMethod.POST, Void.class, null, connector, task);
    }

    /**
     * Check connector exists
     * @param connectorName connector name
     * @return
     */
    @Override
    public Boolean connectorExists(String connectorName) {
        try {
            Collection<String> response = this.restTemplate.getForObject(CONNECTORS_PATH, Collection.class);
            return response.contains(connectorName);
        }catch(Exception e) {
            throw new KafkaConnectException(e);
        }
    }


    /**
     * Delete a connector
     * @param connectorName connector name
     * @return
     */
    @Override
    public boolean deleteConnector(String connectorName) {
        try {
            this.restTemplate.delete(CONNECTORS_PATH + "/{connectorName}", connectorName);
            if (log.isDebugEnabled()) {
                log.debug("Connector {} successfully deleted", connectorName);
            }
            return true;
        }catch(HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to delete connector {} : connector not found", connectorName);
                }
                return false;
            }else {
                throw new KafkaConnectException(e);
            }
        } catch(Exception e) {
            throw new KafkaConnectException(e);
        }
    }

    /**
     * Get all plugins
     * @return
     */
    @Override
    public List<ConnectorPluginInfo> getConnectorPlugins() {
        try {
            return this.restTemplate.getForObject(CONNECTOR_PLUGINS_PATH + "/", List.class);
        }catch(Exception e) {
            throw new KafkaConnectException(e);
        }
    }

    /**
     * Validate the connector configuration
     * @param connectorType connector type
     * @param connectorConfig connector configuration
     * @return
     */
    @Override
    public ConfigInfos validateConnectorConfigs(String connectorType, Map<String, String> connectorConfig) {
        return exchangeRequest(CONNECTOR_PLUGINS_PATH + "/{connectorType}/config/validate", HttpMethod.PUT, ConfigInfos.class, connectorConfig, connectorType);
    }

    /**
     *
     * @param url
     * @param method
     * @param body
     * @param responseType
     * @param variables
     * @param <T>
     * @param <K>
     * @return
     */
    private <T,K> T exchangeRequest(String url, HttpMethod method, Class<T> responseType, K body, Object... variables) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            HttpEntity<K> entity = new HttpEntity<>(body, headers);
            ResponseEntity<T> response = this.restTemplate.exchange(url, method, entity, responseType, variables);
            return response.getBody();
        }catch(HttpClientErrorException e) {
            if (e.getStatusCode() == BAD_REQUEST || e.getStatusCode() == CONFLICT) {
                throw KafkaConnectUtils.mappingException(e.getResponseBodyAsString());
            }else {
                throw new KafkaConnectException(e);
            }
        }catch(Exception e) {
            throw new KafkaConnectException(e);
        }
    }
}
