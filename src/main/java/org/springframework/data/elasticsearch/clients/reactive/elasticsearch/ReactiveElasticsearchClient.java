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
package org.springframework.data.elasticsearch.clients.reactive.elasticsearch;

import co.elastic.clients.elasticsearch._global.SearchRequest;
import co.elastic.clients.elasticsearch._global.SearchResponse;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

import java.io.IOException;

import org.elasticsearch.client.RequestOptions;
import org.springframework.data.elasticsearch.clients.reactive.base.ReactiveApiClient;
import org.springframework.data.elasticsearch.clients.reactive.base.ReactiveTransport;

/**
 * Reactive version of {@link co.elastic.clients.elasticsearch.ElasticsearchClient}.
 *
 * @author Peter-Josef Meisch
 * @since 4.3
 */
public class ReactiveElasticsearchClient extends ReactiveApiClient<ReactiveElasticsearchClient> {

	public ReactiveElasticsearchClient(ReactiveTransport transport) {
		super(transport, null);
	}

	public ReactiveElasticsearchClient(ReactiveTransport transport, @Nullable RequestOptions options) {
		super(transport, options);
	}

	@Override
	public ReactiveElasticsearchClient withRequestOptions(@Nullable RequestOptions options) {
		return new ReactiveElasticsearchClient(transport, options);
	}

	public <TDocument> Mono<SearchResponse<TDocument>> search(SearchRequest request, Class<TDocument> tDocumentClass)
			throws IOException {
		return this.transport.performRequest(request,
				SearchRequest.createSearchEndpoint(this.getDeserializer(tDocumentClass)), this.requestOptions);
	}

}
