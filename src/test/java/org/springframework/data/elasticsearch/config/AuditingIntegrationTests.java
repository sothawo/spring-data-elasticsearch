/*
 * Copyright 2020-2025 the original author or authors.
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
package org.springframework.data.elasticsearch.config;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Persistable;
import org.springframework.data.elasticsearch.core.event.BeforeConvertCallback;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.junit.jupiter.SpringIntegrationTest;
import org.springframework.data.mapping.callback.EntityCallbacks;

/**
 * @author Peter-Josef Meisch
 * @author Roman Puchkovskiy
 */
@SpringIntegrationTest
public abstract class AuditingIntegrationTests {

	@Configuration
	@EnableElasticsearchAuditing(auditorAwareRef = "auditorAware")
	static class Config {
		@Bean
		public AuditorAware<String> auditorAware() {
			return auditorProvider();
		}
	}

	public static AuditorAware<String> auditorProvider() {
		return new AuditorAware<>() {
			int count = 0;

			@Override
			public Optional<String> getCurrentAuditor() {
				return Optional.of("Auditor " + (++count));
			}
		};
	}

	@Autowired ApplicationContext applicationContext;

	@Test // DATAES-68
	void shouldEnableAuditingAndSetAuditingDates() throws InterruptedException {
		SimpleElasticsearchMappingContext mappingContext = applicationContext
				.getBean(SimpleElasticsearchMappingContext.class);

		mappingContext.getPersistentEntity(Entity.class);

		EntityCallbacks callbacks = EntityCallbacks.create(applicationContext);

		Entity entity = new Entity();
		entity.setId("1");
		entity = callbacks.callback(BeforeConvertCallback.class, entity, IndexCoordinates.of("index"));

		assertThat(entity.getCreated()).isNotNull();
		assertThat(entity.getModified()).isEqualTo(entity.created);
		assertThat(entity.getCreatedBy()).isEqualTo("Auditor 1");
		assertThat(entity.getModifiedBy()).isEqualTo("Auditor 1");

		Thread.sleep(50);

		entity = callbacks.callback(BeforeConvertCallback.class, entity, IndexCoordinates.of("index"));

		assertThat(entity.getCreated()).isNotNull();
		assertThat(entity.getModified()).isNotEqualTo(entity.created);
		assertThat(entity.getCreatedBy()).isEqualTo("Auditor 1");
		assertThat(entity.getModifiedBy()).isEqualTo("Auditor 2");
	}

	static class Entity implements Persistable<String> {
		private @Nullable @Id String id;
		private @Nullable @CreatedDate LocalDateTime created;
		private @Nullable LocalDateTime modified;
		private @Nullable @CreatedBy String createdBy;
		private @Nullable @LastModifiedBy String modifiedBy;

		@Nullable
		public String getId() {
			return id;
		}

		public void setId(@Nullable String id) {
			this.id = id;
		}

		@Nullable
		public LocalDateTime getCreated() {
			return created;
		}

		public void setCreated(@Nullable LocalDateTime created) {
			this.created = created;
		}

		public void setModified(@Nullable LocalDateTime modified) {
			this.modified = modified;
		}

		@Nullable
		@LastModifiedDate
		public LocalDateTime getModified() {
			return modified;
		}

		@Nullable
		public String getCreatedBy() {
			return createdBy;
		}

		public void setCreatedBy(@Nullable String createdBy) {
			this.createdBy = createdBy;
		}

		@Nullable
		public String getModifiedBy() {
			return modifiedBy;
		}

		public void setModifiedBy(@Nullable String modifiedBy) {
			this.modifiedBy = modifiedBy;
		}

		@Override
		public boolean isNew() {
			return id == null || (created == null && createdBy == null);
		}
	}
}
