@file:OptIn(ExperimentalMaterial3Api::class)

package fr.dcs.mdk

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.VideoSettings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import fr.dcs.mdk.player.MediaType
import fr.dcs.mdk.player.Player
import fr.dcs.mdk.player.SeekFlag
import fr.dcs.mdk.player.state.Audio
import fr.dcs.mdk.player.state.PlaybackStatus
import fr.dcs.mdk.player.state.Subtitle
import fr.dcs.mdk.player.state.Track
import fr.dcs.mdk.player.state.Video
import fr.dcs.mdk.ui.PlayerView
import fr.dcs.mdk.ui.rememberPlayer
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Duration

@Composable
@Preview
fun App() {
  MaterialTheme(
    colorScheme = darkColorScheme(),
    content = {
      Surface(
        modifier = Modifier.fillMaxSize(),
        content = {
          Scaffold(
            modifier = Modifier,
            topBar = {
              TopAppBar(
                title = { Text("MDK Compose") },
              )
            },
            content = {
              PlayerPage(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(top = it.calculateTopPadding()),
              )
            },
          )
        },
      )
    }
  )

}

@Composable
fun PlayerPage(modifier: Modifier = Modifier) {
  val player = rememberPlayer()
  var isReady by remember { mutableStateOf(false) }
  var isResumed by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    println("Launched effect dans da mere")
    //player.setMedia("https://vfx.mtime.cn/Video/2021/11/16/mp4/211116131456748178.mp4")
    player.setMedia("https://lafibre.info/videos/test/201411_blender_big_buck_bunny_60fps_2160p_hevc.mp4")
    //player.setMedia("https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear1/prog_index.m3u8")
    player.prepare()
    isReady = true
  }

  LaunchedEffect(isResumed, isReady) {
    when {
      !isResumed -> player.pause()
      isReady -> player.play()
    }
  }

  Box(
    modifier = modifier,
    content = {
      PlayerView(
        modifier = Modifier
          .fillMaxSize(),
        player = player,
      )
      Controls(
        modifier = Modifier.fillMaxSize(),
        player = player
      )
    },
  )




  val owner = LocalLifecycleOwner.current
  DisposableEffect(Unit) {
    val observer = LifecycleEventObserver { _, event ->
      val targetState = event.targetState
      isResumed = targetState == Lifecycle.State.RESUMED
    }
    owner.lifecycle.addObserver(observer)
    onDispose {
      owner.lifecycle.removeObserver(observer)
      player.stop()
      player.release()
    }
  }
}




@Composable
fun Controls(
  modifier: Modifier = Modifier,
  player: Player,
) {
  var isVisible by remember { mutableStateOf(true) }
  val sliderSource = remember { MutableInteractionSource() }
  val isSliding by sliderSource.collectIsDraggedAsState()

  val playbackProgress = remember {
    derivedStateOf {
      val duration = player.state.duration
      val result = when {
        duration == Duration.ZERO -> 0f
        else -> (player.state.position / duration).toFloat()
      }
      result.coerceIn(0f, 1f)
    }
  }

  val userSliderPosition = remember { mutableFloatStateOf(0f) }
  Box(
    modifier = modifier
      .fillMaxSize()
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = { isVisible = !isVisible },
      ),
    content = {
      AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        content = {
          Box(
            modifier = Modifier.fillMaxSize(),
            content = {
              Row(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                content = {
                  val onTrackClicked = { track: Track ->
                    when {
                      track.isActive -> player.setTrack(track.type, -1)
                      else -> player.setTrack(track.type, track.index)
                    }
                  }
                  TrackSelector(
                    icon = Icons.Rounded.VideoSettings,
                    tracks = remember { derivedStateOf { player.state.video } },
                    onClick = onTrackClicked,
                  )
                  TrackSelector(
                    icon = Icons.Rounded.Audiotrack,
                    tracks = remember { derivedStateOf { player.state.audio } },
                    onClick = onTrackClicked,
                  )
                  TrackSelector(
                    icon = Icons.Rounded.Subtitles,
                    tracks = remember { derivedStateOf { player.state.subtitles } },
                    onClick = onTrackClicked,
                  )
                }
              )

              IconButton(
                modifier = Modifier
                  .align(Alignment.Center),
                onClick = {
                  when (player.state.playbackStatus) {
                    PlaybackStatus.EndOfFile -> player.play()
                    PlaybackStatus.Idle -> player.play()
                    PlaybackStatus.Paused -> player.play()
                    is PlaybackStatus.Playing -> player.pause()
                    PlaybackStatus.Stopped -> player.play()
                  }
                },
                content = {
                  Icon(
                    modifier = Modifier.size(64.dp),
                    imageVector = when (player.state.playbackStatus) {
                      PlaybackStatus.EndOfFile, PlaybackStatus.Stopped -> Icons.Rounded.RestartAlt
                      PlaybackStatus.Idle -> Icons.Rounded.PlayArrow
                      PlaybackStatus.Paused -> Icons.Rounded.PlayArrow
                      is PlaybackStatus.Playing -> Icons.Rounded.Pause
                    },
                    contentDescription = null,
                  )
                }
              )

              Column(
                modifier = Modifier
                  .align(Alignment.BottomCenter)
                  .fillMaxWidth(.9f)
                  .windowInsetsPadding(WindowInsets.systemBars)
                  .fillMaxWidth(),
                content = {
                  val positionAsText = remember {
                    val builder = StringBuilder()
                    derivedStateOf { player.state.position.formatElapsedTime(builder) }
                  }
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    content = {
                      Text(
                        text = positionAsText.value,
                        style = MaterialTheme.typography.bodySmall,
                      )
                      Text(
                        text = player.state.duration.formatElapsedTime(),
                        style = MaterialTheme.typography.bodySmall,
                      )
                    }
                  )
                  Slider(
                    modifier = Modifier.fillMaxWidth(),
                    interactionSource = sliderSource,
                    value = when {
                      isSliding -> userSliderPosition.floatValue
                      else -> playbackProgress.value
                    },
                    onValueChange = { userSliderPosition.floatValue = it },
                    onValueChangeFinished = {
                      val target = player.state.duration * userSliderPosition.floatValue.toDouble()
                      player.seek(target, SeekFlag.Fast)
                    },
                  )
                }
              )
            },
          )

        },
      )

    },
  )

}

@Composable
fun TrackSelector(
  modifier: Modifier = Modifier,
  icon: ImageVector,
  tracks: State<List<Track>>,
  onClick: (Track) -> Unit,
) {
  var isExpanded by remember { mutableStateOf(false) }
  IconButton(
    modifier = modifier,
    onClick = { isExpanded = true },
    enabled = tracks.value.isNotEmpty(),
    content = { Icon(imageVector = icon, contentDescription = null) }
  )
  DropdownMenu(
    expanded = isExpanded,
    onDismissRequest = { isExpanded = false },
    content = {
      for ( track in tracks.value) {
        DropdownMenuItem(
          onClick = {
            onClick.invoke(track)
            isExpanded = false
          },
          leadingIcon = {
            Icon(
              modifier = Modifier
                .alpha(if (track.isActive) 1f else 0f),
              imageVector = Icons.Rounded.Check,
              contentDescription = null,
            )
          },
          text = {
            Text(
              text = when (track) {
                is Audio -> track.metaData["language"] ?: "Track ${track.index}"
                is Subtitle -> track.metaData["language"] ?: "Track ${track.index}"
                is Video -> track.metaData["language"] ?: "Track ${track.index}"
              },
            )
          }
        )
      }
    },
  )
}


val Track.type: MediaType
  get() = when (this) {
    is Audio -> MediaType.Audio
    is Subtitle -> MediaType.Subtitle
    is Video -> MediaType.Video
  }