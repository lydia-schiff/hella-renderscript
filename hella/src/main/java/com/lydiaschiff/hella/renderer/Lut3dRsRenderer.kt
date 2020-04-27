package com.lydiaschiff.hella.renderer

import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsic3DLUT
import com.lydiaschiff.hella.RsRenderer
import com.lydiaschiff.hella.RsUtil

open class Lut3dRsRenderer(val cubeDim: Int) : RsRenderer {

    // explicit zero-arg constructor
    constructor() : this(CUBE_DIM)

    // all guarded by "this"
    private lateinit var lut3dScript: ScriptIntrinsic3DLUT
    protected lateinit var lutAlloc: Allocation
    protected var firstDraw = true
        private set

    protected var hasUpdate = false
    protected val rgbFloatLut: FloatArray = FloatArray(cubeDim * cubeDim * cubeDim * 3)

    @Synchronized
    override fun renderFrame(rs: RenderScript, inAlloc: Allocation, outAlloc: Allocation) {
        if (firstDraw) {
            lut3dScript = ScriptIntrinsic3DLUT.create(rs, Element.RGBA_8888(rs))
            lutAlloc = RsUtil.lut3dAlloc(rs, SET_IDENTITY_BY_DEFAULT, cubeDim)
            lut3dScript.setLUT(lutAlloc)
            onFirstDraw(rs)
            firstDraw = false
        }

        if (hasUpdate) {
            onUpdate()
        }

        lut3dScript.forEach(inAlloc, outAlloc)
    }

    protected open fun onFirstDraw(rs : RenderScript) = Unit

    protected open fun onUpdate() {
        RsUtil.copyRgbFloatsToAlloc(rgbFloatLut, lutAlloc)
        lut3dScript.setLUT(lutAlloc)
        hasUpdate = false
    }

    @Synchronized
    fun setLutData(rgbFloatLut: FloatArray) {
        require(rgbFloatLut.size == N_VALUES_RGB)

        if (!rgbFloatLut.contentEquals(this.rgbFloatLut)) {
            rgbFloatLut.copyInto(this.rgbFloatLut)
            hasUpdate = true
        }
    }

    override val name = "Lut3dRenderer Cool Algebra!"

    override val canRenderInPlace = true

    companion object {
        const val CUBE_DIM = 17
        const val N_COLORS = CUBE_DIM * CUBE_DIM * CUBE_DIM
        const val N_VALUES_RGB = N_COLORS * 3
        const val SET_IDENTITY_BY_DEFAULT = true
        fun emptyRgbFloatLut() = FloatArray(N_VALUES_RGB)
    }
}
