package fr.dcs.mdk.player.models

import androidx.compose.runtime.*

@Immutable
class PlaybackStatus internal constructor(
  internal val state: State,
  internal val status: MediaStatus,
) {

  val isPlaying: Boolean
    get() = state == State.Playing

  val isPaused: Boolean
    get() = state == State.Paused

  val isStopped: Boolean
    get() = state == State.Stopped

  val hasMedia: Boolean
    get() = status and MediaStatus.NoMedia

  val endOfFile: Boolean
    get() = status and MediaStatus.End

  val isBuffering: Boolean
    get() = status and MediaStatus.Buffering

  val isSeeking: Boolean
    get() = status and MediaStatus.Seeking

  val isLoading: Boolean
    get() = status and MediaStatus.Loading

  val isLoaded: Boolean
    get() = status and MediaStatus.Loaded

  val isUnloaded: Boolean
    get() = status and MediaStatus.Unloaded

  val isPrepared: Boolean
    get() = status and MediaStatus.Prepared


}