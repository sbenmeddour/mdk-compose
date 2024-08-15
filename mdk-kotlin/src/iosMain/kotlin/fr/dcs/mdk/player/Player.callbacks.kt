package fr.dcs.mdk.player

import cocoapods.mdk.*
import fr.dcs.mdk.player.events.*
import fr.dcs.mdk.player.models.*
import fr.dcs.mdk.utils.*
import kotlinx.cinterop.*
import platform.MetalKit.*

internal class Callbacks(val reference: StableRef<Player>) {

  val onMediaStatus = cValue<mdkMediaStatusCallback> {
    cb = staticCFunction(::_onMediaStatus)
    opaque = reference.asCPointer()
  }

  val onStateChanged = cValue<mdkStateChangedCallback> {
    cb = staticCFunction(::_onStateChanged)
    opaque = reference.asCPointer()
  }

  val onMediaEvent = cValue<mdkMediaEventCallback> {
    cb = staticCFunction(::_onMediaEvent)
    opaque = reference.asCPointer()
  }

  val logHandler = cValue<mdkLogHandler> {
    cb = staticCFunction { level, value, opaque ->
      val message = value?.toKStringFromUtf8().orEmpty()
      when {
        message.contains("getVideoOutContext") -> return@staticCFunction
        message.contains("to be destroyed is not rendered by") -> return@staticCFunction
        else -> println("MDK LOG: = ${value?.toKStringFromUtf8()}")
      }
    }
    opaque = reference.asCPointer()
  }

  val onMediaStatusToken = nativeHeap.alloc<MDK_CallbackTokenVar>()
  val onEventToken = nativeHeap.alloc<MDK_CallbackTokenVar>()

}


private fun _onMediaEvent(event: CPointer<mdkMediaEvent>?, opaque: COpaquePointer?): Boolean {
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

private fun _onMediaStatus(oldValue: MDK_MediaStatus, newValue: MDK_MediaStatus, opaque: COpaquePointer?): Boolean {
  val playerRef = opaque?.asStableRef<Player>() ?: return false
  val player = playerRef.get()
  player.state.status = MediaStatus.Mixed(newValue)
  return false
}

private fun _onStateChanged(state: MDK_State, opaque: COpaquePointer?) {
  val playerRef = opaque?.asStableRef<Player>() ?: return
  val player = playerRef.get()
  player.state.state = State.fromInt(state.toInt())
}

internal fun _onPrepared(position: Long, boost: CPointer<BooleanVar>?, opaque: COpaquePointer?): Boolean {
  boost?.pointed?.value = true
  val unwrapped = opaque!!.asStableRef<KotlinPrepareCallback>().get()
  when {
    position < 0 -> {
      val error = PlayerException.PrepareException(position)
      unwrapped.completeExceptionally(error)
    }
    else -> {
      val result = KotlinPrepareResult(
        position = position,
        info = with (unwrapped.player.pointed) {
          val mdkMediaInfo = this.mediaInfo!!.invoke(`object`)
          MediaInfo.fromC(mdkMediaInfo?.pointed)
        },
      )
      unwrapped.complete(result)
    }
  }
  return true
}

internal fun _renderOnMetalTexture(pointer: COpaquePointer?): COpaquePointer? {
  if (pointer == null) return null
  val metalView = interpretObjCPointerOrNull<MTKView>(pointer.rawValue) ?: return null
  val texture = metalView.currentDrawable?.texture ?: return null
  return interpretCPointer(texture.objcPtr())
}