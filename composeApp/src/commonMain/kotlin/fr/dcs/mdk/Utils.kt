package fr.dcs.mdk

import kotlin.time.Duration

expect fun Duration.formatElapsedTime(builder: StringBuilder? = null): String