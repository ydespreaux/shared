package com.ydespreaux.shared.testcontainers.mssql;

import com.ydespreaux.shared.testcontainers.common.jdbc.AbstractJdbcContainer;
import com.ydespreaux.shared.testcontainers.common.utils.ContainerUtils;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.Network;
import org.testcontainers.shaded.com.google.common.base.Strings;
import org.testcontainers.utility.LicenseAcceptance;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static com.ydespreaux.shared.testcontainers.common.utils.ContainerUtils.containerLogsConsumer;
import static java.lang.String.format;

/**
 * SQL Server container.
 *
 * @since 1.1.2
 * @param <SELF>
 */
@Slf4j
public class MSSQLServerContainer<SELF extends MSSQLServerContainer<SELF>> extends AbstractJdbcContainer<SELF> {

    /**
     * Default image name.
     */
    private static final String IMAGE = "microsoft/mssql-server-linux";
    /**
     * Default version.
     */
    private static final String DEFAULT_TAG = "2017-CU9";

    private static final Integer MS_SQL_SERVER_PORT = 1433;
    private static final String MS_SQL_INIT_DIRECTORY = "/docker-entrypoint-initdb.d";

    private static final int DEFAULT_STARTUP_TIMEOUT_SECONDS = 240;
    private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 240;

    private static final String JDBC_URL = "jdbc:sqlserver://%s:%d;databaseName=%s";

    /**
     * Sql files path.
     */
    private final List<String> initSqlFiles = new ArrayList<>();

    /**
     * Register springboot properties in environment
     */
    private boolean registerSpringbootProperties = true;

    /**
     *
     */
    private boolean databaseAutocreate = true;

    /**
     *
     */
    public MSSQLServerContainer() {
        this(IMAGE, DEFAULT_TAG);
    }

    /**
     *
     * @param version
     */
    public MSSQLServerContainer(String version) {
        this (IMAGE, version);
    }

    /**
     *
     * @param baseUrl
     * @param version
     */
    public MSSQLServerContainer(String baseUrl, String version) {
        super(baseUrl + ":" + version);
        this.withUsername("SA");
        this.withPassword("A_Str0ng_Required_Password");
        this.withDatabaseName("db_unit");
        withStartupTimeoutSeconds(DEFAULT_STARTUP_TIMEOUT_SECONDS);
        withConnectTimeoutSeconds(DEFAULT_CONNECT_TIMEOUT_SECONDS);
    }

    /**
     * Get the numbers port for liveness check.
     * @return
     */
    @Override
    public Set<Integer> getLivenessCheckPortNumbers() {
        return new HashSet(this.getMappedPort(MS_SQL_SERVER_PORT).intValue());
    }

    /**
     * Configure the conatiner.
     */
    @Override
    protected void configure() {
        LicenseAcceptance.assertLicenseAccepted(this.getDockerImageName());
        this.withLogConsumer(containerLogsConsumer(log))
            .withExposedPorts(MS_SQL_SERVER_PORT)
            .withEnv("ACCEPT_EULA", "Y")
            .withEnv("SA_PASSWORD", this.getPassword())
            .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withName("testcontainers-mssql-" + UUID.randomUUID()));
        this.setStartupAttempts(1);
    }

    /**
     * Get the driver class name.
     * @return
     */
    @Override
    public String getDriverClassName() {
        return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    }

    /**
     * Get the jdbc url.
     *
     * @return
     */
    @Override
    public String getJdbcUrl() {
        return "jdbc:sqlserver://" + getContainerIpAddress() + ":" + getMappedPort(MS_SQL_SERVER_PORT);
    }

    /**
     * Get the server port.
     *
     * @return
     */
    public Integer getPort() {
        return this.getMappedPort(MS_SQL_SERVER_PORT);
    }


    @Override
    public String getTestQueryString() {
        return "SELECT 1";
    }

    /**
     * Start the container.
     *
     */
    @Override
    public void start() {
        super.start();
        // Création de la database
        if (this.databaseAutocreate && !Strings.isNullOrEmpty(this.getDatabaseName())) {
            String[] cmds = new String[] {
                    "CREATE DATABASE " + this.getDatabaseName()
            };
            for (String cmd : cmds) {
                ContainerUtils.ExecCmdResult result = ContainerUtils.execCmd(this.getDockerClient(), this.getContainerId(), new String[]{"/opt/mssql-tools/bin/sqlcmd", "-S", "localhost", "-U", this.getUsername(), "-P", this.getPassword(), "-Q", cmd});
                if (result.getExitCode() != 0) {
                    throw new ContainerLaunchException(format("Execute command %s failed", cmd), new Exception(result.getOutput()));
                }
            }
        }
        // Exécution des scripts
        if (!initSqlFiles.isEmpty()) {
            for (String initSqlFile : initSqlFiles) {
                ContainerUtils.ExecCmdResult result = ContainerUtils.execCmd(this.getDockerClient(), this.getContainerId(), new String[]{"/opt/mssql-tools/bin/sqlcmd", "-S", "localhost", "-U", this.getUsername(), "-P", this.getPassword(), "-d", this.getDatabaseName(), "-i", initSqlFile});
                if (result.getExitCode() != 0) {
                    throw new ContainerLaunchException(format("Execute script %s failed", initSqlFile), new Exception(result.getOutput()));
                }
            }
        }
        if (registerSpringbootProperties()) {
            registerMsSqlEnvironment();
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

    /**
     * Set the sql script.
     *
     * @param sqlInit
     * @return
     */
    public SELF withMsSqlInit(String sqlInit) {
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
        String containerPath = MS_SQL_INIT_DIRECTORY + "/" + scriptsDir.getFileName();
        this.addFileSystemBind(mountableFile.getResolvedPath(), containerPath, BindMode.READ_ONLY);
        initSqlFiles.add(containerPath);

        return this.self();
    }

    /**
     * Set the directory scripts.
     *
     * @param directory
     * @return
     */
    public SELF withMsSqlInitDirectory(String directory) {
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
        this.addFileSystemBind(scriptsDir.toString(), MS_SQL_INIT_DIRECTORY, BindMode.READ_ONLY);
        try (Stream<Path> paths = Files.list(scriptsDir)){
            paths.filter(path -> path.toFile().isFile()).sorted().forEach(path -> this.initSqlFiles.add(MS_SQL_INIT_DIRECTORY + "/" + path.getFileName()));
        } catch (IOException e) {
            throw new IllegalArgumentException("Error listing scripts", e);
        }
        return this.self();
    }

    /**
     * Register the spring boot properties.
     */
    protected void registerMsSqlEnvironment() {
        System.setProperty(this.getDriverClassSystemProperty(), getDriverClassName());
        System.setProperty(this.getUrlSystemProperty(), constructUrlForConnection(";databaseName=" + this.getDatabaseName()));
        System.setProperty(this.getUsernameSystemProperty(), this.getUsername());
        System.setProperty(this.getPasswordSystemProperty(), this.getPassword());
        System.setProperty(this.getPlatformSystemProperty(), "sqlserver");
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
     * @param autoCreate
     * @return
     */
    public SELF withDatabaseAutoCreate(boolean autoCreate) {
        this.databaseAutocreate = autoCreate;
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
     * Get the sql server url.
     * @return
     */
    @Override
    public String getURL() {
        return constructUrlForConnection(";databaseName=" + this.getDatabaseName());
    }

    /**
     * Get the local sql server url.
     * @return
     */
    @Override
    public String getInternalURL() {
        return format(JDBC_URL, this.getNetworkAliases().get(0), MS_SQL_SERVER_PORT, this.getDatabaseName());
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
