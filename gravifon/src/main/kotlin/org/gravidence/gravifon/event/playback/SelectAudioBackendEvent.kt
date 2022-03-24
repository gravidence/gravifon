package org.gravidence.gravifon.event.playback

import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.playback.backend.AudioBackend

/**
 * Use selected audio backend.
 */
class SelectAudioBackendEvent(val audioBackend: AudioBackend) : Event