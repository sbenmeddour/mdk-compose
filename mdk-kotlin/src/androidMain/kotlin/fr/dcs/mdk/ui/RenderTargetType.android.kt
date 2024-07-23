package fr.dcs.mdk.ui

actual sealed interface RenderTargetType {
  data class SurfaceView(val decodeToSurfaceView: Boolean) : RenderTargetType
  data object OpenGl : RenderTargetType
  data object Vulkan : RenderTargetType
}