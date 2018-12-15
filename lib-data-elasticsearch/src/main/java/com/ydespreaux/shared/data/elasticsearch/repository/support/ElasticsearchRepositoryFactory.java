/*
 * Copyright 2013-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ydespreaux.shared.data.elasticsearch.repository.support;

import com.ydespreaux.shared.data.elasticsearch.core.ElasticsearchOperations;
import com.ydespreaux.shared.data.elasticsearch.repository.ElasticsearchRepository;
import com.ydespreaux.shared.data.elasticsearch.repository.SimpleElasticsearchRepository;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.util.Assert;

/**
 * Factory to create {@link ElasticsearchRepository}
 *
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Ryan Henszey
 * @author Gad Akuka
 * @author Mark Paluch
 * @author Christoph Strobl
 */
public class ElasticsearchRepositoryFactory extends RepositoryFactorySupport {

	private final ElasticsearchOperations elasticsearchOperations;

	public ElasticsearchRepositoryFactory(ElasticsearchOperations elasticsearchOperations) {

		Assert.notNull(elasticsearchOperations, "ElasticsearchOperations must not be null!");

		this.elasticsearchOperations = elasticsearchOperations;
	}

	@Override
	public <T, ID> ElasticsearchEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
		return (ElasticsearchEntityInformation<T, ID>) this.elasticsearchOperations.getElasticsearchConverter().getRequiredPersistentEntity(domainClass);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object getTargetRepository(RepositoryInformation metadata) {
		return getTargetRepositoryViaReflection(metadata, getEntityInformation(metadata.getDomainType()),
				elasticsearchOperations);
	}

	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		if (metadata.getIdType() == String.class) {
			return SimpleElasticsearchRepository.class;
		} else {
			throw new IllegalArgumentException("Unsupported ID type " + metadata.getIdType());
		}
	}

}
