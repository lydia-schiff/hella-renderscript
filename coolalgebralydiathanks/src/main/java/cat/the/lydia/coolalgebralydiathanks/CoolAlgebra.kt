package cat.the.lydia.coolalgebralydiathanks

/**
 * This is our complete CoolAlgebra, all we need is to implement
 * val colorCubeToColorFunc: (ColorCube) -> ColorFunc and set the value at runtime.
 * There are several ways to do this, but all of them do the same thing, namely turn the ColorCube's
 * Color data into a Function from Colors to Colors.
 *
 * The apply* functions below are implemented as the infix operator times '*'.. This operation is
 * implemented by each class that is allowed to be on the left side of the operation, and has 1
 * version for each of the classes that are allowed to be on the right side
 *
 * Left : ColorFunc, ColorCube
 * Right : List<Color>, Photo, ColorCube
 *
 * ColorCube is the only one allowed to be on both sides and this is where our binary operation
 * comes from.
 *
 * ColorCube * ColorCube is
 * associative: (a * b) * c == a * (b * c)
 * unital: identity * a == a == a * identity
 *
 * That means M(ColorCube, *, identity) is a pretty good algebra all by itself. Associativity makes
 * it a semi-group which sounds cool, add unitality and we get a Monoid.
 *
 * All we add to this is that ColorCubes can left multiply with ColorFunc:
 * f * a == f(a)
 * and right multiply with any-sized list of Colors, specifically Photos:
 * a * colors = a(colors)
 * a * photo = a(photo)
 */
object CoolAlgebra {
    // The size of each edge of our ColorCube! Defines our universe!
    const val N = 17

    // We'll need to implement this.
    lateinit var colorCubeToColorFunc: (ColorCube) -> ColorFunc

    // Our one operation! Use a ColorFunc to apply a filter to some Colors!
    fun applyColorFuncToColors(func: ColorFunc, colors: List<Color>): List<Color> =
            func.mapColors(colors)

    // we get these three for free:
    fun applyColorFuncToPhoto(func: ColorFunc, photo: Photo) =
            Photo(applyColorFuncToColors(func, photo.colors), photo.width, photo.height)

    fun applyColorFunToColorCube(func: ColorFunc, colorCube: ColorCube): ColorCube =
            ColorCube(applyColorFuncToColors(func, colorCube.colors))

    fun applyColorCubeToColorCube(a: ColorCube, b: ColorCube): ColorCube =
            applyColorFunToColorCube(a.toColorFunc(), b)
}
