/*
 * Copyright 2021 the original author or authors.
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
package org.springframework.data.elasticsearch.clients;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._global.SearchRequest;
import co.elastic.clients.elasticsearch._global.SearchResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.elasticsearch.client.RequestOptions;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.clients.reactive.ElasticsearchClients;
import org.springframework.data.elasticsearch.clients.reactive.elasticsearch.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.support.DefaultStringObjectMap;
import org.springframework.http.HttpHeaders;

/**
 * @author Peter-Josef Meisch
 */
public class DevTests {

	private final RequestOptions requestOptions = RequestOptions.DEFAULT.toBuilder()
			.addHeader("X-SpringDataElasticsearch-AlwaysThere", "true").addParameter("bretty", "true").build();
	private final ReactiveElasticsearchClient reactiveElasticsearchClient = ElasticsearchClients
			.createReactive(clientConfiguration(), requestOptions);
	private final ElasticsearchClient imperativeElasticsearchClient = ElasticsearchClients
			.createImperative(clientConfiguration(), requestOptions);

	@Test
	void search() throws IOException {

		SearchRequest searchRequest = new SearchRequest.Builder().index("appdata-index").build();

		SearchResponse<EntityAsMap> searchResponse = null;
		try {
			searchResponse = searchImperative(searchRequest);
			assertThat(searchResponse).isNotNull();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			searchResponse = searchReactive(searchRequest);
			assertThat(searchResponse).isNotNull();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private SearchResponse<EntityAsMap> searchImperative(SearchRequest searchRequest) throws IOException {
		return imperativeElasticsearchClient.search(searchRequest, EntityAsMap.class);
	}

	private SearchResponse<EntityAsMap> searchReactive(SearchRequest searchRequest) {
		return Objects.requireNonNull(reactiveElasticsearchClient.search(searchRequest, EntityAsMap.class).block());
	}

	private ClientConfiguration clientConfiguration() {
		return ClientConfiguration.builder() //
				.connectedTo("localhost:9200")//
				.withProxy("localhost:8080") //
				.withHeaders(() -> {
					HttpHeaders headers = new HttpHeaders();
					headers.add("X-SpringDataElasticsearch-timestamp",
							LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
					return headers;
				}) //
				.build();
	}

	private static class EntityAsMap extends DefaultStringObjectMap<EntityAsMap> {}
}
