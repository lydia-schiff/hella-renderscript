package cat.the.lydia.coolalgebralydiathanks

import android.graphics.Bitmap
import cat.the.lydia.coolalgebralydiathanks.utils.BitmapFriend
import kotlin.random.Random

/**
 * A Photo is a width x height rectangle of Colors.
 */
data class Photo(
        val colors: List<Color>,
        val width: Int,
        val height: Int
) {
    init {
        require(width > 0 && height > 0) { "oops!" }
        require(colors.size == width * height) { "oops, exact size lists please!" }
    }

    constructor(b: Bitmap) : this(BitmapFriend.bitmapToColors(b), b.width, b.height)

    constructor(cube: ColorCube) : this(cube.colors, CoolAlgebra.N2, CoolAlgebra.N)

    fun toBitmap(): Bitmap = BitmapFriend.photoToBitmap(this)


    companion object {

        fun random(w: Int = randomDim(), h: Int = randomDim()): Photo =
                Photo(Color.random(w * h), w, h)

        private val DEFAULT_MAX_RANDOM_DIM = 1000
        private fun randomDim(): Int = Random.nextInt(1, DEFAULT_MAX_RANDOM_DIM)
    }
}
