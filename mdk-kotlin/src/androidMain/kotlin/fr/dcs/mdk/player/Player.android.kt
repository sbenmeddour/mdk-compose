package fr.dcs.mdk.player

import android.view.SurfaceHolder
import android.view.SurfaceView
import fr.dcs.mdk.jni.JNIPlayer
import fr.dcs.mdk.jni.JniListener
import fr.dcs.mdk.native.NativeMediaStatus
import fr.dcs.mdk.native.NativeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

actual class Player(
  val state: PlayerState,
  internal actual val scope: CoroutineScope = CoroutineScope(context = SupervisorJob() + Dispatchers.IO),
) : SurfaceHolder.Callback {

  private val listener = object : JniListener {

    override fun onStateChanged(value: Int) {
      state.nativeState = value
    }

    override fun onMediaStatus(previousStatus: Int, newStatus: Int) {
      state.nativeMediaStatus = newStatus
    }

    override fun onMediaEvent(error: Long, category: String?, detail: String?) {
    }

  }
  private val handle = JNIPlayer.createWrapper(this.listener)

  init {
    JNIPlayer.setAudioBackends(
      handle = this.handle,
      values = arrayOf("AudioTrack", "OpenSL")
    )
    JNIPlayer.setDecoders(
      handle = this.handle,
      mediaType = 0,
      values = arrayOf("AMediaCodec:java=0:copy=0:surface=1:image=1:async=0:low_latency=1", "FFmpeg"),
    )
  }

  private var nativeWindowHandle = 0L

  private var currentSurfaceView: SurfaceView? = null
    set(value) {
      if (field == value) return
      field?.holder?.removeCallback(this)
      field = value
      if (value == null) {
        return
      }
      value.holder.addCallback(this)
    }

  override fun surfaceCreated(holder: SurfaceHolder) {
    this.nativeWindowHandle = JNIPlayer.setSurface(handle = handle, surface = null, width = 0, height = 0)
    this.nativeWindowHandle = JNIPlayer.setSurface(
      handle = handle,
      surface = holder.surface,
      width = -1,
      height = -1,
    )
  }

  override fun surfaceDestroyed(holder: SurfaceHolder) {
    this.nativeWindowHandle = JNIPlayer.setSurface(handle, null, -1, -1)
  }

  override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    this.nativeWindowHandle = JNIPlayer.setSurface(
      handle = handle,
      surface = holder.surface,
      width = width,
      height = height,
    )
  }

  fun setSurfaceView(view: SurfaceView) {
    this.currentSurfaceView = view

  }

  fun detachSurfaceView(view: SurfaceView?) {
    this.currentSurfaceView = null
  }

  actual fun play() {
    JNIPlayer.setState(handle, NativeState.Playing.nativeValue)
  }

  actual fun pause() {
    JNIPlayer.setState(handle, NativeState.Paused.nativeValue)
  }

  actual fun stop() {
    JNIPlayer.setState(handle, NativeState.Stopped.nativeValue)
  }

  actual fun setMedia(url: String) {
    JNIPlayer.setMedia(handle, url)
  }

  actual fun release() {
    JNIPlayer.release(handle)
    scope.cancel()
  }

  actual suspend fun prepare() {
    JNIPlayer.prepare(
      handle,
      0L,
      0,
      false,
    )
  }

}