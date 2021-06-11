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

import co.elastic.clients.base.Endpoint;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.JsonpValueParser;
import co.elastic.clients.json.ToJsonp;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.elasticsearch.client.RequestOptions;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Peter-Josef Meisch
 * @since 4.3
 */
public class WebClientTransport implements ReactiveTransport {

	private final WebClient webClient;
	private final JsonpMapper mapper;

	public WebClientTransport(WebClient webClient, JsonpMapper mapper) {
		this.webClient = webClient;
		this.mapper = mapper;
	}

	@Override
	public <RequestT, ResponseT, ErrorT> Mono<ResponseT> performRequest(RequestT request,
			Endpoint<RequestT, ResponseT, ErrorT> endpoint, @Nullable RequestOptions options) {
		String path = endpoint.requestUrl(request);

		Map<String, String> params = endpoint.queryParameters(request);
		Map<String, String> headers = endpoint.headers(request);

		WebClient.RequestBodySpec requestBodySpec = webClient.method(HttpMethod.valueOf(endpoint.method(request)))
				.uri(builder -> {
					builder = builder.path(endpoint.requestUrl(request));
					return builder.build();
				});

		if (endpoint.hasRequestBody()) {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				JsonGenerator generator = this.mapper.jsonpProvider().createGenerator(baos);
				((ToJsonp) request).toJsonp(generator, this.mapper);
				generator.close();
				// TODO: use non-blocking call
				String data = baos.toString("UTF-8");
				requestBodySpec.contentType(MediaType.APPLICATION_JSON).body(Mono.just(data), String.class);
			} catch (UnsupportedEncodingException e) {
				return Mono.error(e);
			}
		}

		Mono<ResponseT> responseTMono = requestBodySpec.exchangeToMono(clientResponse -> clientResponse
				.body(BodyExtractors.toMono(byte[].class)).map(ByteArrayInputStream::new).flatMap(bais -> {
					ResponseT response = null;
					JsonpValueParser<ResponseT> responseParser = endpoint.responseParser();
					if (responseParser != null) {
						JsonParser parser = mapper.jsonpProvider().createParser(bais);
						response = responseParser.parse(parser, mapper);
					}
					return Mono.justOrEmpty(response);
				}));

		return responseTMono;
	}

	@Override
	public JsonpMapper jsonpMapper() {
		return mapper;
	}
}
