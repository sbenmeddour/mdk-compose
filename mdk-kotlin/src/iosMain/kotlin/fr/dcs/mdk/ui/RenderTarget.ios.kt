package fr.dcs.mdk.ui

import kotlinx.cinterop.*
import platform.CoreGraphics.*
import platform.MetalKit.*
import platform.UIKit.*

/**
 * This class is more complicated than the android one because Objective-C and Kotlin types cannot
 * be mixed for inheritance
 */
actual sealed interface RenderTarget {


  fun view(): UIView = when (this) {
    is Metal -> view
    is View -> view
  }

   class Metal(val view: MTKView = MTKView(frame)) : RenderTarget {
     override fun equals(other: Any?): Boolean = other is Metal && other.view === this.view
     override fun hashCode(): Int = view.hashCode()
  }

  class View(val view: UIView = UIView(frame)) : RenderTarget {
    override fun equals(other: Any?): Boolean = other is View && other.view === this.view
    override fun hashCode(): Int = view.hashCode()
  }

  companion object {

    private val frame: CValue<CGRect>
      get() = CGRectMake(x = 0.0, y = 0.0, width = 0.0, height = 0.0)

    fun fromView(view: UIView): RenderTarget = when (view) {
      is MTKView -> Metal(view)
      else -> View(view)
    }
  }

}