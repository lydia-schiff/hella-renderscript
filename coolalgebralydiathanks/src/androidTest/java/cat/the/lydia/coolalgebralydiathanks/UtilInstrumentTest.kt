package cat.the.lydia.coolalgebralydiathanks

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cat.the.lydia.coolalgebralydiathanks.implementation.KotlinCpuTrilinear
import cat.the.lydia.coolalgebralydiathanks.rs.RsFriend
import cat.the.lydia.coolalgebralydiathanks.utils.Util
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class UtilInstrumentTest {

    @Before
    fun start() {
        RsFriend.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Test
    fun bitmapToColorInts() {
        val w = Random.nextInt(MAX_DIM)
        val h = Random.nextInt(MAX_DIM)
        val orig = Util.randomBitmap(w, h)
        val pixels = Util.bitmapToColorInts(orig)
        val result = Util.colorIntsToBitmap(pixels, w, h)

        assertTrue(orig.sameAs(result))
    }

    @Test
    fun colorIntsToBitmap() {
        val w = Random.nextInt(MAX_DIM)
        val h = Random.nextInt(MAX_DIM)
        val colorInts = Util.randomColorInts(w * h)
        val b = Util.colorIntsToBitmap(colorInts, w, h)
        val result = Util.bitmapToColorInts(b)

        assertTrue(colorInts.contentEquals(result))
    }

    @Test
    fun bitmapToColors() {
        val w = Random.nextInt(MAX_DIM).coerceAtLeast(1)
        val h = Random.nextInt(MAX_DIM).coerceAtLeast(1)
        val b = Util.randomBitmap(w, h)
        val colors = Util.bitmapToColorInts(b)
        val result = Util.colorIntsToBitmap(colors, w, h)

        assertTrue(b.sameAs(result))
    }

    @Test
    fun bitmapToPhoto() {
        val w = Random.nextInt(MAX_DIM)
        val h = Random.nextInt(MAX_DIM)
        val p = Util.randomU8Photo(w, h)
        val b = Util.photoToBitmap(p)
        val result = Util.bitmapToPhoto(b)

        assertEquals(p, result)
    }

    @Test
    fun photoToBitmap() {
        val b = Util.randomBitmap(MAX_DIM)
        val p = Util.bitmapToPhoto(b)
        val result = Util.photoToBitmap(p)

        assertTrue(b.sameAs(result))
    }

    @Test
    fun sameData() {
        assertTrue(Util.sameColorData(IdentityCube, IdentityCube))
        val c0 = Util.randomColorCube()
        val c1 = Util.randomColorCube()
        assertTrue(Util.sameColorData(c0, c0))
        assertTrue(Util.sameColorData(c1, c1))
        assertFalse(Util.sameColorData(c0, c1))
    }

    @Test
    fun photo() {
        val p = Util.randomU8Photo(100, 100)
        assertEquals(p, IdentityCube * p)
        assertNotEquals(p, Util.randomColorCube() * p)
    }

    @Test
    fun lridentity() {
        val id = ColorCube(IdentityCube.colors)
        val cube = Util.clampColorCubeToU8(randomWellBehavedCube())
        val c0 = id * cube
        val c1 = cube * id
        assertEquals(c0, c1)
    }


    @Test
    @Ignore("$TAG: timing-only tests disabled")
    fun applyCubeToColors_timing() {
        //Thread.sleep(10000)
        val cube = randomWellBehavedCube()
        val colors = Util.randomColors(ColorCube.N_COLORS * 10)
        //  val apply = { KotlinCpuTrilinear.applyCubeToColorsInParallel(cube, colors, Util.executor, ColorCube.N2) }
        //  val apply = { KotlinCpuTrilinear.applyCubeToColors(cube, colors) }
        val apply = { RsFriend.applyColorCubeToColors(cube, colors) }
        //  val apply = { cube * colors }
        val start = System.currentTimeMillis()
        repeat(100) {
            apply()
        }
        RsFriend.rs.finish()
        throw AssertionError("${System.currentTimeMillis() - start} ms")
    }

    @Test
    fun photoConcat() {
        if (RsFriend.initialized) return
        val cs = listOf(
                randomWellBehavedCube(),
                randomWellBehavedCube(),
                randomWellBehavedCube(),
                randomWellBehavedCube(),
                randomWellBehavedCube()
        )
        val orig = Util.randomPhoto(100, 100)

        var p0 = orig
        for (c in cs.asReversed()) {
            p0 = c * p0
        }

        val mappend = { a: ColorCube, acc: ColorCube -> a * acc }
        val combined: ColorCube = cs.foldRight(IdentityCube, mappend)
        val p1 = combined * orig

        assertEquals(p0, p1)
    }

    @Test
    fun photoConcat2() {
        val cs = listOf<ColorCube>(
                randomWellBehavedCube(),
                randomWellBehavedCube(),
                randomWellBehavedCube(),
                randomWellBehavedCube(),
                randomWellBehavedCube()
        )
        val orig = Util.randomPhoto(100, 100)

        val mappend1 = { a: ColorCube, acc: ColorCube -> a * acc }
        val combined1: ColorCube = cs.foldRight(IdentityCube, mappend1)
        val mappend2 = { acc: ColorCube, a: ColorCube -> a * acc }
        val combined2: ColorCube = cs.fold(IdentityCube, mappend2)

        val p0 = combined1 * orig
        val p1 = combined2 * orig

        assertEquals(p0, p1)
    }

    companion object {
        private const val TAG = "UtilInstrumentTest"
        private const val MAX_DIM = 1000

        private fun randomWellBehavedCube(): ColorCube {
            val off = Util.clampColorToU8(Util.randomColor())
            val colorFunc = SimpleColorFunc { c ->
                Color(
                        Bounded(c.r.value * off.r.value),
                        Bounded(c.g.value * off.g.value),
                        Bounded(c.b.value * off.b.value)
                )
            }
            return colorFunc.toColorCube()
        }
    }
}
