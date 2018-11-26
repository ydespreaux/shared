package com.ydespreaux.shared.kafka.connect.support;

import com.ydespreaux.shared.autoconfigure.kafka.connect.KafkaConnectProperties;
import com.ydespreaux.shared.kafka.connect.KafkaConnectException;
import com.ydespreaux.shared.testcontainers.kafka.config.TopicConfiguration;
import com.ydespreaux.shared.testcontainers.kafka.rule.ConfluentKafkaConnectContainer;
import com.ydespreaux.shared.testcontainers.mysql.MySQLContainer;
import org.apache.kafka.connect.runtime.AbstractStatus;
import org.apache.kafka.connect.runtime.rest.entities.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.Network;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DirtiesContext
@RunWith(SpringJUnit4ClassRunner.class)
public class ITKafkaConnectRestTemplateTest {

    public static final String TOPIC_NAME = "an_dev_user_out_v2";

    private static final String DEFAULT_CONNECTOR_JDBC = "connector-jdbc";
    private static final String CONNECTOR_JDBC_1 = "connector-jdbc-1";

    private static Network network = Network.newNetwork();

    @ClassRule
    public static MySQLContainer mySQLContainer = new MySQLContainer("5.7.22")
            .withDatabaseName("an_springboot_aa")
            .withUsername("user")
            .withPassword("password")
            .withMySqlInit("mysql-init/mysql-init.sql")
            .withRegisterSpringbootProperties(false)
            .withNetwork(network);

    @ClassRule
    public static ConfluentKafkaConnectContainer kafkaContainer = new ConfluentKafkaConnectContainer<>("4.1.0")
            .withSchemaRegistry(true)
            .withPlugins("plugins/kafka-connect-jdbc/mysql-connector-java-5.1.45-bin.jar")
            .withKeyConverter("org.apache.kafka.connect.storage.StringConverter")
            .withValueConverter("io.confluent.connect.avro.AvroConverter")
            .withRestAppSystemProperty("spring.kafka-connect.rest-app")
            .withFormatMessageVersion("0.11.0")
            .withSchemaRegistrySystemProperty(null)
            .withBrokerServersSystemProperty(null)
            .withNetwork(network);

    private static KafkaConnectTemplate template;

    @BeforeClass
    public static void onSetupClass(){
        kafkaContainer.createTopic(new TopicConfiguration(TOPIC_NAME, 3, true));
        template = createKafkaConnectTemplate();
    }


    @Before
    public void onSetup() throws InterruptedException {
        KafkaConnectProperties.ConnectorConfiguration config = createDefaultConnectorConfig();
        template.createConnector(config.getName(), config.getConfig());
        waitConnector(DEFAULT_CONNECTOR_JDBC);
    }

    @After
    public void onTeardown(){
        template.deleteConnector(CONNECTOR_JDBC_1);
        template.deleteConnector(DEFAULT_CONNECTOR_JDBC);
    }

    @Test
    public void getServerInfo() throws Exception {
        ServerInfo info = template.getServerInfo();
        assertThat(info.version(), is(notNullValue()));
        assertThat(info.commit(), is(notNullValue()));
    }

    @Test
    public void getConnectors() throws Exception {
        Collection<String> connectors = template.getConnectors();
        assertThat(connectors.size(), is(equalTo(1)));
        assertThat(connectors, is(contains(DEFAULT_CONNECTOR_JDBC)));
    }

    @Test
    public void createConnector() throws Exception {
        Map<String, String> config = new HashMap<>();
        config.put("connector.class", "io.confluent.connect.jdbc.JdbcSourceConnector");
        config.put("tasks.max", "10");
        config.put("connection.url", mySQLContainer.getInternalURL() + "?user=user&password=password&useSSL=false");
        config.put("table.whitelist", "tb_user");
        config.put("mode", "timestamp+incrementing");
        config.put("timestamp.column.name", "last_modified");
        config.put("validate.non.null", "false");
        config.put("incrementing.column.name", "id");
        config.put("topic.prefix", "an_dev_");

        KafkaConnectProperties.ConnectorConfiguration connectorConfig = KafkaConnectProperties.ConnectorConfiguration.builder()
                .name(CONNECTOR_JDBC_1)
                .config(config)
                .build();

        ConnectorInfo info = template.createConnector(connectorConfig.getName(), connectorConfig.getConfig());
        assertThat(info, is(notNullValue()));
        assertThat(info.name(), is(equalTo(CONNECTOR_JDBC_1)));

        Map<String, String> configInfo = info.config();
        config.keySet().forEach(key -> {
            assertThat(configInfo.get(key), is(equalTo(config.get(key))));
        });
    }

    @Test
    public void createConnectorWithConnectorAlreadyExists() throws Exception {
        try {
            KafkaConnectProperties.ConnectorConfiguration config = createDefaultConnectorConfig();
            template.createConnector(config.getName(), config.getConfig());
            assertThat("KafkaConnectException must be throws", false);
        }catch(KafkaConnectException e) {
            assertThat(e.getErrorCode(), is(equalTo(HttpStatus.CONFLICT)));
        }
    }

    @Test
    public void createConnectorWithInvalidConfiguration() throws Exception {
        try {

            Map<String, String> config = new HashMap<>();
            config.put("connector.class", "io.confluent.connect.jdbc.JdbcSourceConnector");

            KafkaConnectProperties.ConnectorConfiguration connectorConfig = KafkaConnectProperties.ConnectorConfiguration.builder()
                    .name("connector-jdbc-invalid")
                    .config(config)
                    .build();
            template.createConnector(connectorConfig.getName(), connectorConfig.getConfig());
            assertThat("KafkaConnectException must be throws", false);
        }catch(KafkaConnectException e) {
            assertThat(e.getErrorCode(), is(equalTo(HttpStatus.BAD_REQUEST)));
        }
    }

    @Test
    public void updateConnector() throws Exception {
        KafkaConnectProperties.ConnectorConfiguration config = createDefaultConnectorConfig();
        config.getConfig().put("tasks.max", "1");

        ConnectorInfo configUpdated = template.updateConnector(config.getName(), config.getConfig());
        assertThat(configUpdated.name(), is(equalTo(config.getName())));
        for (String key : config.getConfig().keySet()) {
            assertThat(configUpdated.config().get(key), is(equalTo(config.getConfig().get(key))));
        }
    }

    @Test
    public void getConnector() throws Exception {
        ConnectorInfo info = template.getConnector(DEFAULT_CONNECTOR_JDBC);
        assertThat(info, is(notNullValue()));
    }

    @Test(expected = KafkaConnectException.class)
    public void getConnectorWithInvalidConnector() throws Exception {
        template.getConnector("connector-jdbc-invalid");
    }

    @Test
    public void getConnectorConfig() throws Exception {
        KafkaConnectProperties.ConnectorConfiguration defaultConfig = createDefaultConnectorConfig();
        Map<String, String> config = template.getConnectorConfig(DEFAULT_CONNECTOR_JDBC);
        assertThat(config, is(notNullValue()));
        defaultConfig.getConfig().keySet().forEach(key -> assertThat(config.get(key), is(equalTo(defaultConfig.getConfig().get(key)))));
    }

    @Test(expected = KafkaConnectException.class)
    public void getConnectorConfigWithInvalidConnector() throws Exception {
        template.getConnectorConfig("connector-jdbc-invalid");
    }

    @Test
    public void getConnectorStatus() throws Exception {
        ConnectorStateInfo info = template.getConnectorStatus(DEFAULT_CONNECTOR_JDBC);
        assertThat(info, is(notNullValue()));
        assertThat(info.name(), is(equalTo(DEFAULT_CONNECTOR_JDBC)));
        assertThat(info.connector().state(), is(equalTo(AbstractStatus.State.RUNNING.name())));
        assertThat(info.connector().workerId(), is(notNullValue()));
        assertThat(info.tasks().size(), is(equalTo(1)));
        assertThat(info.tasks().get(0).state(), is(equalTo(AbstractStatus.State.RUNNING.name())));
        assertThat(info.tasks().get(0).id(), is(equalTo(0)));
        assertThat(info.tasks().get(0).workerId(), is(equalTo(info.connector().workerId())));
    }

    @Test
    public void getConnectorStatusWithConnectorNotFound() {
        ConnectorStateInfo info = template.getConnectorStatus("connector-jdbc-not-found");
        assertThat(info, is(nullValue()));
    }

    @Test
    public void restartConnector() throws Exception {
        template.restartConnector(DEFAULT_CONNECTOR_JDBC);
    }

    @Test(expected = KafkaConnectException.class)
    public void restartConnectorWithInvalidConnector() throws Exception {
        template.restartConnector("connector-jdbc-invalid");
    }

    @Test
    public void pauseConnector() throws Exception {
        template.pauseConnector(DEFAULT_CONNECTOR_JDBC);
    }

    @Test(expected = KafkaConnectException.class)
    public void pauseConnectorWithInvalidConnector() throws Exception {
        template.pauseConnector("connector-jdbc-invalid");
    }

    @Test
    public void resumeConnector() throws Exception {
        template.resumeConnector(DEFAULT_CONNECTOR_JDBC);
    }

    @Test(expected = KafkaConnectException.class)
    public void resumeConnectorWithInvalidConnector() throws Exception {
        template.resumeConnector("connector-jdbc-invalid");
    }

    @Test
    public void getTaskConfigs() throws Exception {
        List<TaskInfo> taskInfos = template.getTaskConfigs(DEFAULT_CONNECTOR_JDBC);
        assertThat(taskInfos.size(), is(equalTo(1)));
    }

    @Test(expected = KafkaConnectException.class)
    public void getTaskConfigsWithInvalidConnector() throws Exception {
        template.getTaskConfigs("connector-jdbc-invalid");
    }

    @Test
    public void getTaskStatus() throws Exception {
        ConnectorStateInfo.TaskState state = template.getTaskStatus(DEFAULT_CONNECTOR_JDBC, 0);
        assertThat(state, is(notNullValue()));
        assertThat(state.id(), is(equalTo(0)));
        assertThat(state.state(), is(equalTo(AbstractStatus.State.RUNNING.name())));
    }

    @Test(expected = KafkaConnectException.class)
    public void getTaskStatusWithInvalidConnector() throws Exception {
        template.getTaskStatus("connector-jdbc-invalid", 0);
    }

    @Test(expected = KafkaConnectException.class)
    public void getTaskStatusWithInvalidTask() throws Exception {
        template.getTaskStatus(DEFAULT_CONNECTOR_JDBC, 1);
    }

    @Test
    public void restartTask() throws Exception {
        template.restartTask(DEFAULT_CONNECTOR_JDBC, 0);
    }

    @Test(expected = KafkaConnectException.class)
    public void restartTaskWithInvalidConnector() throws Exception {
        template.restartTask("connector-jdbc-invalid", 0);
    }

    @Test(expected = KafkaConnectException.class)
    public void restartTaskWithInvalidTask() throws Exception {
        template.restartTask(DEFAULT_CONNECTOR_JDBC, 1);
    }

    @Test
    public void connectorExists() throws Exception {
        assertThat(template.connectorExists(DEFAULT_CONNECTOR_JDBC), is(true));
    }

    @Test
    public void connectorExistsWithConnectorNotFound(){
        assertThat(template.connectorExists("connector-not-exists"), is(false));
    }

    @Test
    public void deleteConnector() throws Exception {
        template.deleteConnector(DEFAULT_CONNECTOR_JDBC);
    }

    @Test
    public void getConnectorPlugins() throws Exception {
        List<ConnectorPluginInfo> plugins = template.getConnectorPlugins();
        assertThat(plugins, is(notNullValue()));
        assertThat(plugins.isEmpty(), is(false));
    }

    @Test
    public void validateConnectorConfigs() throws Exception {
        KafkaConnectProperties.ConnectorConfiguration config = createDefaultConnectorConfig();
        template.validateConnectorConfigs("JdbcSourceConnector", config.getConfig());
    }


    /**
     *
     * @return
     */
    private static KafkaConnectTemplate createKafkaConnectTemplate(){
        RestTemplateBuilder builder = new RestTemplateBuilder();
        builder = builder.rootUri(kafkaContainer.getRestAppServers());
        return new KafkaConnectRestTemplate(builder.build());
    }
    /**
     *
     * @return
     */
    private KafkaConnectProperties.ConnectorConfiguration createDefaultConnectorConfig(){

        Map<String, String> config = new HashMap<>();
        config.put("connector.class", "io.confluent.connect.jdbc.JdbcSourceConnector");
        config.put("tasks.max", "10");
        config.put("connection.url", mySQLContainer.getInternalURL() + "?user=user&password=password&useSSL=false");
        config.put("table.whitelist", "tb_user");
        config.put("mode", "timestamp+incrementing");
        config.put("timestamp.column.name", "last_modified");
        config.put("validate.non.null", "false");
        config.put("incrementing.column.name", "id");
        config.put("topic.prefix", "an_dev_");

        return KafkaConnectProperties.ConnectorConfiguration.builder()
                .name(DEFAULT_CONNECTOR_JDBC)
                .config(config)
                .build();
    }
    /**
     *
     * @param connectorName
     * @throws InterruptedException
     */
    private void waitConnector(String connectorName) throws InterruptedException {
        int tryCount = 0;
        while (tryCount < 600) {
            ConnectorStateInfo info = template.getConnectorStatus(connectorName);
            if (info == null) {
                TimeUnit.MILLISECONDS.sleep(1000);
            }else {
                break;
            }
            tryCount++;
        }
    }


}