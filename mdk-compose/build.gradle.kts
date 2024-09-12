import com.vanniktech.maven.publish.*

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.native.cocoapods)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.jetbrainsCompose)
  id("com.vanniktech.maven.publish") version "0.28.0"
}

val versionName = project.property("versionName") as String

group = "fr.dcs.mdk"
version = versionName

kotlin {

  androidTarget {
    publishLibraryVariants("release", "debug")
  }
  jvm("desktop")

  iosX64()
  iosArm64()
  iosSimulatorArm64()

  cocoapods {
    ios.deploymentTarget = "14.1"
    framework {
      baseName = "MdkKotlin"
    }
    pod(name = "mdk", version = "0.29.1")
    noPodspec()
  }

  applyDefaultHierarchyTemplate()

  sourceSets {
    all {
      languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
    }
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


mavenPublishing {
  coordinates(
    groupId = "io.github.sbenmeddour",
    artifactId = "mdk-compose",
    version = versionName
  )
  pom {
    name.set("MDK-Player compose wrapper")
    description.set("A compose wrapper for MDK-Player")
    inceptionYear.set("2024")
    url.set("https://github.com/sbenmeddour/mdk-compose")
    licenses {
      license {
        name.set("Apache-2.0")
        url.set("http://www.apache.org/licenses/")
      }
    }
    developers {
      developer {
        id.set("sbenmeddour")
        name.set("Samy")
        email.set("samy.benmeddour@gmail.com")
      }
    }
    scm {
      url.set("https://github.com/sbenmeddour/mdk-compose")
    }
  }

  publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
  signAllPublications()
}