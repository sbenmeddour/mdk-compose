package fr.dcs.mdk.player.configuration

import fr.dcs.mdk.ui.RenderTargetType

expect class PlayerConfiguration {

  val renderTargetType: RenderTargetType
  val properties: Map<String, String>

  companion object {
    val defaultConfig: PlayerConfiguration
  }

}