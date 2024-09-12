# mdk-compose
MDK player wrapper for Compose-Multiplatform
Under development, not ready at all

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

#### TO DO :
- [ ] Windows
- [ ] macOS
- [ ] fix issues
- [ ] iOS UIView
- [ ] Expose more APIs
- [ ] Publish artifact

## Usage:
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