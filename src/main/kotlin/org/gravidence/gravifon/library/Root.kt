package org.gravidence.gravifon.library

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.gravidence.gravifon.domain.PhysicalTrack
import org.gravidence.gravifon.domain.VirtualTrack
import org.jaudiotagger.audio.AudioFileFilter
import org.jaudiotagger.audio.AudioFileIO
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

@Serializable
class Root(
    val rootDir: String,
    var watchForChanges: Boolean = false,
    var scanOnInit: Boolean = false,
    val tracks: MutableList<VirtualTrack> = ArrayList()
) {

    @Transient
    private val audioFileFilter = AudioFileFilter(false)

    init {
        if (scanOnInit) {
            scan()
        }
    }

    fun scan(): List<VirtualTrack> {
        tracks.clear()
        tracks.addAll(Files.walk(Path.of(rootDir))
            .map { it.toFile() }
            .filter { audioFileFilter.accept(it) }
            .map { PhysicalTrack(AudioFileIO.read(it)).toVirtualTrack() }
            .toList())

        return tracks
    }

}