package org.gravidence.gravifon.query

import mu.KotlinLogging
import org.gravidence.gravifon.domain.track.FileVirtualTrack
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.springframework.expression.Expression
import org.springframework.expression.spel.standard.SpelExpressionParser

private val logger = KotlinLogging.logger {}

object TrackQueryParser : QueryParser<VirtualTrack> {

    private val parser = SpelExpressionParser()

    private val testContext: VirtualTrack = FileVirtualTrack("here-path-doesnt-matter")

    override fun validate(query: String): Boolean {
        return try {
            parser.parseExpression(query).getValue(testContext) is Boolean
        } catch (e: Exception) {
            logger.trace(e) { "Query evaluated as invalid" }
            false
        }
    }

    override fun execute(expression: Expression, context: VirtualTrack): Boolean {
        return try {
            expression.getValue(context) as Boolean
        } catch (e: Exception) {
            logger.debug(e) { "Query execution against '$context' failed" }
            false
        }
    }

    override fun execute(query: String, context: VirtualTrack): Boolean {
        return execute(parser.parseExpression(query), context)
    }

    override fun execute(query: String, context: List<VirtualTrack>): List<VirtualTrack> {
        val queryExpression = parser.parseExpression(query)
        return context.filter { execute(queryExpression, it) }
    }

}