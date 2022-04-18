package org.gravidence.gravifon

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.referentialEqualityPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.gravidence.gravifon.domain.notification.Notification
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.orchestration.marker.Viewable
import org.gravidence.gravifon.playback.PlaybackStatus
import org.gravidence.gravifon.playlist.Playlist
import org.gravidence.gravifon.playlist.manage.StopAfter
import org.gravidence.gravifon.ui.dialog.TrackMetadataState
import org.gravidence.gravifon.ui.state.PlaybackPositionState

object GravifonContext {

    val scopeDefault = CoroutineScope(Dispatchers.Default)
    val scopeIO = CoroutineScope(Dispatchers.IO)

    val activeView: MutableState<Viewable?> = mutableStateOf(null)
    val activePlaylist: MutableState<Playlist?> = mutableStateOf(null)
    // force state update handling, as active track is set to null on failing that may repeat
    val activeTrack: MutableState<VirtualTrack?> = mutableStateOf(null, neverEqualPolicy())
    val activeTrackExtraInfo: MutableState<List<String>> = mutableStateOf(listOf())
    val activeInnerNotification: MutableState<Notification?> = mutableStateOf(null, referentialEqualityPolicy())

    val applicationSettingsDialogVisible: MutableState<Boolean> = mutableStateOf(false)

    val pluginSettingsDialogVisible: MutableState<Boolean> = mutableStateOf(false)

    val trackMetadataDialogVisible: MutableState<Boolean> = mutableStateOf(false)
    val trackMetadataDialogState: TrackMetadataState = TrackMetadataState(
        tracks = mutableStateOf(listOf()),
        selectedTracks = mutableStateOf(setOf())
    )

    val playbackStatusState: MutableState<PlaybackStatus> = mutableStateOf(PlaybackStatus.STOPPED)
    val playbackPositionState: MutableState<PlaybackPositionState> = mutableStateOf(PlaybackPositionState(), referentialEqualityPolicy())
    val stopAfterState: MutableState<StopAfter> = mutableStateOf(StopAfter())

    fun setActiveTrack(track: VirtualTrack?) {
        activeTrack.value = track
        activeTrackExtraInfo.value = listOf()
    }

}