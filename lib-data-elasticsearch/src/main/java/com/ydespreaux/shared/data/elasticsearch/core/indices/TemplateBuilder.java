package com.ydespreaux.shared.data.elasticsearch.core.indices;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class TemplateBuilder extends IndiceBuilder<PutIndexTemplateRequest, TemplateBuilder> {

    private static List<String> attributeNames = Arrays.asList(INDEX_PATTERNS_CONFIG,
            SETTINGS_CONFIG,
            ALIASES_CONFIG,
            MAPPINGS_CONFIG,
            ORDER_CONFIG);

    /**
     *
     * @return
     * @throws IOException
     */
    @Override
    public PutIndexTemplateRequest build() {
        PutIndexTemplateRequest templateRequest = new PutIndexTemplateRequest()
                .name(this.getName());
        Map<String, JsonElement> settings = this.buildJsonElement(this.getSources());
        // Index patterns
        List<String> indexPatterns = new ArrayList<>();
        JsonElement patternsElement = settings.get(INDEX_PATTERNS_CONFIG);
        if (patternsElement.isJsonArray()) {
            patternsElement.getAsJsonArray().forEach(pattern -> indexPatterns.add(pattern.getAsString()));
        }else {
            indexPatterns.add(patternsElement.getAsString());
        }
        templateRequest.patterns(indexPatterns);
        // Aliases
        templateRequest.aliases(xContentBuilder(settings.get(ALIASES_CONFIG)));
        // Settings
        templateRequest.settings(settings.get(SETTINGS_CONFIG).toString(), XContentType.JSON);
        // Mappings
        JsonObject mappingsElement = settings.get(MAPPINGS_CONFIG).getAsJsonObject();
        mappingsElement.entrySet().forEach(entry -> templateRequest.mapping(entry.getKey(), xContentBuilder(entry.getValue())));
        // Order
        if (settings.containsKey(ORDER_CONFIG)) {
            templateRequest.order(settings.get(ORDER_CONFIG).getAsInt());
        }
        return templateRequest;
    }

    /**
     * @return
     */
    @Override
    protected List<String> getAttributeNames() {
        return attributeNames;
    }

}
