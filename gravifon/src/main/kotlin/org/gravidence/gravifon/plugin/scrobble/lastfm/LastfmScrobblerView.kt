package org.gravidence.gravifon.plugin.scrobble.lastfm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.orchestration.marker.EventAware
import org.gravidence.gravifon.orchestration.marker.Viewable
import org.gravidence.gravifon.playlist.DynamicPlaylist
import org.gravidence.gravifon.playlist.Playlist
import org.gravidence.gravifon.playlist.item.PlaylistItem
import org.gravidence.gravifon.playlist.item.TrackPlaylistItem
import org.gravidence.gravifon.plugin.scrobble.Scrobble
import org.gravidence.gravifon.plugin.scrobble.lastfm.event.LastfmScrobbleCacheUpdatedEvent
import org.gravidence.gravifon.ui.PlaylistComposable
import org.gravidence.gravifon.ui.rememberPlaylistState
import org.springframework.stereotype.Component

@Component
class LastfmScrobblerView(val lastfmScrobbler: LastfmScrobbler) : Viewable, EventAware {

    private val playlist: Playlist = DynamicPlaylist(
        ownerName = "Last.fm Scrobbler",
        displayName = "Scrobble Cache",
        items = scrobbleCacheToPlaylistItems(lastfmScrobbler.lastfmScrobblerStorage.scrobbleCache())
    )
    private val playlistItems: MutableState<List<PlaylistItem>> = mutableStateOf(playlist.items())

    override fun consume(event: Event) {
        if (event is LastfmScrobbleCacheUpdatedEvent) {
            val scrobbleCachePlaylistItems = scrobbleCacheToPlaylistItems(event.scrobbleCache)
            playlist.replace(scrobbleCachePlaylistItems)
            playlistItems.value = scrobbleCachePlaylistItems
        }
    }

    override fun viewDisplayName(): String {
        return lastfmScrobbler.pluginDisplayName
    }

    inner class LastfmScrobblerViewState(
        val playlistItems: MutableState<List<PlaylistItem>>,
        val playlist: Playlist
    )

    @Composable
    fun rememberLastfmScrobblerViewState(
        playlistItems: MutableState<List<PlaylistItem>>,
        playlist: Playlist
    ) = remember(playlistItems) { LastfmScrobblerViewState(playlistItems, playlist) }

    @Composable
    override fun composeView() {
        val lastfmScrobblerViewState = rememberLastfmScrobblerViewState(
            playlistItems = playlistItems,
            playlist = playlist
        )
        val playlistState = rememberPlaylistState(
            playlistItems = playlistItems,
            playlist = playlist,
        )

        Box(
            modifier = Modifier
                .padding(5.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(5.dp)
                ) {
                    Text(
                        text = "Scrobbles: ${playlistItems.value.size}",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .weight(1f)
                    )
                    Button(
                        enabled = playlistItems.value.isNotEmpty(),
                        onClick = { lastfmScrobbler.scrobble() }
                    ) {
                        Text("Scrobble")
                    }
                }
                Divider(color = Color.Transparent, thickness = 2.dp)
                Row {
                    PlaylistComposable(playlistState)
                }
            }
        }
    }

    private fun scrobbleCacheToPlaylistItems(scrobbleCache: List<Scrobble>): MutableList<PlaylistItem> {
        return scrobbleCache.map { TrackPlaylistItem(it.track) }.toMutableList()
    }

}