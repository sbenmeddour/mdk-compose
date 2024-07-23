package fr.dcs.mdk.player.models

import androidx.compose.runtime.Immutable
import fr.dcs.mdk.player.*

@Immutable
internal data class MediaInfo(
  val startTime: Long,
  val duration: Long,
  val bitRate: Long,
  val size: Long,
  val format: String,
  val streams: Int,
  //todo: val chapters: List<Chapter>,
  //todo: val metaData: MetaData,
  val audio: List<AudioStream>,
  val video: List<VideoStream>,
  val subtitles: List<Subtitle>,
  //todo: val programInfo: List<ProgramInfo>,
) {
  companion object
}

//todo: data class MetaData

@Immutable
internal data class AudioStream(
  override val index: Int,
  val startTime: Long,
  val duration: Long,
  val frames: Long,
  val metaData: Map<String?, String?>,
  val codec: AudioCodec,
) : Stream

@Immutable
internal data class AudioCodec(
  val codec: String,
  val codecTag: Int,
  //todo: val extraData: ByteArray,
  val bitRate: Long,
  val profile: Int,
  val level: Int,
  val frameRate: Float,
  val isFloat: Boolean,
  val isUnsigned: Boolean,
  val isPlanar: Boolean,
  val rawSampleSize: Int,
  val channels: Int,
  val sampleRate: Int,
  val blockAlign: Int,
  val frameSize: Int,
)


@Immutable
internal data class VideoStream(
  override val index: Int,
  val startTime: Long,
  val duration: Long,
  val frames: Long,
  val rotation: Int,
  val metaData: Map<String?, String?>,
  val codec: VideoCodec,
  //todo: val imageData: ByteArray,
  //todo: val imageSize: Int,
) : Stream

@Immutable
internal data class VideoCodec(
  val codec: String,
  val codecTag: Int,
  //todo: val extraData: ByteArray,
  //todo: val extraDataSize: Int,
  val bitRate: Long,
  val profile: Int,
  val level: Int,
  val frameRate: Float,
  val format: Int,
  val formatName: String,
  val width: Int,
  val height: Int,
  val bFrames: Int,
  val par: Float,
)

@Immutable
internal data class Subtitle(
  override val index: Int,
  val startTime: Long,
  val duration: Long,
  val metaData: Map<String?, String?>,
  val codec: SubtitleCodec,
) : Stream

internal interface Stream { val index: Int }

@Immutable
internal data class SubtitleCodec(
  val codec: String,
  val codecTag: Int,
  //todo: val extraData: ByteArray,
  //todo: val extraDataSize: Int,
  //todo: val width: Int,
  //todo: val height: Int,
)

internal fun MediaInfo?.indexOf(type: MediaType, trackIndex: Int): Int? {
  if (this == null) return null
  return when (type) {
    MediaType.Unknown -> null
    MediaType.Video -> video.indexOfFirst { it.index == trackIndex }
    MediaType.Audio -> audio.indexOfFirst { it.index == trackIndex }
    MediaType.Subtitles -> subtitles.indexOfFirst { it.index == trackIndex }
  }

}

internal val MediaInfo.Companion.empty: MediaInfo
  get() = MediaInfo(
    startTime = 0L,
    duration = 0L,
    bitRate = 0L,
    size = 0L,
    format = "",
    streams = 0,
    video = emptyList(),
    subtitles = emptyList(),
    audio = emptyList(),
  )

internal operator fun MediaInfo.get(type: MediaType): List<Stream> = when (type) {
  MediaType.Unknown -> emptyList()
  MediaType.Video -> video
  MediaType.Audio -> audio
  MediaType.Subtitles -> subtitles
}