package com.lydiaschiff.hella.renderer

import android.renderscript.Allocation
import android.renderscript.RenderScript
import androidx.annotation.RequiresApi
import com.lydiaschiff.hella.RsRenderer

/**
 * Render an image allocation without modification.
 */
@RequiresApi(18)
class DefaultRsRenderer : RsRenderer {

    override fun renderFrame(rs: RenderScript, inAlloc: Allocation, outAlloc: Allocation) {
        outAlloc.copyFrom(inAlloc)
    }

    override val name = "default (no edits applied)"

    override fun canRenderInPlace() = true
}