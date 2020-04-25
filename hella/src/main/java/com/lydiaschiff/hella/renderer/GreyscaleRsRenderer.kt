package com.lydiaschiff.hella.renderer

import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicColorMatrix
import androidx.annotation.RequiresApi
import com.lydiaschiff.hella.RsRenderer

/**
 * Render an image allocation in greyscale.
 */
@RequiresApi(17)
class GreyscaleRsRenderer : RsRenderer {

    private var greyscaleScript: ScriptIntrinsicColorMatrix? = null

    override fun renderFrame(rs: RenderScript, inAlloc: Allocation, outAlloc: Allocation) {
        val script = greyscaleScript ?: createScript(rs, inAlloc.element).also {
            it.setGreyscale()
            greyscaleScript = it
        }
        script.forEach(inAlloc, outAlloc)
    }

    override val name = "Greyscale with ScriptIntrinsicColorMatrix"

    override val canRenderInPlace = true

    companion object {
        private fun createScript(rs: RenderScript, e: Element): ScriptIntrinsicColorMatrix {
            return if (Build.VERSION.SDK_INT >= 19) {
                ScriptIntrinsicColorMatrix.create(rs)
            } else {
                ScriptIntrinsicColorMatrix.create(rs, e)
            }
        }
    }
}