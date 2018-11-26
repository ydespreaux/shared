package com.ydespreaux.shared.kafka.connect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ydespreaux.shared.autoconfigure.kafka.connect.KafkaConnectProperties;
import com.ydespreaux.shared.kafka.connect.app.KafkaConnectApplicationWithSwagger;
import com.ydespreaux.shared.kafka.connect.support.KafkaConnectTemplate;
import com.ydespreaux.shared.testcontainers.kafka.config.TopicConfiguration;
import com.ydespreaux.shared.testcontainers.kafka.rule.ConfluentKafkaContainer;
import com.ydespreaux.shared.testcontainers.mysql.MySQLContainer;
import org.apache.kafka.connect.runtime.rest.entities.ConnectorStateInfo;
import org.apache.kafka.connect.runtime.rest.entities.CreateConnectorRequest;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;
import org.testcontainers.containers.Network;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@DirtiesContext
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("embedded")
@SpringBootTest(classes = {KafkaConnectApplicationWithSwagger.class})
@WebAppConfiguration
public class ITKafkaConnectEmbeddedRestApiTest {

    public static final String TOPIC_NAME = "an_dev_user_out_v2";

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
    public static ConfluentKafkaContainer kafkaContainer = new ConfluentKafkaContainer<>("3.3.1")
            .withSchemaRegistry(true)
            .withNetwork(network);

    @BeforeClass
    public static void onSetupClass() throws IOException {
        System.setProperty("spring.datasource.connect.url", mySQLContainer.getURL());
        kafkaContainer.createTopic(new TopicConfiguration(TOPIC_NAME, 3, true));
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String DEFAULT_CONNECTOR_NAME = "default-connector-jdbc";
    private static final String NEW_CONNECTOR_NAME = "new-connector-source";

    @Autowired
    private KafkaConnectTemplate template;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private KafkaConnectProperties kafkaConnectProperties;

    /**
     *
     * @return
     */
    protected MockMvc createMockMvc(){
        return webAppContextSetup(webApplicationContext).build();
    }

    /**
     *
     * @throws InterruptedException
     */
    @Before
    public void onSetup() throws InterruptedException {
        KafkaConnectProperties.ConnectorConfiguration config = createDefaultConnectorConfig(DEFAULT_CONNECTOR_NAME);
        template.createConnector(config.getName(), config.getConfig());
        waitConnector(DEFAULT_CONNECTOR_NAME);
    }

    @After
    public void onTeardown(){
        template.deleteConnector(NEW_CONNECTOR_NAME);
        template.deleteConnector(DEFAULT_CONNECTOR_NAME);
    }

    @Test
    public void health() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(is(notNullValue())))
                .andExpect(jsonPath("status").value("UP"));
    }

    @Test
    public void swagger() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(get("/v2/api-docs?group=kconnect"))
                .andExpect(status().isOk())
                .andExpect(content().string(is(notNullValue())));
    }

    @Test
    public void serverInfo() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(get(kafkaConnectProperties.getContextPath()))
                .andExpect(status().isOk())
                .andExpect(content().string(is(notNullValue())))
                .andExpect(jsonPath("version").value("1.1.0"))
                .andExpect(jsonPath("commit").isNotEmpty())
                .andExpect(jsonPath("kafka_cluster_id").isNotEmpty());
    }

    @Test
    public void getConnectors() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(get(kafkaConnectProperties.getContextPath()+"/connectors"))
                .andExpect(status().isOk())
                .andExpect(content().string(is(notNullValue())))
                .andExpect(content().string(containsString(DEFAULT_CONNECTOR_NAME)));
    }

    @Test
    public void createConnector() throws Exception {
        MockMvc mockMvc = createMockMvc();
        KafkaConnectProperties.ConnectorConfiguration config = createDefaultConnectorConfig(NEW_CONNECTOR_NAME);
        ResultActions result = mockMvc.perform(post(kafkaConnectProperties.getContextPath()+"/connectors")
                .content(toJson(new CreateConnectorRequest(config.getName(), config.getConfig())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("context.entity.name").value(NEW_CONNECTOR_NAME))
                .andExpect(jsonPath("context.entity.config.name").value(NEW_CONNECTOR_NAME));
        for (String key : config.getConfig().keySet()) {
            result.andExpect(jsonPath("context.entity.config.['" + key + "']").value(config.getConfig().get(key)));
        }
        result.andExpect(jsonPath("context.entity.tasks[0].connector").value(NEW_CONNECTOR_NAME))
        .andExpect(jsonPath("context.entity.tasks[0].task").value("0"));
    }



    @Test(expected = NestedServletException.class)
    public void createConnectorWithConnectorAlreadyExists() throws Exception {
        MockMvc mockMvc = createMockMvc();
        KafkaConnectProperties.ConnectorConfiguration config = createDefaultConnectorConfig(DEFAULT_CONNECTOR_NAME);
        mockMvc.perform(post(kafkaConnectProperties.getContextPath()+"/connectors")
                .content(toJson(new CreateConnectorRequest(config.getName(), config.getConfig())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test(expected = NestedServletException.class)
    public void createConnectorWithInvalidConfiguration() throws Exception {
        MockMvc mockMvc = createMockMvc();
        Map<String, String> config = new HashMap<>();
        config.put("connector.class", "io.confluent.connect.jdbc.JdbcSourceConnector");

        KafkaConnectProperties.ConnectorConfiguration connectorConfig = KafkaConnectProperties.ConnectorConfiguration.builder()
                .name(NEW_CONNECTOR_NAME)
                .config(config)
                .build();
        mockMvc.perform(post(kafkaConnectProperties.getContextPath()+"/connectors")
                .content(toJson(new CreateConnectorRequest(connectorConfig.getName(), connectorConfig.getConfig())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateConnector() throws Exception {
        KafkaConnectProperties.ConnectorConfiguration config = createDefaultConnectorConfig(DEFAULT_CONNECTOR_NAME);
        MockMvc mockMvc = createMockMvc();
        config.getConfig().put("tasks.max", "1");

        ResultActions result = mockMvc.perform(put(kafkaConnectProperties.getContextPath()+"/connectors/" + DEFAULT_CONNECTOR_NAME+ "/config")
                .content(toJson(config.getConfig()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("context.entity.name").value(DEFAULT_CONNECTOR_NAME))
                .andExpect(jsonPath("context.entity.config.name").value(DEFAULT_CONNECTOR_NAME));
        for (String key : config.getConfig().keySet()) {
            result.andExpect(jsonPath("context.entity.config.['" + key + "']").value(config.getConfig().get(key)));
        }
        result.andExpect(jsonPath("context.entity.tasks[0].connector").value(DEFAULT_CONNECTOR_NAME))
                .andExpect(jsonPath("context.entity.tasks[0].task").value("0"));
    }

    @Test
    public void getConnector() throws Exception {
        MockMvc mockMvc = createMockMvc();
        ResultActions result = mockMvc.perform(get(kafkaConnectProperties.getContextPath()+"/connectors/" + DEFAULT_CONNECTOR_NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(DEFAULT_CONNECTOR_NAME))
                .andExpect(jsonPath("type").value("source"))
                .andExpect(jsonPath("tasks[0].connector").value(DEFAULT_CONNECTOR_NAME))
                .andExpect(jsonPath("tasks[0].task").value("0"))
                ;

        Map<String, String> config = createDefaultConnectorConfig(DEFAULT_CONNECTOR_NAME).getConfig();
        for (String key : config.keySet()) {
            result.andExpect(jsonPath("config.['" + key + "']").value(config.get(key)));
        }
    }

    @Test(expected = NestedServletException.class)
    public void getConnectorWithInvalidConnector() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(get(kafkaConnectProperties.getContextPath()+"/connectors/connector-invalid"));
    }

    @Test
    public void getConnectorConfig() throws Exception {
        MockMvc mockMvc = createMockMvc();
        ResultActions result = mockMvc.perform(get(kafkaConnectProperties.getContextPath()+"/connectors/" + DEFAULT_CONNECTOR_NAME + "/config"))
        .andExpect(status().isOk());

        Map<String, String> config = createDefaultConnectorConfig(DEFAULT_CONNECTOR_NAME).getConfig();
        for (String key : config.keySet()) {
            result.andExpect(jsonPath("$['" + key + "']").value(config.get(key)));
        }
    }

    @Test(expected = NestedServletException.class)
    public void getConnectorConfigWithInvalidConnector() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(get(kafkaConnectProperties.getContextPath()+"/connectors/connector-invalid/config"));
    }

    @Test
    public void getConnectorStatus() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(get(kafkaConnectProperties.getContextPath()+"/connectors/" + DEFAULT_CONNECTOR_NAME + "/status"))
        .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(DEFAULT_CONNECTOR_NAME))
                .andExpect(jsonPath("type").value("source"))
                .andExpect(jsonPath("connector.state").value("RUNNING"))
                .andExpect(jsonPath("tasks[0].id").value("0"))
                .andExpect(jsonPath("tasks[0].state").value("RUNNING"))
        ;
    }

    @Test(expected = NestedServletException.class)
    public void getConnectorStatusWithConnectorNotFound() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(get(kafkaConnectProperties.getContextPath()+"/connectors/connector-invalid/status"));
    }

    @Test
    public void restartConnector() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(post(kafkaConnectProperties.getContextPath()+"/connectors/" + DEFAULT_CONNECTOR_NAME + "/restart"))
        .andExpect(status().isOk());
    }

    @Test(expected = NestedServletException.class)
    public void restartConnectorWithInvalidConnector() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(post(kafkaConnectProperties.getContextPath()+"/connectors/connector-invalid/restart"));
    }

    @Test
    public void pauseConnector() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(put(kafkaConnectProperties.getContextPath()+"/connectors/" + DEFAULT_CONNECTOR_NAME + "/pause"))
                .andExpect(status().isAccepted());
    }

    @Test(expected = NestedServletException.class)
    public void pauseConnectorWithInvalidConnector() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(put(kafkaConnectProperties.getContextPath()+"/connectors/connector-invalid/pause"));
    }

    @Test
    public void resumeConnector() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(put(kafkaConnectProperties.getContextPath()+"/connectors/" + DEFAULT_CONNECTOR_NAME + "/resume"))
        .andExpect(status().isAccepted());
    }

    @Test(expected = NestedServletException.class)
    public void resumeConnectorWithInvalidConnector() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(put(kafkaConnectProperties.getContextPath()+"/connectors/connector-invalid/resume"));
    }

    @Test
    public void getTaskConfigs() throws Exception {
        MockMvc mockMvc = createMockMvc();
        ResultActions result = mockMvc.perform(get(kafkaConnectProperties.getContextPath()+"/connectors/" + DEFAULT_CONNECTOR_NAME + "/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id.connector").value(DEFAULT_CONNECTOR_NAME))
                .andExpect(jsonPath("$[0].id.task").value("0"));
        Map<String, String> config = createDefaultConnectorConfig(DEFAULT_CONNECTOR_NAME).getConfig();
        for (String key : config.keySet()) {
            result.andExpect(jsonPath("$[0].config.['" + key + "']").value(config.get(key)));
        }
    }

    @Test(expected = NestedServletException.class)
    public void getTaskConfigsWithInvalidConnector() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(get(kafkaConnectProperties.getContextPath()+"/connectors/connector-invalid/tasks"));
    }

    @Test
    public void getTaskStatus() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(get(kafkaConnectProperties.getContextPath()+"/connectors/" + DEFAULT_CONNECTOR_NAME + "/tasks/0/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("state").value("RUNNING"))
                .andExpect(jsonPath("id").value("0"))
        ;
    }

    @Test
    public void getTaskStatusWithInvalidConnector() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(get(kafkaConnectProperties.getContextPath()+"/connectors/connector-invalid/tasks/0/status"));
    }

    @Test(expected = NestedServletException.class)
    public void getTaskStatusWithInvalidTask() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(get(kafkaConnectProperties.getContextPath()+"/connectors/" + DEFAULT_CONNECTOR_NAME + "/tasks/1/status"));
    }

    @Test
    public void restartTask() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(post(kafkaConnectProperties.getContextPath()+"/connectors/" + DEFAULT_CONNECTOR_NAME + "/tasks/0/restart"))
        .andExpect(status().isOk());
    }

    @Test(expected = NestedServletException.class)
    public void restartTaskWithInvalidConnector() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(post(kafkaConnectProperties.getContextPath()+"/connectors/connector-invalid/tasks/0/restart"));
    }

    @Test(expected = NestedServletException.class)
    public void restartTaskWithInvalidTask() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(post(kafkaConnectProperties.getContextPath()+"/connectors/" + DEFAULT_CONNECTOR_NAME + "/tasks/1/restart"));
    }

    @Test
    public void deleteConnector() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(delete(kafkaConnectProperties.getContextPath()+"/connectors/" + DEFAULT_CONNECTOR_NAME))
        .andExpect(status().isOk());
    }

    @Test
    public void getConnectorPlugins() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(get(kafkaConnectProperties.getContextPath()+"/connector-plugins"))
        .andExpect(status().isOk())
                .andExpect(jsonPath("[0].class").value("io.confluent.connect.jdbc.JdbcSinkConnector"))
                .andExpect(jsonPath("[0].type").value("sink"))
                .andExpect(jsonPath("[1].class").value("io.confluent.connect.jdbc.JdbcSourceConnector"))
                .andExpect(jsonPath("[1].type").value("source"))
        ;
    }

    @Test
    public void validateConnectorConfigs() throws Exception {
        Map<String, String> config = createDefaultConnectorConfig(NEW_CONNECTOR_NAME).getConfig();
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(put(kafkaConnectProperties.getContextPath()+"/connector-plugins/JdbcSourceConnector/config/validate")
                .content(toJson(config))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("error_count").value(0));
    }


    private static String toJson(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     *
     * @return
     */
    private KafkaConnectProperties.ConnectorConfiguration createDefaultConnectorConfig(String connectorName){

        Map<String, String> config = new HashMap<>();
        config.put("name", connectorName);
        config.put("connector.class", "io.confluent.connect.jdbc.JdbcSourceConnector");
        config.put("tasks.max", "10");
        config.put("connection.url", mySQLContainer.getURL() + "?user=user&password=password&useSSL=false");
        config.put("table.whitelist", "tb_user");
        config.put("mode", "timestamp+incrementing");
        config.put("timestamp.column.name", "last_modified");
        config.put("validate.non.null", "false");
        config.put("incrementing.column.name", "id");
        config.put("topic.prefix", "an_dev_");

        return KafkaConnectProperties.ConnectorConfiguration.builder()
                .name(connectorName)
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
