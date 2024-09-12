package fr.dcs.mdk.player.models

import androidx.compose.runtime.*

@Immutable
enum class SeekFlag(internal val nativeValue: Int) {
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

internal val List<SeekFlag>.combined: Int
  get() = when {
    this.isEmpty() -> SeekFlag.Default.nativeValue
    this.size == 1 -> this.first().nativeValue
    else -> this.map(SeekFlag::nativeValue).reduce { acc, flag ->  acc or flag }
  }

internal val Array<out SeekFlag>.combined: Int
  get() = this.toList().combined
