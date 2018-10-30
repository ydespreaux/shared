package com.ydespreaux.shared.data.elasticsearch;import com.ydespreaux.shared.data.elasticsearch.mapping.ElasticsearchPersistentEntity;import io.searchbox.core.Search;import org.elasticsearch.common.Nullable;import org.elasticsearch.index.query.QueryBuilder;import java.util.List;/** * define the {@link ElasticsearchOperations} methods. */public interface ElasticsearchOperations extends ElasticsearchAdminOperations {    /**     * Give the {@link ElasticsearchPersistentEntity} for the given {@link Class}.     *     * @param clazz the given {@link Class}.     * @return ElasticsearchPersistentEntity the persitant entity for the given {@link Class} parameter.     */    <T> ElasticsearchPersistentEntity<T> getPersistentEntityFor(Class<T> clazz);    //***************************************    // Index / search operations    //***************************************    /**     * Index the given T entity, for the geiven clazz.     *     * @param entity the given entity.     * @param clazz  the gievn {@link Class}.     * @return T the indexed entity.     */    <T> T index(T entity, Class<T> clazz);    /**     * Bulk index operation for the given {@link List} of entities, and gievn {@link Class}.     *     * @param entities the given entities {@link List}.     * @param clazz    the given {@link Class}.     * @param <T>      the {@link List} of indexed entities.     * @return     */    <T> List<T> bulkIndex(List<T> entities, Class<T> clazz);    /**     * Find an elasticsearch document for the given clazz, and documentId.     *     * @param clazz      the given clazz.     * @param documentId the given documentId.     * @param <T>        the document     * @return the entity for the given documentId or null.     */    <T> T findById(Class<T> clazz, String documentId);    /**     * Delete all the documents for the given clazz     *     * @param clazz the given clazz.     * @param <T>   method generic.     */    <T> void deleteAll(Class<T> clazz);    /**     * Delete all the {@link List} of entities, for the given clazz.     *     * @param entities the {@link List} of entities.     * @param clazz    the given clazz.     * @param <T>      method generic.     */    <T> void deleteAll(List<T> entities, Class<T> clazz);    /**     * delete the document ofr the given entity, and clazz     *     * @param entity the given entity.     * @param clazz  the given clazz.     * @param <T>    method generic.     */    <T> void delete(T entity, Class<T> clazz);    /**     * delete the document with the given documentId and clazz.     *     * @param documentId the given documentId.     * @param clazz      the given clazz.     * @param <T>        method generic.     */    <T> void deleteById(String documentId, Class<T> clazz);    /**     * refresh the elasticsearch index for the given clazz     *     * @param clazz     the given clazz.     * @param <T>method generic.     */    <T> void refresh(Class<T> clazz);    /**     * Prepare the given quey for the given clazz     *     * @param query the given {@link QueryBuilder}.     * @param clazz the given clazz.     * @param <T>   method generic.     * @return a Serach.Builder instance     */    <T> Search.Builder prepareSearch(QueryBuilder query, Class<T> clazz);    /**     * Search with the given {@link Search} search, and given {@link Class} clazz.     *     * @param search the given {@link Search} instance.     * @param clazz  the given clazz.     * @param <T>    generic method.     * @return a {@link List} of the method generic type.     */    <T> List<T> search(Search search, Class<T> clazz);    /**     * Start the {@link ScrolledPage}, with the given scrollTimeInMinutes, size, builder and clazz.     *     * @param scrollTimeInMinutes the scroll time in minutes.     * @param size                the given size.     * @param builder             the given Search.Builder builder.     * @param clazz               the given {@link Class} clazz.     * @param <T>                 method generic.     * @return a {@link ScrolledPage} of T instances.     */    <T> ScrolledPage<T> startScroll(long scrollTimeInMinutes, int size, Search.Builder builder, Class<T> clazz);    /**     * Continue the {@link ScrolledPage} for the given scrollId, scrollTimeInMinutes, and clazz.     *     * @param scrollId            the given scrollId.     * @param scrollTimeInMinutes the scrol time in minutess.     * @param clazz               the given clazz.     * @param <T>                 method generic.     * @return a {@link ScrolledPage} of T instancess.     */    <T> ScrolledPage<T> continueScroll(@Nullable String scrollId, long scrollTimeInMinutes, Class<T> clazz);    /**     * Clear the {@link ScrolledPage} for the given scrollId.     *     * @param scrollId the given scrollId.     */    void clearScroll(String scrollId);}