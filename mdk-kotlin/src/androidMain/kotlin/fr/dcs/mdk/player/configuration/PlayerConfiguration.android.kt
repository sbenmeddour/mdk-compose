package fr.dcs.mdk.player.configuration

actual class PlayerConfiguration(
  val audioBackends: List<String>,
  val videoDecoders: List<String>,
  actual val properties: Map<String, String>,
) {

  actual companion object {

    actual val defaultConfig: PlayerConfiguration
      get() = PlayerConfiguration(
        audioBackends = listOf("AudioTrack", "OpenSL"),
        videoDecoders = listOf(
          "AMediaCodec:java=0:copy=0:surface=1:image=1:async=0:low_latency=1",
          "FFmpeg",
        ),
        properties = emptyMap(),
      )
  }

}