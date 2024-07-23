package fr.dcs.mdk.ui

import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.viewinterop.*
import fr.dcs.mdk.player.*
import fr.dcs.mdk.player.configuration.*

@Composable
actual fun PlayerView(modifier: Modifier, player: Player) {
  AndroidView(
    modifier = modifier.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen),
    factory = when (player.configuration.renderTargetType) {
      RenderTargetType.OpenGl -> RenderTarget::Gl
      is RenderTargetType.SurfaceView -> RenderTarget::AndroidSurfaceView
      RenderTargetType.Vulkan -> RenderTarget::Vulkan
    },
    update = { player.currentRenderTarget = it },
    onRelease = { player.currentRenderTarget = null },
  )
}

@Stable
@Composable
actual fun rememberPlayer(
  configuration: PlayerConfiguration,
): Player {
  return remember { Player(configuration) }
}