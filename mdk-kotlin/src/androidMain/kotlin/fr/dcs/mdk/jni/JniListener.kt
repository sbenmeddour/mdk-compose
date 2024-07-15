package fr.dcs.mdk.jni

interface JniListener {
  fun onStateChanged(value: Int)
  fun onMediaStatus(previousStatus: Int, newStatus: Int)
  fun onMediaEvent(error: Long, category: String?, detail: String?)
}