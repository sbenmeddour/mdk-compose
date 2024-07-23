package fr.dcs.mdk

import android.os.Bundle
import androidx.activity.*
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { App() }
  }

}
