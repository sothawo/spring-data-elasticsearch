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

        val criteriaDSL = criteria {
            and {
                +"must-first-name" isEqualTo firstName
                +"must-last-name" isEqualTo lastName
                +Criteria("must-ugly-call").`is`("ugly")
            }
            or {
                +Person::firstName isEqualTo firstName
                +Person::lastName isEqualTo lastName
                +Criteria("oopsie").`is`("oopsie")
            }
        }

        val criteria = criteriaDSL.build()
        println(CriteriaQueryProcessor().createQuery(criteria))
    }

    @Test
    fun `Rodeck or Meisch`() {


        val criteriaDSL = criteria {
            or {
                +criteria {
                    and {
                        +Person::lastName isEqualTo "Rodeck"
                        +criteria {
                            or {
                                +Person::firstName isEqualTo "Alaya"
                                +Person::firstName isEqualTo "Salome"
                            }
                        }
                    }
                }
                +criteria {
                    and {
                        +Person::lastName isEqualTo "Meisch"
                        +criteria {
                            or {
                                +Person::firstName isEqualTo "P.J."
                                +Person::firstName isEqualTo "Annabelle"
                            }
                        }
                    }
                }
            }
        }

        val criteria = criteriaDSL.build()
        println(CriteriaQueryProcessor().createQuery(criteria))
    }
}

data class Person(
    val firstName: String,
    val lastName: String
)
