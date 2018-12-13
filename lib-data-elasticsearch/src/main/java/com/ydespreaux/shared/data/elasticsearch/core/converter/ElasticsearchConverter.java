/*
 * Copyright 2013 the original author or authors.
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
package com.ydespreaux.shared.data.elasticsearch.core.converter;

import com.ydespreaux.shared.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.ydespreaux.shared.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.data.mapping.context.MappingContext;

/**
 * ElasticsearchConverter
 */
public interface ElasticsearchConverter {

    /**
     * Give the required {@link ElasticsearchPersistentEntity} for the given clazz parameter.
     * @param clazz the given clazz.
     * @return an {@link ElasticsearchPersistentEntity} of T.
     */
    <T> ElasticsearchPersistentEntity<T> getRequiredPersistentEntity(Class<T> clazz);

    /**
     * Returns the underlying {@link org.springframework.data.mapping.context.MappingContext} used by the converter.
     *
     * @return never {@literal null}
     */
    MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> getMappingContext();
}
