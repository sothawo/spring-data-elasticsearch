package org.springframework.data.elasticsearch.annotations;

import org.springframework.data.annotation.QueryAnnotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a repository method as a search template method. The annotation defines the search template id,
 * the parameters for the search template are taken from the method's arguments.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com)
 * @since 5.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Documented
@QueryAnnotation
public @interface SearchTemplateQuery {
}
