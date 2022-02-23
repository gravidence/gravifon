package org.gravidence.gravifon.util

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class CommonUtilTest {

    @Test
    fun nullableListOf() {
        assertEquals(null, nullableListOf(null))
        assertEquals(listOf(10), nullableListOf(10))
    }

    @Test
    fun firstNotEmptyOrNull() {
        assertEquals(null, firstNotEmptyOrNull<Int>(null))
        assertEquals(null, firstNotEmptyOrNull<Int>(null, listOf()))
        assertEquals(null, firstNotEmptyOrNull<Int>(listOf(), null))
        assertEquals(listOf(20), firstNotEmptyOrNull<Int>(null, listOf(20)))
        assertEquals(listOf(10), firstNotEmptyOrNull<Int>(listOf(10), listOf(20)))
        assertEquals(listOf(10), firstNotEmptyOrNull<Int>(listOf(10), null))
    }

}