package com.ydespreaux.shared.kafka.connect.rest;

import com.ydespreaux.shared.kafka.connect.support.KafkaConnectTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.runtime.rest.entities.ServerInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static com.ydespreaux.shared.autoconfigure.kafka.connect.KafkaConnectDataAutoConfiguration.TAG_CONNECTORS;

/**
 * Rest api for root controller
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping(path = "${spring.kafka-connect.cluster.rest-api.context-path:/api/kconnect}", produces = {MediaType.APPLICATION_JSON_VALUE})
@Api(tags = TAG_CONNECTORS)
@Slf4j
public class RootRestController {


    private final KafkaConnectTemplate template;

    /**
     * Default constructor
     *
     * @param template
     */
    public RootRestController(final KafkaConnectTemplate template) {
        this.template = template;
    }

    /**
     * Get the cluster information
     * @return
     */
    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    @ApiOperation(value = "${swagger.root-rest-controller.serverInfo.value}", notes = "${swagger.root-rest-controller.serverInfo.notes}")
    public ServerInfo serverInfo() {
        return this.template.getServerInfo();
    }

}
