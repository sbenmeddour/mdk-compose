package fr.dcs.mdk.native

internal enum class NativeSeekFlag(val nativeValue: Int) {
  FromZero(1),
  FromStart(1 shl 1),
  FromNow(1 shl 2),
  Frame(1 shl 6),
  KeyFrame(1 shl 8),
  Fast(KeyFrame.nativeValue),
  AnyFrame(1 shl 9),
  InCache(1 shl 10),
  Backward(1 shl 16),
  Default(KeyFrame.nativeValue or FromStart.nativeValue or InCache.nativeValue)
}