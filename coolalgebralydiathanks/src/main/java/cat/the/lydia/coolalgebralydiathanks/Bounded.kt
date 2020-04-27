package cat.the.lydia.coolalgebralydiathanks

import kotlin.random.Random

/**
 * Just what it says. It's an immutable float value between 0 and 1.
 */
data class Bounded(val value: Float) {

    init {
        require(value in 0f..1f) { "expected value in [0,1] got value=$value" }
    }

    constructor(color: Int, channel: Channel) : this(when (channel) {
        Channel.R -> (color shr 16 and 0xff) / 255f
        Channel.G -> (color shr 8 and 0xff) / 255f
        Channel.B -> (color and 0xff) / 255f
    })

    companion object {
        val one = Bounded(1f)
        val zero = Bounded(0f)

        fun random() = Bounded(Random.nextFloat())
        fun randomLatticePoint() = ColorCube.cubeLatticePoints.random()

        fun lerp(a: Float, b: Float, scale: Float): Float =
                a * (1 - scale) + b * scale

        fun lerp(a: Bounded, b: Bounded, scale: Bounded): Bounded =
                Bounded(lerp(a.value, b.value, scale.value))
    }
}
