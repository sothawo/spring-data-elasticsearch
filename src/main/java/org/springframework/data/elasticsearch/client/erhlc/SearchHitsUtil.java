/*
 * Copyright 2019-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.elasticsearch.client.erhlc;

import org.elasticsearch.search.SearchHits;

/**
 * Utility class to prevent leaking of Lucene API into Spring Data Elasticsearch.
 *
 * @author Peter-Josef Meisch
 * @since 4.4
 * @deprecated since 5.0
 */
@Deprecated
public final class SearchHitsUtil {
	private SearchHitsUtil() {}

	public static long getTotalCount(SearchHits searchHits) {
		return searchHits.getTotalHits().value;
	}
}
