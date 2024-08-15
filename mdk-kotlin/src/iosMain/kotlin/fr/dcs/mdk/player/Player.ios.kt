package fr.dcs.mdk.player

import cocoapods.mdk.*
import fr.dcs.mdk.player.configuration.*
import fr.dcs.mdk.player.events.*
import fr.dcs.mdk.player.models.*
import fr.dcs.mdk.ui.*
import fr.dcs.mdk.utils.*
import kotlinx.cinterop.*
import kotlinx.cinterop.ByteVar
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import platform.CoreGraphics.*
import platform.Metal.*
import platform.MetalKit.*
import platform.UIKit.*
import platform.darwin.*
import platform.posix.*
import kotlin.math.*
import kotlin.time.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

actual class Player actual constructor(actual val configuration: PlayerConfiguration) : NSObject(), MTKViewDelegateProtocol {

  private val mdkPlayerApi: CPointer<mdkPlayerAPI> = mdkPlayerAPI_new()!!

  internal actual val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  internal val _events = MutableSharedFlow<PlayerEvent>(replay = 0, extraBufferCapacity = 1)
  private val callbacks = Callbacks(StableRef.create(this))

  actual val state: PlayerState = PlayerState()
  actual val events: Flow<PlayerEvent> get() = _events
  actual val properties: Properties = object : Properties {

    override operator fun set(key: String, value: String) = memScoped {
      val player = mdkPlayerApi.pointed.`object`
      val cKey = key.cstr.ptr
      val cValue = value.cstr.ptr
      mdkPlayerApi.pointed.setProperty!!.invoke(player, cKey, cValue)
    }

    override operator fun get(key: String): String? = memScoped {
      val player = mdkPlayerApi.pointed.`object`
      val cKey = key.cstr.ptr
      mdkPlayerApi.pointed.getProperty!!.invoke(player, cKey)?.toKStringFromUtf8()
    }
  }

  init {
    MDK_setLogLevel(MDK_LogLevel.MDK_LogLevel_All)
    MDK_setGlobalOptionString("logLevel", "all")
    MDK_setGlobalOptionString("profiler.gpu", "0")
    MDK_setGlobalOptionInt32("videoout.clear_on_stop", 1)
    MDK_setLogHandler(callbacks.logHandler)
    for (entry in configuration.properties) properties[entry.key] = entry.value


    memScoped {
      /* null terminated array */
      val videoDecoders = allocArray<CPointerVar<ByteVar>>(configuration.videoDecoders.size + 1).apply {
        for (i in configuration.videoDecoders.indices) this[i] = configuration.videoDecoders[i].cstr.ptr
      }
      /* null terminated array */
      val audioDecoders = allocArray<CPointerVar<ByteVar>>(configuration.audioDecoders.size + 1).apply {
        for (i in configuration.audioDecoders.indices) this[i] = configuration.audioDecoders[i].cstr.ptr
      }

      val playerApi = mdkPlayerApi.pointed
      val playerObject = playerApi.`object`
      playerApi.setVideoDecoders!!.invoke(playerObject, videoDecoders)
      playerApi.setAudioDecoders!!.invoke(playerObject, audioDecoders)
      playerApi.onMediaStatus!!.invoke(playerObject, callbacks.onMediaStatus, callbacks.onMediaStatusToken.ptr)
      playerApi.onEvent!!.invoke(playerObject, callbacks.onMediaEvent, callbacks.onEventToken.ptr)
      playerApi.onStateChanged!!.invoke(playerObject, callbacks.onStateChanged)
    }
  }

  actual fun play(): Unit = with (mdkPlayerApi.pointed) { setState?.invoke(`object`, MDK_State_Playing) }

  actual fun pause(): Unit = with (mdkPlayerApi.pointed) { setState?.invoke(`object`, MDK_State_Paused) }

  actual fun stop(): Unit = with (mdkPlayerApi.pointed) { setState?.invoke(`object`, MDK_State_Stopped) }

  actual fun playPause(): Unit = with (mdkPlayerApi.pointed) {
    when (state?.invoke(`object`)) {
      MDK_State_Paused -> setState?.invoke(`object`, MDK_State_Playing)
      MDK_State_Playing -> setState?.invoke(`object`, MDK_State_Paused)
      MDK_State_Stopped -> return
      else -> return
    }
  }

  actual fun setMedia(url: String) {
    memScoped {
      val cUrl = url.cstr.ptr
      with (mdkPlayerApi.pointed) {
        setMedia?.invoke(`object`, cUrl)
      }
    }
  }

  actual suspend fun prepare(position: Duration, vararg flags: SeekFlag): Result<Unit> {
    val player = mdkPlayerApi.pointed.`object`
    val kotlinCallback = KotlinPrepareCallback(mdkPlayerApi)
    val kotlinCallbackRef = StableRef.create(kotlinCallback)
    val callback = cValue<mdkPrepareCallback> {
      cb = staticCFunction(::_onPrepared)
      opaque = kotlinCallbackRef.asCPointer()
    }
    mdkPlayerApi.pointed.prepare!!.invoke(
      p1 = player,
      p2 = position.inWholeMilliseconds,
      p3 = callback,
      p4 = flags.combined.toUInt(),
    )
    return kotlinCallback.result()
      .also { kotlinCallbackRef.dispose() }
      .mapCatching { it.info }
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

    memScoped {
      val arraySize = indices.size + 1
      val cArray = allocArray<IntVar>(arraySize).apply {
        for (i in indices.indices) this[i] = indices[i]
      }
      val player = mdkPlayerApi.pointed.`object`
      mdkPlayerApi.pointed.setActiveTracks!!.invoke(player, type.nativeValue, cArray, arraySize.toULong())
    }
    state.activeTracks[type] = tracks.map { it.first }
  }

  actual fun seek(position: Duration, vararg flag: SeekFlag) {
    val nativePosition = position.inWholeMilliseconds
    memScoped {
      val player = mdkPlayerApi.pointed.`object`
      val callback = cValue<mdkSeekCallback>()
      mdkPlayerApi.pointed.seekWithFlags!!.invoke(player, nativePosition, flag.combined.toUInt(), callback)
    }
  }

  private fun CPointer<mdkPlayerAPI>.withRef(block: CValuesRef<CPointerVar<mdkPlayerAPI>>.() -> Unit) {
    val ref = StableRef.create(this)
    try {
      block.invoke(ref.asCPointer().reinterpret())
    } finally {
      ref.dispose()
    }
  }

  init {
    scope.launch(Dispatchers.IO) {
      while (isActive) {
        try {
          val position = mdkPlayerApi.pointed.run { position?.invoke(`object`) } ?: 0L
          withContext(Dispatchers.Main) {
            state._position = position.milliseconds
          }
        } finally {
          delay(200)
        }
      }
    }
  }

  actual fun release() {
    with(mdkPlayerApi.pointed) {
      onMediaStatus!!.invoke(`object`, zeroValue(), callbacks.onMediaStatusToken.ptr)
      onEvent!!.invoke(`object`, zeroValue(), callbacks.onEventToken.ptr)
      onSync?.invoke(`object`, zeroValue(), -1)
    }
    mdkPlayerApi.withRef { mdkPlayerAPI_delete(this) }
    callbacks.reference.dispose()
    with(nativeHeap) {
      free(callbacks.onEventToken)
      free(callbacks.onMediaStatusToken)
    }
  }

  actual var currentRenderTarget: RenderTarget? = null
    set(value) {
      with(mdkPlayerApi.pointed) {
        //fixme: crash setRenderAPI?.invoke(`object`, null, null)
      }
      when (val actualValue = field) {
        is RenderTarget.Metal -> {
          actualValue.view.device = null
          actualValue.view.delegate = null
        }
        is RenderTarget.View -> TODO()
        null -> Unit
      }
      field = value
      when (val newValue = value) {
        is RenderTarget.Metal -> {
          newValue.view.framebufferOnly = false
          newValue.view.device = metalDevice
          newValue.view.delegate = this
          memScoped {
            val renderApi = alloc<mdkMetalRenderAPI>().apply {
              type = MDK_RenderAPI_Metal
              device = interpretCPointer(newValue.view.device!!.objcPtr())
              cmdQueue = interpretCPointer(metalCommandQueue.objcPtr())
              opaque = interpretCPointer(newValue.view.objcPtr())
              currentRenderTarget = staticCFunction(::_renderOnMetalTexture)
              layer = interpretCPointer(newValue.view.layer.objcPtr())
            }
            with(mdkPlayerApi.pointed) {
              setRenderAPI?.invoke(`object`, renderApi.ptr.reinterpret(), null)
            }
          }
        }
        is RenderTarget.View -> TODO()
        null -> Unit
      }
    }


  /**
   * Render region
   */
  internal val metalDevice = MTLCreateSystemDefaultDevice() ?: throw Exception("Unable to create MTLDeviceProtocol")
  private val metalCommandQueue = metalDevice.newCommandQueue() ?: throw Exception("Unable to create MTLCommandQueueProtocol")

  internal fun setRenderTarget(view: MTKView?) {
    when {
      view == null -> {
        with(mdkPlayerApi.pointed) { //todo: not tested yet
          setRenderAPI?.invoke(`object`, null, null)
        }
      }
      else -> {
        memScoped {
          val renderApi = alloc<mdkMetalRenderAPI>().apply {
            type = MDK_RenderAPI_Metal
            device = interpretCPointer(view.device!!.objcPtr())
            cmdQueue = interpretCPointer(metalCommandQueue.objcPtr())
            opaque = interpretCPointer(view.objcPtr())
            currentRenderTarget = staticCFunction(::_renderOnMetalTexture)
            layer = interpretCPointer(view.layer.objcPtr())
          }
          with(mdkPlayerApi.pointed) {
            setRenderAPI?.invoke(`object`, renderApi.ptr.reinterpret(), null)
          }
        }
      }
    }

  }

  override fun drawInMTKView(view: MTKView) {
    with(mdkPlayerApi.pointed) {
      renderVideo?.invoke(`object`, null)
    }
    val drawable = view.currentDrawable
    val buffer = metalCommandQueue.commandBuffer()
    if (drawable == null || buffer == null) return

    buffer.presentDrawable(drawable)
    buffer.commit()
  }

  override fun mtkView(view: MTKView, drawableSizeWillChange: CValue<CGSize>) {
    val (width, height) = drawableSizeWillChange.useContents { width.roundToInt() to height.roundToInt() }
    with(mdkPlayerApi.pointed) {
      setVideoSurfaceSize?.invoke(`object`, width, height,null)
    }
  }


}

private suspend fun <T> Deferred<T>.result(): Result<T> = kotlin.runCatching { await() }





