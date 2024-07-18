package fr.dcs.mdk.player.configuration

expect class PlayerConfiguration {

  val properties: Map<String, String>

  companion object {
    val defaultConfig: PlayerConfiguration
  }

}