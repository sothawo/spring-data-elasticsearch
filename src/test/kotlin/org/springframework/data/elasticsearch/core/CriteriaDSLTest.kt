package org.springframework.data.elasticsearch.core

import org.junit.jupiter.api.Test
import org.springframework.data.elasticsearch.core.query.Criteria
import org.springframework.data.elasticsearch.core.query.isEqualTo

/**
 * @author Peter-Josef Meisch
 */
class CriteriaDSLTest {

    @Test
    fun `should do some foo`() {

        val firstName = "James"
        val lastName = "Bond"

        val criteria = criteria {
            must {
                +"must-first-name" isEqualTo firstName
                +"must-last-name" isEqualTo lastName
                +Criteria("must-ugly-call").`is`("ugly")
            }
            should {
                +Person::firstName isEqualTo firstName
                +Person::lastName isEqualTo lastName
                +Criteria("oopsie").`is`("oopsie")
            }
        }

        println(CriteriaQueryProcessor().createQuery(criteria).toString())
    }

    @Test
    fun `Rodeck or Meisch`() {

        val criteria = criteria {
            should {
                +criteria {
                    must {
                        +Person::lastName isEqualTo "Rodeck"
                        +criteria {
                            should {
                                +Person::firstName isEqualTo "Alaya"
                                +Person::firstName isEqualTo "Salome"
                            }
                        }
                    }
                }
                +criteria {
                    must {
                        +Person::lastName isEqualTo "Meisch"
                        +criteria {
                            should {
                                +Person::firstName isEqualTo "P.J."
                                +Person::firstName isEqualTo "Annabelle"
                            }
                        }
                    }
                }
            }
        }

        println(CriteriaQueryProcessor().createQuery(criteria).toString())
    }
}

data class Person(
    val firstName: String,
    val lastName: String
)
