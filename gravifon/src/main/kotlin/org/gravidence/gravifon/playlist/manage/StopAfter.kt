package org.gravidence.gravifon.playlist.manage

data class StopAfter(private val n: Int = -1) {

    val activated: Boolean
        get() = n > -1
    val stop: Boolean
        get() = n == 0

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