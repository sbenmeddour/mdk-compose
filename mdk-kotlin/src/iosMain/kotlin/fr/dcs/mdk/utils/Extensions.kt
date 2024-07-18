package fr.dcs.mdk.utils

import cocoapods.mdk.*
import fr.dcs.mdk.native.*
import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
internal fun NativeMediaInfo.Companion.fromC(value: mdkMediaInfo?): NativeMediaInfo? {
  if (value == null) return null
  return NativeMediaInfo(
    startTime = value.start_time,
    duration = value.duration,
    bitRate = value.bit_rate,
    size = value.size,
    format = value.format?.toKStringFromUtf8().orEmpty(),
    streams = value.streams,
    audio = emptyList(),
    video = emptyList(),
    subtitles = emptyList(),
  )
}