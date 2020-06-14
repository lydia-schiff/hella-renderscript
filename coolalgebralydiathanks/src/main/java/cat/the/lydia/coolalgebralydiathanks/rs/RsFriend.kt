package cat.the.lydia.coolalgebralydiathanks.rs

import android.content.Context
import android.renderscript.*
import cat.the.lydia.coolalgebralydiathanks.Bounded
import cat.the.lydia.coolalgebralydiathanks.Color
import cat.the.lydia.coolalgebralydiathanks.ColorCube
import cat.the.lydia.coolalgebralydiathanks.ColorData
import cat.the.lydia.coolalgebralydiathanks.utils.Util

/**
 * Thanks RsFriend!
 */
object RsFriend {
    lateinit var rs: RenderScript

    private lateinit var EMPTY_ALLOC: Allocation

    val emptyAlloc: Allocation get() = EMPTY_ALLOC

    val F32_3 : Element
            get() = Element.F32_3(rs)

    val RGBA_8888 : Element
            get() = Element.RGBA_8888(rs)

    lateinit var element: Element
    private lateinit var lut3dScript: ScriptIntrinsic3DLUT
    private lateinit var lut3dAlloc: Allocation
    private val lutIntArrayBuffer = IntArray(ColorCube.N_COLORS)
    lateinit var cachedColorAlloc: Allocation
    lateinit var cachedColorsIntArrayBuffer: IntArray
    lateinit var cachedColorsFloatArrayBuffer: FloatArray
    var initialized = false

    private lateinit var custom_lut_script: ScriptC_lut3d
    private val lutFloatArrayBuffer = FloatArray(ColorCube.N_COLORS * 3)

    var use_custom_script = true

    fun init(c: Context) {
        rs = RenderScript.create(c)
        EMPTY_ALLOC = Allocation.createSized(rs, Element.F32_3(rs), 1)
        element = if (use_custom_script) Element.F32_3(
            rs
        ) else Element.RGBA_8888(rs)
        val lut3dType = Type.createXYZ(
            rs,
            element,
            ColorCube.N,
            ColorCube.N,
            ColorCube.N
        )
        lut3dAlloc = Allocation.createTyped(rs, lut3dType)

        cachedColorAlloc = Allocation.createSized(rs, element, 1)
        cachedColorsIntArrayBuffer = IntArray(1)
        cachedColorsFloatArrayBuffer = FloatArray(1)

        if (use_custom_script) {
            custom_lut_script = ScriptC_lut3d(rs)//.also { it._N = ColorCube.N }
            lut3dAlloc.setAutoPadding(true)
        } else {
            lut3dScript = ScriptIntrinsic3DLUT.create(
                rs,
                element
            )
        }

        initialized = true
    }

    private fun setLutData(cube: ColorCube) {
        if (use_custom_script) {
            var n = 0
            cube.colors.forEach { color ->
                lutFloatArrayBuffer[n++] = color.r.value
                lutFloatArrayBuffer[n++] = color.g.value
                lutFloatArrayBuffer[n++] = color.b.value
            }
            lut3dAlloc.copyFrom(lutFloatArrayBuffer)
            custom_lut_script._g_lut_alloc = lut3dAlloc
        } else {
            cube.colors.forEachIndexed { index, color ->
                lutIntArrayBuffer[index] =
                    Util.packColor8888(color)
            }
            lut3dAlloc.copyFromUnchecked(
                lutIntArrayBuffer
            )
            lut3dScript.setLUT(
                lut3dAlloc
            )
        }
    }

    private fun colorsToAllocation(colors: List<Color>): Allocation {
        if (cachedColorAlloc.type.count != colors.size) {
            cachedColorAlloc.destroy()
            cachedColorAlloc = Allocation.createSized(
                rs,
                element, colors.size
            )
            if (use_custom_script) {
                cachedColorAlloc.setAutoPadding(true)
                cachedColorsFloatArrayBuffer = FloatArray(colors.size * 3)

            } else {
                cachedColorsIntArrayBuffer = IntArray(colors.size)
            }
        }
        if (use_custom_script) {
            var n = 0
            colors.forEach { color ->
                cachedColorsFloatArrayBuffer[n++] = color.r.value
                cachedColorsFloatArrayBuffer[n++] = color.g.value
                cachedColorsFloatArrayBuffer[n++] = color.b.value
            }
            cachedColorAlloc.copyFrom(
                cachedColorsFloatArrayBuffer
            )
        } else {
            colors.forEachIndexed { index, color ->
                cachedColorsIntArrayBuffer[index] =
                    Util.packColor8888(color)
                cachedColorAlloc.copyFromUnchecked(
                    cachedColorsIntArrayBuffer
                )
            }
        }
        return cachedColorAlloc
    }

    private fun allocToColors(colorsAlloc: Allocation): List<Color> {
        require(colorsAlloc === cachedColorAlloc) { "hack!" }
        if (!use_custom_script) {
            cachedColorAlloc.copy1DRangeToUnchecked(
                0,
                cachedColorsIntArrayBuffer.size,
                cachedColorsIntArrayBuffer
            )
            return cachedColorsIntArrayBuffer.map(
                Util::unpackColor8888
            )
        } else {
            cachedColorAlloc.copyTo(
                cachedColorsFloatArrayBuffer
            )
            val nColors = cachedColorsFloatArrayBuffer.size / 3
            val result = ArrayList<Color>(nColors)
            var n = 0
            repeat(nColors) {
                result.add(
                    Color(
                        Bounded(cachedColorsFloatArrayBuffer[n++]),
                        Bounded(cachedColorsFloatArrayBuffer[n++]),
                        Bounded(cachedColorsFloatArrayBuffer[n++])
                    )
                );
            }
            return result
        }
    }

    fun applyColorCubeToColors(cube: ColorCube, colorData: ColorData): List<Color> =
            applyColorCubeToColors(cube, colorData.colors)

    fun applyColorCubeToColors(cube: ColorCube, colors: List<Color>): List<Color> {
        val colorsAlloc =
            colorsToAllocation(colors)
        setLutData(cube)
        if (use_custom_script) {
            custom_lut_script.forEach_apply_float3(colorsAlloc, colorsAlloc)
        } else {
            lut3dScript.forEach(colorsAlloc, colorsAlloc)
        }
        return allocToColors(colorsAlloc)
    }



    fun copyColorCubeToLutAlloc(cube: ColorCube, lutAlloc: Allocation) {

    }
}
