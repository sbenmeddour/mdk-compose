package fr.dcs.mdk.player

import android.opengl.*
import android.view.*
import fr.dcs.mdk.jni.*
import fr.dcs.mdk.player.configuration.*
import fr.dcs.mdk.player.events.*
import fr.dcs.mdk.player.models.*
import fr.dcs.mdk.ui.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.*
import kotlin.reflect.*
import kotlin.time.*
import kotlin.time.Duration.Companion.milliseconds

actual class Player actual constructor(actual val configuration: PlayerConfiguration) : Properties {

  internal actual val scope: CoroutineScope = CoroutineScope(context = SupervisorJob() + Dispatchers.IO)
  private val handle = Mdk.newInstance()
  actual val properties: Properties get() = this
  actual val state: PlayerState = PlayerState()

  init {
    Mdk.setAudioBackends(handle, configuration.audioBackends.toTypedArray())
    Mdk.setDecoders(handle, MediaType.Video.nativeValue, configuration.videoDecoders.toTypedArray())
    for (entry in configuration.properties) properties[entry.key] = entry.key
  }

  private val _events = MutableSharedFlow<PlayerEvent>(replay = 0, extraBufferCapacity = 1)
  actual val events: Flow<PlayerEvent> get() = _events

  override operator fun get(key: String): String? = Mdk.getProperty(handle, key)
  override operator fun set(key: String, value: String) = Mdk.setProperty(handle, key, value)
  actual fun play() = Mdk.setState(handle, State.Playing.nativeValue)
  actual fun pause() = Mdk.setState(handle, State.Paused.nativeValue)
  actual fun stop() = Mdk.setState(handle, State.Stopped.nativeValue)

  actual fun playPause() = when {
    state.state == State.Playing -> pause()
    else -> play()
  }

  actual fun setMedia(url: String) {
    Mdk.setMedia(handle,url, MediaType.Video.nativeValue)
    for (type in MediaType.entries) state.activeTracks[type] = emptyList()
  }

  actual suspend fun prepare(position: Duration, vararg flags: SeekFlag): Result<Unit> {
    val mediaInfo = withContext(Dispatchers.IO) {
      runCatching {
        Mdk.prepare(
          player = handle,
          position = position.inWholeMilliseconds,
          flags = flags.combined,
          unload = false,
        )
      }
    }
    return withContext(Dispatchers.Main) {
      mediaInfo
        .onSuccess {
          state.mediaInfo = it
          state.activeTracks[MediaType.Video] = if (it.video.isEmpty()) emptyList() else listOf(Track.Id(it.video.first().index))
          state.activeTracks[MediaType.Audio] = if (it.audio.isEmpty()) emptyList() else listOf(Track.Id(it.audio.first().index))
          state.activeTracks[MediaType.Subtitles] = if (it.subtitles.isEmpty()) emptyList() else listOf(Track.Id(it.subtitles.first().index))
        }
        .onFailure {
          state.mediaInfo = MediaInfo.empty
          state.activeTracks[MediaType.Video] = emptyList()
          state.activeTracks[MediaType.Audio] = emptyList()
          state.activeTracks[MediaType.Subtitles] = emptyList()
        }
        .map { Unit }
    }

  }

  actual fun setTrack(type: MediaType, id: Track.Id?) {
    val tracks: List<Pair<Track.Id, Int>> = this.state
      .mediaInfo[type]
      .mapIndexedNotNull { index, stream ->
        when {
          id != null && stream.index == id.value -> Track.Id(stream.index) to index
          else -> null
        }
      }
    val indices: IntArray = tracks.map(Pair<*, Int>::second).toIntArray()

    Mdk.setActiveTracks(handle, type.nativeValue, indices)
    state.activeTracks[type] = tracks.map { it.first }
  }

  actual fun seek(position: Duration, vararg flag: SeekFlag) = Mdk.seek(handle, position.inWholeMilliseconds, flag.combined)

  init {
    scope.launch(Dispatchers.IO) {
      while (isActive) {
        try {
          val position = Mdk.getPosition(handle)
          withContext(Dispatchers.Main) { state._position = position.milliseconds }
        } finally {
          delay(200.milliseconds)
        }
      }
    }
  }


  private val onStatusChanged = Mdk.OnMediaStatus { _, next ->
    scope.launch(Dispatchers.Main) {
      state.status = MediaStatus.Mixed(next)
    }
  }


  private val onStateChanged = Mdk.OnStateChanged { value: Int ->
    scope.launch(Dispatchers.Main) {
      state.state = State.fromInt(value)
    }
  }

  private val onEvent = Mdk.OnEvent { code: Long, category: String?, details: String? ->
    val event = PlayerEvent.fromData(code, category, details) ?: return@OnEvent
    _events.tryEmit(event)
  }

  private val onLoop = Mdk.OnLoop {
  }

  private val onCurrentMediaChanged = Mdk.OnCurrentMediaChanged {
  }

  private val onStatusToken = Mdk.onMediaStatusChanged(handle, onStatusChanged)
  private val onEventToken = Mdk.onEvent(handle, onEvent)
  private val onLoopToken = Mdk.onLoop(handle, onLoop)
  private val onStateChangedAddress = Mdk.onStateChanged(handle, onStateChanged)
  private val onMediaChangedAddress = Mdk.onCurrentMediaChanged(handle, onCurrentMediaChanged)

  actual fun release() {
    Mdk.unregisterOnEventCallback(handle, onEventToken.listener, onEventToken.token)
    Mdk.unregisterOnLoopCallback(handle, onLoopToken.listener, onLoopToken.token)
    Mdk.unregisterOnMediaStatusChangedCallback(handle, onStatusToken.listener, onStatusToken.token)
    Mdk.unregisterOnStateChangedCallback(handle, onStateChangedAddress)
    Mdk.unregisterOnCurrentMediaChangedCallback(handle, onMediaChangedAddress)
    stop()
    scope.cancel()
    Mdk.deleteInstance(handle)
  }

  private fun invalidRenderTarget(clazz: KClass<out RenderTarget>): Nothing = error("Invalid render target: expected [$clazz]")

  actual var currentRenderTarget: RenderTarget? = null
    set(value) {
      if (field === value) return
      when (val actualValue = field) {
        is RenderTarget.AndroidSurfaceView -> actualValue.holder.removeCallback(surfaceViewCallback)
        is RenderTarget.Gl -> actualValue.setRenderer(null)
        is RenderTarget.Vulkan -> actualValue.holder.removeCallback(vulkanCallback)
        null -> Unit
      }
      field = value
      when (value) {
        is RenderTarget.AndroidSurfaceView -> value.holder.addCallback(surfaceViewCallback)
        is RenderTarget.Gl -> value.setRenderer(glCallback)
        is RenderTarget.Vulkan -> value.holder.addCallback(vulkanCallback)
        null -> Unit
      }
    }

  private val glCallback = object : GLSurfaceView.Renderer {
    override fun onDrawFrame(gl: GL10?) = Mdk.renderVideo(handle, null)
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) = Unit
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) = Mdk.setVideoSurfaceSize(handle, width, height, null)
  }

  private val surfaceViewCallback = object : SurfaceHolder.Callback {

    override fun surfaceCreated(holder: SurfaceHolder) {
      Mdk.updateNativeSurface(handle, null, 0, 0)
      Mdk.updateNativeSurface(handle, holder.surface, -1, -1)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
      Mdk.updateNativeSurface(handle, holder.surface, 0, 0)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
      Mdk.resizeSurface(handle, width, height)
    }
  }

  private val vulkanCallback = object : SurfaceHolder.Callback {


    override fun surfaceCreated(holder: SurfaceHolder) {
      when (val target = currentRenderTarget) {
        is RenderTarget.Vulkan -> {
          Mdk.updateNativeSurface(handle, null, 0, 0)
          target.globalRef = Mdk.setupVulkanRenderer(handle, holder.surface)
        }
        else -> invalidRenderTarget(RenderTarget.Vulkan::class)
      }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
      when (val target = currentRenderTarget) {
        is RenderTarget.Vulkan -> Mdk.detachVulkanRenderer(handle, target.globalRef)
        else -> invalidRenderTarget(RenderTarget.Vulkan::class)
      }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
      when (currentRenderTarget) {
        is RenderTarget.Vulkan -> Mdk.resizeSurface(handle, width, height)
        else -> invalidRenderTarget(RenderTarget.Vulkan::class)
      }
    }
  }


}
