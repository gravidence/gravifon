package org.gravidence.gravifon.util.serialization

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.gravidence.gravifon.configuration.ComponentConfiguration
import org.gravidence.gravifon.playback.backend.gstreamer.GstreamerAudioBackend
import org.gravidence.gravifon.plugin.autosaveus.AutosaveUs
import org.gravidence.gravifon.plugin.bandcamp.Bandcamp
import org.gravidence.gravifon.plugin.library.Library
import org.gravidence.gravifon.plugin.queue.Queue
import org.gravidence.gravifon.plugin.scrobble.lastfm.LastfmScrobbler

val gravifonSerializersModule = SerializersModule {
    contextual(DurationAsStringSerializer)
    polymorphic(ComponentConfiguration::class) {
        subclass(Library.LibraryComponentConfiguration::class)
        subclass(Queue.QueueComponentConfiguration::class)
        subclass(LastfmScrobbler.LastfmScrobblerComponentConfiguration::class)
        subclass(Bandcamp.BandcampComponentConfiguration::class)
        subclass(AutosaveUs.AutosaveUsComponentConfiguration::class)
        subclass(GstreamerAudioBackend.GstreamerComponentConfiguration::class)
    }
}

val gravifonSerializer = Json { serializersModule = gravifonSerializersModule }