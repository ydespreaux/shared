package com.ydespreaux.shared.testcontainers.kafka.containers;

import com.ydespreaux.shared.testcontainers.common.IContainer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Objects;
import java.util.UUID;

import static com.ydespreaux.shared.testcontainers.common.utils.ContainerUtils.containerLogsConsumer;
import static com.ydespreaux.shared.testcontainers.common.utils.ContainerUtils.getAvailableMappingPort;
import static java.lang.String.format;

/**
 * Schema registry container.
 *
 * @since 1.1.1
 * @param <SELF>
 */
@Slf4j
public class SchemaRegistryContainer<SELF extends SchemaRegistryContainer<SELF>> extends FixedHostPortGenericContainer<SELF> implements IContainer<SELF> {

    private static final String SCHEMA_REGISTRY_DEFAULT_BASE_URL = "confluentinc/cp-schema-registry";

    private static final String ZOOKEEPER_URL_ENV = "SCHEMA_REGISTRY_KAFKASTORE_CONNECTION_URL";
    private static final String BOOTSTRAP_SERVERS_URL_ENV = "SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS";
    /**
     * Register springboot properties in environment
     */
    private boolean registerSpringbootProperties = true;

    /**
     * Spring boot properties for the schema registry url
     */
    private String schemaRegistrySystemProperty;

    /**
     * Schema registry mapping port.
     */
    @Getter
    private int mappingPort;

    /**
     * @param version
     */
    public SchemaRegistryContainer(String version) {
        this(version, getAvailableMappingPort());
    }

    /**
     * @param version
     * @param mappingPort
     */
    public SchemaRegistryContainer(String version, int mappingPort) {
        super(SCHEMA_REGISTRY_DEFAULT_BASE_URL + ":" + version);
        this.mappingPort = mappingPort;
    }

    /**
     * Configure the container
     */
    @Override
    protected void configure() {
        Objects.requireNonNull(this.getEnvMap().get(ZOOKEEPER_URL_ENV), "Zookeeper url must not be null !!!");
        this.withLogConsumer(containerLogsConsumer(log))
                .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                .withEnv("SCHEMA_REGISTRY_LISTENERS", format("http://0.0.0.0:%d", this.mappingPort))
                .withEnv("SCHEMA_REGISTRY_ACCESS_CONTROL_ALLOW_METHODS", "GET,POST,PUT,OPTIONS")
                .withEnv("SCHEMA_REGISTRY_ACCESS_CONTROL_ALLOW_ORIGIN", "*")
                .withExposedPorts(this.mappingPort)
                .withFixedExposedPort(this.mappingPort, this.mappingPort)
                .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withName("testcontainsers-schema-registry-" + UUID.randomUUID()))
                .waitingFor(Wait.forHttp("/"));
    }


    /**
     * Set the zookeeper url for local container.
     *
     * @param internalURL
     * @return
     */
    public SELF withZookeeperInternalURL(String internalURL) {
        Objects.requireNonNull(internalURL, "Zookeeper url must not be null !!!");
        withEnv(ZOOKEEPER_URL_ENV, internalURL);
        return this.self();
    }

    /**
     * Set bootstrap servers for local container.
     *
     * @param internalURL
     * @return
     */
    public SELF withBootstrapServersInternalURL(String internalURL) {
        Objects.requireNonNull(internalURL, "Bootstrap servers url must not be null !!!");
        withEnv(BOOTSTRAP_SERVERS_URL_ENV, internalURL);
        return this.self();
    }

    /**
     * @param schemaRegistrySystemProperty
     * @return
     */
    public SELF withSchemaRegistrySystemProperty(String schemaRegistrySystemProperty) {
        this.schemaRegistrySystemProperty = schemaRegistrySystemProperty;
        return this.self();
    }

    /**
     * Start the container.
     *
     * @throws Exception
     */
    @Override
    public void start() {
        super.start();
        if (registerSpringbootProperties()) {
            this.registerSchemaRegistryEnvironment();
        }
    }

    /**
     * Register the spring boot properties.
     */
    protected void registerSchemaRegistryEnvironment() {
        if (this.schemaRegistrySystemProperty != null) {
            System.setProperty(this.schemaRegistrySystemProperty, getURL());
        }
    }

    /**
     * @param registerProperties
     * @return
     */
    @Override
    public SELF withRegisterSpringbootProperties(boolean registerProperties) {
        this.registerSpringbootProperties = registerProperties;
        return this.self();
    }

    /**
     * @return
     */
    @Override
    public boolean registerSpringbootProperties() {
        return this.registerSpringbootProperties;
    }

    /**
     * Get the url.
     *
     * @return
     */
    @Override
    public String getURL() {
        return format("http://%s:%d", this.getContainerIpAddress(), this.getFirstMappedPort());
    }

    /**
     * Get the local url.
     *
     * @return
     */
    @Override
    public String getInternalURL() {
        return format("http://%s:%d", this.getNetworkAliases().get(0), this.mappingPort);
    }

}
