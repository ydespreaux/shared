package com.ydespreaux.shared.data.elasticsearch.core;

import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
public class IndexTimeBasedParameter<T> {

    /**
     * Index pattern
     */
    private String indexPattern;
    /**
     * Current document
     */
    private T document;
    /**
     * Date time of the current event
     */
    private Date timeEvent;

    /**
     *
     * @param indexPattern
     * @param timeEvent
     * @param document
     */
    private IndexTimeBasedParameter(String indexPattern, Date timeEvent, T document){
        Objects.requireNonNull(indexPattern, "indexPattern paramater canno't be null !");
        this.indexPattern = indexPattern;
        this.timeEvent = timeEvent;
        this.document = document;
    }

    /**
     *
     * @param indexPattern
     * @param timeEvent
     * @param document
     * @param <T>
     * @return
     */
    public static <T> IndexTimeBasedParameter<T> of(String indexPattern, Date timeEvent, T document) {
        return new IndexTimeBasedParameter<>(indexPattern, timeEvent, document);
    }

    /**
     *
     * @param indexPattern
     * @param timeEvent
     * @param <T>
     * @return
     */
    public static <T> IndexTimeBasedParameter<T> of(String indexPattern, Date timeEvent) {
        return new IndexTimeBasedParameter<>(indexPattern, timeEvent, null);
    }

    /**
     *
     * @param indexPattern
     * @param document
     * @param <T>
     * @return
     */
    public static <T> IndexTimeBasedParameter<T> of(String indexPattern, T document) {
        return new IndexTimeBasedParameter<>(indexPattern, null, document);
    }

    /**
     *
     * @return
     */
    public String generateIndexWithTimeEvent(){
        Objects.requireNonNull(this.timeEvent, "timeEvent attribut canno't be null !!");
        return new SimpleDateFormat(this.indexPattern).format(this.timeEvent);
    }
}
