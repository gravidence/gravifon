package org.gravidence.gravifon.playlist.manage

/**
 * StopAfter state holder. Defaults to deactivated state.
 */
data class StopAfter(private val n: Int = -1) {

    val activated: Boolean = n >= 0
    val stopHere: Boolean = n == 0

    fun setAndGet(n: Int): StopAfter {
        return copy(n = n)
    }

    fun decreaseAndGet(): StopAfter {
        return if (n >= 0) {
            copy(n = n - 1)
        } else {
            this
        }
    }

}