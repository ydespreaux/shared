package com.ydespreaux.shared.kafka.connect.dto;

import org.apache.kafka.connect.runtime.rest.entities.ServerInfo;

/**
 * Cluster information DTO
 *
 * @since 1.0.0
 */
public class ServerInfoDTO extends ServerInfo {

    public ServerInfoDTO() {
        this(null);
    }

    public ServerInfoDTO(String kafkaClusterId) {
        super(kafkaClusterId);
    }
}
