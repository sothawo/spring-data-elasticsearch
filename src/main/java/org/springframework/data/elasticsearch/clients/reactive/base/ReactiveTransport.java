package org.springframework.data.elasticsearch.clients.reactive.base;

import co.elastic.clients.base.Endpoint;
import co.elastic.clients.json.JsonpMapper;
import reactor.core.publisher.Mono;

import org.elasticsearch.client.RequestOptions;
import org.springframework.lang.Nullable;

/**
 * Reactive version of {@link co.elastic.clients.base.Transport}.
 *
 * @author Peter-Josef Meisch
 * @since 4.3
 */
public interface ReactiveTransport {
	<RequestT, ResponseT, ErrorT> Mono<ResponseT> performRequest(RequestT request,
			Endpoint<RequestT, ResponseT, ErrorT> endpoint, @Nullable RequestOptions options);

	JsonpMapper jsonpMapper();
}
