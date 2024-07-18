package fr.dcs.mdk

import kotlin.time.*

actual fun Duration.formatElapsedTime(builder: StringBuilder?): String {
  val stringBuilder = builder ?: StringBuilder()
  stringBuilder.clear()
  this.toComponents { hours, minutes, seconds, _ ->
    if (hours > 0) stringBuilder.append("$hours:")
    stringBuilder.append("$minutes:$seconds")
  }
  return stringBuilder.toString()

}