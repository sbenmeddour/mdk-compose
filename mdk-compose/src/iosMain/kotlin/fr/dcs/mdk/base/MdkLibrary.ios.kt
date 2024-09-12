package fr.dcs.mdk.base

import cocoapods.mdk.*
import kotlinx.cinterop.*

actual object MdkLibrary : Options {

  actual fun initialize(configuration: Configuration) = Unit

  actual val options: Options
    get() = this

  override fun getString(key: String): String? = memScoped {
    val result = alloc<CPointerVar<ByteVar>>()
    val success = MDK_getGlobalOptionString(key, result.ptr)
    if (success) result.value?.toKStringFromUtf8() else null
  }

  override fun getInt(key: String): Int? = memScoped {
    val result = alloc<IntVar>()
    val success = MDK_getGlobalOptionInt32(key, result.ptr)
    if (success) result.value else null
  }

  override fun set(key: String, value: String?) = MDK_setGlobalOptionString(key, value)
  override fun set(key: String, value: Int) = MDK_setGlobalOptionInt32(key, value)
  override fun set(key: String, value: Float) = MDK_setGlobalOptionFloat(key, value)

}
