package fr.dcs.mdk.player

import cocoapods.mdk.*
import fr.dcs.mdk.native.*
import fr.dcs.mdk.player.configuration.*
import fr.dcs.mdk.player.events.*
import fr.dcs.mdk.player.state.*
import fr.dcs.mdk.utils.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import platform.CoreGraphics.*
import platform.Metal.*
import platform.MetalKit.*
import platform.UIKit.*
import kotlin.math.*
import kotlin.time.*
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalForeignApi::class)
actual class Player actual constructor(
  configuration: PlayerConfiguration,
  actual val state: PlayerState
) : Properties {

  internal actual val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  internal val _events = MutableSharedFlow<PlayerEvent>(replay = 0, extraBufferCapacity = 1)

  actual val events: Flow<PlayerEvent> get() = _events

  private val mdkPlayerApi: CPointer<mdkPlayerAPI> = mdkPlayerAPI_new()!!

  private val thisRef = StableRef.create(this)

  private val onMediaStatusCallback = cValue<mdkMediaStatusCallback> {
    cb = staticCFunction(::onMediaStatus)
    opaque = thisRef.asCPointer()
  }

  private val onStateChangedCallback = cValue<mdkStateChangedCallback> {
    cb = staticCFunction(::onStateChanged)
    opaque = thisRef.asCPointer()
  }

  private val onMediaEventCallback = cValue<mdkMediaEventCallback> {
    cb = staticCFunction(::onMediaEvent)
    opaque = thisRef.asCPointer()
  }

  private val logHandler = cValue<mdkLogHandler> {
    cb = staticCFunction { level, value, opaque ->

    }
    opaque = thisRef.asCPointer()
  }

  private val onMediaStatusCallbackToken = nativeHeap.alloc<MDK_CallbackTokenVar>()
  private val onEventCallbackToken = nativeHeap.alloc<MDK_CallbackTokenVar>()

  init {
    MDK_setLogLevel(MDK_LogLevel.MDK_LogLevel_All)
    MDK_setGlobalOptionString("logLevel", "all")
    MDK_setGlobalOptionString("profiler.gpu", "0")
    MDK_setGlobalOptionInt32("videoout.clear_on_stop", 1)
    MDK_setLogHandler(logHandler)

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
      playerApi.onMediaStatus!!.invoke(playerObject, onMediaStatusCallback, onMediaStatusCallbackToken.ptr)
      playerApi.onEvent!!.invoke(playerObject, onMediaEventCallback, onEventCallbackToken.ptr)
      playerApi.onStateChanged!!.invoke(playerObject, onStateChangedCallback)
    }
  }

  actual fun play() {
    println("Player.play")
    val player = mdkPlayerApi.pointed.`object`
    mdkPlayerApi.pointed.setState!!.invoke(player, MDK_State_Playing)
  }

  actual fun pause() {
    println("Player.pause")
    val player = mdkPlayerApi.pointed.`object`
    mdkPlayerApi.pointed.setState!!.invoke(player, MDK_State_Paused)
  }

  actual fun stop() {
    println("Player.stop")
    val player = mdkPlayerApi.pointed.`object`
    mdkPlayerApi.pointed.setState!!.invoke(player, MDK_State_Stopped)
  }

  actual fun playPause() {
    println("Player.playPause")
    val player = mdkPlayerApi.pointed.`object`
    when (state._nativeState.toUInt()) {
      MDK_State_Paused -> mdkPlayerApi.pointed.setState!!.invoke(player, MDK_State_Playing)
      MDK_State_Playing -> mdkPlayerApi.pointed.setState!!.invoke(player, MDK_State_Paused)
      MDK_State_Stopped -> return
      else -> return
    }
  }

  actual fun setMedia(url: String) {
    println("Player.setMedia")
    println("url = [${url}]")
    memScoped {
      val player = mdkPlayerApi.pointed.`object`
      val cUrl = url.cstr.ptr
      mdkPlayerApi.pointed.setMedia!!.invoke(player, cUrl)
    }
  }


  actual suspend fun prepare(position: Duration, vararg flags: SeekFlag) {

    val player = mdkPlayerApi.pointed.`object`

    val kotlinCallback = KotlinPrepareCallback(mdkPlayerApi)
    val kotlinCallbackRef = StableRef.create(kotlinCallback)

    val callback = cValue<mdkPrepareCallback> {
      cb = staticCFunction(::onPrepared)
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

  actual fun setTrack(type: MediaType, index: Int) {
    println("Player.setTrack")
    println("type = [${type}], index = [${index}]")
    memScoped {
      val player = mdkPlayerApi.pointed.`object`
      val nativeType = when (type) {
        MediaType.Unknown -> MDK_MediaType_Unknown
        MediaType.Audio -> MDK_MediaType_Audio
        MediaType.Video -> MDK_MediaType_Video
        MediaType.Subtitle -> MDK_MediaType_Subtitle
      }
      val tracksArray = when {
        index == -1 -> allocArray<IntVar>(1)
        else -> allocArray<IntVar>(2L).apply { this[0] = index }
      }

      val size = when (index) {
        -1 -> 0
        else -> 1
      }

      mdkPlayerApi.pointed.setActiveTracks!!.invoke(player, nativeType, tracksArray, size.toULong())
    }
  }

  actual fun release() {
    println("Player.release")
    with(mdkPlayerApi.pointed) {
      onMediaStatus!!.invoke(`object`, zeroValue(), onMediaStatusCallbackToken.ptr)
      onEvent!!.invoke(`object`, zeroValue(), onEventCallbackToken.ptr)
    }
    mdkPlayerApi.withRef { mdkPlayerAPI_delete(this) }
    thisRef.dispose()
  }

  actual fun seek(position: Duration, vararg flag: SeekFlag) {
    println("Player.seek")
    println("position = [${position}], flag = [${flag}]")
    val combinedFlags = flag.map { it.asNativeSeekFlag().nativeValue }
      .reduce { acc, i -> acc or i }
      .toUInt()
    val nativePosition = position.inWholeMilliseconds
    memScoped {
      val player = mdkPlayerApi.pointed.`object`
      val callback = cValue<mdkSeekCallback>()
      mdkPlayerApi.pointed.seekWithFlags!!.invoke(player, nativePosition, combinedFlags, callback)
    }
  }

  actual val properties: Properties
    get() = this

  override operator fun set(key: String, value: String) {
    memScoped {
      val player = mdkPlayerApi.pointed.`object`
      val cKey = key.cstr.ptr
      val cValue = value.cstr.ptr
      mdkPlayerApi.pointed.setProperty!!.invoke(player, cKey, cValue)
    }
  }

  override operator fun get(key: String): String? {
    return memScoped {
      val player = mdkPlayerApi.pointed.`object`
      val cKey = key.cstr.ptr
      mdkPlayerApi.pointed.getProperty!!.invoke(player, cKey)?.toKStringFromUtf8()
    }
  }

  internal fun setNativeSurface(view: UIView) {
    memScoped {
      val renderApi = alloc<mdkMetalRenderAPI>().apply {
        type = MDK_RenderAPI_Metal
      }
      val player = mdkPlayerApi.pointed.`object`!!
      val viewPointer: COpaquePointer? = interpretCPointer(view.objcPtr())
      mdkPlayerApi.pointed.setRenderAPI!!.invoke(player, renderApi.ptr.reinterpret(), viewPointer)
    }
  }

  internal fun setVideoSurfaceSize(view: UIView, width: Int, height: Int) {
    println("Player.updateNativeSurface")
    println("view = [${view}], width = [${width}], height = [${height}]")
    memScoped {
      val player = mdkPlayerApi.pointed.`object`!!
      val viewPointer: COpaquePointer? = interpretCPointer(rawValue = view.objcPtr())
      mdkPlayerApi.pointed.setVideoSurfaceSize!!.invoke(
        p1 = player,
        p2 = width,
        p3 = height,
        p4 = viewPointer,
      )
    }
  }

  internal fun setRenderTarget(
    metalKitView: MTKView,
    commandQueue: MTLCommandQueueProtocol,
  ) {
    println("Player.setRenderTarget")
    println("metalKitView = [${metalKitView}], commandQueue = [${commandQueue}]")
    memScoped {
      val player = mdkPlayerApi.pointed.`object`!!

      val renderTargetFunction = staticCFunction<COpaquePointer?, COpaquePointer?> { pointer ->
        if (pointer == null) {
          return@staticCFunction null
        }
        val maybeMetalView = interpretObjCPointerOrNull<MTKView>(pointer.rawValue) ?: return@staticCFunction null
        val drawable = maybeMetalView.currentDrawable ?: return@staticCFunction null
        val texture: COpaquePointer? = interpretCPointer(drawable.objcPtr())
        return@staticCFunction texture
      }

      val renderApi = alloc<mdkMetalRenderAPI>().apply {
        type = MDK_RenderAPI_Metal
        device = interpretCPointer(metalKitView.device!!.objcPtr())
        cmdQueue = interpretCPointer(commandQueue.objcPtr())
        opaque = interpretCPointer(metalKitView.objcPtr())
        currentRenderTarget = renderTargetFunction //renderTargetFunction
        layer = interpretCPointer(metalKitView.layer.objcPtr())
      }
      mdkPlayerApi.pointed.setRenderAPI!!.invoke(
        p1 = player,
        p2 = renderApi.ptr.reinterpret(),
        p3 = interpretCPointer(metalKitView.objcPtr()),
      )

    }

  }

  private var currentUIView: COpaquePointer? = null

  internal fun setUiView(view: UIView, width: Int, height: Int) {
    currentUIView = interpretCPointer(rawValue = view.objcPtr())
    memScoped {
      val renderApi = alloc<mdkMetalRenderAPI>().apply {
        type = MDK_RenderAPI_Metal
      }
      val player = mdkPlayerApi.pointed.`object`!!
      mdkPlayerApi.pointed.setRenderAPI!!.invoke(
        p1 = player,
        p2 = renderApi.ptr.reinterpret(),
        p3 = currentUIView
      )
      mdkPlayerApi.pointed.updateNativeSurface!!.invoke(
        p1 = player,
        p2 = currentUIView,
        p3 = width,
        p4 = height,
        p5 = MDK_SurfaceType.MDK_SurfaceType_Auto
      )
    }
  }

  internal fun onUIViewResized(view: UIView, rect: CValue<CGRect>) {
    println("currentUIView = ${currentUIView}")
    println("Player.onUIViewResized")
    val player = mdkPlayerApi.pointed.`object`
    val (width, height) = rect.useContents { this.size.width to this.size.height }
    println("width = ${width}")
    println("height = ${height}")

//    mdkPlayerApi.pointed.resizeSurface?.invoke(
//      player,
//      width.roundToInt(),
//      height.roundToInt(),
//    )

//    mdkPlayerApi.pointed.setVideoSurfaceSize?.invoke(
//      player,
//      width.roundToInt(),
//      height.toInt(),
//      currentUIView,
//    )
    mdkPlayerApi.pointed.updateNativeSurface!!.invoke(
      p1 = player,
      p2 = currentUIView,
      p3 = width.roundToInt(),
      p4 = height.roundToInt(),
      p5 = MDK_SurfaceType.MDK_SurfaceType_Auto
    )
  }

  internal fun detachNativeSurface() {

  }

  fun renderVideo(view: MTKView) {
    memScoped {
      val player = mdkPlayerApi.pointed.`object`!!
      val viewRef = StableRef.create(view)
      try {
        mdkPlayerApi.pointed.renderVideo?.invoke(
          p1 = player,
          p2 = viewRef.asCPointer(),
        )
      } finally {
        viewRef.dispose()
      }

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
    for (entry in configuration.properties) properties[entry.key] = entry.value
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



}


@OptIn(ExperimentalForeignApi::class)
fun onMediaEvent(event: CPointer<mdkMediaEvent>?, opaque: COpaquePointer?): Boolean {
  if (event == null) return false
  val playerRef = opaque?.asStableRef<Player>() ?: return false

  val playerEvent = with (event.pointed) {
    PlayerEvent.fromData(
      error = error, category = category?.toKStringFromUtf8(),
      detail = detail?.toKStringFromUtf8()
    )
  }

  val player = playerRef.get()
  if (playerEvent != null) player._events.tryEmit(playerEvent)
  return false
}

@OptIn(ExperimentalForeignApi::class)
fun onMediaStatus(oldValue: MDK_MediaStatus, newValue: MDK_MediaStatus, opaque: COpaquePointer?): Boolean {
  val playerRef = opaque?.asStableRef<Player>() ?: return false
  val player = playerRef.get()
  player.state._nativeMediaStatus = newValue
  return false
}

@OptIn(ExperimentalForeignApi::class)
private fun onStateChanged(state: MDK_State, opaque: COpaquePointer?) {
  val playerRef = opaque?.asStableRef<Player>() ?: return
  val player = playerRef.get()
  player.state._nativeState = state.toInt()
}

@OptIn(ExperimentalForeignApi::class)
private fun onPrepared(position: Long, boost: CPointer<BooleanVar>?, opaque: COpaquePointer?): Boolean {
  boost?.pointed?.value = true
  val unwrapped = opaque!!.asStableRef<KotlinPrepareCallback>().get()
  val result = KotlinPrepareResult(
    position = position,
    info = with (unwrapped.player.pointed) {
      val mdkMediaInfo = this.mediaInfo!!.invoke(`object`)
      NativeMediaInfo.fromC(mdkMediaInfo?.pointed)
    },
  )
  unwrapped.complete(result)
  return true
}


