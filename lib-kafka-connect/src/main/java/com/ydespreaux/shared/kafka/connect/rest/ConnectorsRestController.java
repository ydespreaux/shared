package com.ydespreaux.shared.kafka.connect.rest;

import com.ydespreaux.shared.kafka.connect.support.KafkaConnectTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.kafka.connect.errors.NotFoundException;
import org.apache.kafka.connect.runtime.rest.entities.ConnectorInfo;
import org.apache.kafka.connect.runtime.rest.entities.ConnectorStateInfo;
import org.apache.kafka.connect.runtime.rest.entities.CreateConnectorRequest;
import org.apache.kafka.connect.runtime.rest.entities.TaskInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.ydespreaux.shared.autoconfigure.kafka.connect.KafkaConnectDataAutoConfiguration.TAG_CONNECTORS;
import static java.lang.String.format;

/**
 * Rest api for connectors
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping(path = "${spring.kafka-connect.cluster.rest-api.context-path:/api/kconnect}/connectors")
@Api(tags = TAG_CONNECTORS)
public class ConnectorsRestController {

    private final KafkaConnectTemplate template;

    /**
     *
     * @param template
     */
    public ConnectorsRestController(final KafkaConnectTemplate template) {
        this.template = template;
    }

    /**
     * Get a list of active connectors
     * @return
     * @throws Throwable
     */
    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    @ApiOperation(value = "${swagger.connectors-rest-controller.listConnectors.value}", notes = "${swagger.connectors-rest-controller.listConnectors.notes}")
    public Collection<String> listConnectors() throws Throwable {
        return this.template.getConnectors();
    }

    /**
     * Create a new connector, returning the current connector info if successful. Return 409 (Conflict) if rebalance is in process.
     *
     * @param createRequest
     * @return
     * @throws Throwable
     */
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(code = HttpStatus.CREATED)
    @ApiOperation(value = "${swagger.connectors-rest-controller.createConnector.value}", notes = "${swagger.connectors-rest-controller.createConnector.notes}")
    public Response createConnector(
            @ApiParam(value = "${swagger.connectors-rest-controller.createConnector.param.config}", required = true) @RequestBody CreateConnectorRequest createRequest) throws Throwable {
        String name = createRequest.name();
        if (name.contains("/")) {
            throw new BadRequestException("connector name should not contain '/'");
        } else {
            Map<String, String> configs = createRequest.config();
            if (!configs.containsKey("name")) {
                configs.put("name", name);
            }
            ConnectorInfo createdInfo = this.template.createConnector(name, configs);
            URI location = UriBuilder.fromUri("/kafka-connect/connectors").path(name).build();
            return Response.created(location).entity(createdInfo).build();
        }
    }

    /**
     * Get information about the connector.
     *
     * @param connector connector name
     * @return
     * @throws Throwable
     */
    @GetMapping(path = "/{connector}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(code = HttpStatus.OK)
    @ApiOperation(value = "${swagger.connectors-rest-controller.getConnector.value}", notes = "${swagger.connectors-rest-controller.getConnector.notes}")
    public ConnectorInfo getConnector(
            @ApiParam(value = "${swagger.connectors-rest-controller.getConnector.param.connector}", required = true) @PathVariable("connector") String connector) throws Throwable {
        return this.template.getConnector(connector);
    }

    /**
     * Get the configuration for the connector.
     *
     * @param connector connector name
     * @return
     * @throws Throwable
     */
    @GetMapping(path = "/{connector}/config", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(code = HttpStatus.OK)
    @ApiOperation(value = "${swagger.connectors-rest-controller.getConnectorConfig.value}", notes = "${swagger.connectors-rest-controller.getConnectorConfig.notes}")
    public Map<String, String> getConnectorConfig(
            @ApiParam(value = "${swagger.connectors-rest-controller.getConnectorConfig.param.connector}", required = true) @PathVariable("connector") String connector) throws Throwable {
        return this.template.getConnectorConfig(connector);
    }

    /**
     * Get current status of the connector, including whether it is running, failed or paused, which worker it is assigned to,
     * error information if it has failed, and the state of all its tasks.
     *
     * @param connector connector name
     * @return
     * @throws Throwable
     */
    @GetMapping(path = "/{connector}/status", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(code = HttpStatus.OK)
    @ApiOperation(value = "${swagger.connectors-rest-controller.getConnectorStatus.value}", notes = "${swagger.connectors-rest-controller.getConnectorStatus.notes}")
    public ConnectorStateInfo getConnectorStatus(
            @ApiParam(value = "${swagger.connectors-rest-controller.getConnectorStatus.param.connector}", required = true) @PathVariable("connector") String connector) throws Throwable {
        ConnectorStateInfo info = this.template.getConnectorStatus(connector);
        if (info == null) {
            throw new NotFoundException(format("Connector %s not found", connector));
        }
        return info;
    }

    /**
     * Create a new connector using the given configuration, or update the configuration for an existing connector.
     * Returns information about the connector after the change has been made. Return 409 (Conflict) if rebalance is in process.
     *
     * @param connector connector name
     * @param connectorConfig connector configuration
     * @return
     * @throws Throwable
     */
    @PutMapping(path = "/{connector}/config", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(code = HttpStatus.OK)
    @ApiOperation(value = "${swagger.connectors-rest-controller.putConnectorConfig.value}", notes = "${swagger.connectors-rest-controller.putConnectorConfig.notes}")
    public Response putConnectorConfig(
            @ApiParam(value = "${swagger.connectors-rest-controller.putConnectorConfig.param.connector}", required = true) @PathVariable("connector") String connector,
            @ApiParam(value = "${swagger.connectors-rest-controller.putConnectorConfig.param.config}", required = true) @RequestBody Map<String, String> connectorConfig) throws Throwable {
        String includedName = connectorConfig.get("name");
        if (includedName != null) {
            if (!includedName.equals(connector)) {
                throw new BadRequestException("Connector name configuration (" + includedName + ") doesn't match connector name in the URL (" + connector + ")");
            }
        } else {
            connectorConfig.put("name", connector);
        }
        ConnectorInfo createdInfo = this.template.updateConnector(connector, connectorConfig);
        return Response.ok().entity(createdInfo).build();
    }

    /**
     * Restart the connector and its tasks. Return 409 (Conflict) if rebalance is in process.
     *
     * @param connector connector name
     * @throws Throwable
     */
    @PostMapping(path = "/{connector}/restart")
    @ResponseStatus(code = HttpStatus.OK)
    @ApiOperation(value = "${swagger.connectors-rest-controller.restartConnector.value}", notes = "${swagger.connectors-rest-controller.restartConnector.notes}")
    public void restartConnector(
            @ApiParam(value = "${swagger.connectors-rest-controller.restartConnector.param.connector}", required = true) @PathVariable("connector") String connector) throws Throwable {
        this.template.restartConnector(connector);
    }

    /**
     * Pause the connector and its tasks, which stops message processing until the connector is resumed.
     * This call asynchronous and the tasks will not transition to PAUSED state at the same time.
     *
     * @param connector connector name
     * @return
     */
    @PutMapping(path = "/{connector}/pause")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    @ApiOperation(value = "${swagger.connectors-rest-controller.pauseConnector.value}", notes = "${swagger.connectors-rest-controller.pauseConnector.notes}")
    public Response pauseConnector(
            @ApiParam(value = "${swagger.connectors-rest-controller.pauseConnector.param.connector}", required = true) @PathVariable("connector") String connector) {
        this.template.pauseConnector(connector);
        return Response.accepted().build();
    }

    /**
     * Resume a paused connector or do nothing if the connector is not paused.
     * This call asynchronous and the tasks will not transition to RUNNING state at the same time.
     * @param connector connector name
     * @return
     */
    @PutMapping(path = "/{connector}/resume")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    @ApiOperation(value = "${swagger.connectors-rest-controller.resumeConnector.value}", notes = "${swagger.connectors-rest-controller.resumeConnector.notes}")
    public Response resumeConnector(
            @ApiParam(value = "${swagger.connectors-rest-controller.resumeConnector.param.connector}", required = true) @PathVariable("connector") String connector) {
        this.template.resumeConnector(connector);
        return Response.accepted().build();
    }

    /**
     * Get a list of tasks currently running for the connector.
     * @param connector connector name
     * @return
     * @throws Throwable
     */
    @GetMapping(path = "/{connector}/tasks", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(code = HttpStatus.OK)
    @ApiOperation(value = "${swagger.connectors-rest-controller.getTaskConfigs.value}", notes = "${swagger.connectors-rest-controller.getTaskConfigs.notes}")
    public List<TaskInfo> getTaskConfigs(
            @ApiParam(value = "${swagger.connectors-rest-controller.getTaskConfigs.param.connector}", required = true) @PathVariable("connector") String connector) throws Throwable {
        return this.template.getTaskConfigs(connector);
    }

    /**
     * Get a list of tasks currently running for the connector.
     * @param connector connector name
     * @param taskConfigs task configuration
     * @throws Throwable
     */
    @PostMapping(path = "/{connector}/tasks", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(code = HttpStatus.OK)
    @ApiOperation(value = "${swagger.connectors-rest-controller.putTaskConfigs.value}", notes = "${swagger.connectors-rest-controller.putTaskConfigs.notes}")
    public void putTaskConfigs(
            @ApiParam(value = "${swagger.connectors-rest-controller.putTaskConfigs.param.connector}", required = true) @PathVariable("connector") String connector,
            @ApiParam(value = "${swagger.connectors-rest-controller.putTaskConfigs.param.config}", required = true) @RequestBody List<Map<String, String>> taskConfigs) throws Throwable {
        this.template.updateTaskConfigs(connector, taskConfigs);
    }

    /**
     * Get a task’s status.
     * @param connector connector name
     * @param task task identifier
     * @return
     * @throws Throwable
     */
    @GetMapping(path = "/{connector}/tasks/{task}/status", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(code = HttpStatus.OK)
    @ApiOperation(value = "${swagger.connectors-rest-controller.getTaskStatus.value}", notes = "${swagger.connectors-rest-controller.getTaskStatus.notes}")
    public ConnectorStateInfo.TaskState getTaskStatus(
            @ApiParam(value = "${swagger.connectors-rest-controller.getTaskStatus.param.connector}", required = true) @PathVariable("connector") String connector,
            @ApiParam(value = "${swagger.connectors-rest-controller.getTaskStatus.param.task}", required = true) @PathVariable("task") Integer task) throws Throwable {
        return this.template.getTaskStatus(connector, task);
    }

    /**
     * Restart an individual task.
     * @param connector connector name
     * @param task task identifier
     * @throws Throwable
     */
    @PostMapping(path = "/{connector}/tasks/{task}/restart")
    @ResponseStatus(code = HttpStatus.OK)
    @ApiOperation(value = "${swagger.connectors-rest-controller.restartTask.value}", notes = "${swagger.connectors-rest-controller.restartTask.notes}")
    public void restartTask(
            @ApiParam(value = "${swagger.connectors-rest-controller.restartTask.param.connector}", required = true) @PathVariable("connector") String connector,
            @ApiParam(value = "${swagger.connectors-rest-controller.restartTask.param.task}", required = true) @PathVariable("task") Integer task) throws Throwable {
        this.template.restartTask(connector, task);
    }

    /**
     * Delete a connector, halting all tasks and deleting its configuration. Return 409 (Conflict) if rebalance is in process.
     * @param connector connector name
     * @throws Throwable
     */
    @DeleteMapping(path = "/{connector}")
    @ResponseStatus(code = HttpStatus.OK)
    @ApiOperation(value = "${swagger.connectors-rest-controller.deleteConnector.value}", notes = "${swagger.connectors-rest-controller.deleteConnector.notes}")
    public void deleteConnector(
            @ApiParam(value = "${swagger.connectors-rest-controller.deleteConnector.param.connector}", required = true) @PathVariable("connector") String connector) throws Throwable {
        this.template.deleteConnector(connector);
    }

}
