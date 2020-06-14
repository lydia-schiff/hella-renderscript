package cat.the.lydia.coolalgebralydiathanks

import cat.the.lydia.coolalgebralydiathanks.utils.Util
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs
import kotlin.math.roundToInt

class UtilTest {
    @Test
    fun packColor8888() {
        val r = Util.randomU8()
        val g = Util.randomU8()
        val b = Util.randomU8()
        val expected = Util.rgb(r, g, b)

        val color = Color(Util.u8ToBounded(r), Util.u8ToBounded(g), Util.u8ToBounded(b))
        val packed = Util.packColor8888(color)

        assertEquals(expected, packed)
    }

    @Test
    fun boundedToU8() {
        assertEquals(0, Util.boundedToU8(Bounded(0f)))
        assertEquals(255, Util.boundedToU8(Bounded(1f)))

        val bounded = Util.randomBounded()

        assertEquals((bounded.value * 255).roundToInt(), Util.boundedToU8(bounded))
    }

    @Test
    fun u8ToBounded() {
        assertEquals(Bounded(0f), Util.u8ToBounded(0))
        assertEquals(Bounded(1f), Util.u8ToBounded(255))

        val u8 = Util.randomU8()
        assertEquals(Bounded(u8 / 255f), Util.u8ToBounded(u8))
    }

    @Test
    fun rgb() {
        val expected = Util.randomColorInt()
        val result = Util.rgb(Util.red(expected), Util.green(expected), Util.blue(expected))
        assertEquals(expected, result)
    }

    @Test
    fun colorIntToColor() {
        assertEquals(BLACK, Util.colorToColorInt(Color(Bounded.zero, Bounded.zero, Bounded.zero)))
        assertEquals(WHITE, Util.colorToColorInt(Color(Bounded.one, Bounded.one, Bounded.one)))

        for (c in COLORS) {
            val r = Util.red(c)
            val g = Util.green(c)
            val b = Util.blue(c)
            val result = Color(
                    Util.u8ToBounded(r),
                    Util.u8ToBounded(g),
                    Util.u8ToBounded(b)
            )
            val expected = Util.colorIntToColor(c)
            assertEquals(expected, result)
        }

        repeat(100) {
            val random = Util.randomColorInt()
            assertEquals(random, Util.colorToColorInt(Util.colorIntToColor(random)))
        }

    }

    @Test
    fun colorToColorInt() {
        assertEquals(BLACK, Util.colorToColorInt(Color(Bounded.zero, Bounded.zero, Bounded.zero)))
        assertEquals(WHITE, Util.colorToColorInt(Color(Bounded.one, Bounded.one, Bounded.one)))

        repeat(100) {
            val random = Util.randomU8Color()
            assertEquals(random, Util.colorIntToColor(Util.colorToColorInt(random)))
        }
    }

    @Test
    fun randomColorInts() {
        assertEquals(100, Util.randomColorInts(100).size)
    }

    @Test
    fun randomColors() {
        assertEquals(100, Util.randomColors(100).size)
    }

    @Test
    fun randomColorData() {
        assertEquals(100, Util.randomColorData(100).colors.size)
    }

    @Test
    fun randomPhoto() {
        val p = Util.randomPhoto(100, 101)
        assertEquals(100, p.width)
        assertEquals(101, p.height)
        val p1 = Util.randomU8Photo(100, 101)
        assertEquals(100, p1.width)
        assertEquals(101, p1.height)
    }

    @Test
    fun randomColorCube() {
        val c = Util.randomColorCube()
        assertEquals(ColorCube.N_COLORS, c.colors.size)
    }

    @Test
    fun boundedToU8Bounded() {
        val bounded = Util.randomBounded()
        val u8Bounded = bounded.clampToU8Bounded()
        val u8 = u8Bounded.toU8()

        assertTrue(u8Bounded.isU8())
        assertEquals(u8Bounded, u8.u8toBounded())
        assertEquals(u8Bounded, u8Bounded.clampToU8Bounded())
    }

    @Test
    fun colorToU8Color() {
        val color = randomColor()
        val u8Color = color.clampToU8Color()
        val colorInt = u8Color.toColorInt()

        assertTrue(u8Color.isU8Color())
        assertEquals(u8Color, colorInt.toColor())
        assertEquals(u8Color, u8Color.clampToU8Color())
    }

    @Test
    fun isU8Bounded() {
        for (n in 0 until 100) {
            assertTrue(Util.randomU8Bounded().isU8())
        }
    }

    @Test
    fun checkIdentityCubeIsInU8Cubes() {
        if (ColorCube.N != 16) return
        assertTrue(IdentityCube.colors.all(Color::isU8Color))
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
        val p = Util.randomPhoto(100, 100)
        assertEquals(p, IdentityCube * p)
        assertNotEquals(p, Util.randomColorCube() * p)
    }

    @Test
    fun lridentity() {
        val id = ColorCube(IdentityCube.colors)
        val cube = Util.randomColorCube()
        val c0 = IdentityCube * cube
        val c1 = cube * IdentityCube
        assertEquals(c0, c1)
    }

    fun randomWellBehavedCube(): ColorCube {
        val off = Util.randomColor()
        val colorFunc = SimpleColorFunc { c ->
            Color(
                    Bounded(c.r.value * off.r.value),
                    Bounded(c.b.value * off.b.value),
                    Bounded(c.g.value * off.g.value)
            )
        }
        return colorFunc.toColorCube()
    }

    @Test
    fun photoConcat() {
        val cs = listOf(randomWellBehavedCube(), randomWellBehavedCube(), randomWellBehavedCube(), randomWellBehavedCube(), randomWellBehavedCube())
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
        val cs = listOf(
                randomWellBehavedCube(),
                randomWellBehavedCube(),
                randomWellBehavedCube(),
                randomWellBehavedCube(),
                randomWellBehavedCube()
        )
        val orig = Util.randomPhoto(100, 100)

//        var p0 = orig
//        for (c in cs.asReversed()) {
//            p0 = c * p0
//        }

        val mappend1 = { a: ColorCube, acc: ColorCube -> a * acc }
        val combined1: ColorCube = cs.foldRight(IdentityCube, mappend1)
        val mappend2 = { acc: ColorCube, a: ColorCube -> a * acc }
        val combined2: ColorCube = cs.fold(IdentityCube, mappend2)

        val p0 = combined1 * orig
        val p1 = combined2 * orig

        assertEquals(p0, p1)
    }

    companion object {
        private const val BLACK = -0x1000000
        private const val DKGRAY = -0xbbbbbc
        private const val GRAY = -0x777778
        private const val LTGRAY = -0x333334
        private const val WHITE = -0x1
        private const val RED = -0x10000
        private const val GREEN = -0xff0100
        private const val BLUE = -0xffff01
        private const val YELLOW = -0x100
        private const val CYAN = -0xff0001
        private const val MAGENTA = -0xff01

        private val COLORS = listOf(BLACK, DKGRAY, GRAY, LTGRAY, WHITE, RED, GREEN, BLUE, YELLOW, CYAN, MAGENTA)
    }
}
