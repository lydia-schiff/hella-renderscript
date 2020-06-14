package cat.the.lydia.coolalgebralydiathanks

import androidx.annotation.ColorInt
import cat.the.lydia.coolalgebralydiathanks.utils.Util

data class Color(
        val r: Bounded,
        val g: Bounded,
        val b: Bounded
)

/**
 * Convert Color to ColorInt.
 */
@ColorInt
fun Color.toColorInt(): Int = Util.colorToColorInt(this)

/**
 * Convert ColorInt to Color.
 */
fun Int.toColor(): Color = Util.colorIntToColor(this)

/**
 * Pack Color into rs RGBA_8888.
 */
fun Color.rsPackColor8888(): Int = Util.packColor8888(this)

/**
 * Unpack Color from rs RGBA_8888.
 */
fun Int.rsUnpackColor8888(): Color = Util.unpackColor8888(this)

/**
 * Check if color values correspond exactly to 8-bit values.
 */
fun Color.isU8Color() = Util.colorIsU8(this)

/**
 * Get a Color with values rounded to nearest 8-bit values.
 */
fun Color.clampToU8Color() = Util.clampColorToU8(this)

/**
 * Get a Color with random Bounded values.
 */
fun randomColor() : Color = Util.randomColor()

/**
 * Get a Color with random Bounded values clamped to U8.
 */
fun randomU8Color() : Color = Util.randomU8Color()
