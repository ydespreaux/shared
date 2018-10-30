package com.ydespreaux.shared.data.elasticsearch;import com.google.gson.JsonObject;import com.ydespreaux.shared.data.autoconfigure.elasticsearch.JestElasticsearchAutoConfiguration;import com.ydespreaux.shared.data.autoconfigure.elasticsearch.JestElasticsearchDataAutoConfiguration;import com.ydespreaux.shared.data.elasticsearch.ElasticsearchOperations;import io.searchbox.client.JestClient;import io.searchbox.client.JestResult;import io.searchbox.indices.mapping.GetMapping;import io.searchbox.indices.settings.GetSettings;import io.searchbox.indices.template.GetTemplate;import org.junit.Before;import org.junit.Test;import org.junit.runner.RunWith;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.test.annotation.DirtiesContext;import org.springframework.test.context.junit4.SpringRunner;import java.io.IOException;import static org.hamcrest.Matchers.is;import static org.junit.Assert.*;@DirtiesContext@RunWith(SpringRunner.class)@SpringBootTest(classes = {JestElasticsearchAutoConfiguration.class,        JestElasticsearchDataAutoConfiguration.class})public class ITElasticsearchTemplateTest {    private static final String DEFAULT_TEMPLATE_NAME = "junit-template";    private static final String DEFAULT_TEMPLATE_URL = "templates/junit.template";    private static final String UPDATE_TEMPLATE_URL = "templates/junit-update.template";    private static final String INDEX1_NAME = "index1";    private static final String INDEX_BOOK_PATH = "indices/book.index";    private static final String INDEX_BOOK_NAME = "books";    @Autowired    private ElasticsearchOperations operations;    @Autowired    private JestClient client;    @Before    public void initialize() {        this.operations.deleteTemplate(DEFAULT_TEMPLATE_NAME);        this.operations.deleteIndexByName(INDEX1_NAME);        this.operations.deleteIndexByName(INDEX_BOOK_NAME);    }    private JestResult getTemplate(String templateName) throws IOException {        return this.client.execute(new GetTemplate.Builder(templateName).build());    }    @Test    public void createTemplate() throws IOException {        this.operations.createTemplate(DEFAULT_TEMPLATE_NAME, DEFAULT_TEMPLATE_URL, false);        JestResult response = getTemplate(DEFAULT_TEMPLATE_NAME);        assertTrue(response.isSucceeded());        JsonObject json = response.getJsonObject();        JsonObject template = json.getAsJsonObject("junit-template");        assertNotNull(template);        assertEquals("junit-*", template.get("index_patterns").getAsString());        JsonObject indexSettings = template.getAsJsonObject("settings").getAsJsonObject("index");        assertEquals("1s", indexSettings.get("refresh_interval").getAsString());        assertEquals("1", indexSettings.get("number_of_shards").getAsString());        assertEquals("1", indexSettings.get("number_of_replicas").getAsString());        assertEquals("fs", indexSettings.getAsJsonObject("store").get("type").getAsString());        assertNotNull(template.getAsJsonObject("mappings"));        assertNotNull(template.getAsJsonObject("aliases").get("junit-alias"));    }    @Test    public void updateTemplate_whenTemplateExists_withCreateOnly() throws IOException {        // Create template        this.operations.createTemplate(DEFAULT_TEMPLATE_NAME, DEFAULT_TEMPLATE_URL, false);        JestResult response = getTemplate(DEFAULT_TEMPLATE_NAME);        assertTrue(response.isSucceeded());        JsonObject json = response.getJsonObject();        JsonObject template = json.getAsJsonObject("junit-template");        assertNotNull(template);        assertEquals("junit-*", template.get("index_patterns").getAsString());        JsonObject indexSettings = template.getAsJsonObject("settings").getAsJsonObject("index");        assertEquals("1", indexSettings.get("number_of_shards").getAsString());        // Update template        this.operations.createTemplate(DEFAULT_TEMPLATE_NAME, UPDATE_TEMPLATE_URL, true);        response = getTemplate(DEFAULT_TEMPLATE_NAME);        assertTrue(response.isSucceeded());        json = response.getJsonObject();        template = json.getAsJsonObject("junit-template");        assertNotNull(template);        assertEquals("junit-*", template.get("index_patterns").getAsString());        indexSettings = template.getAsJsonObject("settings").getAsJsonObject("index");        assertEquals("1", indexSettings.get("number_of_shards").getAsString());    }    @Test    public void updateTemplate_whenTemplateExists() throws IOException {        // Create template        this.operations.createTemplate(DEFAULT_TEMPLATE_NAME, DEFAULT_TEMPLATE_URL, false);        JestResult response = getTemplate(DEFAULT_TEMPLATE_NAME);        assertTrue(response.isSucceeded());        JsonObject json = response.getJsonObject();        JsonObject template = json.getAsJsonObject("junit-template");        assertNotNull(template);        assertEquals("junit-*", template.get("index_patterns").getAsString());        JsonObject indexSettings = template.getAsJsonObject("settings").getAsJsonObject("index");        assertEquals("1", indexSettings.get("number_of_shards").getAsString());        // Update template        this.operations.createTemplate(DEFAULT_TEMPLATE_NAME, UPDATE_TEMPLATE_URL, false);        response = getTemplate(DEFAULT_TEMPLATE_NAME);        assertTrue(response.isSucceeded());        json = response.getJsonObject();        template = json.getAsJsonObject("junit-template");        assertNotNull(template);        assertEquals("junit-*", template.get("index_patterns").getAsString());        indexSettings = template.getAsJsonObject("settings").getAsJsonObject("index");        assertEquals("2", indexSettings.get("number_of_shards").getAsString());    }    @Test    public void templateExists_withTemplateDefined(){        this.operations.createTemplate(DEFAULT_TEMPLATE_NAME, DEFAULT_TEMPLATE_URL, false);        assertTrue(this.operations.templateExists(DEFAULT_TEMPLATE_NAME));    }    @Test    public void templateExists_withTemplateUndefined(){        assertFalse(this.operations.templateExists("UNKNOWN"));    }    @Test    public void createIndex(){        assertThat(this.operations.createIndex(INDEX1_NAME), is(true));        assertThat(this.operations.indexExists(INDEX1_NAME), is(true));    }    @Test    public void createIndexWithSettingsAndMapping() throws Exception{        assertThat(this.operations.createIndexWithSettingsAndMapping(INDEX_BOOK_NAME, INDEX_BOOK_PATH), is(true));        assertThat(this.operations.indexExists(INDEX_BOOK_NAME), is(true));        GetMapping getMapping = new GetMapping.Builder().build();        JestResult result = client.execute(getMapping);        assertTrue(result.getErrorMessage(), result.isSucceeded());        JsonObject mapping = result.getJsonObject().getAsJsonObject(INDEX_BOOK_NAME);        assertNotNull("GetMapping response JSON should include the index " + INDEX_BOOK_NAME, mapping);        JsonObject indexMapping = mapping.getAsJsonObject("mappings");        assertNotNull(indexMapping.get("book"));        GetSettings getSettings = new GetSettings.Builder().build();        result = client.execute(getSettings);        assertTrue(result.isSucceeded());        System.out.println("result.getJsonString() = " + result.getJsonString());        JsonObject settings = result.getJsonObject();        assertNotNull(settings.getAsJsonObject(INDEX_BOOK_NAME));        assertNotNull(settings.getAsJsonObject(INDEX_BOOK_NAME).getAsJsonObject("settings"));        JsonObject indexSettings = settings.getAsJsonObject(INDEX_BOOK_NAME).getAsJsonObject("settings").getAsJsonObject("index");        assertEquals("1s", indexSettings.get("refresh_interval").getAsString());        assertEquals("1", indexSettings.get("number_of_shards").getAsString());        assertEquals("1", indexSettings.get("number_of_replicas").getAsString());        assertEquals("fs", indexSettings.getAsJsonObject("store").get("type").getAsString());    }}