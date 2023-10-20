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
package org.springframework.data.elasticsearch.core

import org.springframework.data.elasticsearch.core.query.Criteria
import org.springframework.data.mapping.toDotPath
import kotlin.reflect.KProperty

class CriteriaDSL {
    private val mustCriteria = mutableListOf<Criteria>()
    private val shouldCriteria = mutableListOf<Criteria>()
    private val mustSubCriteria = mutableListOf<Criteria>()
    private val shouldSubCriteria = mutableListOf<Criteria>()

    fun and(init: CriteriaListDSL.() -> Unit) {
        val criteriaListDSL = CriteriaListDSL()
        criteriaListDSL.init()
        mustCriteria.addAll(criteriaListDSL.criteriaList)
        mustSubCriteria.addAll(criteriaListDSL.subCriteriaList)
    }

    fun or(init: CriteriaListDSL.() -> Unit) {
        val criteriaListDSL = CriteriaListDSL()
        criteriaListDSL.init()
        shouldCriteria.addAll(criteriaListDSL.criteriaList)
        shouldSubCriteria.addAll(criteriaListDSL.subCriteriaList)
    }

    fun build(): Criteria {

        // merge the shoulds into the chain of one Criteria.or()
        var should = when {
            shouldCriteria.isEmpty() -> null
            else -> shouldCriteria.fold(Criteria.or()) { actual, next -> actual.or(next) }
        }

        should = if (shouldSubCriteria.isNotEmpty()) {
            shouldSubCriteria.fold(should ?: Criteria.or()) { actual, next -> actual.subCriteria(next) }
        } else should

        // merge the musts into the chain of one Criteria
        var must = when {
            mustCriteria.isEmpty() -> null
            else -> mustCriteria.reduce { actual, next -> actual.and(next) }
        }

        must = if (mustSubCriteria.isNotEmpty()) {
            if (null == must) {
                mustSubCriteria.reduce { actual, next -> actual.subCriteria(next) }
            } else {
                mustSubCriteria.fold(must) { actual, next -> actual.subCriteria(next) }
            }
        } else must

        return when {
            (null == should && null == must) -> throw CriteriaDSLException("empty CriteriaDSL detected")
            (null == should) -> must!!
            (null == must) -> should
            // append the should to the musts, CriteriaQueryProcessor expects that
            else -> should.criteriaChain.fold(must) { actual, next -> actual.or(next) }
        }
    }
}

class CriteriaDSLException(msg: String) : Throwable(msg) {
}

class CriteriaListDSL() {

    val criteriaList = mutableListOf<Criteria>()
    val subCriteriaList = mutableListOf<Criteria>()

    /**
     * add a Criteria to this instance
     * @return this instance
     */
    operator fun Criteria.unaryPlus(): Criteria {

        if (criteriaChain.size > 1) {
            throw IllegalArgumentException("Cannot used chained Criteria inside the criteria DSL")
        }
        criteriaList.add(this)

        return this
    }

    /**
     * create a new [Criteria] for a field with this as field name and add it to the [criteriaList]
     * @return the Criteria
     */
    operator fun String.unaryPlus() = +(Criteria(this))

    /**
     * create a new [Criteria] for a field with the dot-path of this property and add it to the [criteriaList]
     * @return the Criteria
     */
    operator fun <T> KProperty<T>.unaryPlus() = +toDotPath()

    /**
     * add the [Criteria] built from this instance to the [subCriteriaList].
     * @return this instance
     */
    operator fun CriteriaDSL.unaryPlus() {
        subCriteriaList.add(this.build())
    }
}

fun criteria(init: CriteriaDSL.() -> Unit): CriteriaDSL {
    val criteriaDSL = CriteriaDSL()
    criteriaDSL.init()
    return criteriaDSL

}
