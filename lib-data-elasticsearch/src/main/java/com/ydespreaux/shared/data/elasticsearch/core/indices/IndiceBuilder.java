package com.ydespreaux.shared.data.elasticsearch.core.indices;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.IndicesRequest;
import org.elasticsearch.common.xcontent.*;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@Getter
public abstract class IndiceBuilder<T extends IndicesRequest, SELF extends IndiceBuilder> {

    public static final String INDEX_PATTERNS_CONFIG = "index_patterns";
    public static final String SETTINGS_CONFIG = "settings";
    public static final String ALIASES_CONFIG = "aliases";
    public static final String MAPPINGS_CONFIG = "mappings";
    public static final String ORDER_CONFIG = "order";

    private List<Resource> sources;
    private String name;

    /**
     *
     * @param name
     * @return
     */
    public SELF name(String name) {
        this.name = name;
        return (SELF)this;
    }

    /**
     *
     * @param source
     * @return
     */
    public SELF source(Resource source) {
        this.sources = Collections.singletonList(source);
        return (SELF)this;
    }

    /**
     *
     * @param sources
     * @return
     */
    public SELF sources(List<Resource> sources) {
        this.sources = sources;
        return (SELF)this;
    }

    /**
     *
     * @return
     */
    public abstract T build();

    /**
     *
     * @return
     */
    protected abstract List<String> getAttributeNames();

    /**
     *
     * @param element
     * @return
     * @throws IOException
     */
    protected XContentBuilder xContentBuilder(JsonElement element) {
        try (XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, LoggingDeprecationHandler.INSTANCE, element.toString())) {
            return jsonBuilder().copyCurrentStructure(parser);
        } catch (IOException e) {
            throw new ElasticsearchException("Invalid element: ", e);
        }
    }

    /**
     *
     * @param scripts
     * @return
     * @throws IOException
     */
    public Map<String, JsonElement> buildJsonElement(List<Resource> scripts) {
        if (scripts.isEmpty()) {
            return null;
        }
        final Map<String, JsonElement> settings = new LinkedHashMap<>();
        for (Resource script : scripts) {
            settings.putAll(buildJsonElement(script));
        }
        return settings;
    }

    /**
     *
     * @param rootObject
     * @return
     */
    protected Map<String, JsonElement> buildJsonElement(JsonObject rootObject) {
        Map<String, JsonElement> elements = new HashMap<>();
        this.getAttributeNames().forEach(attribute -> {
            if (rootObject.has(attribute)) {
                elements.put(attribute, rootObject.get(attribute));
            }
        });
        return elements;
    }


    /**
     *
     * @param script
     * @return
     */
    protected Map<String, JsonElement> buildJsonElement(Resource script) {
        String data = readInputStream(script);
        return buildJsonElement(new JsonParser().parse(data).getAsJsonObject());
    }

    /**
     * @param source
     * @return
     */
    protected String readInputStream(InputStreamSource source) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(source.getInputStream()))){
            String line;
            String lineSeparator = System.getProperty("line.separator");
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append(lineSeparator);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            throw new ElasticsearchException(e);
        }
    }
}
