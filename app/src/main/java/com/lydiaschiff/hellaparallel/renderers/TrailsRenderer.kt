package com.lydiaschiff.hellaparallel.renderers

import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlend
import androidx.annotation.RequiresApi
import com.lydiaschiff.hella.RsRenderer
import com.lydiaschiff.hella.RsUtil.createMatchingAlloc
import com.lydiaschiff.hella.renderer.ScriptC_set_alpha

/**
 * Created by lydia on 10/17/17.
 */
@RequiresApi(18)
class TrailsRenderer : RsRenderer {
    private var blendScript: ScriptIntrinsicBlend? = null
    private var setAlphaScript: ScriptC_set_alpha? = null
    private var last: Allocation? = null

    override fun renderFrame(rs: RenderScript, inAlloc: Allocation, outAlloc: Allocation) {
        if (blendScript == null) {
            blendScript = ScriptIntrinsicBlend.create(rs, inAlloc.element)
            setAlphaScript = ScriptC_set_alpha(rs)
            last = createMatchingAlloc(rs, inAlloc).apply { copyFrom(inAlloc) }
        }
        outAlloc.copyFrom(last)

        // setting the alpha here is just to trick ScriptIntrinsicBlend to do linear
        // interpolation for us
        // update: that is tricky past lydia! I dig it.
        setAlphaScript!!._alpha_value = 200.toShort()
        setAlphaScript!!.forEach_filter(outAlloc, outAlloc)
        setAlphaScript!!._alpha_value = 55.toShort()
        setAlphaScript!!.forEach_filter(inAlloc, inAlloc)
        blendScript!!.forEachSrcAtop(inAlloc, outAlloc)
        last!!.copyFrom(outAlloc)
    }

    override val name: String = "ScriptIntrinsicBlend (trails)"

    override val canRenderInPlace = false
}