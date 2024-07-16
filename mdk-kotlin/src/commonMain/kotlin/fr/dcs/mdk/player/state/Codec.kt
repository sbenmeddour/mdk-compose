package fr.dcs.mdk.player.state

sealed interface Codec {
  val codec: String
  val tag: Int
}
data class AudioCodec(
  override val codec: String,
  override val tag: Int,
  val bitrate: Long,
  val profile: Int,
  val level: Int,
  val channels: Int,
  val sampleRate: Int,
) : Codec


data class VideoCodec(
  override val codec: String,
  override val tag: Int,
  val bitRate: Long,
  val profile: Int,
  val level: Int,
  val frameRate: Float,
  val format: Int,
  val width: Int,
  val height: Int,
  val bFrames: Int,
  val par: Float,
) : Codec

data class SubtitleCodec(
  override val codec: String,
  override val tag: Int,
) : Codec