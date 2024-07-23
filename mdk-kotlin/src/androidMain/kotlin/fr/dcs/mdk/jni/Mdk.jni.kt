package fr.dcs.mdk.jni

import android.view.*
import androidx.annotation.*
import fr.dcs.mdk.player.models.*

internal typealias Pointer = Long

internal typealias StringArray = Array<String>

internal const val nullPtr = 0L

internal object Mdk {

  external fun newInstance(): Pointer
  external fun deleteInstance(player: Pointer)

  external fun setMute(player: Pointer, value: Boolean)
  external fun isMuted(player: Pointer): Boolean

  external fun setVolume(player: Pointer, value: Float)
  external fun getVolume(player: Pointer): Float

  external fun setFrameRate(player: Pointer, value: Float)

  external fun setMedia(player: Pointer, url: String, type: Int)
  external fun setNextMedia(player: Pointer, value: String, startPosition: Long, flags: Int)
  external fun getUrl(player: Pointer): String?

  external fun setPreloadImmediately(player: Pointer, value: Boolean)

  external fun setActiveTracks(player: Pointer, type: Int, values: IntArray)

  external fun setAudioBackends(player: Pointer, values: StringArray)
  external fun setDecoders(player: Pointer, type: Int, values: StringArray)

  external fun setState(player: Pointer, value: Int)
  external fun getState(player: Pointer): Int


  external fun getMediaStatus(player: Pointer): Int

  external fun updateNativeSurface(player: Pointer, surface: Surface?, width: Int, height: Int)
  external fun createSurface(player: Pointer, surface: Surface?, type: Int)
  external fun resizeSurface(player: Pointer, width: Int, height: Int)
  external fun showSurface(player: Pointer)

  external fun setProperty(player: Pointer, key: String, value: String?)
  external fun getProperty(player: Pointer, key: String): String?

  external fun setVideoSurfaceSize(player: Pointer, width: Int, height: Int, opaque: Any?)
  external fun setVideoViewport(player: Pointer, x: Float, y: Float, width: Float, height: Float, opaque: Any?)
  external fun setAspectRatio(player: Pointer, value: Float, opaque: Any?)
  external fun rotate(player: Pointer, degree: Int, opaque: Any?)
  external fun scale(player: Pointer, x: Float, y: Float, opaque: Any?)
  external fun renderVideo(player: Pointer, opaque: Any?)

  external fun getPosition(player: Pointer): Long
  external fun seek(player: Pointer, position: Long, flags: Int)

  external fun setPlaybackRate(player: Pointer, value: Float)
  external fun getPlaybackRate(player: Pointer): Float

  external fun setBufferRange(player: Pointer, min: Long, max: Long, drop: Boolean)

  external fun switchBitrate(player: Pointer, url: String, delay: Long): Boolean
  external fun switchBitrateSingleConnection(player: Pointer, url: String): Boolean


  external fun setLoop(player: Pointer, count: Int)

  @WorkerThread
  external fun waitFor(player: Pointer, state: Int, timeout: Long): Boolean//fixme

  @WorkerThread
  external fun setTimeout(player: Pointer, value: Long, abortOperation: Boolean) //todo

  @WorkerThread
  @Throws(PrepareException::class)
  external fun prepare(player: Pointer, position: Long, flags: Int, unload: Boolean): MediaInfo

  @WorkerThread
  external fun seekAwait(player: Pointer, position: Long, flags: Int): Long

  external fun getMediaInfo(player: Pointer): Any?

  external fun onEvent(player: Pointer, listener: OnEvent): ListenerAndToken
  external fun onLoop(player: Pointer, listener: OnLoop): ListenerAndToken
  external fun onMediaStatusChanged(player: Pointer, listener: OnMediaStatus): ListenerAndToken

  external fun onStateChanged(player: Pointer, listener: OnStateChanged): Pointer
  external fun onCurrentMediaChanged(player: Pointer, listener: OnCurrentMediaChanged): Pointer

  external fun unregisterOnEventCallback(player: Pointer, listener: Pointer, token: Pointer)
  external fun unregisterOnLoopCallback(player: Pointer, listener: Pointer, token: Pointer)
  external fun unregisterOnMediaStatusChangedCallback(player: Pointer, listener: Pointer, token: Pointer)
  external fun unregisterOnStateChangedCallback(player: Pointer, listener: Pointer)
  external fun unregisterOnCurrentMediaChangedCallback(player: Pointer, listener: Pointer)

  external fun setupVulkanRenderer(player: Pointer, surface: Surface): Pointer
  external fun detachVulkanRenderer(player: Pointer, globalRef: Pointer)


  fun interface OnStateChanged { fun onState(value: Int) }
  fun interface OnMediaStatus { fun onStatus(old: Int, new: Int) }
  fun interface OnEvent { fun onEvent(code: Long, category: String?, detail: String?) }
  fun interface OnLoop { fun onLoop(value: Long) }
  fun interface OnCurrentMediaChanged { fun onCurrentMediaChanged() }

  data class ListenerAndToken(val listener: Long, val token: Long)

  data class PrepareException(val code: Long) : Exception("Error code: $code")

}