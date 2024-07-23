package fr.dcs.mdk.ui

import androidx.compose.runtime.*
import androidx.compose.ui.*
import fr.dcs.mdk.player.*
import fr.dcs.mdk.player.configuration.*

@Composable
@Stable
expect fun rememberPlayer(
  configuration: PlayerConfiguration = remember { PlayerConfiguration.defaultConfig },
): Player


@Composable
expect fun PlayerView(
  modifier: Modifier,
  player: Player,
)

