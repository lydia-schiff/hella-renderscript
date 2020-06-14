package cat.the.lydia.coolalgebralydiathanks.implementation

import cat.the.lydia.coolalgebralydiathanks.Bounded
import cat.the.lydia.coolalgebralydiathanks.Color
import cat.the.lydia.coolalgebralydiathanks.ColorCube
import cat.the.lydia.coolalgebralydiathanks.utils.Util
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

/**
 * CPU reference implementation of ColorCube application to ColorData. This does 3D-lookup on a
 * ColorCube's data and uses trilinear-interpolation.
 *
 * For any reasonably sized ColorData, this should be slower than equivalent versions implemented
 * in C++, RenderScript, or OpenGL, although the parallel versions should be faster than sequential.
 */
object KotlinCpuTrilinear {

    /**
     * Apply a ColorCube to a Color.
     */
    fun applyCubeToColor(
            cube: ColorCube,
            color: Color
    ): Color = trilinearInterpolateColor(cube, color)

    /**
     * Apply a ColorCube to a list of colors sequentially.
     */
    fun applyCubeToColors(
            cube: ColorCube,
            colors: List<Color>
    ): List<Color> = colors.map { trilinearInterpolateColor(cube, it) }

    /**
     * Apply a ColorCube to a list of colors in parallel, for when you happen to have an
     * ExecutorService lying around, and you're trying to try.
     */
    fun applyCubeToColorsInParallel(
            cube: ColorCube,
            colors: List<Color>,
            executor: ExecutorService,
            batchSize: Int = DEFAULT_BATCH_SIZE
    ): List<Color> {
        val size = batchSize.coerceIn(0, colors.size)
        val tasks = mutableListOf<Future<List<Color>>>()
        var remaining = colors.size
        var n = 0

        while (remaining > 0) {
            val start = n
            val end = (n + size).coerceAtMost(colors.size)
            tasks += executor.submit(Callable {
                applyCubeToColors(cube, colors.subList(start, end))
            })
            n += batchSize
            remaining -= size
        }

        return mutableListOf<Color>().apply {
            tasks.forEach {
                addAll(it.get())
            }
        }
    }

    private const val DEFAULT_BATCH_SIZE = ColorCube.N_COLORS / 17

    /**
     * Use tri-linear interpolation to apply a ColorCube to a Color.
     *
     * We Use the color as a position in the ColorCube and find out local cube. Whatever
     * fractional part of the distance from the ColorCube origin we had left is our local cube
     * offset.
     *
     * Do our little zoom-in and we're just a Color in a unit cube again!
     * The corners of our local cube are the 8 nearest colors in the lattice.
     *  That's everything we need. Calculate the result Color.
     */
    private fun trilinearInterpolateColor(cube: ColorCube, color: Color): Color {
        val position = LocalCubePosition(color)
        val localCube = LocalColorCube(position.localOrigin, cube)

        // Lerp is a great but also kind of bad word for linear interpolate. It's sometimes
        // called mix. Everything in our algebra is lerp-able, and Color is no exception.

        // We choose one channel to interpolate in first. We did red here but it doesn't matter
        // because it's a cube! We now start with the two faces of the cube perpendicular to the
        // red axis and do 4 interpolations to collapse the down to 4 Colors in a green-blue plane.
        val c00x = Util.linearInterpolate(localCube.c000, localCube.c001, position.localOffset.b)
        val c01x = Util.linearInterpolate(localCube.c010, localCube.c011, position.localOffset.b)
        val c10x = Util.linearInterpolate(localCube.c100, localCube.c101, position.localOffset.b)
        val c11x = Util.linearInterpolate(localCube.c110, localCube.c111, position.localOffset.b)

        // Interpolate twice more in the green direction to collapse the plane down to a line
        // parallel to the blue axis.
        val c0xx = Util.linearInterpolate(c00x, c01x, position.localOffset.g)
        val c1xx = Util.linearInterpolate(c10x, c11x, position.localOffset.g)

        // Interpolate once more in the blue direction to collapse the line to a point,
        // our Color!
        return Util.linearInterpolate(c0xx, c1xx, position.localOffset.r)
    }

    /**
     * Use a Color as the location in our ColorCube. The point will map to a point inside a local
     * cube in the lattice with the 8 nearest colors at the corners. We need to figure out the
     * coordinates of the lattice points so we can get the nearby colors, and we need the local
     * position in the cube so we can interpolate.
     */
    private class LocalCubePosition(color: Color) {
        val localOrigin: Int3
        val localOffset: Color

        /**
         * Convert the color from a position in the unit cube to a position in our N*N*N cube.
         * Values will be real numbers in [0,N-1].
         *
         * Floor this value to get the integer part of the position. This will be the indices of the
         * origin for our local cube. From now on our local cube is the origin + [0,1] in each
         * direction.
         *
         * If the Color has the value 1f in any of the channels, then the cube index will be
         * CoolAlgebra.N-1, which is the last index in 3D (the edge of our lattice).
         *
         * What we actually want is an index that is at most CoolAlgebra.N-2. This is because we
         * always want to have a lattice point on each side of us for interpolating.
         *
         * If we need to coerce the point to the interior of the lattice, then rather than being 0f
         * of the way from the edge, we will be 1f of the way from the previous point (which is the
         * same thing! It makes our math nicer.
         *
         *  Next, subtract the base index from the position the get the fractional part of the
         *  position. This is the offset from our local origin, and is a color again! Just as if
         *  the local cube is our unit cube.
         */
        init {
            val realPositionInIndexSpace = Float3(color) * (ColorCube.N - 1)
            val intPositionInIndexSpace: Int3 = realPositionInIndexSpace.floor()
            var rOrigin = intPositionInIndexSpace.x
            var rOffset = realPositionInIndexSpace.x - rOrigin
            if (rOrigin == ColorCube.N - 1) {
                rOrigin--
                rOffset = 1f
            }
            var gOrigin = intPositionInIndexSpace.y
            var gOffset = realPositionInIndexSpace.y - gOrigin
            if (gOrigin == ColorCube.N - 1) {
                gOrigin--
                gOffset = 1f
            }
            var bOrigin = intPositionInIndexSpace.z
            var bOffset = realPositionInIndexSpace.z - bOrigin
            if (bOrigin == ColorCube.N - 1) {
                bOrigin--
                bOffset = 1f
            }
            localOrigin = Int3(rOrigin, gOrigin, bOrigin)
            localOffset = Color(Bounded(rOffset), Bounded(gOffset), Bounded(bOffset))
        }
    }

    /**
     * 8 nearest Colors in a ColorCube based on indices of localOrigin.
     */
    private class LocalColorCube(localOrigin: Int3, cube: ColorCube) {
        val c000: Color = cube.colors[index1d(localOrigin + p000)]
        val c001: Color = cube.colors[index1d(localOrigin + p001)]
        val c010: Color = cube.colors[index1d(localOrigin + p010)]
        val c011: Color = cube.colors[index1d(localOrigin + p011)]
        val c100: Color = cube.colors[index1d(localOrigin + p100)]
        val c101: Color = cube.colors[index1d(localOrigin + p101)]
        val c110: Color = cube.colors[index1d(localOrigin + p110)]
        val c111: Color = cube.colors[index1d(localOrigin + p111)]

        companion object {
            private fun index1d(p: Int3) = p.x + p.y * ColorCube.N + p.z * ColorCube.N2

            // Unit index offsets
            private val p000 = Int3(0, 0, 0)
            private val p001 = Int3(0, 0, 1)
            private val p010 = Int3(0, 1, 0)
            private val p011 = Int3(0, 1, 1)
            private val p100 = Int3(1, 0, 0)
            private val p101 = Int3(1, 0, 1)
            private val p110 = Int3(1, 1, 0)
            private val p111 = Int3(1, 1, 1)
        }
    }

    private data class Float3(val x: Float, val y: Float, val z: Float) {
        constructor(c: Color) : this(c.r.value, c.g.value, c.b.value)

        fun floor() = Int3(x.toInt(), y.toInt(), z.toInt())

        infix operator fun minus(value: Float3) =
                Float3(value.x * x, value.y * y, value.z * z)

        infix operator fun minus(value: Int3) =
                Float3(value.x * x, value.y * y, value.z * z)

        infix operator fun times(value: Float) =
                Float3(value * x, value * y, value * z)

        infix operator fun times(value: Int) =
                Float3(value * x, value * y, value * z)
    }

    private data class Int3(val x: Int, val y: Int, val z: Int) {
        infix operator fun plus(o: Int3) = Int3(x + o.x, y + o.y, z + o.z)
    }
}
