package org.gravidence.gravifon.query

import mu.KotlinLogging
import org.gravidence.gravifon.domain.FileVirtualTrack
import org.gravidence.gravifon.domain.VirtualTrack
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class TrackQueryParser : QueryParser<VirtualTrack> {

    private val parser = SpelExpressionParser()

    private val testContext: VirtualTrack = FileVirtualTrack("~/test.flac")

    override fun validate(query: String): Boolean {
        return try {
            parser.parseExpression(query).getValue(testContext) is Boolean
        } catch (e: Exception) {
            logger.trace(e) { "Query evaluated as invalid" }
            false
        }
    }

    override fun execute(query: String, context: VirtualTrack): Boolean {
        return try {
            parser.parseExpression(query).getValue(context) as Boolean
        } catch (e: Exception) {
            logger.debug(e) { "Query execution against '$context' failed" }
            false
        }
    }

}