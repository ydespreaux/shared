package com.ydespreaux.shared.kafka.connect.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaConnectApplication {
    /**
     * @param args Application start arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(KafkaConnectApplication.class, args);
    }
}
