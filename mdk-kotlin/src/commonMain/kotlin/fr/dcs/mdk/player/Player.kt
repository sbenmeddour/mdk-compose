package fr.dcs.mdk.player

import kotlinx.coroutines.CoroutineScope

expect class Player {

  internal val scope: CoroutineScope

  fun play()
  fun pause()
  fun stop()

  fun setMedia(url: String)

  suspend fun prepare()

  fun release()

}

