package fr.dcs.mdk.player

import fr.dcs.mdk.player.configuration.*
import fr.dcs.mdk.player.events.*
import fr.dcs.mdk.player.models.*
import fr.dcs.mdk.ui.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.*

actual class Player actual constructor(configuration: PlayerConfiguration) {

  internal actual val scope: CoroutineScope
    get() = TODO("Not yet implemented")
  actual val configuration: PlayerConfiguration
    get() = TODO("Not yet implemented")
  actual val state: PlayerState
    get() = TODO("Not yet implemented")
  actual val events: Flow<PlayerEvent>
    get() = TODO("Not yet implemented")
  actual val properties: Properties
    get() = TODO("Not yet implemented")
  actual var currentRenderTarget: RenderTarget?
    get() = TODO("Not yet implemented")
    set(value) {}

  actual fun play() {
    TODO("Not yet implemented")
  }

  actual fun pause() {
    TODO("Not yet implemented")
  }

  actual fun stop() {
    TODO("Not yet implemented")
  }

  actual fun playPause() {
    TODO("Not yet implemented")
  }

  actual fun setMedia(url: String) {
    TODO("Not yet implemented")
  }

  actual fun setTrack(
    type: MediaType,
    id: Track.Id?
  ) {
    TODO("Not yet implemented")
  }

  actual fun release() {
    TODO("Not yet implemented")
  }

  actual fun seek(position: Duration, vararg flag: SeekFlag) {
    TODO("Not yet implemented")
  }

  actual suspend fun prepare(
    position: Duration,
    vararg flags: SeekFlag
  ): Result<Unit> {
    TODO("Not yet implemented")
  }

}