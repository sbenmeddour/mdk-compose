package fr.dcs.mdk.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import fr.dcs.mdk.player.Player
import fr.dcs.mdk.player.configuration.PlayerConfiguration
import fr.dcs.mdk.player.state.PlayerState

@Composable
@Stable
expect fun rememberPlayer(
  configuration: PlayerConfiguration = remember { PlayerConfiguration.defaultConfig },
  state: PlayerState = remember { PlayerState.composeBased() },
): Player


@Composable
expect fun PlayerView(
  modifier: Modifier,
  player: Player,
)