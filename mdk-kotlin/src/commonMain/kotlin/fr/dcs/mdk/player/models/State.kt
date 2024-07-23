package fr.dcs.mdk.player.models

import androidx.compose.runtime.*

@Immutable
enum class State(internal val nativeValue: Int) {

  Stopped(0), Playing(1), Paused(2);

  internal companion object {

    fun fromInt(value: Int): State = when (value) {
      0 -> Stopped
      1 -> Playing
      2 -> Paused
      else -> Stopped //todo throw error ?
    }
  }

}

