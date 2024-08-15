package fr.dcs.mdk

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.navigation.compose.*
import org.jetbrains.compose.ui.tooling.preview.*

@Composable
@Preview
fun App() {
  MaterialTheme(
    colorScheme = darkColorScheme(),
    content = {
      Surface(
        modifier = Modifier.fillMaxSize(),
        content = {
          val controller = rememberNavController()
          CompositionLocalProvider(LocalNavController provides controller) { Navigation()  }
        },
      )
    }
  )

}

