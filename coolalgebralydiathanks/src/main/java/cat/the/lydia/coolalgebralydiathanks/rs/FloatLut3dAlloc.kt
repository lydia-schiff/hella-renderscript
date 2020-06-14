package cat.the.lydia.coolalgebralydiathanks.rs

import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.Type
import cat.the.lydia.coolalgebralydiathanks.ColorCube
import cat.the.lydia.coolalgebralydiathanks.IdentityCube
import cat.the.lydia.coolalgebralydiathanks.utils.Util
import cat.the.lydia.coolalgebralydiathanks.utils.emptyFloatArray

/**
 * A ColorCube-sized 3D allocation that holds ColorData as RGB F32_3 elements.
 */
class FloatLut3dAlloc(rs: RenderScript) : RsResource {

    val alloc: Allocation =
            Allocation.createTyped(rs, lut3dType(rs)).apply { setAutoPadding(true) }
        get() {
            check(!destroyed)
            if (currentCube == null) {
                setColorCube(IdentityCube)
            }
            return field
        }

    private var buffer = FloatArray(ColorCube.N_VALUES_RGB)
    private var currentCube: ColorCube? = null
    private var destroyed = false

    fun setColorCube(cube: ColorCube) {
        check(!destroyed)
        if (currentCube != cube) {
            Util.copyColorCubeIntoFloatArray(cube, buffer)
            currentCube = cube
        }
    }

    override fun reset() {
        currentCube = null
    }

    override fun destroy() {
        if (!destroyed) {
            reset()
            alloc.destroy()
            buffer = emptyFloatArray()
            destroyed = true
        }
    }

    companion object {
        private fun lut3dType(rs: RenderScript) =
                Type.createXYZ(rs, Element.F32_3(rs),
                        ColorCube.N,
                        ColorCube.N,
                        ColorCube.N
                )
    }
}
