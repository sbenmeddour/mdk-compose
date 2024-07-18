package fr.dcs.mdk.utils

import fr.dcs.mdk.player.*

internal val Array<out SeekFlag>.combined: Int
  get() = this.map { it.asNativeSeekFlag().nativeValue }.reduce { acc, i -> acc or i }
