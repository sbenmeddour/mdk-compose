package fr.dcs.mdk.ui

actual sealed interface RenderTargetType {
  data object Metal : RenderTargetType
  data object View : RenderTargetType
}