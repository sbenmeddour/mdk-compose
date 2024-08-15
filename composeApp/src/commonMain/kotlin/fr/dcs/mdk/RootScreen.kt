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
  "720p avc MKV" to "https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-lavf-56.36.100-aac-avc-main-1280x720.mkv",
  "Apple bip bop M3U8" to "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear1/prog_index.m3u8",
  "720p avc MKV" to "https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-lavf-56.36.100-aac-avc-main-1280x720.mkv",
  "H265 mkv" to "https://ks3-cn-beijing.ksyun.com/ksplayer/h265/mp4_resource/jinjie_265.mp4",
  "Sample with subtitles" to "https://github.com/ietf-wg-cellar/matroska-test-files/raw/master/test_files/test5.mkv",
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