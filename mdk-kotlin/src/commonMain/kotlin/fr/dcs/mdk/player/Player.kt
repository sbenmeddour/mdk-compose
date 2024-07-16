package fr.dcs.mdk.player

import fr.dcs.mdk.player.events.PlayerEvent
import fr.dcs.mdk.player.state.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

expect class Player {

  internal val scope: CoroutineScope

  val state: PlayerState
  val events: Flow<PlayerEvent>

  fun play()
  fun pause()
  fun stop()
  fun playPause()

  fun setMedia(url: String)
  suspend fun prepare()

  fun setTrack(type: MediaType, index: Int)

  fun release()

  fun seek(position: Duration, vararg flag: SeekFlag)

  val properties: Properties

}

interface Properties {
  operator fun get(key: String): String?
  operator fun set(key: String, value: String)
}
