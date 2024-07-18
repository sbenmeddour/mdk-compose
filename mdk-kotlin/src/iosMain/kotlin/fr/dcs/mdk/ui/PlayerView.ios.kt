package fr.dcs.mdk.ui

import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.interop.*
import cocoapods.mdk.*
import fr.dcs.mdk.player.*
import fr.dcs.mdk.player.configuration.*
import fr.dcs.mdk.player.state.*
import platform.CoreGraphics.*
import platform.MetalKit.*

@Composable
actual fun PlayerView(
  modifier: Modifier,
  player: Player,
) {
  UIKitView(
    modifier = modifier,
    factory = {
      val frame = CGRectMake(x = 0.0, y = 0.0, width = 0.0, height = 0.0)
      MTKView(frame, player.metalDevice).apply {
        this.framebufferOnly = false
        player.setRenderTarget(this)
        this.delegate = player
      }
    },
    onRelease = {
      it.delegate = null
      it.device = null
      player.setRenderTarget(null)
    },
    background = Color.Black,
    interactive = false,
  )
}



@Stable
@Composable
actual fun rememberPlayer(
  configuration: PlayerConfiguration,
  state: PlayerState
): Player {
  return remember {
    Player(
      configuration = configuration,
      state = state,
    )
  }
}