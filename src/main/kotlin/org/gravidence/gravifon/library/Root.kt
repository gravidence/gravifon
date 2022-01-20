package org.gravidence.gravifon.library

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mu.KotlinLogging
import org.gravidence.gravifon.domain.PhysicalTrack
import org.gravidence.gravifon.domain.VirtualTrack
import org.jaudiotagger.audio.AudioFileFilter
import org.jaudiotagger.audio.AudioFileIO
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

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

    init {
        logger.info { "Initialize library root: $rootDir" }

        if (scanOnInit) {
            scan()
        }
    }

    fun scan(): List<VirtualTrack> {
        logger.info { "Library root ($rootDir) scan started" }
        val scanStartedAt = Clock.System.now()

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

        val scanFinishedAt = Clock.System.now()
        logger.info { "Library root ($rootDir) scan completed (processed in ${scanFinishedAt.minus(scanStartedAt).inWholeSeconds}s)" }

        return tracks
    }

}