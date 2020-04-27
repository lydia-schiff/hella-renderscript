package cat.the.lydia.coolalgebralydiathanks.utils

import android.renderscript.*
import cat.the.lydia.coolalgebralydiathanks.Color
import cat.the.lydia.coolalgebralydiathanks.ColorCube
import cat.the.lydia.coolalgebralydiathanks.CoolAlgebra

object RsFriend {

    // When we copy Int unchecked into renderscript U8_4 we have to swap red and blue in the normal
    // ColorInt. much endian. very byte-order.
    fun swapRb(c: Int): Int = android.graphics.Color.rgb(
            android.graphics.Color.blue(c),
            android.graphics.Color.green(c),
            android.graphics.Color.red(c)
    )

    fun colorToRsPackedColor8888(c: Color): Int = swapRb(BitmapFriend.colorToColorInt(c))

    fun rsPackedColor8888ToColor(c: Int): Color = BitmapFriend.colorIntToColor(swapRb(c))

    fun lut3dType(rs: RenderScript) =
            Type.createXYZ(rs, Element.RGBA_8888(rs), CoolAlgebra.N, CoolAlgebra.N, CoolAlgebra.N)

    fun checkLut3dType(type: Type) =
            require(type.x == CoolAlgebra.N &&
                    type.x == CoolAlgebra.N &&
                    type.x == CoolAlgebra.N)

    fun lut3dAlloc(rs: RenderScript): Allocation = Allocation.createTyped(rs, lut3dType(rs))

    fun lut3dAlloc(rs: RenderScript, cube: ColorCube): Allocation =
            lut3dAlloc(rs).apply { copyColorCubeToLutAlloc(cube, this) }

    fun copyColorCubeToLutAlloc(cube: ColorCube, lutAlloc: Allocation) {
        checkLut3dType(lutAlloc.type)
        copyColorsToAlloc(cube.colors, lutAlloc)
    }

    fun copyColorsToAlloc(colors: List<Color>, alloc: Allocation) {
        require(alloc.type.count >= colors.size)
        colors.map(::colorToRsPackedColor8888)
                .toIntArray()
                .also { alloc.copyFromUnchecked(it) }
    }

    fun allocToColors(alloc: Allocation, size: Int): List<Color> =
            IntArray(size)
                    .also { alloc.copy1DRangeToUnchecked(0, size, it) }
                    .map(::rsPackedColor8888ToColor)

    fun lut3dScript(rs: RenderScript) = ScriptIntrinsic3DLUT.create(rs, Element.RGBA_8888(rs))
}