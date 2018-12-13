package com.ydespreaux.shared.data.elasticsearch.core.mapping;

import org.springframework.data.mapping.PersistentEntity;

/**
 * @param <T>
 * @author xpax624
 * @since 1.1.0
 */
public interface ElasticsearchPersistentEntity<T> extends PersistentEntity<T, ElasticsearchPersistentProperty> {

    /**
     * Retourne le nom de l'alias ou l'ndex de l'entité courante.
     *
     * @return
     */
    String getAliasOrIndexName();

    /**
     * Retourne le type de document de l'entité courante.
     *
     * @return
     */
    String getTypeName();

    /**
     * Retourne le nom de l'index de l'entité courante à indexer.
     *
     * @param source
     * @return
     */
    String getIndex(T source);

    /**
     * @param entity
     * @param id
     */
    void setPersistentEntityId(T entity, String id);
    /**
     * @param entity
     * @param version
     */
    void setPersistentEntityVersion(T entity, Long version);

    /**
     *
     * @param entity
     * @return
     */
    String getPersistentEntityId(T entity);

    /**
     *
     * @param source
     * @return
     */
    Long getPersistentEntityVersion(T source);

    /**
     *
     * @return
     */
    Boolean createIndex();

    /**
     *
     * @return
     */
    Boolean isIndexTimeBased();

    /**
     *
     * @return
     */
    String getIndexPath();

}
