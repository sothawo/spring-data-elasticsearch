/*
 * Copyright 2023-2025 the original author or authors.
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
package org.springframework.data.elasticsearch.repository.support.querybyexample;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.junit.jupiter.ElasticsearchTemplateConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.elasticsearch.utils.IndexNameProvider;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Ezequiel Antúnez Camacho
 * @since 5.1
 */
@ContextConfiguration(classes = { QueryByExampleElasticsearchExecutorELCIntegrationTests.Config.class })
public class QueryByExampleElasticsearchExecutorELCIntegrationTests
		extends QueryByExampleElasticsearchExecutorIntegrationTests {

	@Configuration
	@Import({ ElasticsearchTemplateConfiguration.class })
	@EnableElasticsearchRepositories(basePackages = { "org.springframework.data.elasticsearch.repository.support" },
			considerNestedRepositories = true)
	static class Config {
		@Bean
		IndexNameProvider indexNameProvider() {
			return new IndexNameProvider("query-by-example-repository");
		}
	}
}
