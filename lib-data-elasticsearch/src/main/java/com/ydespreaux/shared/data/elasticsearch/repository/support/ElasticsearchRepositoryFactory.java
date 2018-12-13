package com.ydespreaux.shared.data.elasticsearch.repository.support;

import com.ydespreaux.shared.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * Factory to create {@link ElasticsearchRepository}
 *
 */
public class ElasticsearchRepositoryFactory extends RepositoryFactorySupport {

	private final ElasticsearchOperations elasticsearchOperations;

	public ElasticsearchRepositoryFactory(ElasticsearchOperations elasticsearchOperations) {

		Assert.notNull(elasticsearchOperations, "ElasticsearchOperations must not be null!");

		this.elasticsearchOperations = elasticsearchOperations;
	}

	@Override
	public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
		return (EntityInformation<T, ID>)this.elasticsearchOperations.getPersistentEntityFor(domainClass);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object getTargetRepository(RepositoryInformation metadata) {
		return getTargetRepositoryViaReflection(metadata, getEntityInformation(metadata.getDomainType()),
				elasticsearchOperations);
	}

	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		if (isQueryDslRepository(metadata.getRepositoryInterface())) {
			throw new IllegalArgumentException("QueryDsl Support has not been implemented yet.");
		}
		if (metadata.getIdType() == String.class) {
			return SimpleElasticsearchRepository.class;
		} else {
			throw new IllegalArgumentException("Unsupported ID type " + metadata.getIdType());
		}
	}

	private static boolean isQueryDslRepository(Class<?> repositoryInterface) {
		return false;
	}

	@Override
	protected Optional<QueryLookupStrategy> getQueryLookupStrategy(Key key,
                                                                   QueryMethodEvaluationContextProvider evaluationContextProvider) {
		return Optional.empty();
	}
}
