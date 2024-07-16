package fr.dcs.mdk.jni

import android.view.Surface
import fr.dcs.mdk.native.NativeMediaInfo

internal object JNIPlayer {

  external fun createWrapper(listener: JniListener): Long

  external fun setAudioBackends(handle: Long, values: Array<String>)
  external fun setDecoders(handle: Long, mediaType: Int, values: Array<String>)

  external fun setSurface(handle: Long, surface: Surface?, width: Int, height: Int): Long
  external fun resizeSurface(handle: Long, height: Int, width: Int)

  external fun setState(handle: Long, value: Int)

  external fun setMedia(handle: Long, url: String)
  external fun prepare(handle: Long, startPosition: Long, seekFlags: Int, unloadImmediately: Boolean): NativeMediaInfo?

  external fun getMediaInfo(handle: Long): NativeMediaInfo?

  external fun setFrameRate(handle: Long, value: Float)
  external fun setTrack(handle: Long, type: Int, index: Int)
  external fun setPlaybackRate(handle: Long, rate: Float): Float
  external fun setVolume(handle: Long, value: Float): Float

  external fun setBufferRange(handle: Long, min: Long, max: Long, drop: Boolean)

  external fun seek(handle: Long, flags: Int, position: Long)
  external fun seekAsync(handle: Long, flags: Int, position: Long): Long

  external fun setIsMuted(handle: Long, isMuted: Boolean): Boolean

  external fun release(handle: Long)

  external fun getPosition(handle: Long): Long

  external fun setProperty(handle: Long, key: String, value: String?)
  external fun getProperty(handle: Long, key: String): String?

}



