# mdk-compose
MDK player wrapper for Compose-Multiplatform

## Compatibility
- Android
    - [x] SufaceView
    - [x] Vulkan
    - [x] GL
- iOS
    - [x] Metal
    - [ ] UIView
- Desktop
    - [ ] macOS
    - [ ] Windows

# Usage:
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