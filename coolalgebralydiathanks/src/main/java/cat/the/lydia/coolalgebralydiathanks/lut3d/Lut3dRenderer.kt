package cat.the.lydia.coolalgebralydiathanks.lut3d

import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsic3DLUT
import cat.the.lydia.coolalgebralydiathanks.Color
import cat.the.lydia.coolalgebralydiathanks.ColorCube
import cat.the.lydia.coolalgebralydiathanks.ColorFunc
import cat.the.lydia.coolalgebralydiathanks.utils.RsFriend
import com.lydiaschiff.hella.RsRenderer

class Lut3dRenderer : RsRenderer, ColorFunc {

    // all guarded by "this"
    private var lut3dScript: ScriptIntrinsic3DLUT? = null
    private var lutAlloc: Allocation? = null
    private var hasUpdate = true
    private var colorCube: ColorCube = ColorCube.identity
    private var currentLutHash = colorCube.hashCode()

    @Synchronized
    fun setColorCube(cube: ColorCube) {
        if (cube == colorCube) return
        val hash = cube.hashCode()
        if (hash != currentLutHash) {
            colorCube = cube
            currentLutHash = hash
            hasUpdate = true
        }
    }

    @Synchronized
    override fun renderFrame(rs: RenderScript, inAlloc: Allocation, outAlloc: Allocation) {
        val script = lut3dScript ?: ScriptIntrinsic3DLUT.create(rs, Element.RGBA_8888(rs))
                .also { lut3dScript = it }
        val alloc = lutAlloc ?: RsFriend.lut3dAlloc(rs).also { lutAlloc = it }

        if (hasUpdate) {
            RsFriend.copyColorCubeToLutAlloc(colorCube, alloc)
            script.setLUT(alloc)
            hasUpdate = false
        }

        script.forEach(inAlloc, outAlloc)
    }

    override val name = "Lut3dRenderer Cool Algebra!"

    override fun canRenderInPlace() = true

    override fun mapColor(color: Color): Color {
        TODO("Not yet implemented")
    }

    override fun mapColors(colors: List<Color>): List<Color> {
        TODO("Not yet implemented")
    }

    @Synchronized
    fun reset() {
        lut3dScript?.destroy()
        lut3dScript = null
        lutAlloc?.destroy()
        lutAlloc = null
        hasUpdate = true
        colorCube = ColorCube.identity
        currentLutHash = colorCube.hashCode()
    }
}
