package cat.the.lydia.coolalgebralydiathanks

import android.renderscript.RenderScript
import cat.the.lydia.coolalgebralydiathanks.implementation.RsColorCubeRenderer
import cat.the.lydia.coolalgebralydiathanks.implementation.RsColorFunc

/**
 * This is our complete CoolAlgebra, all we need is to implement
 * val colorCubeToColorFunc: (ColorCube) -> ColorFunc and set the value at runtime.
 * There are several ways to do this, but all of them do the same thing, namely turn the ColorCube's
 * Color data into a Function from Colors to Colors.
 *
 * Result type is always the type of the right operand.
 * ColorFunc * [List<Color> | Photo | ColorCube]
 * ColorCube * [List<Color> | Photo | ColorCube]
 *
 */
object CoolAlgebra {
    // The size of each edge of our ColorCube! Defines our universe!
    const val N = 17
    const val N2 = N * N

    // We'll need to implement this.
    lateinit var colorCubeToColorFunc: (ColorCube) -> ColorFunc

    // here's one way to do it!
    fun initWithRsImplementation(rs: RenderScript) {
        colorCubeToColorFunc = { cube -> RsColorFunc(rs, RsColorCubeRenderer(cube)) }
    }

    /**
     * The apply_ functions below are usually accessed with the infix operator times '*'. This
     * operator is implemented by each class that is allowed to be on the left side of the
     * operation, and has one version for each of the classes that are allowed to be on the right
     * side.
     *
     * Left (the mapping): ColorFunc, ColorCube
     * Right (the colors): List<Color>, Photo, ColorCube
     *
     * ColorCube is the class one allowed to be on both sides, and this is where our binary
     * operation comes from.
     *
     * ColorCube * ColorCube is equivalent to function concatenation. In particular, for
     * a, b, c : ColorCube we have:
     * associativity: (a * b) * c == a * (b * c)
     * unitality: identity * a == a == a * identity
     *
     * Associativity makes it a semi-group which sounds cool, add identity as unit and we get a
     * Monoid! That means M(ColorCube, *, identity) is a pretty good algebra all by itself.
     *
     * All we add to this is that ColorCubes can left-multiply with a ColorFunc.
     * For f : ColorFunc, a : ColorCube we have:
     * f * a == f(a) : ColorCube
     *
     * ColorCubes can also right multiply with any-sized List<Colors>, in particular a Photo.
     * a * colors = a(colors) : List<Color>
     * a * photo = a(photo) : Photo
     */

    // Our one operation! Use a ColorFunc to apply a filter to some Colors!
    // we get the other 5 operations for free, all implemented using the first.

    // ColorFunc * List<Color> -> List<Color>
    fun applyColorFuncToColors(func: ColorFunc, colors: List<Color>): List<Color> =
            func.mapColors(colors)

    // ColorCube * List<Color> -> List<Color>
    fun applyColorCubeToColors(cube: ColorCube, colors: List<Color>): List<Color> =
            applyColorFuncToColors(
                    func = cube.toColorFunc(),
                    colors = colors
            )

    // ColorFunc * Photo -> Photo
    fun applyColorFuncToPhoto(func: ColorFunc, photo: Photo): Photo {
        val result = applyColorFuncToColors(
                func = func,
                colors = photo.colors
        )
        return photo.copy(colors = result)
    }

    // ColorCube * Photo -> Photo
    fun applyColorCubeToPhoto(cube: ColorCube, photo: Photo): Photo {
        val result = applyColorFuncToColors(
                func = cube.toColorFunc(),
                colors = photo.colors
        )
        return photo.copy(colors = result)
    }

    // ColorFunc * ColorCube -> ColorCube
    fun applyColorFuncToColorCube(func: ColorFunc, colorCube: ColorCube): ColorCube {
        val result = applyColorFuncToColors(
                func = func,
                colors = colorCube.colors
        )
        return ColorCube(result)
    }

    // ColorCube * ColorCube -> ColorCube (our binary operation!)
    fun applyColorCubeToColorCube(a: ColorCube, b: ColorCube): ColorCube {
        val result = applyColorFuncToColors(
                func = a.toColorFunc(),
                colors = b.colors
        )
        return ColorCube(result)
    }
}
