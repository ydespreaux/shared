package com.ydespreaux.shared.data.elasticsearch.support;import com.ydespreaux.shared.data.elasticsearch.ElasticsearchOperations;import com.ydespreaux.shared.data.elasticsearch.ScrolledPage;import com.ydespreaux.shared.data.elasticsearch.ScrolledPageable;import com.ydespreaux.shared.data.elasticsearch.mapping.ElasticsearchPersistentEntity;import com.ydespreaux.shared.data.elasticsearch.query.NativeSearchQueryBuilder;import lombok.Getter;import org.elasticsearch.index.query.QueryBuilder;import org.elasticsearch.search.sort.SortBuilders;import org.elasticsearch.search.sort.SortOrder;import org.springframework.data.domain.Sort;import org.springframework.util.Assert;import org.springframework.util.StringUtils;import java.util.List;import java.util.Optional;public abstract class AbstractElasticsearchRepository<T, K> implements ElasticsearchRepository<T,K> {    @Getter    private final Class<T> entityClass;    protected final ElasticsearchOperations elasticsearchOperations;    /**     *     * @param entityClass     * @param elasticsearchOperations     */    public AbstractElasticsearchRepository(Class<T> entityClass, ElasticsearchOperations elasticsearchOperations) {        this.entityClass = entityClass;        this.elasticsearchOperations = elasticsearchOperations;        //        ElasticsearchPersistentEntity<T> persistentEntity  = elasticsearchOperations.getPersistentEntityFor(entityClass);        String indexName = persistentEntity.getIndex(null);        if (persistentEntity.createIndex() && !this.elasticsearchOperations.indexExists(indexName)) {            createIndex(indexName, persistentEntity);        }    }    /**     *     * @param indexName     * @param persistentEntity     */    protected void createIndex(String indexName, ElasticsearchPersistentEntity<T> persistentEntity){        if (!this.elasticsearchOperations.indexExists(indexName)) {            if (persistentEntity.isIndexTimeBased()) {                this.elasticsearchOperations.createIndex(indexName);            }else if (StringUtils.hasText(persistentEntity.getIndexPath())){                this.elasticsearchOperations.createIndexWithSettingsAndMapping(indexName, persistentEntity.getIndexPath());            }        }    }    /**     *     * @param id must not be {@literal null}.     * @return     */    @Override    public Optional<T> findById(K id) {        Assert.notNull(id, "id must not be null!");        return Optional.ofNullable(elasticsearchOperations.findById(getEntityClass(), stringIdRepresentation(id)));    }    @Override    public Boolean existsById(K id) {        Assert.notNull(id, "id must not be null!");        return elasticsearchOperations.existsById(getEntityClass(), stringIdRepresentation(id));    }    /**     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the     * entity instance completely.     *     * @param entity must not be {@literal null}.     * @return the saved entity will never be {@literal null}.     */    @Override    public T save(T entity) {        Assert.notNull(entity, "Cannot save 'null' entity.");        return this.elasticsearchOperations.index(entity, getEntityClass());    }    /**     * Saves all given entities.     *     * @param entities must not be {@literal null}.     * @return the saved entities will never be {@literal null}.     * @throws IllegalArgumentException in case the given entity is {@literal null}.     */    @Override    public List<T> save(List<T> entities) {        Assert.notNull(entities, "Cannot insert 'null' as a List.");        Assert.notEmpty(entities, "Cannot insert empty List.");        return this.elasticsearchOperations.bulkIndex(entities, getEntityClass());    }    /**     * Deletes the entity with the given id.     *     * @param id must not be {@literal null}.     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}     */    @Override    public void deleteById(K id) {        Assert.notNull(id, "id must not be null!");        this.elasticsearchOperations.deleteById(stringIdRepresentation(id), getEntityClass());    }    /**     * Deletes a given entity.     *     * @param entity     * @throws IllegalArgumentException in case the given entity is {@literal null}.     */    @Override    public void delete(T entity) {        Assert.notNull(entity, "Cannot delete 'null' entity.");        this.elasticsearchOperations.delete(entity, getEntityClass());    }    /**     * Deletes the given entities.     *     * @param entities     * @throws IllegalArgumentException in case the given {@link Iterable} is {@literal null}.     */    @Override    public void deleteAll(List<T> entities) {        Assert.notNull(entities, "Cannot delete 'null' as a List.");        Assert.notEmpty(entities, "Cannot delete empty List.");        this.elasticsearchOperations.deleteAll(entities, getEntityClass());    }    /**     * Deletes all entities managed by the repository.     */    @Override    public void deleteAll() {        this.elasticsearchOperations.deleteAll(getEntityClass());    }    /**     *     */    @Override    public void refresh() {        this.elasticsearchOperations.refresh(getEntityClass());    }    /**     * @param query     * @return     */    @Override    public List<T> search(QueryBuilder query, Sort sort) {        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder().withQuery(query);        if (sort != null) {            sort.forEach(order -> queryBuilder.withSort(SortBuilders.fieldSort(order.getProperty()).order(order.isAscending() ? SortOrder.ASC : SortOrder.DESC)));        }        return this.elasticsearchOperations.search(queryBuilder.build(), this.getEntityClass());    }    /**     * @param query     * @param pageable     * @return     */    @Override    public ScrolledPage<T> search(QueryBuilder query, ScrolledPageable pageable) {        Assert.notNull(pageable, "pageable must not be null!");        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()                .withPageable(pageable)                .withQuery(query);        return this.elasticsearchOperations.startScroll(pageable.getScrollTimeInMinutes(), queryBuilder.build(), getEntityClass());    }    @Override    public ScrolledPage<T> search(ScrolledPageable pageable) {        Assert.notNull(pageable, "pageable must not be null!");        Assert.notNull(pageable.getScrollId(), "scrollId must not be null!");        return this.elasticsearchOperations.continueScroll(pageable.getScrollId(), pageable.getScrollTimeInMinutes(), getEntityClass());    }    /**     * @param scrollId     */    @Override    public void clearSearch(String scrollId) {        Assert.notNull(scrollId, "scrollId must not be null!");        this.elasticsearchOperations.clearScroll(scrollId);    }    /**     *     * @param id     * @return     */    protected abstract String stringIdRepresentation(K id);}