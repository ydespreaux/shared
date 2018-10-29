package com.ydespreaux.shared.autoconfigure.kafka.connect;

import lombok.*;
import org.apache.kafka.common.config.SslConfigs;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 *
 * @since 1.0.0
 */
@NoArgsConstructor
@ConfigurationProperties(prefix = "spring.kafka-connect")
@Validated
@Getter
@Setter
public class KafkaConnectProperties {

    private static final String PRODUCER_PREFIX = "producer.";
    private static final String CONSUMER_PREFIX = "consumer.";
    private static final String HTTPS_PREFIX = "https://";

    private static final Pattern ARROWBASE_VALUE_PATTERN = Pattern.compile("@\\{(.+)\\}");

    public enum ConnectorAction { CREATE, CREATE_OR_UPDATE, NONE}

    /**
     * Cluster configuration
     */
    @NotNull
    @Valid
    private final ClusterConfig cluster = new ClusterConfig();

    /**
     * Worker configuration
     */
    private final Map<String, String> worker = new HashMap<>();

    /**
     * Type action to apply of connectors
     */
    @NotNull
    private ConnectorAction connectorAction = ConnectorAction.CREATE;

    /**
     * Connectors configuration
     */
    @Valid
    private final Map<String, ConnectorConfiguration> connectors = new LinkedHashMap<>();


    /**
     * Return all connectors configuration
     * @return
     */
    public Stream<ConnectorConfiguration> getAllConnectors(){
        return connectors.values().stream();
    }

    /**
     * Retur the context path for rest api
     * @return
     */
    public String getContextPath(){
        return this.cluster.getRestApi().getContextPath();
    }

    /**
     *
     */
    public void afterPropertiesSet() {

        // Initialize security and SSL
        if (Boolean.TRUE.equals(this.cluster.getEmbedded())) {
            worker.putAll(cluster.buildProperties(""));
            worker.putAll(cluster.buildProperties(PRODUCER_PREFIX));
            worker.putAll(cluster.buildProperties(CONSUMER_PREFIX));
        }
        // Initialize connectors
        this.connectors.values().forEach(configuration -> {
            Map<String, String> actualConfiguration = configuration.getConfig();
            Map<String, String> formattingConfiguration = new HashMap<>(actualConfiguration.size());
            actualConfiguration.keySet().forEach(key -> {
                String value = actualConfiguration.get(key);
                if (value == null) {
                    formattingConfiguration.put(key, value);
                }else {
                    formattingConfiguration.put(key, ARROWBASE_VALUE_PATTERN.matcher(value).replaceAll("\\$\\{$1\\}"));
                }
            });
            configuration.setConfig(formattingConfiguration);
        });
        //
        if (isSchemaRegistrySSL()) {
            cluster.getSsl().registerSSL();
        }
    }

    /**
     * Check if the url of the schema registry is defined and use https protocol
     * @return
     */
    private boolean isSchemaRegistrySSL(){
        if (KafkaConnectProperties.isSchemaRegistrySSL(this.worker)){
            return true;
        }
        for (ConnectorConfiguration configuration : connectors.values()) {
            if (KafkaConnectProperties.isSchemaRegistrySSL(configuration.getConfig())) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param configuration
     * @return
     */
    private static boolean isSchemaRegistrySSL(Map<String, String> configuration){
        return (configuration.get("key.converter.schema.registry.url") != null && configuration.get("key.converter.schema.registry.url").toLowerCase().startsWith(HTTPS_PREFIX))
                || (configuration.get("value.converter.schema.registry.url") != null && configuration.get("value.converter.schema.registry.url").toLowerCase().startsWith(HTTPS_PREFIX));
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class ConnectorConfiguration {

        /**
         * Connector name
         */
        @NotNull
        private String name;

        /**
         * Connector configuration
         */
        @Builder.Default
        @NotNull
        private Map<String, String> config = new HashMap<>();

    }

    /**
     * Cluster configuration
     */
    @Getter
    @Setter
    public static class ClusterConfig {

        private static final String DEFAULT_SECURITY_CONFIG = "PLAINTEXT";

        public enum KafkaConnectMode { @Deprecated DISTRIBUED, STANDALONE, DISTRIBUTED}


        /**
         * Define embedded mode
         */
        @NotNull
        private Boolean embedded = Boolean.TRUE;

        /**
         * Define mode (STANDALONE / DISTRIBUTED)
         */
        private KafkaConnectMode mode = KafkaConnectMode.DISTRIBUTED;


        /**
         * Rest api configuration
         */
        private final RestApiManagement restApi = new RestApiManagement();

        /**
         *
         */
        private final SslConfig ssl = new SslConfig();

        /**
         *
         */
        private String securityProtocol = DEFAULT_SECURITY_CONFIG;

        /**
         *
         * @param prefix
         * @return
         */
        public Map<String, String> buildProperties(String prefix) {
            Map<String, String> properties = new HashMap<>();
            properties.put(prefix + "security.protocol", securityProtocol == null ? DEFAULT_SECURITY_CONFIG : securityProtocol);
            properties.putAll(ssl.buildProperties(prefix));
            return properties;
        }

    }

    /**
     *
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SslConfig {

        /**
         * SSL password
         */
        private String keyPassword;
        /**
         * Keystore file location
         */
        private Resource keystoreLocation;
        /**
         * Keystore password
         */
        private String keystorePassword;
        /**
         * Trutsore file location
         */
        private Resource truststoreLocation;
        /**
         * Trutstore password
         */
        private String truststorePassword;

        /**
         * Return the SSL configuration.
         *
         * @param prefix
         * @return
         */
        public Map<String, String> buildProperties(String prefix) {
            Map<String, String> properties = new HashMap<>();
            if (keystoreLocation == null && truststoreLocation == null) {
                return properties;
            }
            properties.put(prefix + SslConfigs.SSL_KEY_PASSWORD_CONFIG, keyPassword);
            if (keystoreLocation != null) {
                properties.put(prefix + SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, resourceToPath(keystoreLocation));
                properties.put(prefix + SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, this.keystorePassword);
            }
            if (truststoreLocation != null) {
                properties.put(prefix + SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, resourceToPath(truststoreLocation));
                properties.put(prefix + SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, this.truststorePassword);
            }
            return properties;
        }

        /**
         * Add SSL parameters in system properties
         */
        public void registerSSL(){
            if (keystoreLocation == null && truststoreLocation == null) {
                return;
            }
            if (System.getProperty("javax.net.ssl.trustStore") == null && truststoreLocation != null){
                System.setProperty("javax.net.ssl.trustStore", resourceToPath(truststoreLocation));
                System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);
            }
            if (System.getProperty("javax.net.ssl.keyStore") == null && keystoreLocation != null){
                System.setProperty("javax.net.ssl.trustStore", resourceToPath(keystoreLocation));
                System.setProperty("javax.net.ssl.trustStorePassword", keystorePassword);
            }
        }

        /**
         *
         * @param resource
         * @return
         */
        private String resourceToPath(Resource resource) {
            try {
                return resource.getFile().getAbsolutePath();
            } catch (IOException err) {
                throw new IllegalStateException("Resource '" + resource + "' must be on a file system", err);
            }
        }
    }

    /**
     * Rest api configuration
     */
    public static class RestApiManagement {

        private static final String DEFAULT_CONTEXT_PATH = "/api/kconnect";

        /**
         * Active rest api for embedded mode
         */
        @Getter
        @Setter
        private boolean enabled = true;

        /**
         * Context path for api rest (use with embedded mode)
         */
        @Getter
        private String contextPath = DEFAULT_CONTEXT_PATH;

        /**
         * Url for rest api
         */
        @Getter
        @Setter
        private String url;

        /**
         *
         * @param contextPath
         */
        public void setContextPath(String contextPath) {
            if (contextPath == null) {
                this.contextPath = "";
            }else if (contextPath.endsWith("/")) {
                this.contextPath = contextPath.substring(0, contextPath.length() - 1);
            }else {
                this.contextPath = contextPath;
            }
        }
    }
}
