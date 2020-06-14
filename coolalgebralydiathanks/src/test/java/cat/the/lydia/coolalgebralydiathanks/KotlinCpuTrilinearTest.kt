package cat.the.lydia.coolalgebralydiathanks

import cat.the.lydia.coolalgebralydiathanks.implementation.KotlinCpuTrilinear
import cat.the.lydia.coolalgebralydiathanks.utils.Util
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import java.lang.AssertionError
import java.util.concurrent.Executors

class KotlinCpuTrilinearTest {

    @Test
    fun applyCubeToColors_identity() {
        var secondRun = false

        // identity cube should work in either case
        val current = ENABLE_IDENTITY_SPECIAL_CASES
        ENABLE_IDENTITY_SPECIAL_CASES = true
        try {
            while (true) {
                val cube = IdentityCube
                repeat(1000000) {
                    val color = Util.randomColor()
                    val result = KotlinCpuTrilinear.applyCubeToColor(cube, color)
                    assertEquals(result, color)
                }

                // check each lattice point
                for (color in IdentityCube.colors) {
                    val result = KotlinCpuTrilinear.applyCubeToColor(cube, color)
                    assertEquals(result, color)
                }

                if (secondRun) break
                ENABLE_IDENTITY_SPECIAL_CASES = false
                secondRun = true
            }
        } finally {
            ENABLE_IDENTITY_SPECIAL_CASES = current
        }
    }

    @Test
    @Ignore("$TAG: timing-only tests disabled")
    fun applyCubeToColors_random() {
        val cube = randomWellBehavedCube()
        val colors = Util.randomColors(1_000_000)

        val apply = { KotlinCpuTrilinear.applyCubeToColorsInParallel(cube, colors, pool) }

        val start = System.currentTimeMillis()
        repeat(100) {
            apply()
        }
        throw AssertionError("${System.currentTimeMillis() - start} ms")
    }


    @Test
    fun applyColorFuncToColors() {
        val off = Util.randomColor()
        val colorFunc = SimpleColorFunc { c ->
            Color(
                    Bounded(c.r.value * off.r.value),
                    Bounded(c.g.value * off.g.value),
                    Bounded(c.b.value * off.b.value)
            )
        }
        val cube = colorFunc.toColorCube()
        repeat(1000000) {
            val color = Util.randomColor()
            val result = KotlinCpuTrilinear.applyCubeToColor(cube, color)
            assertEquals(result, colorFunc.apply(color))
        }
    }

    companion object {
        private const val TAG = "CpuInterpolateTest"
        private fun randomWellBehavedCube(): ColorCube {
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

        private val pool = Executors.newFixedThreadPool(9)

    }
}
