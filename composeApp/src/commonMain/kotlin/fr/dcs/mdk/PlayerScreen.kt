package fr.dcs.mdk

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.*
import fr.dcs.mdk.player.*
import fr.dcs.mdk.player.models.*
import fr.dcs.mdk.ui.*
import kotlin.time.*

@Composable
fun PlayerScreen(url: String) {
  Scaffold(
    modifier = Modifier,
    content = {
      val player = rememberPlayer()
      var isReady by remember { mutableStateOf(false) }
      var isResumed by remember { mutableStateOf(false) }

      LaunchedEffect(Unit) {
        player.setMedia(url)
        val result = player.prepare()
        isReady = result.isSuccess

      }

      LaunchedEffect(isResumed, isReady) {
        when {
          !isResumed -> player.pause()
          isReady -> player.play()
        }
      }

      Box(
        modifier = Modifier.fillMaxSize(),
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
    },
  )
}


@Composable
private fun Controls(
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
      .drawBehind {
        drawRect(color = Color.Black, alpha = if (isVisible) .5f else 0f)
      }
      .windowInsetsPadding(WindowInsets.systemBars)
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
                      track.isActive -> player.setTrack(track.type, null)
                      else -> player.setTrack(track.type, track.id)
                    }
                  }
                  TrackSelector(
                    icon = Icons.Rounded.VideoSettings,
                    getTracks = { player.state.tracks.video },
                    onClick = onTrackClicked,
                  )
                  TrackSelector(
                    icon = Icons.Rounded.Audiotrack,
                    getTracks = { player.state.tracks.audio },
                    onClick = onTrackClicked,
                  )
                  TrackSelector(
                    icon = Icons.Rounded.Subtitles,
                    getTracks = { player.state.tracks.subtitles },
                    onClick = onTrackClicked,
                  )
                }
              )

              IconButton(
                modifier = Modifier
                  .align(Alignment.Center),
                onClick = when  {
                  player.state.playbackStatus.isPlaying -> player::pause
                  else -> player::play
                },
                content = {
                  Icon(
                    modifier = Modifier.size(64.dp),
                    imageVector = with (player.state.playbackStatus) {
                      when {
                        isPlaying -> Icons.Rounded.Pause
                        endOfFile || isStopped -> Icons.Rounded.RestartAlt
                        !hasMedia || isUnloaded -> Icons.Rounded.PlayArrow
                        else -> Icons.Rounded.PlayArrow
                      }
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
private fun TrackSelector(
  modifier: Modifier = Modifier,
  icon: ImageVector,
  getTracks: () -> List<Track>,
  onClick: (Track) -> Unit,
) {
  var isExpanded by remember { mutableStateOf(false) }
  val tracks by rememberUpdatedState(getTracks.invoke())
  val isEnabled by remember { derivedStateOf { tracks.isNotEmpty() }}
  IconButton(
    modifier = modifier,
    onClick = { isExpanded = true },
    enabled = isEnabled,
    content = { Icon(imageVector = icon, contentDescription = null) }
  )
  DropdownMenu(
    expanded = isExpanded,
    onDismissRequest = { isExpanded = false },
    content = {
      for (track in tracks) {
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
          text = { Text(text = track.localizedName) },
        )
      }
    },
  )
}

val Track.localizedName: String
  get() {
    return metaData["language"] ?: "Track ${id.value}"
  }