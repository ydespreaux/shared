package com.ydespreaux.shared.autoconfigure.kafka.connect;

import com.ydespreaux.shared.kafka.connect.runtime.Connect;
import com.ydespreaux.shared.kafka.connect.runtime.ConnectEmbedded;
import com.ydespreaux.shared.kafka.connect.support.KafkaConnectTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.runtime.Herder;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 *
 * @since 1.0.0
 */
@Slf4j
@Configuration
@AutoConfigureAfter({KafkaConnectAutoConfiguration.class})
public class KafkaConnectDataAutoConfiguration {

    /**
     * Tag for api rest
     */
    public static final String TAG_CONNECTORS = "KafkaConnectRestAPI";


    /**
     * @since 1.0.0
     */
    @Configuration
    @ConditionalOnProperty(prefix = "spring.kafka-connect.cluster", name = "embedded", havingValue = "true", matchIfMissing = true)
    public static class EmbeddedKafkaConnectDataAutoConfiguration {

        /**
         * Create a embedded cluster
         * @param template
         * @param properties
         * @param herder
         * @return
         */
        @Bean
        ConnectEmbedded connectEmbedded(KafkaConnectTemplate template, KafkaConnectProperties properties, Herder herder){
            ConnectEmbedded connect = new ConnectEmbedded(template, herder);
            connect.start(properties);
            return connect;
        }

        /**
         * Active cors
         * @return
         */
        @Bean
        CorsFilter corsFilter() {
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowCredentials(false);
            config.addAllowedOrigin("*");
            config.addAllowedHeader("*");
            config.addAllowedMethod("*");
            source.registerCorsConfiguration("/**", config);
            return new CorsFilter(source);
        }
    }

    /**
     * @since 1.0.0
     */
    @Configuration
    @ConditionalOnProperty(prefix = "spring.kafka-connect.cluster", name = "embedded", havingValue = "false")
    public static class RestKafkaConnectDataAutoConfiguration {
        @Bean
        public Connect connectRest(KafkaConnectTemplate template, KafkaConnectProperties properties) {
            Connect connect = new Connect(template);
            connect.start(properties);
            return connect;
        }
    }

}
