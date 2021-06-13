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

import co.elastic.clients.base.RestClientTransport;
import co.elastic.clients.base.Transport;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;

import java.net.InetSocketAddress;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.client.reactive.HostProvider;
import org.springframework.data.elasticsearch.client.reactive.WebClientProvider;
import org.springframework.data.elasticsearch.clients.imperative.CloseableElasticsearchClient;
import org.springframework.data.elasticsearch.clients.reactive.base.WebClientTransport;
import org.springframework.data.elasticsearch.clients.reactive.elasticsearch.ReactiveElasticsearchClient;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Utility class to create a {@link ElasticsearchClients}
 *
 * @author Peter-Josef Meisch
 * @since ES_CLIENT_8
 */
public final class ElasticsearchClients {
	/**
	 * Name of whose value can be used to correlate log messages for this request.
	 */
	private static final String LOG_ID_ATTRIBUTE = ElasticsearchClients.class.getName() + ".LOG_ID";

	/**
	 * Creates a new {@link ReactiveElasticsearchClient}
	 *
	 * @param clientConfiguration configuration options, must not be {@literal null}.
	 * @return the {@link ReactiveElasticsearchClient}
	 */
	public static ReactiveElasticsearchClient createReactive(ClientConfiguration clientConfiguration) {
		return createReactive(clientConfiguration, null);
	}

	/**
	 * Creates a new {@link ReactiveElasticsearchClient}
	 *
	 * @param clientConfiguration configuration options, must not be {@literal null}.
	 * @param requestOptions options to be added to each request.
	 * @return the {@link ReactiveElasticsearchClient}
	 */
	public static ReactiveElasticsearchClient createReactive(ClientConfiguration clientConfiguration,
			@Nullable RequestOptions requestOptions) {

		Assert.notNull(clientConfiguration, "ClientConfiguration must not be null!");

		WebClientProvider provider = WebClientProvider.getWebClientProvider(clientConfiguration);
		HostProvider<?> hostProvider = HostProvider.provider(provider, clientConfiguration.getHeadersSupplier(),
				clientConfiguration.getEndpoints().toArray(new InetSocketAddress[0]));

		WebClientTransport transport = new WebClientTransport(hostProvider);

		return new ReactiveElasticsearchClient(transport, requestOptions);
	}

	/**
	 * Creates a new imperative {@link ElasticsearchClient}
	 *
	 * @param clientConfiguration configuration options, must not be {@literal null}.
	 * @return the {@link ElasticsearchClient}
	 */
	public static ElasticsearchClient createImperative(ClientConfiguration clientConfiguration) {
		return createImperative(clientConfiguration, null);
	}

	/**
	 * Creates a new imperative {@link ElasticsearchClient}
	 *
	 * @param clientConfiguration configuration options, must not be {@literal null}.
	 * @param requestOptions options to be added to each request.
	 * @return the {@link ElasticsearchClient}
	 */
	public static ElasticsearchClient createImperative(ClientConfiguration clientConfiguration,
			@Nullable RequestOptions requestOptions) {

		Assert.notNull(clientConfiguration, "ClientConfiguration must not be null!");

		RestClient restClient = RestClients.getRestClient(clientConfiguration);
		Transport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

		return new CloseableElasticsearchClient(restClient, transport, requestOptions);
	}
}
