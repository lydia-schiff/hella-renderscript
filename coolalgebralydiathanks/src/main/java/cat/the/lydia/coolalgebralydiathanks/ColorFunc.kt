package cat.the.lydia.coolalgebralydiathanks

/**
 * Something that maps Colors to Colors. There's a version for 1 color and for List<Color> and they
 * need to do the same thing. Sometimes it's easier to implement one than the other.
 */
interface ColorFunc {
    fun mapColor(color: Color): Color
    fun mapColors(colors: List<Color>): List<Color>

    fun toColorCube(): ColorCube = this * ColorCube.identity

    infix operator fun times(colors: List<Color>): List<Color> =
            CoolAlgebra.applyColorFuncToColors(this, colors)

    infix operator fun times(cube: ColorCube): ColorCube =
            CoolAlgebra.applyColorFuncToColorCube(this, cube)

    infix operator fun times(photo: Photo): Photo =
            CoolAlgebra.applyColorFuncToPhoto(this, photo)

    companion object {
        /**
         * The identity function on Color and List<Color>! Do nothing!
         */
        val identity = object : ColorFunc {
            override fun mapColor(color: Color) = color
            override fun mapColors(colors: List<Color>) = colors
        }

        /**
         * Helper to make a ColorFunc from a Color mapping.
         */
        fun createFromColorFunction(f: (Color) -> Color) = object : ColorFunc {
            override fun mapColor(color: Color) = f(color)
            override fun mapColors(colors: List<Color>) = colors.map(f)
        }

        /**
         * Helper to make a ColorFunc from a List<Color> mapping.
         */
        fun createFromColorListFunction(f: (List<Color>) -> List<Color>) = object : ColorFunc {
            override fun mapColor(color: Color) = f(listOf(color)).first()
            override fun mapColors(colors: List<Color>) = f(colors)
        }
    }
}
