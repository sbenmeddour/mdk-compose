@file:OptIn(ExperimentalEncodingApi::class, ExperimentalEncodingApi::class)

package fr.dcs.mdk

import androidx.compose.runtime.*
import androidx.navigation.*
import androidx.navigation.compose.*
import kotlin.io.encoding.*

val LocalNavController = staticCompositionLocalOf<NavHostController> { error("") }

@Composable
fun Navigation() {
  NavHost(
    navController = LocalNavController.current,
    startDestination = "/",
    builder = {
      composable("/") { RootScreen() }
      composable("/player/{base_64_url}") { entry ->
        val url = entry.arguments!!.getString("base_64_url")!!.let(Base64::decode).decodeToString()
        PlayerScreen(url)
      }
    },
  )


}