package com.ydespreaux.shared.testcontainers.mysql;

import com.ydespreaux.shared.testcontainers.common.jdbc.AbstractJdbcContainer;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.ydespreaux.shared.testcontainers.common.utils.ContainerUtils.containerLogsConsumer;
import static java.lang.String.format;

/**
 * MySQL container.
 *
 * @since 1.0.0
 * @param <SELF>
 */
@Slf4j
public class MySQLContainer<SELF extends MySQLContainer<SELF>> extends AbstractJdbcContainer<SELF> {

    private static final String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
    private static final String DRIVER_V8_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

    /**
     * Default image name
     */
    private static final String MYSQL_DEFAULT_BASE_URL = "mysql";
    /**
     * Default version
     */
    private static final String MYSQL_DEFAULT_VERSION = "5.7.22";

    private static final String MY_CNF_CONFIG_OVERRIDE_PARAM_NAME = "TC_MY_CNF";
    private static final String JDBC_URL = "jdbc:mysql://%s:%d/%s";
    private static final Integer MYSQL_PORT = 3306;

    private static final String MYSQL_INIT_DIRECTORY = "/docker-entrypoint-initdb.d";

    private final String driverClassName;

    private String rootPassword = UUID.randomUUID().toString();

    /**
     * Register springboot properties in environment
     */
    private boolean registerSpringbootProperties = true;


    /**
     *
     */
    public MySQLContainer() {
        this(MYSQL_DEFAULT_BASE_URL, MYSQL_DEFAULT_VERSION);
    }

    /**
     * @param version
     */
    public MySQLContainer(String version) {
        this(MYSQL_DEFAULT_BASE_URL, version);
    }

    /**
     * @param version
     */
    public MySQLContainer(String baseUrl, String version) {
        super(baseUrl + ":" + version);
        this.withUsername("db_user_test");
        this.withPassword(UUID.randomUUID().toString());
        this.withDatabaseName("db_test");
        this.driverClassName = retrieveDriverClassName();
    }

    /**
     *
     * @return
     */
    private String retrieveDriverClassName(){
        try {
            Class.forName(DRIVER_V8_CLASS_NAME);
            return DRIVER_V8_CLASS_NAME;
        } catch (ClassNotFoundException e) {
            return DRIVER_CLASS_NAME;
        }
    }

    /**
     * Get the numbers port for the liveness check.
     * @return
     */
    @Override
    public Set<Integer> getLivenessCheckPortNumbers() {
        return new HashSet(this.getMappedPort(MYSQL_PORT.intValue()).intValue());
    }

    /**
     * Configure the container.
     */
    @Override
    protected void configure() {
        this.withLogConsumer(containerLogsConsumer(log))
                .withExposedPorts(MYSQL_PORT)
                .withEnv("MYSQL_DATABASE", getDatabaseName())
                .withEnv("MYSQL_USER", getUsername())
                .withEnv("MYSQL_PASSWORD", getPassword())
                .withEnv("MYSQL_ROOT_PASSWORD", getRootPassword())
                .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withName("testcontainsers-mysql-" + UUID.randomUUID()));
        this.optionallyMapResourceParameterAsVolume(MY_CNF_CONFIG_OVERRIDE_PARAM_NAME, "/etc/mysql/conf.d", "mysql-default-conf");
        this.setStartupAttempts(1);
    }

    /**
     * Get the driver class name.
     *
     * @return
     */
    @Override
    public String getDriverClassName() {
        return this.driverClassName;
    }

    /**
     * Get the jdbc url.
     *
     * @return
     */
    @Override
    public String getJdbcUrl() {
        return format(JDBC_URL, this.getContainerIpAddress(), this.getMappedPort(MYSQL_PORT.intValue()), this.getDatabaseName());
    }

    /**
     * Get the mySQL port.
     *
     * @return
     */
    public Integer getPort() {
        return this.getMappedPort(MYSQL_PORT);
    }

    @Override
    protected String constructUrlForConnection(String queryString) {
        StringBuilder url = new StringBuilder(super.constructUrlForConnection(queryString));
        if (url.indexOf("useSSL=") == -1) {
            url.append(url.indexOf("?") == -1 ? "?" : "&").append("useSSL=false");
        }
        return url.toString();
    }

    /**
     * Get the root password.
     *
     * @return
     */
    public String getRootPassword() {
        return this.rootPassword;
    }

    @Override
    public String getTestQueryString() {
        return "SELECT 1";
    }

    /**
     * Start the container.
     */
    @Override
    public void start() {
        super.start();
        if (registerSpringbootProperties()) {
            registerMySqlEnvironment();
        }
    }

    /**
     * Set the network.
     *
     * @param network
     * @return
     */
    @Override
    public SELF withNetwork(Network network) {
        super.withNetwork(network);
        return this.self();
    }

    public SELF withConfigurationOverride(String s) {
        this.parameters.put(MY_CNF_CONFIG_OVERRIDE_PARAM_NAME, s);
        return this.self();
    }

    /**
     * Set the root password.
     *
     * @param password
     * @return
     */
    public SELF withRootPassword(String password) {
        this.rootPassword = password;
        return this.self();
    }

    /**
     * Set the sql file script
     * @param sqlInit
     * @return
     */
    public SELF withMySqlInit(String sqlInit) {
        if (sqlInit == null) {
            return this.self();
        }
        MountableFile mountableFile = MountableFile.forClasspathResource(sqlInit);
        Path scriptsDir = Paths.get(mountableFile.getResolvedPath());
        File toFile = scriptsDir.toFile();
        if (!toFile.exists()) {
            throw new IllegalArgumentException(format("Resource with path %s could not be found", scriptsDir.toString()));
        }
        if (toFile.isDirectory()) {
            throw new IllegalArgumentException(format("Resource with path %s must be a file", scriptsDir.toString()));
        }
        // Create the volume that will be need for scripts
        this.addFileSystemBind(mountableFile.getResolvedPath(), MYSQL_INIT_DIRECTORY + "/" + scriptsDir.getFileName(), BindMode.READ_ONLY);
        return this.self();
    }

    /**
     * Set the directory sql files.
     *
     * @param directory
     * @return
     */
    public SELF withMySqlInitDirectory(String directory) {
        if (directory == null) {
            return this.self();
        }
        MountableFile mountableFile = MountableFile.forClasspathResource(directory);
        Path scriptsDir = Paths.get(mountableFile.getResolvedPath());
        File toFile = scriptsDir.toFile();
        if (!toFile.exists()) {
            throw new IllegalArgumentException(format("Resource with path %s could not be found", scriptsDir.toString()));
        }
        if (toFile.isFile()) {
            throw new IllegalArgumentException(format("Resource with path %s must be a directory", scriptsDir.toString()));
        }
        // Create the volume that will be need for scripts
        this.addFileSystemBind(mountableFile.getResolvedPath(), MYSQL_INIT_DIRECTORY, BindMode.READ_ONLY);
        return this.self();
    }


    /**
     * Register all properties
     */
    protected void registerMySqlEnvironment() {
        System.setProperty(this.getDriverClassSystemProperty(), getDriverClassName());
        System.setProperty(this.getUrlSystemProperty(), constructUrlForConnection(""));
        System.setProperty(this.getUsernameSystemProperty(), this.getUsername());
        System.setProperty(this.getPasswordSystemProperty(), this.getPassword());
        System.setProperty(this.getPlatformSystemProperty(), "mysql");
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
     * Get the MySQL url
     *
     * @return
     */
    @Override
    public String getURL() {
        return getJdbcUrl();
    }

    /**
     * Get the local MySQL url.
     * @return
     */
    @Override
    public String getInternalURL() {
        return format(JDBC_URL, this.getNetworkAliases().get(0), MYSQL_PORT, this.getDatabaseName());
    }

    /**
     * @param driverClassSystemProperty
     * @return
     */
    @Override
    public SELF withDriverClassSystemProperty(String driverClassSystemProperty) {
        return super.withDriverClassSystemProperty(driverClassSystemProperty);
    }

    /**
     * @param urlSystemProperty
     * @return
     */
    @Override
    public SELF withUrlSystemProperty(String urlSystemProperty) {
        return super.withUrlSystemProperty(urlSystemProperty);
    }

    /**
     * @param usernameSystemProperty
     * @return
     */
    @Override
    public SELF withUsernameSystemProperty(String usernameSystemProperty) {
        return super.withUsernameSystemProperty(usernameSystemProperty);
    }

    /**
     * @param passwordSystemProperty
     * @return
     */
    @Override
    public SELF withPasswordSystemProperty(String passwordSystemProperty) {
        return super.withPasswordSystemProperty(passwordSystemProperty);
    }

    /**
     * @param platformSystemProperty
     * @return
     */
    @Override
    public SELF withPlatformSystemProperty(String platformSystemProperty) {
        return super.withPlatformSystemProperty(platformSystemProperty);
    }

    /**
     *
     * @param databaseName
     * @return
     */
    @Override
    public SELF withDatabaseName(String databaseName) {
        return super.withDatabaseName(databaseName);
    }

    /**
     *
     * @param username
     * @return
     */
    @Override
    public SELF withUsername(String username) {
        return super.withUsername(username);
    }

    /**
     *
     * @param password
     * @return
     */
    @Override
    public SELF withPassword(String password) {
        return super.withPassword(password);
    }
}
