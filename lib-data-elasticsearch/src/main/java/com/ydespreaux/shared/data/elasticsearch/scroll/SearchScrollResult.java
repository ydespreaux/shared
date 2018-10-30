package com.ydespreaux.shared.data.elasticsearch.scroll;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.searchbox.client.JestResult;
import io.searchbox.cloning.CloneUtils;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.RootAggregation;
import lombok.*;

import java.util.*;

/**
 * Search scroll result of elasticsearch action.
 *
 * @author xpax624
 */
public class SearchScrollResult extends JestResult {

    private static final String EXPLANATION_KEY = "_explanation";
    private static final String HIGHLIGHT_KEY = "highlight";
    private static final String FIELDS_KEY = "fields";
    private static final String SORT_KEY = "sort";
    private static final String INDEX_KEY = "_index";
    private static final String TYPE_KEY = "_type";
    private static final String SCORE_KEY = "_score";

    private static final String[] PATH_TO_TOTAL = "hits/total".split("/");
    private static final String[] PATH_TO_MAX_SCORE = "hits/max_score".split("/");

    /**
     * Construct instance with the given {@link JestResult}.
     *
     * @param source the given {@link JestResult}.
     */
    public SearchScrollResult(JestResult source) {
        super(source);
    }

    public <T> Hit<T, Void> getFirstHit(Class<T> sourceType) {
        return getFirstHit(sourceType, Void.class);
    }

    /**
     * Give the first hit of
     * @param sourceType
     * @param explanationType
     * @param <T>
     * @param <K>
     * @return
     */
    public <T, K> Hit<T, K> getFirstHit(Class<T> sourceType, Class<K> explanationType) {
        Hit<T, K> hit = null;

        List<Hit<T, K>> hits = getHits(sourceType, explanationType, true);
        if (!hits.isEmpty()) {
            hit = hits.get(0);
        }

        return hit;
    }

    public <T> List<Hit<T, Void>> getHits(Class<T> sourceType) {
        return getHits(sourceType, true);
    }

    public <T> List<Hit<T, Void>> getHits(Class<T> sourceType, boolean addEsMetadataFields) {
        return getHits(sourceType, Void.class, addEsMetadataFields);
    }

    public <T, K> List<Hit<T, K>> getHits(Class<T> sourceType, Class<K> explanationType) {
        return getHits(sourceType, explanationType, false, true);
    }

    public <T, K> List<Hit<T, K>> getHits(Class<T> sourceType, Class<K> explanationType, boolean addEsMetadataFields) {
        return getHits(sourceType, explanationType, false, addEsMetadataFields);
    }

    protected <T, K> List<Hit<T, K>> getHits(Class<T> sourceType, Class<K> explanationType, boolean returnSingle, boolean addEsMetadataFields) {
        final List<Hit<T, K>> sourceList = new ArrayList<>();
        final String[] keys = getKeys();
        if (jsonObject != null && keys != null) {
            String sourceKey = keys[keys.length - 1];
            JsonElement obj = jsonObject.get(keys[0]);
            for (int i = 1; i < keys.length - 1; i++) {
                obj = ((JsonObject) obj).get(keys[i]);
            }

            if (obj.isJsonObject()) {
                sourceList.add(extractHit(sourceType, explanationType, obj, sourceKey, addEsMetadataFields));
            } else if (obj.isJsonArray()) {
                for (JsonElement hitElement : obj.getAsJsonArray()) {
                    sourceList.add(extractHit(sourceType, explanationType, hitElement, sourceKey, addEsMetadataFields));
                    if (returnSingle) {
                        break;
                    }
                }
            }
        }
        return sourceList;
    }

    protected <T, K> Hit<T, K> extractHit(Class<T> sourceType, Class<K> explanationType, JsonElement hitElement, String sourceKey, boolean addEsMetadataFields) {
        Hit<T, K> hit = null;

        if (hitElement.isJsonObject()) {
            JsonObject hitObject = hitElement.getAsJsonObject();
            Double score = null;
            if (hitObject.has(SCORE_KEY) && !hitObject.get(SCORE_KEY).isJsonNull()) {
                score = hitObject.get(SCORE_KEY).getAsDouble();
            }
            JsonElement explanation = hitObject.get(EXPLANATION_KEY);
            JsonObject source = hitObject.getAsJsonObject(sourceKey);
            if (source == null) {
                source = new JsonObject();
            }
            if (addEsMetadataFields) {
                final JsonObject clonedSource = (JsonObject) CloneUtils.deepClone(source);
                META_FIELDS.forEach(metaField -> {
                    JsonElement metaElement = hitObject.get(metaField.esFieldName);
                    if (metaElement != null) {
                        clonedSource.add(metaField.internalFieldName, metaElement);
                    }
                });
                source = clonedSource;
            }

            hit = Hit.<T, K>builder()
                    .source(createSourceObject(source, sourceType))
                    .explanation(explanation == null ? null : createSourceObject(explanation, explanationType))
                    .highlight(extractJsonObject(hitObject.getAsJsonObject(HIGHLIGHT_KEY)))
                    .fields(extractJsonObject(hitObject.getAsJsonObject(FIELDS_KEY)))
                    .sort(extractSort(hitObject.getAsJsonArray(SORT_KEY)))
                    .index(hitObject.get(INDEX_KEY).getAsString())
                    .type(hitObject.get(TYPE_KEY).getAsString())
                    .score(score)
                    .build();
        }

        return hit;
    }

    protected List<String> extractSort(JsonArray sort) {
        if (sort == null) {
            return null;
        }

        List<String> retval = new ArrayList<>(sort.size());
        for (JsonElement sortValue : sort) {
            retval.add(sortValue.isJsonNull() ? "" : sortValue.getAsString());
        }
        return retval;
    }

    protected Map<String, List<String>> extractJsonObject(JsonObject highlight) {
        Map<String, List<String>> retval = null;

        if (highlight != null) {
            Set<Map.Entry<String, JsonElement>> highlightSet = highlight.entrySet();
            retval = new HashMap<>(highlightSet.size());

            for (Map.Entry<String, JsonElement> entry : highlightSet) {
                List<String> fragments = new ArrayList<>();
                for (JsonElement element : entry.getValue().getAsJsonArray()) {
                    fragments.add(element.getAsString());
                }
                retval.put(entry.getKey(), fragments);
            }
        }

        return retval;
    }

    public Integer getTotal() {
        Integer total = null;
        JsonElement obj = getPath(PATH_TO_TOTAL);
        if (obj != null) {
            total = obj.getAsInt();
        }
        return total;
    }

    public Float getMaxScore() {
        Float maxScore = null;
        JsonElement obj = getPath(PATH_TO_MAX_SCORE);
        if (obj != null) maxScore = obj.getAsFloat();
        return maxScore;
    }

    protected JsonElement getPath(String[] path) {
        JsonElement retval = null;
        if (jsonObject != null) {
            JsonElement obj = jsonObject;
            for (String component : path) {
                if (obj == null) {
                    break;
                }
                obj = ((JsonObject) obj).get(component);
            }
            retval = obj;
        }
        return retval;
    }

    public String getScrollId() {
        return jsonObject.get("_scroll_id").getAsString();
    }

    public MetricAggregation getAggregations() {
        final String rootAggrgationName = "aggs";
        if (jsonObject == null) return new RootAggregation(rootAggrgationName, new JsonObject());
        if (jsonObject.has("aggregations"))
            return new RootAggregation(rootAggrgationName, jsonObject.getAsJsonObject("aggregations"));
        if (jsonObject.has("aggs")) return new RootAggregation(rootAggrgationName, jsonObject.getAsJsonObject("aggs"));

        return new RootAggregation(rootAggrgationName, new JsonObject());
    }

    /**
     * Immutable class representing a search hit.
     *
     * @param <T> type of source
     * @param <K> type of explanation
     * @author cihat keser
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Hit<T, K> {

        private T source;
        private K explanation;
        private Map<String, List<String>> highlight;
        private Map<String, List<String>> fields;
        private List<String> sort;
        private String index;
        private String type;
        private Double score;


        @Override
        public int hashCode() {
            return Objects.hash(
                    source,
                    explanation,
                    highlight,
                    sort,
                    index,
                    type);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }

            Hit rhs = (Hit) obj;
            return Objects.equals(source, rhs.source)
                    && Objects.equals(explanation, rhs.explanation)
                    && Objects.equals(highlight, rhs.highlight)
                    && Objects.equals(sort, rhs.sort)
                    && Objects.equals(index, rhs.index)
                    && Objects.equals(type, rhs.type);
        }
    }

}