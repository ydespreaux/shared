package com.ydespreaux.shared.kafka.connect.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ydespreaux.shared.kafka.connect.KafkaConnectException;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.io.IOException;

/**
 * Class utilities.
 *
 * @since 1.0.0
 */
public final class KafkaConnectUtils {

    private static final ObjectMapper mapper = new ObjectMapper();


    /**
     *
     */
    private KafkaConnectUtils(){
        // Nothing to do
    }

    /**
     * Mapping json to KafkaconnectException exception.
     * @param content json format
     * @return new instance of KafkaconnectException from json content
     */
    public static KafkaConnectException mappingException(String content) {
        try {
            KafkaConnectRestException exception = mapper.readValue(content, KafkaConnectRestException.class);
            return new KafkaConnectException(HttpStatus.valueOf(exception.errorCode), exception.getMessage());
        } catch (IOException e) {
            return new KafkaConnectException(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class KafkaConnectRestException extends RuntimeException{

        /**
         *
         */
        @JsonProperty("error_code")
        private Integer errorCode;

        /**
         *
         */
        private String message;

    }

}
