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
package org.springframework.data.elasticsearch.core;

import org.springframework.data.elasticsearch.core.query.Criteria
import org.springframework.data.mapping.toDotPath
import kotlin.reflect.KProperty

class CriteriaDSL {
    private val mustCriteria = mutableListOf<Criteria>()
    private val shouldCriteria = mutableListOf<Criteria>()

    fun must(init: CriteriaListDSL.() -> Unit) {
        val criteriaListDSL = CriteriaListDSL()
        criteriaListDSL.init()
        mustCriteria.addAll(criteriaListDSL.criteriaList)
    }

    fun should(init: CriteriaListDSL.() -> Unit) {
        val criteriaListDSL = CriteriaListDSL()
        criteriaListDSL.init()
        shouldCriteria.addAll(criteriaListDSL.criteriaList)
    }

    fun build(): Criteria? {

        // merge the shoulds into the chain of one Criteria
        val should = when {
            shouldCriteria.isEmpty() -> null
            else -> shouldCriteria.fold(Criteria.or()) { actual: Criteria, next: Criteria -> actual.or(next) }
        }

        // merge the musts into the chain of one Criteria
        val must = when {
            mustCriteria.isEmpty() -> null
            else -> mustCriteria.reduce { actual: Criteria, next: Criteria -> actual.and(next) }
        }

        return when {
            (should == null) -> must
            (must == null) -> should
            // append the should to the musts, CriteriaQueryProcessor expects that
            else -> should.criteriaChain.fold(must) { actual, next -> actual.or(next) }
        }
    }
}

class CriteriaListDSL() {

    val criteriaList = mutableListOf<Criteria>()

    operator fun Criteria?.unaryPlus(): Criteria? {

        if (this == null) {
            return null
        }

        if (criteriaChain.size > 1) {
            throw IllegalArgumentException("Cannot used chained Criteria inside the criteria DSL")
        }
        criteriaList.add(this)
        return this
    }

    operator fun String.unaryPlus(): Criteria {
        return (+(Criteria(this)))!!
    }

    operator fun <T> KProperty<T>.unaryPlus(): Criteria {
        return +toDotPath()
    }
}

fun criteria(init: CriteriaDSL.() -> Unit): Criteria? {
    val criteriaDSL = CriteriaDSL()
    criteriaDSL.init()
    return criteriaDSL.build()

}

