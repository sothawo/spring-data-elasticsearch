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
package org.springframework.data.elasticsearch.clients.reactive.base;

import static org.springframework.data.elasticsearch.support.ExceptionUtils.*;
import static org.springframework.web.reactive.function.client.WebClient.*;

import co.elastic.clients.base.BooleanResponse;
import co.elastic.clients.base.ElasticsearchError;
import co.elastic.clients.base.Endpoint;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.JsonpValueParser;
import co.elastic.clients.json.ToJsonp;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;

import org.elasticsearch.client.RequestOptions;
import org.springframework.data.elasticsearch.client.ClientLogger;
import org.springframework.data.elasticsearch.client.reactive.HostProvider;
import org.springframework.data.elasticsearch.clients.ElasticsearchErrorException;
import org.springframework.data.util.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Peter-Josef Meisch
 * @since ES_CLIENT_8
 */
public class WebClientTransport implements ReactiveTransport {

	private final HostProvider<?> hostProvider;
	private final JsonpMapper mapper;

	public WebClientTransport(HostProvider<?> hostProvider) {

		Assert.notNull(hostProvider, "hostProvider must not be null");

		this.hostProvider = hostProvider;
		this.mapper = new JacksonJsonpMapper();
	}

	@Override
	public JsonpMapper jsonpMapper() {
		return mapper;
	}

	@Override
	public <REQ, RESP, ERROR> Flux<RESP> performRequest(REQ request, Endpoint<REQ, RESP, ERROR> endpoint,
			@Nullable RequestOptions options) {

		Assert.notNull(request, "request must not be null");
		Assert.notNull(endpoint, "endpoint must not be null");

		String logId = ClientLogger.newLogId();

		return Flux.from(execute(webClient -> requestBodySpec(webClient, logId, request, endpoint, options)
				.exchangeToMono(clientResponse -> getResponse(endpoint, clientResponse))));
	}

	private <REQ, RESP, ERROR> Mono<RESP> getResponse(Endpoint<REQ, RESP, ERROR> endpoint,
			org.springframework.web.reactive.function.client.ClientResponse clientResponse) {

		int statusCode = clientResponse.statusCode().value();

		if (endpoint.isError(statusCode)) {
			// todo, the ElasticsearchError parser currently cannot parse the nested error object of an error response and
			// fails on parsing, so we create one with the unparsed error string
			return clientResponse.body(BodyExtractors.toMono(byte[].class)) //
					.flatMap(
							buf -> Mono.error(new ElasticsearchErrorException(new ElasticsearchError(statusCode, new String(buf)))));

			// return clientResponse.body(BodyExtractors.toMono(byte[].class)) //
			// .map(buf -> new ByteArrayInputStream(buf)) //
			// .flatMap(bais -> {
			// ERROR response = null;
			// JsonpValueParser<ERROR> responseParser = endpoint.errorParser(statusCode);
			// if (responseParser != null) {
			// JsonParser parser = mapper.jsonpProvider().createParser(bais);
			// response = responseParser.parse(parser, mapper);
			// }
			// return Mono.error(new ApiException(response));
			// });
		} else if (endpoint instanceof Endpoint.Boolean) {
			// noinspection unchecked
			RESP response = (RESP) new BooleanResponse(((Endpoint.Boolean<?>) endpoint).getResult(statusCode));
			return Mono.just(response);
		} else {
			return clientResponse.body(BodyExtractors.toMono(byte[].class)) //
					.map(ByteArrayInputStream::new) //
					.flatMap(bais -> {
						RESP response = null;
						JsonpValueParser<RESP> responseParser = endpoint.responseParser();
						if (responseParser != null) {
							JsonParser parser = mapper.jsonpProvider().createParser(bais);
							response = responseParser.parse(parser, mapper);
						}
						return Mono.justOrEmpty(response);
					});
		}
	}

	private <REQ, RESP, ERROR> RequestBodySpec requestBodySpec(WebClient webClient, String logId, REQ request,
			Endpoint<REQ, RESP, ERROR> endpoint, @Nullable RequestOptions options) {

		String method = endpoint.method(request);
		String requestUrl = endpoint.requestUrl(request);

		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		Map<String, String> optionParameters = options != null ? options.getParameters() : Collections.emptyMap();
		optionParameters.forEach(queryParams::add);
		endpoint.queryParameters(request).forEach(optionParameters::put);

		RequestBodySpec requestBodySpec = webClient.method(HttpMethod.valueOf(method)) //
				.uri(builder -> {
					builder = builder.path(requestUrl);

					if (!queryParams.isEmpty()) {
						builder = builder.queryParams(queryParams);
					}
					return builder.build();
				}) //
				.attribute(ClientRequest.LOG_ID_ATTRIBUTE, logId);

		if (options != null) {
			options.getHeaders().forEach(header -> requestBodySpec.header(header.getName(), header.getValue()));
		}
		endpoint.headers(request).forEach(requestBodySpec::header);

		if (endpoint.hasRequestBody()) {
			Lazy<String> body = requestBodyExtractor((ToJsonp) request);
			ClientLogger.logRequest(logId, method, requestUrl, optionParameters, body::get);
			requestBodySpec.contentType(MediaType.APPLICATION_JSON).body(Mono.fromSupplier(body), String.class);
		} else {
			ClientLogger.logRequest(logId, method, requestUrl, optionParameters);
		}

		return requestBodySpec;
	}

	private Lazy<String> requestBodyExtractor(ToJsonp request) {

		return Lazy.of(() -> {
			Writer writer = new StringWriter();
			JsonGenerator generator = mapper.jsonpProvider().createGenerator(writer);
			request.toJsonp(generator, mapper);
			generator.close();
			return writer.toString();
		});
	}

	/**
	 * Calls the given callback with a {@link WebClient} communicating with a configured Elasticsearch host. If the call
	 * fails with an exception that has a {@link java.net.ConnectException} as cause, the callback is retried with another
	 * host, if one is available
	 *
	 * @param callback the callback to execute
	 * @param <T> the type emitted by the returned Mono.
	 * @return the {@link Mono} emitting the result type once subscribed.
	 */
	public <T> Mono<T> execute(ReactiveElasticsearchClientCallback<T> callback) {

		return hostProvider.getWebClient() //
				.flatMap(callback::doWithClient) //
				.onErrorResume(throwable -> {

					if (isCausedByConnectionException(throwable)) {
						return hostProvider.getActive(HostProvider.Verification.ACTIVE) //
								.flatMap(callback::doWithClient);
					}

					return Mono.error(throwable);
				});
	}

	/**
	 * Low level callback interface operating upon {@link WebClient} to send commands towards elasticsearch.
	 *
	 * @param <T> the type emitted by the returned Mono.
	 */
	interface ReactiveElasticsearchClientCallback<T> {
		Mono<T> doWithClient(WebClient client);
	}

}
