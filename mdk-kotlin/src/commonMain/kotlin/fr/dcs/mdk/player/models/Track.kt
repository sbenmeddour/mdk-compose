package fr.dcs.mdk.player.models

import androidx.compose.runtime.*

//TODO: Add more properties
@Immutable
sealed interface Track {

  data class Id(val value: Int)

  val id: Id
  val metaData: Map<String, String>
  val codec: String
  val isActive: Boolean

  val type: MediaType
    get() = when (this) {
      is Audio -> MediaType.Audio
      is Subtitle -> MediaType.Subtitles
      is Video -> MediaType.Video
    }

  data class Video(
    override val id: Id,
    override val metaData: Map<String, String>,
    override val codec: String,
    override val isActive: Boolean,
    val size: VideoSize,
    val frameRate: Float,
  ): Track

  data class Audio(
    override val id: Id,
    override val metaData: Map<String, String>,
    override val codec: String,
    override val isActive: Boolean,
    val channels: Int,
  ): Track

  data class Subtitle(
    override val id: Id,
    override val metaData: Map<String, String>,
    override val codec: String,
    override val isActive: Boolean,
  ): Track


}

data class VideoSize(val width: Int, val height: Int)

private fun Map<String?, String?>.filterNotNulls(): Map<String, String> {
  return this.entries
    .mapNotNull {
      when {
        it.key.isNullOrBlank() -> null
        it.value.isNullOrBlank() -> null
        else -> it.key!! to it.value!!
      }
    }
    .toMap()
}

internal fun AudioStream.asTrack(isActive: Boolean): Track.Audio = Track.Audio(
  id = Track.Id(index),
  metaData = metaData.filterNotNulls(),
  channels = codec.channels,
  codec = codec.codec,
  isActive = isActive,
)

internal fun Subtitle.asTrack(isActive: Boolean): Track.Subtitle = Track.Subtitle(
    id = Track.Id(index),
    metaData = metaData.filterNotNulls(),
    codec = codec.codec,
    isActive = isActive,
  )

internal fun VideoStream.asTrack(isActive: Boolean): Track.Video = Track.Video(
    id = Track.Id(index),
    metaData = metaData.filterNotNulls(),
    codec = codec.codec,
    size = VideoSize(codec.width, codec.height),
    frameRate = codec.frameRate,
    isActive = isActive,
  )