package com.ydespreaux.shared.kafka.connect.app;

import com.ydespreaux.shared.kafka.connect.annotations.EnableKafkaConnectSwagger2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableKafkaConnectSwagger2
public class KafkaConnectApplicationWithSwagger {
    /**
     * @param args Application start arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(KafkaConnectApplicationWithSwagger.class, args);
    }
}
