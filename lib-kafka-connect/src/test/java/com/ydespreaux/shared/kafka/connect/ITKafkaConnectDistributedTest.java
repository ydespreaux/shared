package com.ydespreaux.shared.kafka.connect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ydespreaux.shared.autoconfigure.kafka.connect.KafkaConnectProperties;
import com.ydespreaux.shared.kafka.connect.app.KafkaConnectApplication;
import com.ydespreaux.shared.testcontainers.kafka.config.TopicConfiguration;
import com.ydespreaux.shared.testcontainers.kafka.rule.ConfluentKafkaConnectContainer;
import com.ydespreaux.shared.testcontainers.mysql.MySQLContainer;
import org.apache.kafka.connect.runtime.rest.entities.CreateConnectorRequest;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.Network;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@DirtiesContext
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("distributed")
@SpringBootTest(classes = {KafkaConnectApplication.class})
@WebAppConfiguration
public class ITKafkaConnectDistributedTest {

    public static final String TOPIC_NAME = "an_dev_user_out_v2";
    private static final String CONNECTOR_NAME = "connector-jdbc";

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
            .withRestAppSystemProperty("spring.kafka-connect.cluster.rest-api.url")
            .withFormatMessageVersion("0.11.0")
            .withSchemaRegistrySystemProperty(null)
            .withBrokerServersSystemProperty(null)
            .withNetwork(network);

    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private KafkaConnectProperties kafkaConnectProperties;

    @BeforeClass
    public static void onSetupClass() throws IOException {
        System.setProperty("spring.datasource.connect.url", mySQLContainer.getInternalURL());
        kafkaContainer.createTopic(new TopicConfiguration(TOPIC_NAME, 3, true));
    }

    @Test
    public void health() throws Exception {
        MockMvc mockMvc = webAppContextSetup(webApplicationContext).build();
        MvcResult resultActions = mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(content().string(is(notNullValue())))
                .andExpect(jsonPath("status").value("UP"))
                .andReturn();
    }


    @Test
    public void serverInfo() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(get(kafkaConnectProperties.getContextPath()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getConnectors() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(get(kafkaConnectProperties.getContextPath()+"/connectors"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createConnector() throws Exception {
        MockMvc mockMvc = createMockMvc();
        KafkaConnectProperties.ConnectorConfiguration config = createDefaultConnectorConfig(CONNECTOR_NAME);
        ResultActions result = mockMvc.perform(post(kafkaConnectProperties.getContextPath()+"/connectors")
                .content(toJson(new CreateConnectorRequest(config.getName(), config.getConfig())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateConnector() throws Exception {
        KafkaConnectProperties.ConnectorConfiguration config = createDefaultConnectorConfig(CONNECTOR_NAME);
        MockMvc mockMvc = createMockMvc();
        ResultActions result = mockMvc.perform(put(kafkaConnectProperties.getContextPath()+"/connectors/" + CONNECTOR_NAME+ "/config")
                .content(toJson(config.getConfig()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getConnector() throws Exception {
        MockMvc mockMvc = createMockMvc();
        ResultActions result = mockMvc.perform(get(kafkaConnectProperties.getContextPath()+"/connectors/" + CONNECTOR_NAME))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getConnectorConfig() throws Exception {
        MockMvc mockMvc = createMockMvc();
        ResultActions result = mockMvc.perform(get(kafkaConnectProperties.getContextPath()+"/connectors/" + CONNECTOR_NAME + "/config"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getConnectorStatus() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(get(kafkaConnectProperties.getContextPath()+"/connectors/" + CONNECTOR_NAME + "/status"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void restartConnector() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(post(kafkaConnectProperties.getContextPath()+"/connectors/" + CONNECTOR_NAME + "/restart"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void pauseConnector() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(put(kafkaConnectProperties.getContextPath()+"/connectors/" + CONNECTOR_NAME + "/pause"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void resumeConnector() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(put(kafkaConnectProperties.getContextPath()+"/connectors/" + CONNECTOR_NAME + "/resume"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getTaskConfigs() throws Exception {
        MockMvc mockMvc = createMockMvc();
        ResultActions result = mockMvc.perform(get(kafkaConnectProperties.getContextPath()+"/connectors/" + CONNECTOR_NAME + "/tasks"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getTaskStatus() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(get(kafkaConnectProperties.getContextPath()+"/connectors/" + CONNECTOR_NAME + "/tasks/0/status"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void restartTask() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(post(kafkaConnectProperties.getContextPath()+"/connectors/" + CONNECTOR_NAME + "/tasks/0/restart"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteConnector() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(delete(kafkaConnectProperties.getContextPath()+"/connectors/" + CONNECTOR_NAME))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getConnectorPlugins() throws Exception {
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(get(kafkaConnectProperties.getContextPath()+"/connector-plugins"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void validateConnectorConfigs() throws Exception {
        Map<String, String> config = createDefaultConnectorConfig(CONNECTOR_NAME).getConfig();
        MockMvc mockMvc = createMockMvc();
        mockMvc.perform(put(kafkaConnectProperties.getContextPath()+"/connector-plugins/JdbcSourceConnector/config/validate")
                .content(toJson(config))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
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
     * @return
     */
    protected MockMvc createMockMvc(){
        return webAppContextSetup(webApplicationContext).build();
    }
}
