/*
 * Copyright 2019-2021 the original author or authors.
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
package org.springframework.data.elasticsearch.junit.jupiter;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.support.VersionInfo;
import org.springframework.lang.Nullable;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

/**
 * This class manages the connection to an Elasticsearch Cluster, starting a containerized one if necessary. The
 * information about the ClusterConnection is stored both as a variable in the instance for direct access from JUnit 5
 * and in a static ThreadLocal<ClusterConnectionInfo> accessible with the
 * {@link ClusterConnection#clusterConnectionInfo()} method to be integrated in the Spring setup
 *
 * @author Peter-Josef Meisch
 */
public class ClusterConnection implements ExtensionContext.Store.CloseableResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClusterConnection.class);

	private static final int ELASTICSEARCH_DEFAULT_PORT = 9200;
	private static final int ELASTICSEARCH_DEFAULT_TRANSPORT_PORT = 9300;
	private static final String ELASTICSEARCH_DEFAULT_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch";

	private static final ThreadLocal<ClusterConnectionInfo> clusterConnectionInfoThreadLocal = new ThreadLocal<>();

	@Nullable private final ClusterConnectionInfo clusterConnectionInfo;

	/**
	 * creates the ClusterConnection, starting a container
	 */
	public ClusterConnection() {
		clusterConnectionInfo = startElasticsearchContainer();

		if (clusterConnectionInfo != null) {
			LOGGER.debug(clusterConnectionInfo.toString());
			clusterConnectionInfoThreadLocal.set(clusterConnectionInfo);
		} else {
			LOGGER.error("could not create ClusterConnectionInfo");
		}
	}

	/**
	 * @return the {@link ClusterConnectionInfo} from the ThreadLocal storage.
	 */
	@Nullable
	public static ClusterConnectionInfo clusterConnectionInfo() {
		return clusterConnectionInfoThreadLocal.get();
	}

	@Nullable
	public ClusterConnectionInfo getClusterConnectionInfo() {
		return clusterConnectionInfo;
	}

	@Nullable
	private ClusterConnectionInfo startElasticsearchContainer() {

		LOGGER.debug("Starting Elasticsearch Container");

		try {
			String elasticsearchVersion = VersionInfo.versionProperties()
					.getProperty(VersionInfo.VERSION_ELASTICSEARCH_CLIENT);
			Map<String, String> elasticsearchProperties = elasticsearchProperties();

			String dockerImageName = ELASTICSEARCH_DEFAULT_IMAGE + ':' + elasticsearchVersion;
			LOGGER.debug("Docker image: {}", dockerImageName);

			ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(dockerImageName);
			elasticsearchContainer.withEnv(elasticsearchProperties);
			elasticsearchContainer.start();
			return ClusterConnectionInfo.builder() //
					.withHostAndPort(elasticsearchContainer.getHost(),
							elasticsearchContainer.getMappedPort(ELASTICSEARCH_DEFAULT_PORT)) //
					.withTransportPort(elasticsearchContainer.getMappedPort(ELASTICSEARCH_DEFAULT_TRANSPORT_PORT)) //
					.withElasticsearchContainer(elasticsearchContainer) //
					.build();
		} catch (Exception e) {
			LOGGER.error("Could not start Elasticsearch container", e);
		}

		return null;
	}

	private Map<String, String> elasticsearchProperties() {

		String propertiesFile = "testcontainers-elasticsearch.properties";
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertiesFile)) {
			Properties props = new Properties();

			if (inputStream != null) {
				props.load(inputStream);
			}
			Map<String, String> elasticsearchProperties = new LinkedHashMap<>();
			props.forEach((key, value) -> elasticsearchProperties.put(key.toString(), value.toString()));
			return elasticsearchProperties;
		} catch (Exception e) {
			LOGGER.error("Cannot load " + propertiesFile);
		}
		return Collections.emptyMap();
	}

	@Override
	public void close() {

		if (clusterConnectionInfo != null && clusterConnectionInfo.getElasticsearchContainer() != null) {
			LOGGER.debug("Stopping container");
			clusterConnectionInfo.getElasticsearchContainer().stop();
		}

		LOGGER.debug("closed");
	}
}
