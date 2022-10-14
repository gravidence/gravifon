package org.gravidence.gravifon.util

class DualStateObject<T>(private val defaultState: T, private val otherState: T) {

    fun state(default: Boolean = true): T {
        return if (default) {
            defaultState
        } else {
            otherState
        }
    }

}