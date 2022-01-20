package org.gravidence.gravifon.library

import org.gravidence.gravifon.Initializable
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.EventConsumer
import org.springframework.stereotype.Component

@Component
class Library : Initializable, EventConsumer() {

    private var isInitialized: Boolean = false

    private val roots: MutableList<Root> = ArrayList()

    fun init(roots: MutableList<Root>) {
        isInitialized = false

        this.roots.clear()
        this.roots.addAll(roots)

        isInitialized = true
    }

    fun getRoots(): List<Root> {
        if (!isInitialized) {
            TODO("Not yet implemented")
        }
        return roots.toList()
    }

    fun addRoot(root: Root) {
        if (roots.none { it.rootDir == root.rootDir }) {
            roots.add(root)
        }
    }

    fun random(): VirtualTrack {
        if (!isInitialized) {
            TODO("Not yet implemented")
        }
        return roots.first().tracks.random()
    }

    override fun isInitialized(): Boolean {
        return isInitialized
    }

    override fun consume(event: Event) {
//        TODO("Not yet implemented")
    }

}