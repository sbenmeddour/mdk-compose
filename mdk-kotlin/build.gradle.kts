plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.native.cocoapods)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.jetbrainsCompose)
}

kotlin {

  androidTarget()
  jvm("desktop")

  iosX64()
  iosArm64()
  iosSimulatorArm64()

  cocoapods {
    ios.deploymentTarget = "14.1"
    framework {
      baseName = "MdkKotlin"
    }
    pod(name = "mdk")
    noPodspec()
  }

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain.dependencies {
      implementation(libs.kotlinx.coroutines.core)
      implementation(compose.runtime)
      implementation(compose.ui)
      implementation(compose.foundation)
    }
    androidMain.dependencies {
      implementation(libs.androidx.core.ktx)
      implementation(libs.androidx.appcompat)
    }
  }

  targets.configureEach {
    compilations.configureEach {
      compileTaskProvider.configure {
        compilerOptions {
          freeCompilerArgs.add("-Xexpect-actual-classes")
        }
      }
    }
  }

}


android {
  namespace = "fr.dcs.mdk"
  compileSdk = libs.versions.android.compileSdk.get().toInt()
  ndkVersion = "26.1.10909125"
  defaultConfig {
    minSdk = libs.versions.android.minSdk.get().toInt()
    externalNativeBuild {
      cmake {
        cppFlags += ""
        arguments += "-DANDROID_STL=c++_shared"
      }
    }
  }
  externalNativeBuild {
    cmake {
      path = file("src/androidMain/cpp/CMakeLists.txt")
      version = "3.22.1"
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}
