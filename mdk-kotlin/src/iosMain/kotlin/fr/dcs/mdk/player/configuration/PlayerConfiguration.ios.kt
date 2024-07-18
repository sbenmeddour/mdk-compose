package fr.dcs.mdk.player.configuration

actual class PlayerConfiguration(
  val videoDecoders: List<String>,
  val audioDecoders: List<String>,
  actual val properties: Map<String, String>
) {

  actual companion object {
    actual val defaultConfig: PlayerConfiguration
      get() = PlayerConfiguration(
        properties = emptyMap(),
        videoDecoders = listOf("VT:copy=0", "VideoToolbox", "BRAW:gpu=auto", "R3D", "hap", "FFmpeg", "dav1d"),
        audioDecoders = listOf("FFmpeg"),
      )
  }

}