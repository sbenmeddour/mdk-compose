package fr.dcs.mdk.player.models

import androidx.compose.runtime.*

@Immutable
sealed class MediaStatus(internal val nativeValue: Int) {
  internal data object NoMedia : MediaStatus(0)
  internal data object Unloaded : MediaStatus(1)
  internal data object Loading : MediaStatus(1 shl 1)
  internal data object Loaded : MediaStatus(1 shl 2)
  internal data object Prepared : MediaStatus(1 shl 8)
  internal data object Stalled : MediaStatus(1 shl 3)
  internal data object Buffering : MediaStatus(1 shl 4)
  internal data object Buffered : MediaStatus(1 shl 5)
  internal data object End : MediaStatus(1 shl 6)
  internal data object Seeking : MediaStatus(1 shl 7)
  internal data object Invalid : MediaStatus(1 shl 31)
  class Mixed internal constructor(value: Int) : MediaStatus(value)
}

infix fun MediaStatus.and(other: MediaStatus): Boolean = this.nativeValue and other.nativeValue != 0

