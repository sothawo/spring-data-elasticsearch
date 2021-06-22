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

import co.elastic.clients.base.ElasticsearchError;

/**
 * Exception class containing an {@link co.elastic.clients.base.ElasticsearchError}
 *
 * @author Peter-Josef Meisch
 * @since ES_CLIENT_8
 */
public class ElasticsearchErrorException extends RuntimeException {
	private final ElasticsearchError elasticsearchError;

	public ElasticsearchErrorException(ElasticsearchError elasticsearchError) {
		this.elasticsearchError = elasticsearchError;
	}

	@Override
	public String toString() {
		return "ElasticsearchErrorException{" +
				"status=" + elasticsearchError.status() +
				", error=" + elasticsearchError.error() +
				"} " + super.toString();
	}
}
