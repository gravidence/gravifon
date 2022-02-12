package org.gravidence.gravifon.query

import org.springframework.expression.Expression

interface QueryParser<T> {

    /**
     * Validates [query].
     * Valid query should produce a boolean result.
     */
    fun validate(query: String): Boolean

    /**
     * Executes parsed query [expression] against [context] object. Result indicates if context object matches the query.
     */
    fun execute(expression: Expression, context: T): Boolean

    /**
     * Executes [query] against [context] object. Result indicates if context object matches the query.
     */
    fun execute(query: String, context: T): Boolean

    /**
     * Executes [query] against [context] objects. Result is a list of objects that match the query.
     */
    fun execute(query: String, context: List<T>): List<T>

}