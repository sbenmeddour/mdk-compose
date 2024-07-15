package fr.dcs.mdk.native

internal enum class NativeState(val nativeValue: Int) {
  Stopped(0),
  Playing(1),
  Paused(2)
}

