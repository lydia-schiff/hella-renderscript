package cat.the.lydia.coolalgebralydiathanks.implementation

import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsic3DLUT
import cat.the.lydia.coolalgebralydiathanks.ColorCube
import cat.the.lydia.coolalgebralydiathanks.utils.RsFriend
import com.lydiaschiff.hella.RsRenderer

class RsLut3dRenderer(private var colorCube : ColorCube) : RsRenderer {

    // explicit no-arg constructor for use with camera
    constructor() : this(ColorCube.identity)

    // all guarded by "this"
    private var lut3dScript: ScriptIntrinsic3DLUT? = null
    private var lutAlloc: Allocation? = null
    private var hasUpdate = true
    private var currentLutHash = colorCube.hashCode()

    @Synchronized
    override fun renderFrame(rs: RenderScript, inAlloc: Allocation, outAlloc: Allocation) {
        val script = lut3dScript ?: RsFriend.lut3dScript(rs).also { lut3dScript = it }
        val lutAlloc = lutAlloc ?: RsFriend.lut3dAlloc(rs).also { lutAlloc = it }

        if (hasUpdate) {
            RsFriend.copyColorCubeToLutAlloc(colorCube, lutAlloc)
            script.setLUT(lutAlloc)
            hasUpdate = false
        }

        script.forEach(inAlloc, outAlloc)
    }

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

    /**
     * Release all memory. RsRenderer remains in a valid state.
     */
    @Synchronized
    fun reset() {
        lut3dScript?.destroy()
        lut3dScript = null
        lutAlloc?.destroy()
        lutAlloc = null
        colorCube = ColorCube.identity
        currentLutHash = colorCube.hashCode()
        hasUpdate = true
    }

    override val name = "Lut3dRenderer Cool Algebra!"

    override val canRenderInPlace = true
}
