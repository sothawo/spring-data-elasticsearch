/**
 * This is the base package for the different client related classes that are used with the Elasticsearch libraries providing the
 * {@link org.elasticsearch.client.RestHighLevelClient}. This client will be replaced in version 8 - eventually being available
 * for 7.x versions as well - by {@link co.elastic.clients.elasticsearch.ElasticsearchClient}, the new client licensed under
 * the Apache 2 license.
 *
 * Code using the new client is found in the org.springframework.data.elasticsearch.clients package (note the "s").
 */
@org.springframework.lang.NonNullApi
@org.springframework.lang.NonNullFields
package org.springframework.data.elasticsearch.client;
