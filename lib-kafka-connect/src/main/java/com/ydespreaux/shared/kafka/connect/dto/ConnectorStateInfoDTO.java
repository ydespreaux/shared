package com.ydespreaux.shared.kafka.connect.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.connect.runtime.rest.entities.ConnectorStateInfo;
import org.apache.kafka.connect.runtime.rest.entities.ConnectorType;

import java.util.ArrayList;
import java.util.List;

/**
 * State connector DTO
 *
 * @since 1.0.0
 */
@Getter
@Setter
public class ConnectorStateInfoDTO {

    /**
     * Connector name
     */
    private String name;
    /**
     * Connector state
     */
    private ConnectorStateInfoDTO.ConnectorStateDTO connector;
    /**
     * Connector tasks list
     */
    private List<ConnectorStateInfoDTO.TaskStateDTO> tasks;
    /**
     * Connector type
     */
    private ConnectorType type;


    /**
     *
     * @return
     */
    public ConnectorStateInfo build() {
        List<ConnectorStateInfo.TaskState> listTask = new ArrayList<>();
        if (tasks != null) {
            tasks.forEach(taskDTO -> listTask.add(taskDTO.build()));
        }
        return new ConnectorStateInfo(name, connector.build(), listTask, type);
    }

    @Getter
    @Setter
    public static class TaskStateDTO extends AbstractStateDTO {

        /**
         * task identifier
         */
        @JsonProperty
        private int id;

        public ConnectorStateInfo.TaskState build(){
            return new ConnectorStateInfo.TaskState(id, getState(), getWorkerId(), getTrace());
        }
    }

    /**
     *
     */
    public static class ConnectorStateDTO extends AbstractStateDTO {

        /**
         *
         * @return
         */
        public ConnectorStateInfo.ConnectorState build(){
            return new ConnectorStateInfo.ConnectorState(getState(), getWorkerId(), getTrace());
        }
    }

    /**
     *
     */
    @Getter
    @Setter
    public abstract static class AbstractStateDTO {
        /**
         * Connector state
         */
        @JsonProperty
        private String state;
        /**
         * Error message
         */
        @JsonProperty
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private String trace;
        /**
         * Worker identifier
         */
        @JsonProperty("worker_id")
        private String workerId;
    }
}
