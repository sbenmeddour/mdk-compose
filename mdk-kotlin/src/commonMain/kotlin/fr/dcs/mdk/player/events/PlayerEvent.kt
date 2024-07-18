package fr.dcs.mdk.player.events

import fr.dcs.mdk.player.MediaType

sealed interface PlayerEvent {
  data object FirstFrameRendered : PlayerEvent
  data object MetaDataReady : PlayerEvent
  data class ThreadStarted(val type: MediaType) : PlayerEvent
  data class Buffering(val progress: Float) : PlayerEvent
  data class Error(val code: Int, val message: String) : PlayerEvent

  companion object {

    internal fun fromData(error: Long, category: String?, detail: String?): PlayerEvent? {
      return when (category) {
        "reader.buffering" -> Buffering(progress = error / 100f)
        "metadata" -> MetaDataReady
        "thread.audio" -> when (error) {
          0L -> return null//todo: ThreadStopped
          1L -> ThreadStarted(MediaType.Audio)
          else -> return null
        }
        "thread.video" -> when (error) {
          0L -> return null //todo: ThreadStopped
          1L -> ThreadStarted(MediaType.Video)
          else -> return null
        }
        "render.video" -> when (detail) {
          "1st_frame" -> FirstFrameRendered
          else -> return null
        }
        else -> return null
      }
    }
  }
}
