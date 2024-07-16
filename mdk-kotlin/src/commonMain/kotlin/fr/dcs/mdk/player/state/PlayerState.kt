package fr.dcs.mdk.player.state


import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import fr.dcs.mdk.native.NativeMediaInfo
import fr.dcs.mdk.player.MediaType
import kotlin.time.Duration


@Stable
sealed class PlayerState {

  internal val _activeTracks = mutableStateMapOf(
    *MediaType.entries.map { it to listOf(0) }.toTypedArray()
  )

  internal abstract var _position: Duration
  internal abstract var _nativeMediaStatus: Int
  internal abstract var _nativeState: Int
  internal abstract var _nativeMediaInfo: NativeMediaInfo?

  abstract val position: Duration
  abstract val duration: Duration
  abstract val audio: List<Audio>
  abstract val video: List<Video>
  abstract val subtitles: List<Subtitle>
  abstract val playbackStatus: PlaybackStatus

  companion object {
    fun composeBased(): PlayerState = ComposePlayerState()
  }
}


@Immutable
sealed interface PlaybackStatus {
  @Immutable data object Idle : PlaybackStatus
  @Immutable data object Paused : PlaybackStatus
  @Immutable data object Stopped : PlaybackStatus
  @Immutable data object EndOfFile : PlaybackStatus
  @Immutable data class Playing(val isBuffering: Boolean) : PlaybackStatus
}
