package fr.dcs.mdk.ui

import android.content.*
import android.opengl.*
import android.view.*

actual sealed interface RenderTarget {
  class Vulkan(context: Context) : SurfaceView(context), RenderTarget {
    internal var globalRef: Long = 0
  }
  class Gl(context: Context) : GLSurfaceView(context), RenderTarget
  class AndroidSurfaceView(context: Context) : SurfaceView(context), RenderTarget
}

