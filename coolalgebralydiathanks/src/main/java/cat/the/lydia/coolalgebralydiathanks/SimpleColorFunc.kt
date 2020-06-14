package cat.the.lydia.coolalgebralydiathanks

/**
 * A ColorFunc wrapping an actual (Color) -> Color function.
 */
class SimpleColorFunc(val f: (Color) -> Color) : ColorFunc {
    override fun apply(color: Color): Color = f(color)
    override fun apply(colors: List<Color>): List<Color> = colors.map(f)
    override fun isIdentity(): Boolean = false
    override fun toColorCube(): ColorCube = ColorCube(apply(IdentityCube.colors))
}
