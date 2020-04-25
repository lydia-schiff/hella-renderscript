package com.lydiaschiff.hella.renderer

import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicResize
import androidx.annotation.RequiresApi
import com.lydiaschiff.hella.RsRenderer

/**
 * Resize an allocation to the size of the output allocation. Uses bicubic interpolation.
 */
@RequiresApi(20)
class ResizeRsRenderer : RsRenderer {
    private var resizeScript: ScriptIntrinsicResize? = null
    override fun renderFrame(rs: RenderScript, inAlloc : Allocation, outAlloc: Allocation) {
        val script = resizeScript ?: ScriptIntrinsicResize.create(rs).also { resizeScript = it }
        script.setInput(inAlloc)
        script.forEach_bicubic(outAlloc)
    }

    override val name = "bicubic resize with ScriptInstrinsicResize"


    // input and output allocations are not the same size todo: this is confusing
    override val canRenderInPlace = true
}