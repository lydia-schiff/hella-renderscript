package com.lydiaschiff.hellaparallel.renderers

import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.Short4
import androidx.annotation.RequiresApi
import com.lydiaschiff.hella.RsRenderer
import com.lydiaschiff.hella.RsUtil.createRgbAlloc
import com.lydiaschiff.hella.RsUtil.height
import com.lydiaschiff.hella.RsUtil.width
import com.lydiaschiff.hella.renderer.ResizeRsRenderer
import com.lydiaschiff.hella.renderer.ScriptC_color_frame
import kotlin.random.Random

/**
 * Created by lydia on 10/17/17.
 */
@RequiresApi(20)
class ColorFrameRenderer : RsRenderer {
    private val rgbaColor = Short4() // same as uchar4 in rs script
    private var resizeRsRenderer: ResizeRsRenderer? = null
    private var colorFrameScript: ScriptC_color_frame? = null
    private var scaledAlloc: Allocation? = null
    private var count = 0

    override fun renderFrame(rs: RenderScript, inAlloc: Allocation, outAlloc: Allocation) {
        if (resizeRsRenderer == null) {
            resizeRsRenderer = ResizeRsRenderer()
            colorFrameScript = ScriptC_color_frame(rs)
            scaledAlloc = createRgbAlloc(rs, width(inAlloc) / 2, height(outAlloc) / 2)
            colorFrameScript!!.invoke_prepare(scaledAlloc)
        }
        if (count++ % FRAMES_BEFORE_COLOR_REFRESH == 0) {
            loadRandomRGBAColor()
            colorFrameScript!!._color = rgbaColor
        }

        // scale image
        resizeRsRenderer!!.renderFrame(rs, inAlloc, scaledAlloc!!)

        // draw scaled image and color frame. we use out as input and output because we don't
        // actually use the input value in the calculation, just the rectangle bounds
        colorFrameScript!!.forEach_frame_image(outAlloc, outAlloc)
    }

    override val name: String = "Color frame with ScriptIntrinsicResize"

    override val canRenderInPlace = true

    /**
     * Load a random RGBA color corresponding to a uchar4 in rs.
     */
    private fun loadRandomRGBAColor() {
        rgbaColor.x = Random.nextInt(0xff).toShort()
        rgbaColor.y = Random.nextInt(0xff).toShort()
        rgbaColor.z = Random.nextInt(0xff).toShort()
        rgbaColor.w = 0xff.toShort()
    }

    companion object {
        private const val FRAMES_BEFORE_COLOR_REFRESH = 10
    }
}