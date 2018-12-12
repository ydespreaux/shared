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
package com.ydespreaux.shared.data.elasticsearch.query;

import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * NativeSearchQuery
 *
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Artur Konczak
 * @author Mark Paluch
 * @author Alen Turkovic
 * @author Sascha Woo
 * @author Jean-Baptiste Nizet
 */
public class NativeSearchQueryBuilder {

	private QueryBuilder queryBuilder;
	private QueryBuilder filterBuilder;
	private List<SortBuilder> sortBuilders = new ArrayList<>();
	private List<AbstractAggregationBuilder> aggregationBuilders = new ArrayList<>();
	private HighlightBuilder highlightBuilder;
	private HighlightBuilder.Field[] highlightFields;
	private Pageable pageable = Pageable.unpaged();
	private String[] indices;
	private String[] types;
	private String[] fields;
	private SourceFilter sourceFilter;
	private List<IndexBoost> indicesBoost;
	private float minScore;
	private boolean trackScores;
	private Collection<String> ids;
	private String route;
	private SearchType searchType;
	private IndicesOptions indicesOptions;

	public NativeSearchQueryBuilder withQuery(QueryBuilder queryBuilder) {
		this.queryBuilder = queryBuilder;
		return this;
	}

	public NativeSearchQueryBuilder withFilter(QueryBuilder filterBuilder) {
		this.filterBuilder = filterBuilder;
		return this;
	}

	public NativeSearchQueryBuilder withSort(SortBuilder sortBuilder) {
		this.sortBuilders.add(sortBuilder);
		return this;
	}

	public NativeSearchQueryBuilder addAggregation(AbstractAggregationBuilder aggregationBuilder) {
		this.aggregationBuilders.add(aggregationBuilder);
		return this;
	}

	public NativeSearchQueryBuilder withHighlightBuilder(HighlightBuilder highlightBuilder) {
		this.highlightBuilder = highlightBuilder;
		return this;
	}

	public NativeSearchQueryBuilder withHighlightFields(HighlightBuilder.Field... highlightFields) {
		this.highlightFields = highlightFields;
		return this;
	}

	public NativeSearchQueryBuilder withIndicesBoost(List<IndexBoost> indicesBoost) {
		this.indicesBoost = indicesBoost;
		return this;
	}

	public NativeSearchQueryBuilder withPageable(Pageable pageable) {
		this.pageable = pageable;
		return this;
	}

	public NativeSearchQueryBuilder withIndices(String... indices) {
		this.indices = indices;
		return this;
	}

	public NativeSearchQueryBuilder withTypes(String... types) {
		this.types = types;
		return this;
	}

	public NativeSearchQueryBuilder withFields(String... fields) {
		this.fields = fields;
		return this;
	}

	public NativeSearchQueryBuilder withSourceFilter(SourceFilter sourceFilter) {
		this.sourceFilter = sourceFilter;
		return this;
	}

	public NativeSearchQueryBuilder withMinScore(float minScore) {
		this.minScore = minScore;
		return this;
	}

	/**
	 * @param trackScores whether to track scores.
	 * @return
	 * @since 3.1
	 */
	public NativeSearchQueryBuilder withTrackScores(boolean trackScores) {
		this.trackScores = trackScores;
		return this;
	}

	public NativeSearchQueryBuilder withIds(Collection<String> ids) {
		this.ids = ids;
		return this;
	}

	public NativeSearchQueryBuilder withRoute(String route) {
		this.route = route;
		return this;
	}

	public NativeSearchQueryBuilder withSearchType(SearchType searchType) {
		this.searchType = searchType;
		return this;
	}

	public NativeSearchQueryBuilder withIndicesOptions(IndicesOptions indicesOptions) {
		this.indicesOptions = indicesOptions;
		return this;
	}

	public NativeSearchQuery build() {
		NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryBuilder, filterBuilder, sortBuilders,
				highlightBuilder, highlightFields);

		nativeSearchQuery.setPageable(pageable);
		nativeSearchQuery.setTrackScores(trackScores);

		if (indices != null) {
			nativeSearchQuery.addIndices(indices);
		}

		if (types != null) {
			nativeSearchQuery.addTypes(types);
		}

		if (fields != null) {
			nativeSearchQuery.addFields(fields);
		}

		if (sourceFilter != null) {
			nativeSearchQuery.addSourceFilter(sourceFilter);
		}

		if (indicesBoost != null) {
			nativeSearchQuery.setIndicesBoost(indicesBoost);
		}

		if (!isEmpty(aggregationBuilders)) {
			nativeSearchQuery.setAggregations(aggregationBuilders);
		}

		if (minScore > 0) {
			nativeSearchQuery.setMinScore(minScore);
		}

		if (ids != null) {
			nativeSearchQuery.setIds(ids);
		}

		if (route != null) {
			nativeSearchQuery.setRoute(route);
		}

		if (searchType != null) {
			nativeSearchQuery.setSearchType(searchType);
		}

		if (indicesOptions != null) {
			nativeSearchQuery.setIndicesOptions(indicesOptions);
		}

		return nativeSearchQuery;
	}
}
