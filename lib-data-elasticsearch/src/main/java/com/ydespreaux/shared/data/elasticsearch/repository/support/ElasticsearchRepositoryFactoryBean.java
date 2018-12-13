package com.ydespreaux.shared.data.elasticsearch.repository.support;

import com.ydespreaux.shared.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * Spring {@link org.springframework.beans.factory.FactoryBean} implementation to ease container based configuration for
 * XML namespace and JavaConfig.
 *
 */
public class ElasticsearchRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable> extends
        RepositoryFactoryBeanSupport<T, S, ID> {

	private ElasticsearchOperations operations;

	/**
	 * Creates a new {@link ElasticsearchRepositoryFactoryBean} for the given repository interface.
	 * 
	 * @param repositoryInterface must not be {@literal null}.
	 */
	public ElasticsearchRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
		super(repositoryInterface);
	}

	/**
	 * Configures the {@link ElasticsearchOperations} to be used to create Elasticsearch repositories.
	 *
	 * @param operations the operations to set
	 */
	public void setElasticsearchOperations(ElasticsearchOperations operations) {
		Assert.notNull(operations, "ElasticsearchOperations must not be null!");
		setMappingContext(operations.getElasticsearchConverter().getMappingContext());
		this.operations = operations;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		Assert.notNull(operations, "ElasticsearchOperations must be configured!");
	}

	@Override
	protected RepositoryFactorySupport createRepositoryFactory() {
		return new ElasticsearchRepositoryFactory(operations);
	}
}
