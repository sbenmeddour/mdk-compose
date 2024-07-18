package fr.dcs.mdk.player.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fr.dcs.mdk.native.NativeMediaInfo
import fr.dcs.mdk.native.NativeMediaStatus
import fr.dcs.mdk.native.NativeState
import fr.dcs.mdk.player.MediaType
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Stable
class ComposePlayerState internal constructor() : PlayerState() {

  override var _position: Duration by mutableStateOf(Duration.ZERO)
  override var _nativeMediaStatus: Int by mutableIntStateOf(0)
  override var _nativeState: Int by mutableIntStateOf(0)

  private var nativeMediaInfo: NativeMediaInfo? by mutableStateOf(null)

  override var _nativeMediaInfo: NativeMediaInfo?
    get() = nativeMediaInfo
    set(value) {
      nativeMediaInfo = value
      activeTracks[MediaType.Video] = listOf(value?.video?.firstOrNull()?.index ?: 0)
      activeTracks[MediaType.Audio] = listOf(value?.audio?.firstOrNull()?.index ?: 0)
      activeTracks[MediaType.Subtitle] = listOf(value?.subtitles?.firstOrNull()?.index ?: 0)
    }

  override val position: Duration
    get() = this._position

  override val duration: Duration
    get() = this._nativeMediaInfo?.duration?.milliseconds ?: Duration.ZERO

  override val audio: List<Audio>
    get() = this._nativeMediaInfo
      ?.audio
      .orEmpty()
      .map {
        Audio(
          index = it.index,
          isActive = activeTracks[MediaType.Audio]?.contains(it.index) == true,
          metaData = it.metaData,
          codec = AudioCodec(
            codec = it.codec.codec,
            tag = it.codec.codecTag,
            bitrate = it.codec.bitRate,
            profile = it.codec.profile,
            level = it.codec.level,
            channels = it.codec.channels,
            sampleRate = it.codec.sampleRate,
          )
        )
      }

  override val video: List<Video>
    get() = this._nativeMediaInfo
      ?.video
      .orEmpty()
      .map {
        Video(
          index = it.index,
          isActive = activeTracks[MediaType.Video]?.contains(it.index) == true,
          metaData = it.metaData,
          frames = it.frames,
          rotation = it.rotation,
          codec = VideoCodec(
            codec = it.codec.codec,
            tag = it.codec.codecTag,
            profile = it.codec.profile,
            level = it.codec.level,
            bitRate = it.codec.bitRate,
            frameRate = it.codec.frameRate,
            format = it.codec.format,
            width = it.codec.width,
            height = it.codec.height,
            bFrames = it.codec.bFrames,
            par = it.codec.par,
          )
        )
      }

  override val subtitles: List<Subtitle>
    get() = this._nativeMediaInfo
      ?.subtitles
      .orEmpty()
      .map {
        Subtitle(
          index = it.index,
          isActive = activeTracks[MediaType.Subtitle]?.contains(it.index) == true,
          metaData = it.metaData,
          codec = SubtitleCodec(
            codec = it.codec.codec,
            tag = it.codec.codecTag,
          )
        )
      }

  override val playbackStatus: PlaybackStatus
    get() = when (this._nativeState) {
      NativeState.Playing.nativeValue -> {
        val status = this._nativeMediaStatus
        when {
          NativeMediaStatus.NoMedia and status -> PlaybackStatus.Idle
          NativeMediaStatus.Unloaded and status -> PlaybackStatus.Idle
          NativeMediaStatus.End and status -> PlaybackStatus.EndOfFile
          else -> {
            val isBuffering = NativeMediaStatus.Buffering and status
            PlaybackStatus.Playing(isBuffering)

          }
        }
      }
      NativeState.Stopped.nativeValue -> PlaybackStatus.Stopped
      NativeState.Paused.nativeValue -> PlaybackStatus.Paused
      else -> PlaybackStatus.Stopped
    }

  override fun equals(other: Any?): Boolean {
    return other != null && other === this
  }

  override fun hashCode(): Int {
    var result = _position.hashCode()
    result = 31 * result + _nativeMediaStatus
    result = 31 * result + _nativeState
    result = 31 * result + (_nativeMediaInfo?.hashCode() ?: 0)
    return result
  }

}