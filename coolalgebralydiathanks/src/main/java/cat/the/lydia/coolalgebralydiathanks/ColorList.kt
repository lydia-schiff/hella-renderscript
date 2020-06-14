package cat.the.lydia.coolalgebralydiathanks


/**
 * Simple ColorData implementation.
 */
data class ColorList(override val colors: List<Color>) : ColorData {
    init {
        require(colors.isNotEmpty()) { "oops no colors!" }
    }

    override fun toString(): String = "ColorList(size=${colors.size})"
}

/**
 * Get a ColorData from a List<Color>
 */
fun List<Color>.toColorData(): ColorData = ColorList(this)
