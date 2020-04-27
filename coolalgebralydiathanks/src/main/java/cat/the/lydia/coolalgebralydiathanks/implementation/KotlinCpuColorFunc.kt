package cat.the.lydia.coolalgebralydiathanks.implementation

import cat.the.lydia.coolalgebralydiathanks.Color
import cat.the.lydia.coolalgebralydiathanks.ColorCube
import cat.the.lydia.coolalgebralydiathanks.ColorFunc
import cat.the.lydia.coolalgebralydiathanks.CoolAlgebra
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

/**
 * Apply a ColorCube to Colors on CPU. Applying to a List<Color> can be done if parallel, if you
 * happen to have an executor service lying around, and you're trying to try.
 */
data class KotlinCpuColorFunc(
        val colorCube: ColorCube,
        val executor: ExecutorService? = null,
        val batchSize: Int = DEFAULT_BATCH_SIZE
) : ColorFunc {

    override fun mapColor(color: Color): Color =
            trilinearInterpolateColor(colorCube, color)

    override fun mapColors(colors: List<Color>): List<Color> = executor
            ?.let { trilinearInterpolateColorsInParallel(colorCube, colors, it, batchSize) }
            ?: trilinearInterpolateColors(colorCube, colors)

    companion object {
        const val DEFAULT_BATCH_SIZE = ColorCube.N_COLORS / 2

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
        fun trilinearInterpolateColor(cube: ColorCube, color: Color): Color {
            val position = LocalCubePosition(color)
            val localCube = LocalColorCube(position.localOrigin, cube)

            // Lerp is a great but also kind of bad word for linear interpolate. It's sometimes
            // called mix. Everything in our algebra is lerp-able, and Color is no exception.

            // We choose one channel to interpolate in first. We did red here but it doesn't matter
            // because it's a cube! We now start with the two faces of the cube perpendicular to the
            // red axis and do 4 interpolations to collapse the down to 4 Colors in a green-blue plane.
            val c00x = Color.lerp(localCube.c000, localCube.c001, position.localOffset.r)
            val c01x = Color.lerp(localCube.c010, localCube.c011, position.localOffset.r)
            val c10x = Color.lerp(localCube.c100, localCube.c101, position.localOffset.r)
            val c11x = Color.lerp(localCube.c110, localCube.c111, position.localOffset.r)

            // Interpolate twice more in the green direction to collapse the plane down to a line
            // parallel to the blue axis.
            val c0xx = Color.lerp(c00x, c01x, position.localOffset.g)
            val c1xx = Color.lerp(c10x, c11x, position.localOffset.g)

            // Interpolate once more in the blue direction to collapse the line to a point,
            // our Color!
            return Color.lerp(c0xx, c1xx, position.localOffset.b)
        }


        /**
         * Apply a ColorCube to a list of colors sequentially.
         */
        fun trilinearInterpolateColors(
                cube: ColorCube,
                colors: List<Color>
        ): List<Color> = colors.map { trilinearInterpolateColor(cube, it) }

        /**
         * Apply a ColorCube to a list of colors in parallel.
         */
        fun trilinearInterpolateColorsInParallel(
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
                    trilinearInterpolateColors(cube, colors.subList(start, end))
                })
                n += batchSize
                remaining -= size
            }

            return mutableListOf<Color>().apply { tasks.forEach { addAll(it.get()) } }
        }
    }

    /**
     * Use a Color as the location in our ColorCube. The point will map to a point inside a local
     * cube in the lattice with the 8 nearest colors at the corners. We need to figure out the
     * coordinates of the lattice points so we can get the nearby colors, and we need the local
     * position in the cube so we can interpolate.
     */
    internal class LocalCubePosition(color: Color) {
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
            val realPositionInIndexSpace = Float3(color) * (CoolAlgebra.N - 1)
            val intPositionInIndexSpace: Int3 = realPositionInIndexSpace.floor()
            localOrigin = intPositionInIndexSpace.coerceAtMost(CoolAlgebra.N - 2)
            localOffset = (realPositionInIndexSpace - localOrigin).toColor()
        }
    }

    internal class LocalColorCube(localOrigin: Int3, cube: ColorCube) {
        val c000: Color = cube.colors[index1d(localOrigin + p000)]
        val c001: Color = cube.colors[index1d(localOrigin + p001)]
        val c010: Color = cube.colors[index1d(localOrigin + p010)]
        val c011: Color = cube.colors[index1d(localOrigin + p011)]
        val c100: Color = cube.colors[index1d(localOrigin + p100)]
        val c101: Color = cube.colors[index1d(localOrigin + p101)]
        val c110: Color = cube.colors[index1d(localOrigin + p110)]
        val c111: Color = cube.colors[index1d(localOrigin + p111)]

        companion object {
            private fun index1d(p: Int3) = p.x + p.y * CoolAlgebra.N + p.z * CoolAlgebra.N2

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

    internal data class Float3(val x: Float, val y: Float, val z: Float) {
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

        fun toColor() = Color(x, y, z)
    }

    internal data class Int3(val x: Int, val y: Int, val z: Int) {
        infix operator fun plus(o: Int3) = Int3(x + o.x, y + o.y, z + o.z)

        fun coerceAtMost(n: Int): Int3 {
            if (x < n && y < n && z < n) return this
            return Int3(x.coerceAtMost(n), y.coerceAtMost(n), z.coerceAtMost(n))
        }
    }
}
