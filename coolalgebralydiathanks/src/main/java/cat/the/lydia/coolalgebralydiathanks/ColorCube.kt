package cat.the.lydia.coolalgebralydiathanks

import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.RenderScript
import androidx.annotation.ColorInt
import cat.the.lydia.coolalgebralydiathanks.utils.BitmapFriend
import cat.the.lydia.coolalgebralydiathanks.utils.RsFriend

data class ColorCube(val colors: List<Color>) {

    init {
        require(colors.size == N_COLORS) {
            "Oops, colors.size=${colors.size}! Since CoolAlgebra.N=${CoolAlgebra.N}, we need " +
                    "exactly N^3=$N_COLORS colors. Thanks!"
        }
    }

    constructor(f: ColorFunc) : this(f * identity.colors)

    constructor(rgbFloats: FloatArray) : this(rgbFloats.let {
        ArrayList<Color>(N_COLORS).apply {
            require(it.size == N_VALUES_RGB)
            (0 until N_COLORS).forEach { add(Color(rgbFloats, it)) }
        }
    })

    private val function: ColorFunc by lazy { CoolAlgebra.colorCubeToColorFunc(this) }

    /**
     * Our Cool Algebra Knows how to apply this ColorCube as a Function. The ColorFunc is uniquely
     * defined by our Colors.
     */
    fun toColorFunc(): ColorFunc = function

    fun toPhoto(landscape: Boolean = true): Photo =
            if (landscape) Photo(colors, CoolAlgebra.N2, CoolAlgebra.N)
            else Photo(colors, CoolAlgebra.N2, CoolAlgebra.N)

    fun toBitmap(landscape: Boolean = true): Bitmap = toPhoto(landscape).toBitmap()

    fun toLut3dAllocation(rs: RenderScript): Allocation =
            RsFriend.lut3dAlloc(rs).also { a -> RsFriend.copyColorCubeToLutAlloc(this, a) }

    @ColorInt
    fun toColorInts() = IntArray(N_COLORS) { n -> BitmapFriend.colorToColorInt(colors[n]) }

    fun toRgbFloats() = FloatArray(N_COLORS * 3).also {
        var n = 0
        colors.forEach { c ->
            it[n++] = c.r.value
            it[n++] = c.g.value
            it[n++] = c.b.value
        }
    }

    /**
     * Apply the cube to a list of colors.
     */
    infix operator fun times(colors: List<Color>): List<Color> =
            CoolAlgebra.applyColorFuncToColors(function, colors)

    /**
     * Apply the cube to a photo.
     */
    infix operator fun times(photo: Photo): Photo =
            CoolAlgebra.applyColorFuncToPhoto(function, photo)

    /**
     * Apply the cube to another cube. This is our binary operation! This cube is the left argument
     * which means that it is applied as a ColorFunc to the right argument cube's colors.
     *
     * The result is a ColorCube that acts as a function that first applies the right cube then
     * applies this cube.
     */
    infix operator fun times(rhs: ColorCube): ColorCube =
            CoolAlgebra.applyColorCubeToColorCube(this, rhs)

    companion object {

        const val N_COLORS = CoolAlgebra.N * CoolAlgebra.N * CoolAlgebra.N
        const val N_VALUES_RGB = N_COLORS * 3

        /**
         * The Bounded values at each lattice point a ColorCube dimension.
         */
        val cubeLatticeSteps: List<Bounded> =
                (0 until CoolAlgebra.N).map { Bounded(it / (CoolAlgebra.N - 1f)) }

        val cubeLatticePoints: List<Color> = ArrayList<Color>(N_COLORS).apply {
            for (b in cubeLatticeSteps)
                for (g in cubeLatticeSteps)
                    for (r in cubeLatticeSteps)
                        add(Color(r, g, b))
        }

        /**
         * The ColorCube that maps every Color to itself! Looked at another way, this is the exact
         * sampling of RGB that defines what makes our ColorCube's Colors into a 3DLUT-based
         * ColorFunc!
         */
        val identity : ColorCube = ColorCube(identityColorLattice())

        /**
         * The Colors corresponding to each point in the 3D lattice of uniformly spaced samples of
         * RGB space (including the edges)!
         */
        private fun identityColorLattice(): List<Color> = ArrayList<Color>(N_COLORS).apply {
            val step = 1 / (CoolAlgebra.N - 1f)
            for (z in 0 until CoolAlgebra.N)
                for (y in 0 until CoolAlgebra.N)
                    for (x in 0 until CoolAlgebra.N)
                        add(Color(
                                r = Bounded(x * step),
                                g = Bounded(y * step),
                                b = Bounded(z * step)
                        ))
        }
    }
}