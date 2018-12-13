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
package com.ydespreaux.shared.data.elasticsearch.core;

import com.ydespreaux.shared.data.elasticsearch.ScrolledPage;
import org.elasticsearch.action.search.SearchResponse;

/**
 * @author Artur Konczak
 * @author Petar Tahchiev
 */
public interface SearchResultMapper {

	<T> ScrolledPage<T> mapResults(SearchResponse response, Class<T> clazz);
}
