package cat.the.lydia.coolalgebralydiathanks.utils

import androidx.test.platform.app.InstrumentationRegistry
import cat.the.lydia.coolalgebralydiathanks.ColorCube
import cat.the.lydia.coolalgebralydiathanks.R
import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*


class CubeFileParserTest {

    @Test
    fun loadCubeResource() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val colorCube = CubeFileParser.loadCubeResource(appContext, R.raw.fg_cine_drama_17)
        Assert.assertTrue(colorCube.colors.size == ColorCube.N_COLORS)
    }
}