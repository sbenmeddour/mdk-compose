package fr.dcs.mdk

import android.text.format.DateUtils
import kotlin.time.Duration

actual fun Duration.formatElapsedTime(builder: StringBuilder?): String {
  return when {
    builder == null -> DateUtils.formatElapsedTime(this.inWholeSeconds)
    else -> DateUtils.formatElapsedTime(builder, this.inWholeSeconds)
  }
}