package fr.dcs.mdk

import kotlin.time.*

actual fun Duration.formatElapsedTime(builder: StringBuilder?): String {
  val stringBuilder = builder ?: StringBuilder()
  val seconds = this.absoluteValue.inWholeSeconds
  val minutes = (seconds / 60) % 60
  val hours = seconds / 3600
  with (stringBuilder) {
    clear()
    if (this@formatElapsedTime == Duration.ZERO) {
      append("00:00")
      return@with
    }
    if (this@formatElapsedTime < Duration.ZERO) {
      append("-")
    }
    if (hours > 0) {
      append(
        hours.toString().padStart(2, '0')
      )
      append(":")
    }
    append(minutes.toString().padStart(2, '0'))
    append(":")
    append(
      seconds.rem(60).toString().padStart(2, '0')
    )
  }
  return stringBuilder.toString()

}