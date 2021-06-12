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
package org.springframework.data.elasticsearch.clients.reactive;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import co.elastic.clients.elasticsearch._global.SearchRequest;
import co.elastic.clients.elasticsearch._global.SearchResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.clients.reactive.elasticsearch.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.support.DefaultStringObjectMap;
import org.springframework.http.HttpHeaders;

/**
 * @author Peter-Josef Meisch
 */
public class DevTests {

	@Test
	void someTestsForTheNewReactiveClient() {

		ReactiveElasticsearchClient client = ReactiveElasticsearchClients.create(clientConfiguration());

		SearchRequest searchRequest = new SearchRequest.Builder().index("appdata-index").build();

		SearchResponse<EntityAsMap> searchResponse = client.search(searchRequest, EntityAsMap.class).block();

		assertThat(searchResponse).isNotNull();
	}

	private ClientConfiguration clientConfiguration() {
		return ClientConfiguration.builder() //
				.connectedTo("localhost:9200")//
				.withProxy("localhost:8080") //
				.withHeaders(() -> {
					HttpHeaders headers = new HttpHeaders();
					headers.add("currentTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
					return headers;
				}) //
				.build();
	}

	private static class EntityAsMap extends DefaultStringObjectMap<EntityAsMap> {}
}
