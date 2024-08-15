package fr.dcs.mdk.ui

import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.interop.*
import fr.dcs.mdk.player.*
import fr.dcs.mdk.player.configuration.*
import platform.CoreGraphics.*

@Composable
actual fun PlayerView(
  modifier: Modifier,
  player: Player,
) {
  UIKitView(
    modifier = modifier,
    factory = {
      //val frame = CGRectMake(x = 0.0, y = 0.0, width = 0.0, height = 0.0)
      val target = when (player.configuration.renderTargetType) {
        RenderTargetType.Metal -> RenderTarget.Metal()
        RenderTargetType.View -> RenderTarget.View()
      }
      target.view()
    },
    update = { player.currentRenderTarget = RenderTarget.fromView(it) },
    onRelease = { player.currentRenderTarget = null },
    background = Color.Black,
    interactive = false,
  )
}



@Stable
@Composable
actual fun rememberPlayer(configuration: PlayerConfiguration): Player {
  return remember { Player(configuration) }
}