package cat.the.lydia.coolalgebralydiathanks.utils

import android.graphics.Bitmap
import androidx.annotation.ColorInt
import androidx.core.graphics.get
import cat.the.lydia.coolalgebralydiathanks.Bounded
import cat.the.lydia.coolalgebralydiathanks.Color
import cat.the.lydia.coolalgebralydiathanks.Photo
import kotlin.math.roundToInt

object BitmapFriend {

    fun colorIntToColor(@ColorInt c: Int): Color =
            Color(
                    r = Bounded(android.graphics.Color.red(c) / 255f),
                    g = Bounded(android.graphics.Color.green(c) / 255f),
                    b = Bounded(android.graphics.Color.blue(c) / 255f)
            )

    @ColorInt
    fun colorToColorInt(c: Color): Int {
        val r = (c.r.value * 255).roundToInt()
        val g = (c.g.value * 255).roundToInt()
        val b = (c.b.value * 255).roundToInt()
        return android.graphics.Color.rgb(r, g, b)
    }

    fun bitmapToColors(b: Bitmap): List<Color> =
            IntArray(b.width * b.height)
                    .also { b.getPixels(it, 0, 0, 0, 0, b.width, b.height) }
                    .map(::colorIntToColor)

    fun bitmapToPhoto(b: Bitmap): Photo =
            Photo(bitmapToColors(b), b.width, b.height)

    fun colorsToBitmap(cs: List<Color>, w: Int, h: Int): Bitmap =
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    .apply {
                        cs.map(::colorToColorInt)
                                .toIntArray()
                                .also { setPixels(it, 0, 0, 0, 0, w, h) }
                    }

    fun photoToBitmap(p: Photo): Bitmap =
            colorsToBitmap(p.colors, p.width, p.height)
}
