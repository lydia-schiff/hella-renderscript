package com.lydiaschiff.hella.renderer

import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.annotation.RequiresApi
import com.lydiaschiff.hella.RsRenderer

/**
 * Apply a gaussian blur to an image allocation.
 */
@RequiresApi(17)
class BlurRsRenderer : RsRenderer {
    private var blurScript: ScriptIntrinsicBlur? = null

    override fun renderFrame(rs: RenderScript, inAlloc: Allocation, outAlloc: Allocation) {
        val script = blurScript ?: ScriptIntrinsicBlur.create(rs, inAlloc.element).also {
            it.setRadius(BLUR_RADIUS)
            blurScript = it
        }
        script.setInput(inAlloc)
        script.forEach(outAlloc)
    }

    override val name: String = "Gaussian blur (radius $BLUR_RADIUS) with ScriptIntrinsicBlur"

    override val canRenderInPlace = false

    companion object {
        private const val BLUR_RADIUS = 10f // in range [0, 25]
    }
}
