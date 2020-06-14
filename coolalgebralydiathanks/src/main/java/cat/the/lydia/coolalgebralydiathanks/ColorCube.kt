package cat.the.lydia.coolalgebralydiathanks

import cat.the.lydia.coolalgebralydiathanks.implementation.KotlinCpuTrilinear
import cat.the.lydia.coolalgebralydiathanks.rs.RsFriend
import cat.the.lydia.coolalgebralydiathanks.utils.N_CHANNELS_RGB
import cat.the.lydia.coolalgebralydiathanks.utils.Util

internal const val DO_RENDERSCRIPT_3DLUT = true
internal const val DO_PARALLEL_3DLUT = true

internal fun useRenderScript3dLut() = DO_RENDERSCRIPT_3DLUT && RsFriend.initialized
internal fun useParallelCpu3dLut() = DO_PARALLEL_3DLUT

open class ColorCube(
        override val colors: List<Color>
) : ColorFunc, ColorData {

    /**
     * For a single color we always use CPU.
     */
    override fun apply(color: Color): Color = KotlinCpuTrilinear.applyCubeToColor(this, color)

    /**
     * For lists of colors we choose based on the settings at the top of this file, and whether the
     * RS context has been initialized.
     *
     * For instance, RS 3DLut is not available in junit tests, but may be used in Instrument tests.
     */
    override fun apply(colors: List<Color>): List<Color> = when {
        useRenderScript3dLut() -> RsFriend.applyColorCubeToColors(this, colors)
        useParallelCpu3dLut() -> KotlinCpuTrilinear.applyCubeToColorsInParallel(
                this, colors, Util.executor)
        else -> colors.map(::apply)
    }

    final override fun toColorCube(): ColorCube = this

    final override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is ColorCube -> false
        else -> colors == other.colors
    }

    final override fun hashCode(): Int = colors.hashCode()

    companion object {
        const val N = 17
        const val N2 = N * N
        const val N_COLORS = N * N * N
        const val N_VALUES_RGB = N_COLORS * N_CHANNELS_RGB

        val identity: ColorCube = IdentityCube
    }
}
