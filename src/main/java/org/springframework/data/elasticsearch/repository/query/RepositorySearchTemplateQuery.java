package org.springframework.data.elasticsearch.repository.query;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.BaseQuery;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.lang.Nullable;

import java.util.Map;

/**
 * A repository query that uses a search template already stored in Elasticsearch.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com)
 * @since 5.5
 */
public class RepositorySearchTemplateQuery extends AbstractElasticsearchRepositoryQuery {

	private String id;
	private Map<String, Object> params;

	public RepositorySearchTemplateQuery(ElasticsearchQueryMethod queryMethod,
			ElasticsearchOperations elasticsearchOperations, QueryMethodEvaluationContextProvider evaluationContextProvider) {
		super(queryMethod, elasticsearchOperations, evaluationContextProvider);
		// todo #2997 check id and paramse
	}

	public String getId() {
		return id;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	@Override
	public boolean isCountQuery() {
		return false;
	}

	@Override
	protected boolean isDeleteQuery() {
		return false;
	}

	@Override
	protected boolean isExistsQuery() {
		return false;
	}

	@Override
	protected BaseQuery createQuery(ElasticsearchParametersParameterAccessor accessor) {
		return null;
	}
}
