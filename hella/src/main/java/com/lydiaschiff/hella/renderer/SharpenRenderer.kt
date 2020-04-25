package com.lydiaschiff.hella.renderer

import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicConvolve5x5
import androidx.annotation.RequiresApi
import com.lydiaschiff.hella.RsRenderer

/**
 * Apply RGB sharpen to an image allocation using a 5x5 kernel.
 */
@RequiresApi(17)
class SharpenRenderer : RsRenderer {
    private var sharpenScript: ScriptIntrinsicConvolve5x5? = null

    override fun renderFrame(rs: RenderScript, inAlloc: Allocation, outAlloc: Allocation) {
        val script = sharpenScript ?: ScriptIntrinsicConvolve5x5.create(rs, inAlloc.element)
                .also {
                    it.setCoefficients(createSharpenKernel5x5())
                    sharpenScript = it
                }
        script.setInput(inAlloc)
        script.forEach(outAlloc)
    }

    override val name = "RGB sharpen with ScriptInstrinsic5x5Convolve"

    override val canRenderInPlace = false

    companion object {
        private const val SHARPEN_INTENSITY = 1.0f

        /**
         * Create a 5x5 sharpen convolution kernel to use with [ScriptIntrinsicConvolve5x5].
         *
         * @param intensity sharpen intensity in [0,1]
         * @return new 5x5 sharpen kernel
         */
        private fun createSharpenKernel5x5(): FloatArray {
            val centralWeightValue = 180.0f - SHARPEN_INTENSITY * 130.0f
            val totalWeight = centralWeightValue - 32.0f
            val x = -1.0f / totalWeight
            val y = -2.0f / totalWeight
            val z = centralWeightValue / totalWeight
            return floatArrayOf(
                    x, x, x, x, x,
                    x, y, y, y, x,
                    x, y, z, y, x,
                    x, y, y, y, x,
                    x, x, x, x, x
            )
        }
    }
}