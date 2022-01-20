package org.gravidence.gravifon.query

interface QueryParser<T> {

    /**
     * Validates [query].
     * Valid query should produce a boolean result.
     */
    fun validate(query: String): Boolean

    /**
     * Executes [query] against [context] object. Result indicates if context object matches the query.
     */
    fun execute(query: String, context: T): Boolean

}