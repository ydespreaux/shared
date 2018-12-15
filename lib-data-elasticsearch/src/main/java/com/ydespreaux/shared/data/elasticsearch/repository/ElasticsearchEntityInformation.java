package com.ydespreaux.shared.data.elasticsearch.repository;

import org.springframework.data.repository.core.EntityInformation;

/**
 * @param <T>
 * @param <ID>
 */
public interface ElasticsearchEntityInformation<T, ID> extends EntityInformation<T, ID> {

	String getIdAttribute();

	String getIndexName();

	String getType();

	Long getVersion(T entity);

	String getParentId(T entity);
}
