package cat.the.lydia.coolalgebralydiathanks

data class Photo(
        override val colors: List<Color>,
        val width: Int,
        val height: Int
) : ColorData {

    init {
        require(width > 0 && height > 0) { "width and height should be positive" }
        require(colors.size == width * height) { "exact sizes please for ColorData" }
    }

    override fun toString(): String = "Photo($width, $height)"
}
