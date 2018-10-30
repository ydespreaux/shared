package com.ydespreaux.shared.data.elasticsearch.mapping;

import java.util.Optional;

/**
 * @param <T>
 * @author xpax624
 * @since 1.1.0
 */
public interface ElasticsearchPersistentEntity<T> {

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
    String getType();

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
    void setPersistentEntityVersion(T entity, Integer version);

    /**
     *
     * @param entity
     * @return
     */
    Optional<String> getPersistentEntityId(T entity);

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
