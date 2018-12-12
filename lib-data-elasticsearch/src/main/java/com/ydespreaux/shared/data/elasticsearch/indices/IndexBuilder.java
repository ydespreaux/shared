package com.ydespreaux.shared.data.elasticsearch.indices;import lombok.extern.slf4j.Slf4j;import org.elasticsearch.ElasticsearchException;import org.elasticsearch.common.xcontent.*;import org.springframework.core.io.InputStreamSource;import org.springframework.core.io.Resource;import java.io.BufferedReader;import java.io.IOException;import java.io.InputStreamReader;import java.util.LinkedHashMap;import java.util.List;import java.util.Map;import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;/** * */@Slf4jpublic class IndexBuilder {    /**     * @param scripts     * @return     * @throws IOException     */    public XContentBuilder buildTemplate(List<Resource> scripts) throws IOException {        if (scripts.isEmpty()) {            return null;        }        final Map<String, Object> settings = new LinkedHashMap<>();        for (Resource script : scripts) {            settings.putAll(buildSettings(script));        }        XContentBuilder xContentBuilder = jsonBuilder().startObject();        for (Map.Entry<String,Object> entry : settings.entrySet()) {            xContentBuilder.field(entry.getKey(), entry.getValue());        }        return xContentBuilder.endObject();    }    /**     *     * @return     * @throws IOException     */    private Map<String, Object> buildSettings(Resource script) throws IOException {        String data = readInputStream(script);        try (XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, LoggingDeprecationHandler.INSTANCE, data)) {            return parser.mapOrdered();        }    }    /**     * @param source     * @return     */    private String readInputStream(InputStreamSource source) {        StringBuilder stringBuilder = new StringBuilder();        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(source.getInputStream()))){            String line;            String lineSeparator = System.getProperty("line.separator");            while ((line = bufferedReader.readLine()) != null) {                stringBuilder.append(line).append(lineSeparator);            }            return stringBuilder.toString();        } catch (IOException e) {            throw new ElasticsearchException(e);        }    }}