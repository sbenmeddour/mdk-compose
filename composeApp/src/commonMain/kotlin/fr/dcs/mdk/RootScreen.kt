@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalEncodingApi::class)

package fr.dcs.mdk

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import kotlin.io.encoding.*

private val urls = listOf(
  "Chronometer [m3u8]" to "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear1/prog_index.m3u8",
  "Test subtitles [mkv]" to "https://github.com/ietf-wg-cellar/matroska-test-files/raw/master/test_files/test5.mkv",
  "1080p - 60fps - HEVC [mp4]" to "https://lafibre.info/videos/test/201411_blender_big_buck_bunny_60fps_1080p_hevc.mp4",
  "1440p - 60fps - HEVC [mp4]" to "https://lafibre.info/videos/test/201411_blender_big_buck_bunny_60fps_1440p_hevc.mp4",
  "2160p - 60fps - HEVC [mp4]" to "https://lafibre.info/videos/test/201411_blender_big_buck_bunny_60fps_2160p_hevc.mp4",
)

@Composable
fun RootScreen() {
  val navController = LocalNavController.current
  Scaffold(
    modifier = Modifier,
    topBar = {
      TopAppBar(
        title = { Text("Mdk compose") },
      )
    },
    content = {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize(),
        contentPadding = PaddingValues(
          top = it.calculateTopPadding(),
          start = 12.dp,
          end = 12.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = {
          items(urls) { (name, url) ->
            Card(
              modifier = Modifier
                .fillMaxWidth(),
              content = {
                Text(
                  modifier = Modifier.padding(12.dp),
                  text = name,
                )
              },
              onClick = {
                val encodedUrl = Base64.encode(url.encodeToByteArray())
                navController.navigate("/player/$encodedUrl")
              }
            )
          }
        }
      )
    },
  )
}