package org.gravidence.gravifon.util

fun <T> nullableListOf(element: T?): List<T>? {
    return if (element == null) {
        null
    } else {
        listOf(element)
    }
}

fun <T> firstNotEmptyOrNull(vararg collection: Collection<T>?): Collection<T>? {
    return collection.firstOrNull { it?.isNotEmpty() ?: false }
}

fun String.removePrefix(prefix: CharSequence, ignoreCase: Boolean): String {
    if (startsWith(prefix, ignoreCase)) {
        return substring(prefix.length)
    }
    return this
}

fun <T> Iterable<T>.joinViaSpace(): String {
    return joinToString(separator = " ")
}