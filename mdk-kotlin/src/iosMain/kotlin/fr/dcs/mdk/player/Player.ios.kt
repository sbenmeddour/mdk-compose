package fr.dcs.mdk.player

import cocoapods.mdk.*
import fr.dcs.mdk.player.configuration.*
import fr.dcs.mdk.player.events.*
import fr.dcs.mdk.player.state.*
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
import kotlin.math.*
import kotlin.time.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

actual class Player actual constructor(
  configuration: PlayerConfiguration,
  actual val state: PlayerState
) : NSObject(), MTKViewDelegateProtocol {

  private val mdkPlayerApi: CPointer<mdkPlayerAPI> = mdkPlayerAPI_new()!!

  internal actual val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  internal val _events = MutableSharedFlow<PlayerEvent>(replay = 0, extraBufferCapacity = 1)
  private val callbacks = Callbacks(StableRef.create(this))

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

  actual suspend fun prepare(position: Duration, vararg flags: SeekFlag) {
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
    val result = try { kotlinCallback.await() } finally { kotlinCallbackRef.dispose() }
    withContext(Dispatchers.Main) { state._nativeMediaInfo = result.info }
  }

  //todo: Not tested yet
  actual fun setTrack(type: MediaType, index: Int) {
    memScoped {
      val player = mdkPlayerApi.pointed.`object`
      val nativeType = when (type) {
        MediaType.Unknown -> MDK_MediaType_Unknown
        MediaType.Audio -> MDK_MediaType_Audio
        MediaType.Video -> MDK_MediaType_Video
        MediaType.Subtitle -> MDK_MediaType_Subtitle
      }
      val activeTracksList: List<Int> = when {
        index == -1 -> emptyList()
        else -> listOf(index)
      }
      state.activeTracks[type] = activeTracksList

      val arraySize = activeTracksList.size + 1
      val cArray = allocArray<IntVar>(arraySize).apply {
        for (i in activeTracksList.indices) this[i] = activeTracksList[i]
      }
      mdkPlayerApi.pointed.setActiveTracks!!.invoke(player, nativeType, cArray, arraySize.toULong())
    }
  }


  actual fun seek(position: Duration, vararg flag: SeekFlag) {
    val nativePosition = position.inWholeMilliseconds
    memScoped {
      val player = mdkPlayerApi.pointed.`object`
      val callback = cValue<mdkSeekCallback>()
      mdkPlayerApi.pointed.seekWithFlags!!.invoke(player, nativePosition, flag.combined, callback)
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





