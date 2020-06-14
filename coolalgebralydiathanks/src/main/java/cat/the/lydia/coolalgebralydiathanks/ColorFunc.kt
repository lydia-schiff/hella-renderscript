package cat.the.lydia.coolalgebralydiathanks

import cat.the.lydia.coolalgebralydiathanks.utils.linearInterpolate

interface ColorFunc {
    fun apply(color: Color): Color
    fun apply(colors: List<Color>): List<Color>
    fun toColorCube(): ColorCube

    fun isIdentity(): Boolean = if (ENABLE_IDENTITY_SPECIAL_CASES) this == IdentityCube else false

    fun interpWith(b: ColorFunc, scale: Bounded): ColorCube = linearInterpolate(this, b, scale)

    // ColorFunc -> ColorData -> ColorData
    // a * b = ab
    infix operator fun times(data: ColorData): ColorData =
            if (isIdentity()) data
            else ColorList(apply(data.colors))

    infix operator fun times(data: List<Color>): ColorData = this * data.toColorData()

    // ColorFunc -> Photo -> Photo
    infix operator fun times(photo: Photo): Photo =
            if (isIdentity()) photo
            else {
                Photo(apply(photo.colors), photo.width, photo.height)
            }

    // ColorFunc -> ColorCube -> ColorCube
    infix operator fun times(cube: ColorCube): ColorCube = when {
        cube.isIdentity() -> this.toColorCube()
        this.isIdentity() -> cube
        this == cube -> cube
        else -> ColorCube(apply(cube.colors))
    }

    // ColorFunc -> ColorCube -> ColorCube
    infix operator fun times(cube: ColorFunc): ColorCube = when {
        cube.isIdentity() -> this.toColorCube()
        this.isIdentity() -> cube.toColorCube()
        this == cube -> cube.toColorCube()
        else -> ColorCube(apply(cube.toColorCube().colors))
    }
}


/**
 * A ColorFunc wrapping a (List<Color>) -> List<Color>.
 */
class ColorListFunction(val f: (List<Color>) -> List<Color>) : ColorFunc {
    override fun apply(color: Color): Color = f(listOf(color)).first()
    override fun apply(colors: List<Color>): List<Color> = f(colors)
    override fun isIdentity(): Boolean = false
    override fun toColorCube(): ColorCube = ColorCube(apply(IdentityCube.colors))
}

class U8ColorFunc(val f: ColorFunc) : ColorFunc {
    override fun apply(color: Color): Color = f.apply(color).clampToU8Color()
    override fun apply(colors: List<Color>): List<Color> =
            f.apply(colors).map(Color::clampToU8Color)

    override fun isIdentity(): Boolean = false
    override fun toColorCube(): ColorCube = ColorCube(apply(IdentityCube.colors))
}
