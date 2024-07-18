package fr.dcs.mdk.player

import android.provider.MediaStore.Audio.Media
import android.view.SurfaceHolder
import android.view.SurfaceView
import fr.dcs.mdk.jni.JNIPlayer
import fr.dcs.mdk.jni.JniListener
import fr.dcs.mdk.native.NativeMediaType
import fr.dcs.mdk.native.NativeState
import fr.dcs.mdk.player.configuration.PlayerConfiguration
import fr.dcs.mdk.player.events.PlayerEvent
import fr.dcs.mdk.player.state.PlayerState
import fr.dcs.mdk.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

actual class Player actual constructor(
  configuration: PlayerConfiguration,
  actual val state: PlayerState,
) : SurfaceHolder.Callback, Properties {


  internal actual val scope: CoroutineScope = CoroutineScope(context = SupervisorJob() + Dispatchers.IO)

  private val listener = object : JniListener {

    override fun onStateChanged(value: Int) {
      scope.launch(Dispatchers.Main) { state._nativeState = value }
    }

    override fun onMediaStatus(previousStatus: Int, newStatus: Int) {
      scope.launch(Dispatchers.Main) { state._nativeMediaStatus = newStatus }
    }

    override fun onMediaEvent(error: Long, category: String?, detail: String?) {
      val event = PlayerEvent.fromData(error, category, detail) ?: return
      _events.tryEmit(event)
    }

  }

  private val handle = JNIPlayer.createWrapper(this.listener)

  private val _events = MutableSharedFlow<PlayerEvent>(replay = 0, extraBufferCapacity = 1)
  actual val events: Flow<PlayerEvent> get() = _events
  actual val properties: Properties get() = this

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

  override operator fun get(key: String): String? = JNIPlayer.getProperty(handle, key)
  override operator fun set(key: String, value: String) = JNIPlayer.setProperty(handle, key, value)

  actual fun play() {
    JNIPlayer.setState(handle, NativeState.Playing.nativeValue)
  }

  actual fun pause() {
    JNIPlayer.setState(handle, NativeState.Paused.nativeValue)
  }

  actual fun stop() {
    JNIPlayer.setState(handle, NativeState.Stopped.nativeValue)
  }

  actual fun playPause() = when {
    state._nativeState == NativeState.Playing.nativeValue -> pause()
    else -> play()
  }

  actual fun setMedia(url: String) {
    JNIPlayer.setMedia(handle, url)
    for (type in MediaType.entries) state._activeTracks[type] = listOf(0)
  }

  actual fun release() {
    stop()
    scope.cancel()
    JNIPlayer.release(handle)
  }

  actual suspend fun prepare(position: Duration, vararg flags: SeekFlag) {
    val mediaInfo = withContext(Dispatchers.IO) {
      JNIPlayer.prepare(handle, position.inWholeMilliseconds, flags.combined, false)
    }
    withContext(Dispatchers.Main) {
      state._nativeMediaInfo = mediaInfo
      state._activeTracks[MediaType.Video] = listOf(mediaInfo?.video?.firstOrNull()?.index ?: 0)
      state._activeTracks[MediaType.Audio] = listOf(mediaInfo?.audio?.firstOrNull()?.index ?: 0)
      state._activeTracks[MediaType.Subtitle] = listOf(0)
    }
  }

  actual fun setTrack(type: MediaType, index: Int) {
    val nativeType = when (type) {
      MediaType.Audio -> NativeMediaType.Audio
      MediaType.Video -> NativeMediaType.Video
      MediaType.Subtitle -> NativeMediaType.Subtitles
      MediaType.Unknown -> NativeMediaType.Unknown
    }
    JNIPlayer.setTrack(handle, nativeType.nativeValue, index)
    state._activeTracks[type] = listOf(index)
  }

  actual fun seek(position: Duration, vararg flag: SeekFlag) {
    val nativeFlags = flag.map { it.asNativeSeekFlag().nativeValue }.reduce { acc, i -> acc or i }
    JNIPlayer.seek(handle, nativeFlags, position.inWholeMilliseconds)
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
    JNIPlayer.resizeSurface(handle, width, height)
    /*this.nativeWindowHandle = JNIPlayer.setSurface(
      handle = handle,
      surface = holder.surface,
      width = width,
      height = height,
    )*/
  }

  fun setSurfaceView(view: SurfaceView) {
    this.currentSurfaceView = view
  }

  fun detachSurfaceView(view: SurfaceView?) {
    this.currentSurfaceView = null
  }

  init {
    scope.launch(Dispatchers.IO) {
      while (isActive) {
        try {
          val position = JNIPlayer.getPosition(handle)
          withContext(Dispatchers.Main) { state._position = position.milliseconds }
        } finally {
          delay(200.milliseconds)
        }
      }
    }
  }

  init {
    JNIPlayer.setAudioBackends(
      handle = this.handle,
      values = configuration.audioBackends.toTypedArray(),
    )
    JNIPlayer.setDecoders(
      handle = this.handle,
      mediaType = NativeMediaType.Video.nativeValue,
      values = configuration.videoDecoders.toTypedArray(),
    )
    for (entry in configuration.properties) properties[entry.key] = entry.key
  }

}
