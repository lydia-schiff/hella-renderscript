package cat.the.lydia.coolalgebralydiathanks.utils

import cat.the.lydia.coolalgebralydiathanks.Color

object CubeFriend {
    fun rgbFloatsToColors(rgbFloats: FloatArray): List<Color> =
            (rgbFloats.size / 3).let { nColors ->
                ArrayList<Color>(nColors).apply {
                    repeat(nColors) { n -> add(Color(rgbFloats, n)) }
                }
            }

    fun copyColorsToRgbFloats(colors: List<Color>, rgbFloats: FloatArray) {
        require(colors.size == rgbFloats.size / 3)
        var i = 0
        colors.forEachIndexed { n, c ->
            rgbFloats[i++] = c.r.value
            rgbFloats[i++] = c.g.value
            rgbFloats[i++] = c.b.value
        }
    }

}