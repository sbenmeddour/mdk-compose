package fr.dcs.mdk

import android.app.Application
import fr.dcs.mdk.base.Configuration
import fr.dcs.mdk.base.MdkLibrary

class TestApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    MdkLibrary.initialize(configuration = Configuration(apiKey = null))
    MdkLibrary.options["subtitle.fonts.file"] = "assets://fonts/Roboto-Regular.ttf"
  }

}