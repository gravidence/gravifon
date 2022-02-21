package org.gravidence.gravifon.plugin.scrobble.lastfm

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import mu.KotlinLogging
import org.gravidence.gravifon.configuration.FileStorage
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.orchestration.marker.Stateful
import org.gravidence.gravifon.plugin.scrobble.Scrobble
import org.gravidence.gravifon.plugin.scrobble.lastfm.event.LastfmScrobbleCacheUpdatedEvent
import org.gravidence.gravifon.util.serialization.gravifonSerializer
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.exists

private val logger = KotlinLogging.logger {}

@Component
class LastfmScrobblerStorage : Stateful {

    override val fileStorage: FileStorage = LastfmScrobblerFileStorage()

    private val scrobbleCache: MutableList<Scrobble> = mutableListOf()

    init {
        fileStorage.read()
    }

    @Synchronized
    fun scrobbleCache(): List<Scrobble> {
        return scrobbleCache.toList()
    }

    @Synchronized
    fun appendToScrobbleCache(scrobble: Scrobble) {
        scrobbleCache += scrobble
        publishUpdate()
    }

    @Synchronized
    fun removeFromScrobbleCache(scrobbles: List<Scrobble>) {
        scrobbleCache.removeAll(scrobbles)
        publishUpdate()
    }

    /**
     * Removes scrobbles by index from cache. Index is zero-based.
     */
    @Synchronized
    fun removeFromScrobbleCache(scrobbleIndexes: Set<Int>) {
        scrobbleIndexes.forEach { scrobbleCache.removeAt(it) }
        publishUpdate()
    }

    private fun publishUpdate() {
        EventBus.publish(LastfmScrobbleCacheUpdatedEvent(scrobbleCache))
    }

    inner class LastfmScrobblerFileStorage : FileStorage(storageDir = fileStorageHomeDir.resolve("lastfm-scrobbler")) {

        private val scrobbleCacheFile: Path = storageDir.resolve("cache")

        override fun read() {
            logger.debug { "Read scrobble cache from $scrobbleCacheFile" }

            try {
                if (scrobbleCacheFile.exists()) {
                    scrobbleCache.addAll(gravifonSerializer.decodeFromString<List<Scrobble>>(Files.readString(scrobbleCacheFile))).also {
                        logger.trace { "Scrobble cache loaded: $it" }
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to read scrobble cache from $scrobbleCacheFile" }
            }
        }

        override fun write() {
            logger.debug { "Write ${scrobbleCache.size} scrobbles to $scrobbleCacheFile" }

            try {
                val playlistAsString = gravifonSerializer.encodeToString(scrobbleCache).also {
                    logger.trace { "Scrobble cache to be persisted: $it" }
                }
                Files.writeString(
                    scrobbleCacheFile,
                    playlistAsString,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
                )
            } catch (e: Exception) {
                logger.error(e) { "Failed to write scrobble cache to $scrobbleCacheFile" }
            }
        }

    }

}