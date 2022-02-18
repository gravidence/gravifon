package org.gravidence.gravifon

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.orchestration.marker.Viewable
import org.gravidence.gravifon.playback.PlaybackState
import org.gravidence.gravifon.playlist.Playlist
import org.gravidence.gravifon.ui.state.PlaybackPositionState

object GravifonContext {

    val scopeDefault = CoroutineScope(Dispatchers.Default)
    val scopeIO = CoroutineScope(Dispatchers.IO)

    val activeView: MutableState<Viewable?> = mutableStateOf(null)
    val activePlaylist: MutableState<Playlist?> = mutableStateOf(null)
    val activeVirtualTrack: MutableState<VirtualTrack?> = mutableStateOf(null)

    val pluginSettingsDialogVisible: MutableState<Boolean> = mutableStateOf(false)

    val playbackState: MutableState<PlaybackState> = mutableStateOf(PlaybackState.STOPPED)
    val playbackPositionState: MutableState<PlaybackPositionState> = mutableStateOf(PlaybackPositionState())

}