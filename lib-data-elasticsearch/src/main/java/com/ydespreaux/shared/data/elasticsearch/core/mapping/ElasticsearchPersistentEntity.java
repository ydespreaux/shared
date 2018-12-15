package com.ydespreaux.shared.data.elasticsearch.core.mapping;

import com.ydespreaux.shared.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.lang.Nullable;

/**
 * @param <T>
 * @author xpax624
 * @since 1.1.0
 */
public interface ElasticsearchPersistentEntity<T> extends PersistentEntity<T, ElasticsearchPersistentProperty>, ElasticsearchEntityInformation<T, String> {

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
    String getIndexName(T source);

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

    /**
     *
     * @return
     */
    ElasticsearchPersistentProperty getParentIdProperty();

    /**
     * Returns the parent Id. Can be {@literal null}.
     *
     * @return can be {@literal null}.
     */
    @Nullable
    Object getParentId(T source);

    /**
     *
     * @return
     */
    boolean hasParent();

    /**
     *
     * @return
     */
    boolean hasScoreProperty();

    /**
     * Returns the score property of the {@link ElasticsearchPersistentEntity}. Can be {@literal null} in case no score
     * property is available on the entity.
     *
     * @return the score {@link ElasticsearchPersistentProperty} of the {@link PersistentEntity} or {@literal null} if not
     *         defined.
     * @since 3.1
     */
    @Nullable
    ElasticsearchPersistentProperty getScoreProperty();

    /**
     *
     * @param result
     * @param score
     */
    void setPersistentEntityScore(T result, float score);
}
