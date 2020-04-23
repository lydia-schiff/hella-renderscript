package cat.the.lydia.coolalgebralydiathanks

import cat.the.lydia.coolalgebralydiathanks.CoolAlgebra.N

data class ColorCube(val colors: List<Color>) {

    init {
        require(colors.size == N_COLORS) { "oops, we need exactly N^3=$N_COLORS colors" }
    }

    /**
     * Our Cool Algebra Knows how to apply this ColorCube as a Function. The ColorFunc is uniquely
     * defined by our Colors.
     */
    private val function: ColorFunc by lazy { CoolAlgebra.colorCubeToColorFunc(this) }

    fun toColorFunc() : ColorFunc = function

    infix operator fun times(colors : List<Color>) =
            CoolAlgebra.applyColorFuncToColors(function, colors)

    infix operator fun times(photo: Photo) =
            CoolAlgebra.applyColorFuncToPhoto(function, photo)

    infix operator fun times(rhs : ColorCube) =
            CoolAlgebra.applyColorCubeToColorCube(this, rhs)

    companion object {
        const val N_COLORS = N * N * N

        /**
         * The ColorCube that maps every Color to itself! Looked at another way, this is the exact
         * sampling of RGB that defines what makes our ColorCube's Colors into a 3DLUT-based
         * ColorFunc!
         */
        val identity = ColorCube(identityColorLattice())

        /**
         * The Colors corresponding to each point in the 3D lattice of uniformly spaced samples of
         * RGB space (including the edges)!
         */
        private fun identityColorLattice(): List<Color> = ArrayList<Color>(N_COLORS).apply {
            val step = 1 / (N - 1f)
            for (z in 0 until N)
                for (y in 0 until N)
                    for (x in 0 until N)
                        add(Color(
                                r = Bounded(x * step),
                                g = Bounded(y * step),
                                b = Bounded(z * step)
                        ))
        }
    }
}