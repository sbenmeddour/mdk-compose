package fr.dcs.mdk.player


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fr.dcs.mdk.native.NativeMediaStatus
import fr.dcs.mdk.native.NativeState
import kotlin.time.Duration

abstract class PlayerState internal constructor() {

  abstract var position: Duration internal set
  abstract var duration: Duration internal set

  internal abstract var nativeMediaStatus: Int
  internal abstract var nativeState: Int

  val playbackStatus: PlaybackStatus
    get() = when (this.nativeState) {
      NativeState.Playing.nativeValue -> {
        when {
          NativeMediaStatus.NoMedia and nativeMediaStatus -> PlaybackStatus.Idle
          NativeMediaStatus.Unloaded and nativeMediaStatus -> PlaybackStatus.Idle
          NativeMediaStatus.End and nativeMediaStatus -> PlaybackStatus.EndOfFile
          else -> {
            val isBuffering = NativeMediaStatus.Buffering and nativeMediaStatus
            PlaybackStatus.Playing(isBuffering)

          }
        }
      }
      NativeState.Stopped.nativeValue -> PlaybackStatus.Stopped
      NativeState.Paused.nativeValue -> PlaybackStatus.Paused
      else -> PlaybackStatus.Stopped
    }

  companion object {
    fun composeBasedState(): ComposeState = ComposeState()
  }
}


sealed interface PlaybackStatus {
  data object Idle : PlaybackStatus
  data object Paused : PlaybackStatus
  data object Stopped : PlaybackStatus
  data object EndOfFile : PlaybackStatus
  data class Playing(val isBuffering: Boolean) : PlaybackStatus
}

class ComposeState internal constructor() : PlayerState() {
  override var position: Duration by mutableStateOf(Duration.ZERO)
  override var duration: Duration by mutableStateOf(Duration.ZERO)
  override var nativeMediaStatus: Int by mutableStateOf(0)
  override var nativeState: Int by mutableStateOf(0)
}