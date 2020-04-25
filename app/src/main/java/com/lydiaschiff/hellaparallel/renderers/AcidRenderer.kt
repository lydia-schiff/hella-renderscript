package com.lydiaschiff.hellaparallel.renderers

import android.renderscript.Allocation
import android.renderscript.RenderScript
import androidx.annotation.RequiresApi
import com.lydiaschiff.hella.RsRenderer
import com.lydiaschiff.hella.RsUtil.createMatchingAlloc

@RequiresApi(18)
class AcidRenderer : RsRenderer {
    private val trailsRenderer = TrailsRenderer()
    private val hueRotationRenderer = HueRotationRenderer()
    private var tempRgb: Allocation? = null
    override fun renderFrame(rs: RenderScript, `in`: Allocation, out: Allocation) {
        if (tempRgb == null) {
            tempRgb = createMatchingAlloc(rs, `in`)
        }
        hueRotationRenderer.renderFrame(rs, `in`, tempRgb!!)
        trailsRenderer.renderFrame(rs, tempRgb!!, out)
    }

    override val name: String = "${trailsRenderer.name} + ${hueRotationRenderer.name} (acid)"
    override val canRenderInPlace = false
}