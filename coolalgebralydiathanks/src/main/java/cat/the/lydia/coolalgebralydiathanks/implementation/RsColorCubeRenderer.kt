package cat.the.lydia.coolalgebralydiathanks.implementation

import android.renderscript.RenderScript
import cat.the.lydia.coolalgebralydiathanks.ColorCube
import cat.the.lydia.coolalgebralydiathanks.rs.RsFriend
import com.lydiaschiff.hella.renderer.Lut3dRsRenderer

class RsColorCubeRenderer(private var colorCube : ColorCube) : Lut3dRsRenderer(ColorCube.N) {

    private var cubeUpdate = false

    // explicit no-arg constructor for use with camera
    constructor() : this(ColorCube.identity)

    override fun onFirstDraw(rs: RenderScript) {
        RsFriend.copyColorCubeToLutAlloc(colorCube, lutAlloc)
    }

    @Synchronized
    fun setColorCube(cube: ColorCube) {
        if (this.colorCube != cube) {
            this.colorCube = cube
            cubeUpdate = true
        }
    }

    override fun onUpdate() {
        if (cubeUpdate) {
            RsFriend.copyColorCubeToLutAlloc(colorCube, lutAlloc)
            cubeUpdate = false
        }
    }

    override val name = "Lut3dRenderer Cool Algebra!"
}
