package fr.dcs.mdk

import android.os.*
import androidx.activity.*
import androidx.activity.compose.*

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { App() }
  }

}
