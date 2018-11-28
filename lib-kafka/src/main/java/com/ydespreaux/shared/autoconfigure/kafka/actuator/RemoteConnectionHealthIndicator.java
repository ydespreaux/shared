package com.ydespreaux.shared.autoconfigure.kafka.actuator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Health indicator for remote connections.
 */
@Slf4j
@ConditionalOnClass(HealthIndicator.class)
public class RemoteConnectionHealthIndicator implements HealthIndicator {

    private static final Long TIMEOUT = TimeUnit.SECONDS.toMillis(3);
    private static final String DOWN = "DOWN";
    private static final String UP = "UP";
    private static final String HOSTNAME_PORT_SEPARATOR = ":";
    private static final String HTTP_PREFIX = "http://";
    private static final String HTTPS_PREFIX = "https://";
    private static final String EMPTY_STRING = "";

    private List<String> connectionsToCheck = new ArrayList<>();

    public RemoteConnectionHealthIndicator(String connection) {
        this.connectionsToCheck = new ArrayList<>(1);
        this.connectionsToCheck.add(connection);
    }

    /**
     * Construct RemoteConnectionHealthIndicator with a list of connection that we want to check.
     * The list must contain hostname and port infos like: localhost:8080
     *
     * @param connectionsToCheck
     */
    public RemoteConnectionHealthIndicator(List<String> connectionsToCheck) {
        this.connectionsToCheck = connectionsToCheck;
    }

    /**
     * Method called when /health actuator's endpoint is called
     *
     * @return health datas
     */
    @Override
    public Health health() {
        Map<String, String> connectionsStatus = this.checkConnections();
        return this.getDetailsOnHealthBuilder(connectionsStatus).build();
    }

    /**
     * Check every connections set up on construction by opening a socket for each
     *
     * @return a map of connection with UP or DOWN weither the socket test was successfull or not
     */
    private Map<String, String> checkConnections() {
        if (this.connectionsToCheck == null) {
            return null;
        }

        Map<String, String> connectionsStatus = new HashMap<>(this.connectionsToCheck.size());
        for (String connection : this.connectionsToCheck) {
            boolean isHttps = connection.toLowerCase().startsWith(HTTPS_PREFIX);
            connection = connection.toLowerCase().replaceAll(HTTP_PREFIX + "|" + HTTPS_PREFIX, EMPTY_STRING);


            try (Socket socket = isHttps ? SSLSocketFactory.getDefault().createSocket() : SocketFactory.getDefault().createSocket()) {

                String[] connectionProperties = connection.split(HOSTNAME_PORT_SEPARATOR);
                if (connectionProperties.length == 0) {
                    connectionsStatus.put(connection, DOWN);
                }else {
                    String hostname = connectionProperties[0];
                    int port = isHttps ? 443 : 8080;
                    if (connectionProperties.length == 2) {
                        port = Integer.parseInt(connectionProperties[1]);
                    }
                    socket.connect(new InetSocketAddress(hostname, port), TIMEOUT.intValue());
                    connectionsStatus.put(connection, UP);
                }
            } catch (Exception e) {
                log.error("Connection error:", e);
                connectionsStatus.put(connection, DOWN);
            }
        }
        return connectionsStatus;
    }


    /**
     * Pre-build the health details from the connection status map
     *
     * @param connectionStatus a map of connection with their status from tested socket
     * @return pre-build health details
     */
    private Health.Builder getDetailsOnHealthBuilder(Map<String, String> connectionStatus) {

        if (connectionStatus == null) {
            return Health.down();
        } else {
            if (connectionStatus.isEmpty()) {
                return Health.down();
            } else {
                Health.Builder healthBuilder = null;
                if (connectionStatus.values().contains(UP)) {
                    healthBuilder = Health.up();
                } else {
                    healthBuilder = Health.down();
                }

                for (Map.Entry<String, String> entry : connectionStatus.entrySet()) {
                    healthBuilder.withDetail(entry.getKey(), entry.getValue());
                }

                return healthBuilder;
            }
        }
    }
}
