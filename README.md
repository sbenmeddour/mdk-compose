# mdk-compose
MDK player wrapper for Compose-Multiplatform

#Usage:
``` kotlin
val player = rememberPlayer()
PlayerView(
    modifier = Modifier.fillMaxSize(),
    player = player,
)
LaunchedEffect(Unit) {
    player.setMedia(url)
    player.prepare().onSuccess { player.play() }
}