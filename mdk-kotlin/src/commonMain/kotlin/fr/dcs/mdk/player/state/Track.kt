package fr.dcs.mdk.player.state

sealed interface Track {
  val isActive: Boolean
  val index: Int
}

data class Audio(
  override val index: Int,
  override val isActive: Boolean,
  val metaData: Map<String, String>,
  val codec: AudioCodec,
) : Track

data class Video(
  override val index: Int,
  override val isActive: Boolean,
  val metaData: Map<String, String>,
  val frames: Long,
  val rotation: Int,
  val codec: VideoCodec,
) : Track

data class Subtitle(
  override val index: Int,
  override val isActive: Boolean,
  val metaData: Map<String, String>,
  val codec: SubtitleCodec,
) : Track