package cat.the.lydia.coolalgebralydiathanks

import androidx.annotation.ColorInt

/**
 * A Color is 3 Bounded values labeled r, g and b. This is the only Color in our universe!
 */
data class Color(
        val r: Bounded,
        val g: Bounded,
        val b: Bounded
) {
    constructor(r: Float, g: Float, b: Float) : this(Bounded(r), Bounded(g), Bounded(b))

    constructor(rgbFloats: FloatArray, colorIndex: Int) : this(
            rgbFloats[colorIndex],
            rgbFloats[colorIndex + 1],
            rgbFloats[colorIndex + 2]
    )

    constructor(@ColorInt color: Int) : this(
            Bounded(color, Channel.R),
            Bounded(color, Channel.G),
            Bounded(color, Channel.B)
    )

    companion object {
        // swaps r and b channels relative to a ColorInt
        fun fromRsPackedColor8888(color: Int): Color = Color(
                Bounded(color, Channel.B),
                Bounded(color, Channel.G),
                Bounded(color, Channel.R))

        fun random(): Color = Color(Bounded.random(), Bounded.random(), Bounded.random())

        fun random(n: Int): List<Color> =
                ArrayList<Color>(n).apply { repeat(n) { add(random()) } }

        /**
         * Color value for a random lattice point in a ColorCube.
         */
        fun randomCubeLatticePoint(): Color = ColorCube.cubeLatticePoints.random()

        fun lerp(a: Color, b: Color, scale: Bounded): Color =
                Color(
                        Bounded.lerp(a.r, b.r, scale),
                        Bounded.lerp(a.g, b.g, scale),
                        Bounded.lerp(a.b, b.b, scale)
                )
    }
}
