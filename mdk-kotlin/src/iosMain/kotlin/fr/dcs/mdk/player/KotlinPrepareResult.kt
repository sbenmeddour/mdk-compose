package fr.dcs.mdk.player

import fr.dcs.mdk.native.*
import fr.dcs.mdk.native.NativeMediaInfo

internal class KotlinPrepareResult(
  val position: Long,
  val info: NativeMediaInfo?,
)