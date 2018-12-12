/*
 * Copyright 2014-2018 the original author or authors.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ydespreaux.shared.data.elasticsearch.adapter.TimeTypeAdapterRegistry;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;

/**
 * EntityMapper based on a Jackson {@link ObjectMapper}.
 *
 */
public class DefaultEntityMapper implements EntityMapper {

	private final Gson mapper;

	/**
	 *
	 * @param jacksonProperties
	 */
	public DefaultEntityMapper(final JacksonProperties jacksonProperties) {
		GsonBuilder builder = new GsonBuilder();
		TimeTypeAdapterRegistry.registerAll(builder);
		if (jacksonProperties.getDateFormat() != null) {
			builder.setDateFormat(jacksonProperties.getDateFormat());
		} else {
			builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		}
		TimeTypeAdapterRegistry.registerAll(builder);
		mapper = builder.create();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.elasticsearch.core.EntityMapper#mapToString(java.lang.Object)
	 */
	@Override
	public String mapToString(Object object) {
		return mapper.toJson(object);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.elasticsearch.core.EntityMapper#mapToObject(java.lang.String, java.lang.Class)
	 */
	@Override
	public <T> T mapToObject(String source, Class<T> clazz) {
		return mapper.fromJson(source, clazz);
	}

}
