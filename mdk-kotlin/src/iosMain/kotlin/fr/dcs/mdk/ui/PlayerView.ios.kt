package fr.dcs.mdk.ui

import androidx.compose.foundation.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.interop.*
import androidx.compose.ui.unit.*
import fr.dcs.mdk.player.*
import fr.dcs.mdk.player.configuration.*
import fr.dcs.mdk.player.state.*
import kotlinx.cinterop.*
import platform.CoreGraphics.*
import platform.Metal.*
import platform.MetalKit.*
import platform.UIKit.*
import platform.darwin.*
import kotlin.math.*

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlayerView(
  modifier: Modifier,
  player: Player,
) {
  UIKitView(
    modifier = modifier,
    factory = {
      UIView().apply {
        player.setUiView(this, 0, 0)
      }
    },
    onResize = player::onUIViewResized
  )
  //fixme: Black screen no output... :/
  /*val device = remember { MTLCreateSystemDefaultDevice() ?: throw Exception("Cannot create device") }
  val commandQueue = remember { device.newCommandQueue() ?: throw Exception("Cannot create commandQueue") }
  val delegate = remember { MetalDelegate(player, commandQueue) }

  UIKitView(
    modifier = modifier.border(4.dp, Color.Red),
    factory = {
      val frame = CGRectMake(x = 0.0, y = 0.0, width = 300.0, height = 300.0)
      MTKView(frame = frame, device = device).apply {
        this.device = device
        this.framebufferOnly = false
        this.delegate = delegate
        player.setRenderTarget(metalKitView = this, commandQueue = commandQueue)
      }
    },
    onRelease = {
      it.delegate = null
      it.device = null
      player.detachNativeSurface()
    },
    background = Color.Black,
    interactive = false,
  )*/


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

@OptIn(ExperimentalForeignApi::class)
class MetalDelegate(
  private val player: Player,
  private val commandQueue: MTLCommandQueueProtocol,
) : NSObject(), MTKViewDelegateProtocol {

  override fun mtkView(view: MTKView, drawableSizeWillChange: CValue<CGSize>) {
    drawableSizeWillChange.useContents {
      player.setVideoSurfaceSize(view, height.roundToInt(), width.roundToInt())
    }
  }

  override fun drawInMTKView(view: MTKView) {
    player.renderVideo(view)
    val drawable = view.currentDrawable ?: return
    val buffer = commandQueue.commandBuffer() ?: return
    buffer.presentDrawable(drawable)
    buffer.commit()
  }

}