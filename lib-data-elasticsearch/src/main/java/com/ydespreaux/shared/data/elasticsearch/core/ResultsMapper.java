/*
 * Copyright 2013-2018 the original author or authors.
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
package com.ydespreaux.shared.data.elasticsearch.core;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;

/**
 * ResultsMapper
 *
 */
public interface ResultsMapper extends SearchResultMapper, GetResultMapper, MultiGetResultMapper {

	EntityMapper getEntityMapper();

	/**
	 *
	 * @param values
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	<T> T mapEntity(Collection<DocumentField> values, Class<T> clazz);

	@Nullable
	default <T> T mapEntity(String source, Class<T> clazz) {

		if (StringUtils.isEmpty(source)) {
			return null;
		}
		return getEntityMapper().mapToObject(source, clazz);
	}

	/**
	 * Map a single {@link GetResult} to an instance of the given type.
	 *
	 * @param getResult must not be {@literal null}.
	 * @param type must not be {@literal null}.
	 * @param <T>
	 * @return can be {@literal null} if the {@link GetResult#isSourceEmpty() is empty}.
	 * @since 4.0
	 */
	<T> T mapEntity(GetResult getResult, Class<T> type);

	/**
	 * Map a single {@link SearchHit} to an instance of the given type.
	 *
	 * @param searchHit must not be {@literal null}.
	 * @param type must not be {@literal null}.
	 * @param <T>
	 * @return can be {@literal null} if the {@link SearchHit} does not have {@link SearchHit#hasSource() a source}.
	 * @since 4.0
	 */
	<T> T mapEntity(SearchHit searchHit, Class<T> type);

	/**
	 *
	 * @param searchHits
	 * @param type
	 * @param <T>
	 * @return
	 */
	<T> List<T> mapEntity(SearchHits searchHits, Class<T> type);

	/**
	 *
	 * @param response
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	<T> T mapResult(GetResponse response, Class<T> clazz);
}
