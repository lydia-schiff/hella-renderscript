package cat.the.lydia.coolalgebralydiathanks.rs

import android.renderscript.Allocation
import android.renderscript.RenderScript
import cat.the.lydia.coolalgebralydiathanks.ColorCube
import cat.the.lydia.coolalgebralydiathanks.rs.RsFriend.F32_3
import cat.the.lydia.coolalgebralydiathanks.rs.RsFriend.RGBA_8888
import java.lang.AssertionError

/**
 * A Script that applies float-precision 3D LUT data to Colors.
 */
class FloatLut3dScript(rs: RenderScript) : RsResource {

    private val lut3dAlloc = FloatLut3dAlloc(rs)
    private val script = ScriptC_lut3d(rs)
    private var destroyed = false

    fun setColorCube(cube: ColorCube) {
        check(!destroyed)
        lut3dAlloc.setColorCube(cube)
    }

    fun forEach(colorAlloc: Allocation) {
        check(!destroyed)
        script._g_lut_alloc = lut3dAlloc.alloc
        when (colorAlloc.element) {
            F32_3 -> script.forEach_apply_float3(colorAlloc, colorAlloc)
            RGBA_8888 -> script.forEach_apply_rgba_8888(colorAlloc, colorAlloc)
            else -> throw AssertionError()
        }
    }

    fun forEach(colorAlloc: F32ColorDataAllocation) = forEach(colorAlloc.alloc)

    override fun reset() {
        if (!destroyed) {
            lut3dAlloc.reset()
        }
    }

    override fun destroy() {
        if (!destroyed) {
            lut3dAlloc.destroy()
            script.destroy()
            destroyed = true
        }
    }
}
