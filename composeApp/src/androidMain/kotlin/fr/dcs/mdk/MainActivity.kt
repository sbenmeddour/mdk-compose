package fr.dcs.mdk

import android.os.Bundle
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import fr.dcs.mdk.player.Player
import fr.dcs.mdk.player.PlayerState
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {

      val player = remember {
        val state = PlayerState.composeBasedState()
        Player(state)
      }

      LaunchedEffect(key1 = Unit) {
        player.setMedia("https://vfx.mtime.cn/Video/2021/11/16/mp4/211116131456748178.mp4")
        player.prepare()
        delay(5000)
        player.play()
      }
      AndroidView(
        modifier = Modifier
          .fillMaxSize(),
        factory = ::SurfaceView,
        update = player::setSurfaceView,
        onRelease = player::detachSurfaceView,
      )
    }
  }
}
