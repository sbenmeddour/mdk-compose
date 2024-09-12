package fr.dcs.mdk.player

import fr.dcs.mdk.player.configuration.PlayerConfiguration
import fr.dcs.mdk.player.events.PlayerEvent
import fr.dcs.mdk.player.models.*
import fr.dcs.mdk.ui.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

expect class Player(configuration: PlayerConfiguration) {

  internal val scope: CoroutineScope

  val configuration: PlayerConfiguration
  val state: PlayerState
  val events: Flow<PlayerEvent>
  val properties: Properties
  var currentRenderTarget: RenderTarget?

  fun play()
  fun pause()
  fun stop()
  fun playPause()
  fun setMedia(url: String)
  fun setTrack(type: MediaType, id: Track.Id?)
  fun release()
  fun seek(position: Duration, vararg flag: SeekFlag)

  suspend fun prepare(position: Duration = Duration.ZERO, vararg flags: SeekFlag = arrayOf(SeekFlag.Default)): Result<Unit>

}

sealed class PlayerException(message: String) : Exception(message) {
  class PrepareException(val code: Long) : PlayerException("Prepare failed: [error=$code")
}

interface Properties {
  operator fun get(key: String): String?
  operator fun set(key: String, value: String)
}
