package org.springframework.data.elasticsearch.clients.reactive.base;

import co.elastic.clients.base.Endpoint;
import co.elastic.clients.json.JsonpMapper;
import reactor.core.publisher.Flux;

import org.elasticsearch.client.RequestOptions;
import org.springframework.lang.Nullable;

/**
 * Reactive version of {@link co.elastic.clients.base.Transport}.
 *
 * @author Peter-Josef Meisch
 * @since 4.3
 */
public interface ReactiveTransport {
	<REQ, RESP, ERROR> Flux<RESP> performRequest(REQ request, Endpoint<REQ, RESP, ERROR> endpoint,
			@Nullable RequestOptions options);

	JsonpMapper jsonpMapper();
}
