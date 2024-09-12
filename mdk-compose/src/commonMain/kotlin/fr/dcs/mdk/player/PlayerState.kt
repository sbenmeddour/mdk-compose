package fr.dcs.mdk.player

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.*
import fr.dcs.mdk.player.models.*
import fr.dcs.mdk.player.models.State
import kotlin.time.*
import kotlin.time.Duration.Companion.milliseconds

typealias ComposeState<T> = androidx.compose.runtime.State<T>

@Stable
class PlayerState  {

  internal var state: State by mutableStateOf(State.Stopped)
  internal var mediaInfo: MediaInfo by mutableStateOf(MediaInfo.empty)
  internal val activeTracks = mutableStateMapOf<MediaType, List<Track.Id>>()
  internal var _position: Duration by mutableStateOf(Duration.ZERO)

  internal var status: MediaStatus by mutableStateOf(MediaStatus.NoMedia)


  val playbackStatus by derivedStateOf { PlaybackStatus(state, status) }

  val position: Duration
    get() = _position

  val duration: Duration
    get() = mediaInfo.duration.milliseconds

  val tracks: TracksHolder = @Stable object : TracksHolder {

    override val video: List<Track.Video> by derivedStateOf {
      this@PlayerState.mediaInfo
        .video
        .map {
          val isActive = Track.Id(it.index) in activeTracks[MediaType.Video].orEmpty()
          it.asTrack(isActive)
        }
    }

    override val audio: List<Track.Audio> by derivedStateOf {
      this@PlayerState.mediaInfo
        .audio
        .map {
          val isActive = Track.Id(it.index) in activeTracks[MediaType.Audio].orEmpty()
          it.asTrack(isActive)
        }
    }

    override val subtitles: List<Track.Subtitle> by derivedStateOf {
      this@PlayerState.mediaInfo
        .subtitles
        .map {
          val isActive = Track.Id(it.index) in activeTracks[MediaType.Subtitles].orEmpty()
          it.asTrack(isActive)
        }
    }

  }


}


interface TracksHolder {
  val video: List<Track.Video>
  val audio: List<Track.Audio>
  val subtitles: List<Track.Subtitle>
}

operator fun TracksHolder.get(type: MediaType): List<Track> = when (type) {
  MediaType.Unknown -> emptyList()
  MediaType.Video -> video
  MediaType.Audio -> audio
  MediaType.Subtitles -> subtitles
}