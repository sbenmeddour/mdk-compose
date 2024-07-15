package fr.dcs.mdk.base

import android.util.Log

actual object MdkLibrary {

  private const val TAG = "MdkLibrary"

  actual fun initialize(configuration: Configuration) {
    tryLoad("c++_shared")
    tryLoad("ffmpeg")
    tryLoad("mdk")
    tryLoad("mdk-avglue")
    System.loadLibrary("mdk-jni")
  }

  private fun tryLoad(libraryName: String) = try {
    System.loadLibrary(libraryName)
  } catch (error: Throwable) {
    Log.d(TAG, "Failed to load library $libraryName")
  }

  external fun setGlobalOptionChar(name: String, value: Char)
  external fun setGlobalOptionString(name: String, value: String)
  external fun setGlobalOptionInt(name: String, value: Int)
  external fun setGlobalOptionFloat(name: String, value: Float)

}