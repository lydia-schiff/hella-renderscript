package cat.the.lydia.coolalgebralydiathanks

/**
 * A Photo is a width x height rectangle of Colors.
 */
data class Photo(
        val colors: List<Color>,
        val width: Int,
        val height: Int
) {
    init {
        require(width > 0 && height > 0) { "oops, no empty Photos" }
        require(colors.size == width * height) { "oops, we need exact size lists" }
    }
}
