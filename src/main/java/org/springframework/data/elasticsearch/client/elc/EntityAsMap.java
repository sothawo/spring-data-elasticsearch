/*
 * Copyright 2021-2025 the original author or authors.
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
package org.springframework.data.elasticsearch.client.elc;

import org.springframework.data.elasticsearch.support.DefaultStringObjectMap;

/**
 * A Map&lt;String,Object> to represent any entity as it's returned from Elasticsearch and before it is converted to a
 * {@link org.springframework.data.elasticsearch.core.document.Document}.
 *
 * @author Peter-Josef Meisch
 * @since 4.4
 */
public class EntityAsMap extends DefaultStringObjectMap<EntityAsMap> {}
