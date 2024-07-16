package fr.dcs.mdk.player

import fr.dcs.mdk.native.NativeSeekFlag

enum class SeekFlag {
  FromZero,
  FromStart,
  FromNow,
  Frame,
  KeyFrame,
  Fast,
  AnyFrame,
  InCache,
  Backward,
  Default,
}

internal fun SeekFlag.asNativeSeekFlag(): NativeSeekFlag = when (this) {
  SeekFlag.FromZero -> NativeSeekFlag.FromZero
  SeekFlag.FromStart -> NativeSeekFlag.FromStart
  SeekFlag.FromNow -> NativeSeekFlag.FromNow
  SeekFlag.Frame -> NativeSeekFlag.Frame
  SeekFlag.KeyFrame -> NativeSeekFlag.KeyFrame
  SeekFlag.Fast -> NativeSeekFlag.Fast
  SeekFlag.AnyFrame -> NativeSeekFlag.AnyFrame
  SeekFlag.InCache -> NativeSeekFlag.InCache
  SeekFlag.Backward -> NativeSeekFlag.Backward
  SeekFlag.Default -> NativeSeekFlag.Default
}