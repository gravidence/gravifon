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

fun <T> Iterable<T>.joinViaSpace(): String {
    return joinToString(separator = " ")
}