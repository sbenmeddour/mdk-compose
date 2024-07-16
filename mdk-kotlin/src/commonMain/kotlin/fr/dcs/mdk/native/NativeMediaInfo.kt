package fr.dcs.mdk.native

import androidx.compose.runtime.Immutable

@Immutable
internal data class NativeMediaInfo(
  val startTime: Long,
  val duration: Long,
  val bitRate: Long,
  val size: Long,
  val format: String,
  val streams: Int,
  //todo: val chapters: List<NativeChapter>,
  //todo: val metaData: NativeMetaData,
  val audio: List<NativeAudioStream>,
  val video: List<NativeVideoStream>,
  val subtitles: List<NativeSubtitle>,
  //todo: val programInfo: List<NativeProgramInfo>,
)

//todo: data class NativeMetaData

@Immutable
internal data class NativeAudioStream(
  val index: Int,
  val startTime: Long,
  val duration: Long,
  val frames: Long,
  val metaData: Map<String, String>,
  val codec: NativeAudioCodec,
)

@Immutable
internal data class NativeAudioCodec(
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
internal data class NativeVideoStream(
  val index: Int,
  val startTime: Long,
  val duration: Long,
  val frames: Long,
  val rotation: Int,
  val metaData: Map<String, String>,
  val codec: NativeVideoCodec,
  //todo: val imageData: ByteArray,
  //todo: val imageSize: Int,
)

@Immutable
internal data class NativeVideoCodec(
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
internal data class NativeSubtitle(
  val index: Int,
  val startTime: Long,
  val duration: Long,
  val metaData: Map<String, String>,
  val codec: NativeSubtitleCodec,
)


@Immutable
internal data class NativeSubtitleCodec(
  val codec: String,
  val codecTag: Int,
  //todo: val extraData: ByteArray,
  //todo: val extraDataSize: Int,
  //todo: val width: Int,
  //todo: val height: Int,
)