/*
 * Copyright 2019-2025 the original author or authors.
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
package org.springframework.data.elasticsearch.core.query;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter-Josef Meisch
 */
class CriteriaTest {

    @Test // #3159
    @DisplayName("should not slow down on creating long criteria chains")
    void shouldNotSlowDownOnCreatingLongCriteriaChains() {

        var criteria = new Criteria("field").is("value 0");
        for (int i = 1; i < 100; i++) {
            criteria = criteria.and(new Criteria("field").is("and " + i));
            criteria = criteria.or(new Criteria("field").is("or " + i));
        }

        var criteriaChain = criteria.getCriteriaChain();
        assertEquals(criteriaChain.size(), 1);
    }
}
