package cat.the.lydia.coolalgebralydiathanks.utils

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import cat.the.lydia.coolalgebralydiathanks.*
import java.util.concurrent.*
import kotlin.math.roundToInt
import kotlin.random.Random


internal const val N_CHANNELS_RGB = 3
private val EMPTY_FLOAT_ARRAY = FloatArray(0)
private val EMPTY_INT_ARRAY = IntArray(0)

fun emptyFloatArray() : FloatArray = EMPTY_FLOAT_ARRAY
fun emptyIntArray() : IntArray = EMPTY_INT_ARRAY

object Util {

    internal val executor: ExecutorService by lazy {
        if (Build.VERSION.SDK_INT >= 24)
            Executors.newWorkStealingPool()
        else
            ThreadPoolExecutor(
                    0,
                    Runtime.getRuntime().availableProcessors(),
                    2,
                    TimeUnit.SECONDS,
                    LinkedBlockingQueue()
            )
    }

    @ColorInt
    fun rgb(
            red: Int,
            green: Int,
            blue: Int
    ): Int = -0x1000000 or (red shl 16) or (green shl 8) or blue

    fun red(@ColorInt color: Int): Int = (color shr 16) and 0xff
    fun green(@ColorInt color: Int): Int = (color shr 8) and 0xff
    fun blue(@ColorInt color: Int): Int = color and 0xff

    // rs RGBA_8888 bytes
    fun packColor8888(color: Color): Int {
        val r = boundedToU8(color.r)
        val g = boundedToU8(color.g)
        val b = boundedToU8(color.b)
        return -0x1000000 or (r shl 16) or (g shl 8) or b
    }

    fun ensureIsU8(u8: Int): Int = u8.also { require(it in 0..255) }

    fun u8ToNormal(u8: Int): Float = (u8 and 0xff) / 255f

    fun normalToU8(f: Float): Int = (f * 255).roundToInt()

    fun floatIsU8(f: Float): Boolean = f == u8ToNormal(normalToU8(f))

    fun boundedIsU8(b: Bounded) = b == u8ToBounded(normalToU8(b.value))

    fun clampBoundedToU8(b: Bounded) =
            if (boundedIsU8(b)) b
            else Bounded(u8ToNormal(normalToU8(b.value)))

    fun colorIsU8(color: Color): Boolean =
            boundedIsU8(color.r) && boundedIsU8(color.g) && boundedIsU8(color.b)

    fun clampColorToU8(color: Color): Color =
            if (colorIsU8(color)) color
            else Color(
                    color.r.clampToU8Bounded(),
                    color.g.clampToU8Bounded(),
                    color.b.clampToU8Bounded()
            )

    fun clampColorsToU8(colors: List<Color>): List<Color> = colors.map(::clampColorToU8)
    fun clampPhotoToU8(photo: Photo): Photo = SimpleColorFunc(::clampColorToU8) * photo

    fun unpackColor8888(packed: Int): Color =
            colorIntToColor(rgb(blue(packed), green(packed), red(packed)))

    fun boundedToU8(b: Bounded): Int = normalToU8(b.value)

    fun u8ToBounded(u8: Int): Bounded = Bounded(u8ToNormal(u8))

    fun colorIntToColor(@ColorInt color: Int) = Color(
            u8ToBounded(red(color)),
            u8ToBounded(green(color)),
            u8ToBounded(blue(color))
    )

    @ColorInt
    fun colorToColorInt(color: Color): Int = rgb(
            boundedToU8(color.r),
            boundedToU8(color.g),
            boundedToU8(color.b)
    )

    fun normalRgbToColor(r: Float, g: Float, b: Float): Color =
            Color(Bounded(r), Bounded(g), Bounded(b))

    @ColorInt
    fun bitmapToColorInts(b: Bitmap): IntArray = IntArray(b.width * b.height).also {
        b.getPixels(it, 0, b.width, 0, 0, b.width, b.height)
    }

    fun colorIntsToBitmap(@ColorInt colorInts: IntArray, w: Int, h: Int): Bitmap =
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
                setPixels(colorInts, 0, w, 0, 0, w, h)
            }

    fun bitmapToColors(b: Bitmap): List<Color> = bitmapToColorInts(b).map(::colorIntToColor)

    fun bitmapToPhoto(b: Bitmap) = Photo(bitmapToColors(b), b.width, b.height)

    fun photoToBitmap(p: Photo): Bitmap =
            Bitmap.createBitmap(p.width, p.height, Bitmap.Config.ARGB_8888).apply {
                setPixels(
                        p.colors.map(::colorToColorInt).toIntArray(),
                        0, width, 0, 0, width, height
                )
            }

    fun copyColorDataIntoFloatArray(data: ColorData, fs: FloatArray) {
        require(fs.size == data.size * N_CHANNELS_RGB)
        var n = 0
        data.colors.forEach { color ->
            fs[n++] = color.r.value
            fs[n++] = color.g.value
            fs[n++] = color.b.value
        }
    }

    fun copyColorCubeIntoFloatArray(cube: ColorCube, fs: FloatArray) {
        require(fs.size == ColorCube.N_COLORS)
        var n = 0
        cube.colors.forEach { color ->
            fs[n++] = color.r.value
            fs[n++] = color.g.value
            fs[n++] = color.b.value
        }
    }

    fun copyColorDataAsPackedRsColors(data: ColorData, buffer: IntArray) {
        require(data.size == buffer.size)
        data.colors.forEachIndexed { n, color -> buffer[n] = packColor8888(color) }
    }

    fun clampColorCubeToU8(c: ColorCube): ColorCube = ColorCube(c.colors.map(::clampColorToU8))

    fun sameColorData(a: ColorData, b: ColorData) = a.colors == b.colors

    internal fun linearInterpolate(a: List<Color>, b: List<Color>, scale: Bounded): List<Color> =
            ArrayList<Color>(a.size).also {
                a.forEachIndexed { n, x -> it.add(linearInterpolate(x, b[n], scale)) }
            }

    internal fun linearInterpolate(a: Color, b: Color, scale: Bounded): Color = Color(
            linearInterpolate(a.r, b.r, scale),
            linearInterpolate(a.g, b.g, scale),
            linearInterpolate(a.b, b.b, scale)
    )

    private fun linearInterpolate(a: Bounded, b: Bounded, scale: Bounded): Bounded = when {
        scale == Bounded.zero -> a
        scale == Bounded.one -> b
        a == b -> a
        else -> Bounded(linearInterpolate(a.value, b.value, scale.value))
    }

    private fun linearInterpolate(a: Float, b: Float, scale: Float): Float =
            a * (1 - scale) + b * scale

    @IntRange(from = 0, to = 255)
    fun randomU8(): Int = Random.nextInt(256)

    @ColorInt
    fun randomColorInt(): Int = rgb(randomU8(), randomU8(), randomU8())

    @ColorInt
    fun randomColorInts(n: Int): IntArray = IntArray(n) { _ -> randomColorInt() }
    fun randomBounded(): Bounded = Bounded(Random.nextFloat())
    fun randomU8Bounded(): Bounded = u8ToBounded(randomU8())
    fun randomColor(): Color = Color(randomBounded(), randomBounded(), randomBounded())
    fun randomU8Color(): Color = Color(randomU8Bounded(), randomU8Bounded(), randomU8Bounded())
    fun randomColors(n: Int): List<Color> = (0 until n).map { _ -> randomColor() }
    fun randomU8Colors(n: Int): List<Color> = randomColorInts(n).map(::colorIntToColor)
    fun randomColorData(n: Int): ColorData = ColorList(randomColors(n))
    fun randomU8Photo(w: Int, h: Int): Photo = Photo(randomU8Colors(w * h), w, h)
    fun randomPhoto(w: Int, h: Int): Photo = Photo(randomColors(w * h), w, h)
    fun randomU8ColorCube(): ColorCube = ColorCube(randomU8Colors(ColorCube.N_COLORS))
    fun randomColorCube(): ColorCube = ColorCube(randomColors(ColorCube.N_COLORS))
    fun randomColorCubes(n: Int) = Array<ColorCube>(n) { _ -> randomColorCube() }
    fun randomBitmap(w: Int, h: Int): Bitmap = colorIntsToBitmap(randomColorInts(w * h), w, h)
    fun randomBitmap(maxDim: Int): Bitmap =
            randomBitmap(Random.nextInt(maxDim + 1), Random.nextInt(maxDim + 1))
}


/**
 * We can always freely promote a ColorFunc to a ColorCube.
 */
fun linearInterpolate(a: ColorFunc, b: ColorFunc, scale: Bounded): ColorCube = when {
    scale == Bounded.zero -> a.toColorCube()
    scale == Bounded.one -> b.toColorCube()
    a == b -> a.toColorCube()
    else -> ColorCube(Util.linearInterpolate(a.toColorCube().colors, b.toColorCube().colors, scale))
}
