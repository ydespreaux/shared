package com.ydespreaux.shared.testcontainers.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.ydespreaux.shared.testcontainers.common.IContainer;
import com.ydespreaux.shared.testcontainers.common.utils.ContainerUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.ydespreaux.shared.testcontainers.common.utils.ContainerUtils.containerLogsConsumer;
import static java.lang.String.format;

/**
 * The Cassandra container.
 *
 * @param <SELF>
 */
@Slf4j
public class CassandraContainer<SELF extends CassandraContainer<SELF>> extends GenericContainer<SELF> implements IContainer<SELF> {

    private static final String CASSANDRA_DEFAULT_VERSION = "3.11";
    private static final String CASSANDRA_DEFAULT_BASE_URL = "cassandra";
    private static final int CASSANDRA_DEFAULT_PORT = 9042;
    private static final int STARTER_TIMOUT_SECONDS = 120;

    private static final String DB_SCHEMA_DIRECTORY = "/tmp/init-schema";
    /**
     *
     */
    @Getter
    private final List<String> cqlScripts = new ArrayList<>();

    /**
     * Register springboot properties in environment
     */
    private boolean registerSpringbootProperties = true;

    /**
     * Contact points for spring boot properties.
     */
    @Getter
    private String contactPointsSystemProperty = "spring.data.cassandra.contact-points";
    @Getter
    /**
     * Cassandra port for spring boot properties.
     */
    private String cassandraPortSystemProperty = "spring.data.cassandra.port";

    /**
     * Default constructor.
     * By default, image docker for cassandra is cassandra:3.11
     */
    public CassandraContainer() {
        this(CASSANDRA_DEFAULT_BASE_URL, CASSANDRA_DEFAULT_VERSION);
    }

    /**
     * Create a cassandra container with a specific version
     *
     * @param version the version of the image
     */
    public CassandraContainer(String version) {
        this(CASSANDRA_DEFAULT_BASE_URL, version);
    }

    /**
     * @param baseUrl url for the cassandra image
     * @param version the version of the image
     */
    public CassandraContainer(String baseUrl, String version) {
        super(baseUrl + ":" + version);
    }

    /**
     * Configure the container.
     */
    @Override
    protected void configure() {
        this.withLogConsumer(containerLogsConsumer(log))
                .withExposedPorts(CASSANDRA_DEFAULT_PORT)
                .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withName("testcontainsers-cassandra-" + UUID.randomUUID()));
    }

    /**
     * Get the list of ports for the liveness check.
     * @return
     */
    @Override
    public Set<Integer> getLivenessCheckPortNumbers() {
        return new HashSet(this.getMappedPort(CASSANDRA_DEFAULT_PORT));
    }

    /**
     * Get the timeout
     * @return
     */
    protected int getStartupTimeoutSeconds() {
        return STARTER_TIMOUT_SECONDS;
    }

    /**
     * Start the container.
     */
    @Override
    public void start() {
        super.start();
        // Execute all cql scripts
        this.cqlScripts.forEach(script -> {
            ContainerUtils.ExecCmdResult result = ContainerUtils.execCmd(this.getDockerClient(), this.getContainerId(), new String[]{"cqlsh", "-f", script});
            if (result.getExitCode() != 0) {
                throw new ContainerLaunchException(format("Execute script %s failed", script), new Exception(result.getOutput()));
            }
        });
        if (registerSpringbootProperties()) {
            // Register cassandra environment
            registerCassandraEnvironment();
        }
    }

    /**
     * Set the network
     *
     * @param network
     * @return
     */
    @Override
    public SELF withNetwork(Network network) {
        super.withNetwork(network);
        return this.self();
    }

    /**
     * Set the contact-points property for spring boot properties.
     * By default the property is 'pring.data.cassandra.contact-points'
     *
     * @param contactPointsSystemProperty the contact-points system property
     * @return
     */
    public SELF withContactPointsSystemProperty(String contactPointsSystemProperty) {
        this.contactPointsSystemProperty = contactPointsSystemProperty;
        return this.self();
    }

    /**
     * Set the cassandra port property for spring boot properties.
     * By default the property is 'spring.data.cassandra.port'
     *
     * @param cassandraPortSystemProperty the cassandra port system property
     * @return
     */
    public SELF withCassandraPortSystemProperty(String cassandraPortSystemProperty) {
        this.cassandraPortSystemProperty = cassandraPortSystemProperty;
        return this.self();
    }

    /**
     * Set the scripts directory.
     *
     * @param directory
     * @return
     */
    public SELF withCqlScriptDirectory(String directory) {
        if (directory == null) {
            return this.self();
        }
        MountableFile mountableFile = MountableFile.forClasspathResource(directory);
        Path scriptsDir = Paths.get(mountableFile.getResolvedPath());
        File scriptFile = scriptsDir.toFile();
        if (!scriptFile.exists()) {
            throw new IllegalArgumentException(format("Resource with path %s could not be found", scriptsDir.toString()));
        }
        if (scriptFile.isFile()) {
            throw new IllegalArgumentException(format("Resource with path %s must be a directory", scriptsDir.toString()));
        }
        // Create the volume that will be need for scripts
        this.addFileSystemBind(scriptsDir.toString(), DB_SCHEMA_DIRECTORY, BindMode.READ_ONLY);
        // Add all scripts in cqlScripts attribute
        try (Stream<Path> paths = Files.list(scriptsDir)){
            paths.filter(path -> path.toFile().isFile()).sorted().forEach(path -> this.cqlScripts.add(DB_SCHEMA_DIRECTORY + "/" + path.getFileName()));
        } catch (IOException e) {
            throw new IllegalArgumentException("Error listing scripts", e);
        }
        return this.self();
    }

    /**
     * Register cassandra properties for contact-points and port
     */
    protected void registerCassandraEnvironment() {
        if (this.contactPointsSystemProperty != null) {
            System.setProperty(this.contactPointsSystemProperty, this.getContainerIpAddress());
        }
        if (this.cassandraPortSystemProperty != null) {
            System.setProperty(this.cassandraPortSystemProperty, String.valueOf(this.getMappedPort(CASSANDRA_DEFAULT_PORT)));
        }
    }

    /**
     *  Return the mapping cassandra port.
     * @return
     */
    public Integer getCQLNativeTransportPort() {
        return this.getMappedPort(CASSANDRA_DEFAULT_PORT);
    }

    /**
     *
     */
    @Override
    protected void waitUntilContainerStarted() {
        this.logger().info("Waiting for cassandra connection to become available at {}:{}", this.getContainerIpAddress(), this.getMappedPort(CASSANDRA_DEFAULT_PORT));
        Unreliables.retryUntilSuccess(getStartupTimeoutSeconds(), TimeUnit.SECONDS, () -> {
            if (!this.isRunning()) {
                throw new ContainerLaunchException("Container failed to start");
            } else {

                Cluster cluster = null;
                Session session = null;
                try {
                    cluster = Cluster.builder()
                            .addContactPoint(this.getContainerIpAddress())
                            .withPort(this.getMappedPort(9042))
                            .build();

                    session = cluster.connect();
                    this.logger().info("Obtained a connection to container ({}:{})", this.getContainerIpAddress(), this.getMappedPort(CASSANDRA_DEFAULT_PORT));
                } finally {
                    if (session != null) {
                        session.close();
                    }
                    if (cluster != null) {
                        cluster.close();
                    }
                }
                return null;
            }
        });
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
        return registerSpringbootProperties;
    }

    /**
     * Return the rl.
     *
     * @return
     */
    @Override
    public String getURL() {
        return this.getContainerIpAddress();
    }

    /**
     * Return the local url.
     * @return
     */
    @Override
    public String getInternalURL() {
        return this.getNetworkAliases().get(0);
    }

    /**
     * Return the internal cassandra port.
     * @return
     */
    public Integer getInternalCQLNativeTransportPort() {
        return CASSANDRA_DEFAULT_PORT;
    }

}
