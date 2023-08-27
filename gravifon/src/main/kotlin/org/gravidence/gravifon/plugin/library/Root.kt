package org.gravidence.gravifon.plugin.library

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mu.KotlinLogging
import org.gravidence.gravifon.domain.track.PhysicalTrack
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.jaudiotagger.audio.AudioFileFilter
import org.jaudiotagger.audio.AudioFileIO
import java.nio.file.Files
import java.nio.file.Path
import kotlin.time.DurationUnit
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}

@Serializable
class Root(
    val rootDir: String,
    var watchForChanges: Boolean = false,
    var scanOnInit: Boolean = false,
    val tracks: MutableList<VirtualTrack> = ArrayList()
) {

    @Transient
    private val audioFileFilter = AudioFileFilter(false)

    @Synchronized
    fun scan(): List<VirtualTrack> {
        logger.info { "Library root ($rootDir) scan started" }

        val scanDuration = measureTime {
            try {
                val tracksFromRoot = Files.walk(Path.of(rootDir))
                    .map { it.toFile() }
                    .filter { audioFileFilter.accept(it) }
                    .map { PhysicalTrack(AudioFileIO.read(it)).toVirtualTrack() }
                    .toList()

                tracks.clear()
                tracks.addAll(tracksFromRoot)
            } catch (e: Exception) {
                logger.error(e) { "Failed to scan library root: $rootDir" }
            }
        }

        logger.info { "Library root ($rootDir) scan completed (processed in ${scanDuration.toString(DurationUnit.MILLISECONDS)})" }

        return tracks
    }

}