package fr.dcs.mdk.player.events

import fr.dcs.mdk.player.MediaType

sealed interface PlayerEvent {
  data object FirstFrameRendered : PlayerEvent
  data object MetaDataReady : PlayerEvent
  data class ThreadStarted(val type: MediaType) : PlayerEvent
  data class Buffering(val progress: Float) : PlayerEvent
  data class Error(val code: Int, val message: String) : PlayerEvent
}