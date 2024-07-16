package fr.dcs.mdk.ui

import android.view.SurfaceView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import fr.dcs.mdk.player.Player
import fr.dcs.mdk.player.configuration.PlayerConfiguration
import fr.dcs.mdk.player.state.PlayerState

@Composable
actual fun PlayerView(modifier: Modifier, player: Player) {
  AndroidView(
    modifier = Modifier,
    factory = ::SurfaceView,
    update = {
      println("<top>.PlayerView.update: $it")
      player.setSurfaceView(it)
    },
    onRelease = {
      println("<top>.PlayerView.release: $it")
      player.detachSurfaceView(it)
    },
  )
}

@Stable
@Composable
actual fun rememberPlayer(
  configuration: PlayerConfiguration,
  state: PlayerState
): Player {
  return remember { Player(configuration, state) }
}