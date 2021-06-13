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
package org.springframework.data.elasticsearch.clients.imperative;

import co.elastic.clients.base.Transport;
import co.elastic.clients.elasticsearch.ElasticsearchClient;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;

/**
 * Extension of the {@link ElasticsearchClient} class as the underlying {@link org.elasticsearch.client.RestClient} must
 * be closed.
 *
 * @author Peter-Josef Meisch
 * @since ES_CLIENT_8
 */
public class CloseableElasticsearchClient extends ElasticsearchClient implements AutoCloseable {

	private final RestClient client;

	public CloseableElasticsearchClient(RestClient client, Transport transport) {
		super(transport);
		this.client = client;
	}

	public CloseableElasticsearchClient(RestClient client, Transport transport, RequestOptions options) {
		super(transport, options);
		this.client = client;
	}

	@Override
	public void close() throws Exception {
		client.close();
	}
}
