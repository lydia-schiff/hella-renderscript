package com.lydiaschiff.hellaparallel.renderers

import android.renderscript.Allocation
import android.renderscript.Matrix3f
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicColorMatrix
import androidx.annotation.RequiresApi
import com.lydiaschiff.hella.RsRenderer

/**
 * Created by lydia on 10/17/17.
 */
@RequiresApi(17)
class HueRotationRenderer : RsRenderer {
    private val colorMatrix = Matrix3f()
    private var colorMatrixScript: ScriptIntrinsicColorMatrix? = null
    private var hueOffset = 0f

    override fun renderFrame(rs: RenderScript, inAlloc: Allocation, outAlloc: Allocation) {
        if (colorMatrixScript == null) {
            colorMatrixScript = ScriptIntrinsicColorMatrix.create(rs)
        }

        // change the hue a bit each frame
        hueOffset += INCREMENT
        setColorMatrix3f(colorMatrix, hueOffset)
        colorMatrixScript!!.setColorMatrix(colorMatrix)
        colorMatrixScript!!.forEach(inAlloc, outAlloc)
    }

    override val name: String = "hue rotation with ScriptIntrinsicColorMatrix"

    override val canRenderInPlace = true

    companion object {
        // determines speed of rotation
        private const val INCREMENT = 0.15f

        /**
         * Load a color matrix with a hue offset. Can be used with [ScriptIntrinsicColorMatrix].
         * from https://github.com/googlesamples/android-RenderScriptIntrinsic
         *
         * @param mat       3x3 color matrix
         * @param hueOffset offset for hue, any value
         */
        private fun setColorMatrix3f(mat: Matrix3f, hueOffset: Float) {
            val cos = Math.cos(hueOffset.toDouble()).toFloat()
            val sin = Math.sin(hueOffset.toDouble()).toFloat()
            mat[0, 0] = .299f + .701f * cos + .168f * sin
            mat[1, 0] = .587f - .587f * cos + .330f * sin
            mat[2, 0] = .114f - .114f * cos - .497f * sin
            mat[0, 1] = .299f - .299f * cos - .328f * sin
            mat[1, 1] = .587f + .413f * cos + .035f * sin
            mat[2, 1] = .114f - .114f * cos + .292f * sin
            mat[0, 2] = .299f - .300f * cos + 1.25f * sin
            mat[1, 2] = .587f - .588f * cos - 1.05f * sin
            mat[2, 2] = .114f + .886f * cos - .203f * sin
        }
    }
}