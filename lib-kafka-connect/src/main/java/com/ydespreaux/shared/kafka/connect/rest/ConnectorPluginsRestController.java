package com.ydespreaux.shared.kafka.connect.rest;

import com.ydespreaux.shared.kafka.connect.support.KafkaConnectTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.kafka.connect.runtime.rest.entities.ConfigInfos;
import org.apache.kafka.connect.runtime.rest.entities.ConnectorPluginInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.BadRequestException;
import java.util.List;
import java.util.Map;

import static com.ydespreaux.shared.autoconfigure.kafka.connect.KafkaConnectDataAutoConfiguration.TAG_CONNECTORS;

/**
 * Rest api for plugins
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping(path = "${spring.kafka-connect.cluster.rest-api.context-path:/api/kconnect}/connector-plugins", produces = {MediaType.APPLICATION_JSON_VALUE})
@Api(tags = TAG_CONNECTORS)
public class ConnectorPluginsRestController {

    private static final String ALIAS_SUFFIX = "Connector";

    private final KafkaConnectTemplate template;

    public ConnectorPluginsRestController(final KafkaConnectTemplate template) {
        this.template = template;
    }

    /**
     * Validate the provided configuration values against the configuration definition.
     * This API performs per config validation, returns suggested values and error messages during validation.
     *
     * @param connType connector type
     * @param connectorConfig connector configuration
     * @return
     * @throws Throwable
     */
    @PutMapping(path = "/{connectorType}/config/validate")
    @ResponseStatus(code = HttpStatus.OK)
    @ApiOperation(value = "${swagger.connector-plugins-rest-controller.validateConfigs.value}", notes = "${swagger.connector-plugins-rest-controller.validateConfigs.notes}")
    public ConfigInfos validateConfigs(
            @ApiParam(value = "${swagger.connector-plugins-rest-controller.validateConfigs.param.connectorType}", required = true) @PathVariable("connectorType") String connType,
            @ApiParam(value = "${swagger.connector-plugins-rest-controller.validateConfigs.param.config}", required = true) @RequestBody Map<String, String> connectorConfig) throws Throwable {
        String includedConnType = connectorConfig.get("connector.class");
        if (includedConnType != null && !this.normalizedPluginName(includedConnType).endsWith(this.normalizedPluginName(connType))) {
            throw new BadRequestException("Included connector type " + includedConnType + " does not match request type " + connType);
        } else {
            return this.template.validateConnectorConfigs(connType, connectorConfig);
        }
    }

    /**
     * Return a list of connector plugins installed in the Kafka Connect cluster.
     * Note that the API only checks for connectors on the worker that handles the request, which means it is possible to see inconsistent results,
     * especially during a rolling upgrade if you add new connector jars.
     * @return
     */
    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    @ApiOperation(value = "${swagger.connector-plugins-rest-controller.listConnectorPlugins.value}", notes = "${swagger.connector-plugins-rest-controller.listConnectorPlugins.notes}")
    public List<ConnectorPluginInfo> listConnectorPlugins() {
        return this.template.getConnectorPlugins();
    }

    /**
     *
     * @param pluginName
     * @return
     */
    private String normalizedPluginName(String pluginName) {
        return pluginName.endsWith(ALIAS_SUFFIX) && pluginName.length() > ALIAS_SUFFIX.length() ? pluginName.substring(0, pluginName.length() - ALIAS_SUFFIX.length()) : pluginName;
    }
}
