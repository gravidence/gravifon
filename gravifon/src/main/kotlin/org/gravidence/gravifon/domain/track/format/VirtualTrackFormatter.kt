package org.gravidence.gravifon.domain.track.format

import org.gravidence.gravifon.domain.track.VirtualTrack

const val TOKEN_MARKER = '%'
val SPLIT_REGEX = """[^%]+|%.*?%""".toRegex()

class VirtualTrackPattern(val pattern: String) {

    val selectors: Collection<(VirtualTrack) -> String> = SPLIT_REGEX
        .findAll(pattern)
        .toList()
        .map { matchResult ->
            val group = matchResult.groupValues[0]
            if (group == "$TOKEN_MARKER$TOKEN_MARKER") {
                { "%" }
            } else if (group[0] == TOKEN_MARKER) {
                { track ->
                    val token = group.filterNot { c -> c == TOKEN_MARKER }.uppercase()
                    VirtualTrackFormatSelectors.entries.find { it.name == token }?.selector?.invoke(track)?.toString() ?: ""
                }
            } else {
                { group }
            }
        }

}

fun VirtualTrack.format(pattern: String): String {
    return format(VirtualTrackPattern(pattern))
}

fun VirtualTrack.format(pattern: VirtualTrackPattern): String {
    return pattern.selectors.joinToString(separator = "") { it(this) }
}