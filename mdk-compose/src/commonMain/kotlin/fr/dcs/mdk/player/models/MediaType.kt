package fr.dcs.mdk.player.models

import androidx.compose.runtime.*

@Immutable
enum class MediaType { Unknown, Video, Audio, Subtitles }

internal val MediaType.nativeValue: Int
  get() = when (this) {
    MediaType.Unknown -> -1
    MediaType.Video -> 0
    MediaType.Audio -> 1
    MediaType.Subtitles -> 3
  }