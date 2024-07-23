package fr.dcs.mdk.base

import android.util.Log

actual object MdkLibrary : Options {

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

  private external fun setGlobalOptionChar(name: String, value: Char)
  private external fun setGlobalOptionString(name: String, value: String?)
  private external fun setGlobalOptionInt(name: String, value: Int)
  private external fun setGlobalOptionFloat(name: String, value: Float)

  private external fun getGlobalOptionInt(name: String): Int
  private external fun getGlobalOptionString(name: String): String?


  actual val options: Options
    get() = this

  override fun getString(key: String): String? = getGlobalOptionString(key)
  override fun getInt(key: String): Int = getGlobalOptionInt(key)

  override fun set(key: String, value: String?) = setGlobalOptionString(key, value)
  override fun set(key: String, value: Int) =  setGlobalOptionInt(key, value)
  override fun set(key: String, value: Float) = setGlobalOptionFloat(key, value)

}
