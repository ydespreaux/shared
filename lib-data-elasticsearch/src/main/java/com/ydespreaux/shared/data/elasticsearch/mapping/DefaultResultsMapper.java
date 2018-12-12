/*
 * Copyright 2014 the original author or authors.
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
package com.ydespreaux.shared.data.elasticsearch.mapping;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.ydespreaux.shared.data.elasticsearch.ScrolledPage;
import com.ydespreaux.shared.data.elasticsearch.scroll.ScrolledPageResult;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class DefaultResultsMapper implements ResultsMapper {

	private final EntityMapper entityMapper;
	private final ElasticsearchConverter converter;

	public DefaultResultsMapper(final EntityMapper entityMapper, final ElasticsearchConverter converter) {
		Assert.notNull(entityMapper, "EntityMapper must not be null!");
		this.entityMapper = entityMapper;
		this.converter = converter;
	}

	@Override
	public EntityMapper getEntityMapper() {
		return this.entityMapper;
	}

	@Override
	public <T> T mapEntity(Collection<DocumentField> values, Class<T> clazz) {
		return mapEntity(buildJSONFromFields(values), clazz);
	}

	/**
	 * Map a single {@link GetResult} to an instance of the given type.
	 *
	 * @param response must not be {@literal null}.
	 * @param type      must not be {@literal null}.
	 * @return can be {@literal null} if the {@link GetResult#isSourceEmpty() is empty}.
	 * @since 4.0
	 */
	@Override
	public <T> T mapEntity(GetResult response, Class<T> type) {
		if (response.isSourceEmpty()) {
			return null;
		}
		String sourceString = response.sourceAsString();
		T result = mapEntity(sourceString, type);
		if (result != null) {
			setPersistentEntityId(result, response.getId(), type);
			setPersistentEntityVersion(result, response.getVersion(), type);
		}
		return result;
	}

	/**
	 *
	 * @param response
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	@Override
	public <T> T mapResult(GetResponse response, Class<T> clazz) {
		T result = mapEntity(response.getSourceAsString(), clazz);
		if (result != null) {
			setPersistentEntityId(result, response.getId(), clazz);
			setPersistentEntityVersion(result, response.getVersion(), clazz);
		}
		return result;
	}

	/**
	 * Map a single {@link SearchHit} to an instance of the given type.
	 *
	 * @param searchHit must not be {@literal null}.
	 * @param type      must not be {@literal null}.
	 * @return can be {@literal null} if the {@link SearchHit} does not have {@link SearchHit#hasSource() a source}.
	 * @since 4.0
	 */
	@Override
	public <T> T mapEntity(SearchHit searchHit, Class<T> type) {
		if (!searchHit.hasSource()) {
			return null;
		}
		String sourceString = searchHit.getSourceAsString();
		T entity = mapEntity(sourceString, type);
		if (entity != null) {
			setPersistentEntityId(entity, searchHit.getId(), type);
			setPersistentEntityVersion(entity, searchHit.getVersion(), type);
		}
		return entity;
	}

	/**
	 *
	 * @param searchHits
	 * @param type
	 * @param <T>
	 * @return
	 */
	@Override
	public <T> List<T> mapEntity(SearchHits searchHits, Class<T> type) {
		List<T> results = new ArrayList<>();
		searchHits.forEach(hit -> results.add(mapEntity(hit, type)));
		return results.stream().filter(Objects::nonNull).collect(Collectors.toList());
	}

	/**
	 *
	 * @param response
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	@Override
	public <T> ScrolledPage<T> mapResults(SearchResponse response, Class<T> clazz) {
		long totalHits = response.getHits().getTotalHits();
		float maxScore = response.getHits().getMaxScore();
		List<T> results = new ArrayList<>();
		for (SearchHit hit : response.getHits()) {
			if (hit != null) {
				T result = null;
				if (!StringUtils.isEmpty(hit.getSourceAsString())) {
					result = this.mapEntity(hit.getSourceAsString(), clazz);
				} else {
					result = this.mapEntity(hit.getFields().values(), clazz);
				}
				setPersistentEntityId(result, hit.getId(), clazz);
				setPersistentEntityVersion(result, hit.getVersion(), clazz);
				results.add(result);
			}
		}
		return ScrolledPageResult.of(results, totalHits, response.getScrollId());
	}

	/**
	 *
	 * @param responses
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	@Override
	public <T> LinkedList<T> mapResults(MultiGetResponse responses, Class<T> clazz) {
		LinkedList<T> list = new LinkedList<>();
		for (MultiGetItemResponse response : responses.getResponses()) {
			if (!response.isFailed() && response.getResponse().isExists()) {
				list.add(mapResult(response.getResponse(), clazz));
			}
		}
		return list;
	}

	/**
	 *
	 * @param values
	 * @return
	 */
	private String buildJSONFromFields(Collection<DocumentField> values) {
		JsonFactory nodeFactory = new JsonFactory();
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			JsonGenerator generator = nodeFactory.createGenerator(stream, JsonEncoding.UTF8);
			generator.writeStartObject();
			for (DocumentField value : values) {
				if (value.getValues().size() > 1) {
					generator.writeArrayFieldStart(value.getName());
					for (Object val : value.getValues()) {
						generator.writeObject(val);
					}
					generator.writeEndArray();
				} else {
					generator.writeObjectField(value.getName(), value.getValue());
				}
			}
			generator.writeEndObject();
			generator.flush();
			return new String(stream.toByteArray(), Charset.forName("UTF-8"));
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 *
	 * @param result
	 * @param version
	 * @param clazz
	 * @param <T>
	 */
	private <T> void setPersistentEntityVersion(T result, long version, Class<T> clazz) {
		this.converter.getRequiredPersistentEntity(clazz).setPersistentEntityVersion(result, version);
	}

	/**
	 *
	 * @param result
	 * @param id
	 * @param clazz
	 * @param <T>
	 */
	private <T> void setPersistentEntityId(T result, String id, Class<T> clazz) {
		this.converter.getRequiredPersistentEntity(clazz).setPersistentEntityId(result, id);
	}

}
