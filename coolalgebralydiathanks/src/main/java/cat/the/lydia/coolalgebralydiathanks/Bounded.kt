package cat.the.lydia.coolalgebralydiathanks

import androidx.annotation.IntRange
import cat.the.lydia.coolalgebralydiathanks.utils.Util
import kotlin.math.abs
import kotlin.random.Random

/**
 * Just what it says. It's an immutable float value between 0 and 1.
 */
class Bounded(val value: Float) {

    init {
        require(value in 0f..1f) { "expected value in [0,1] got value=$value" }
    }

    constructor(color: Int, channel: Channel) : this(when (channel) {
        Channel.R -> (color shr 16 and 0xff) / 255f
        Channel.G -> (color shr 8 and 0xff) / 255f
        Channel.B -> (color and 0xff) / 255f
    })

    /**
     * We end up needing to use an epsilon comparison here. The EPS is very small so we can fudge a
     * bit and still assume that this is an equivalence relation without anything breaking. The
     * purpose is to make our identity lattice Colors precisely representable in 8-bit (modulo fp
     * precision).
     */
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Bounded -> false
        value == other.value -> true
        else -> abs(value - other.value) < BOUNDED_EPS
    }

    /**
     * This means we can't use hashcode tho.
     */
    override fun hashCode() = throw NotImplementedError()

    override fun toString() = "Bounded($value)"

    companion object {
        val one = Bounded(1f)
        val zero = Bounded(0f)
        private const val BOUNDED_EPS = 0.0001f
    }
}

/**
 * Convert bounded to an unsigned 8-bit value in 0..255.
 */
@IntRange(from = 0, to = 255)
fun Bounded.toU8(): Int = Util.boundedToU8(this)

/**
 * Ensure an Int is a valid u8 value.
 */
@IntRange(from = 0, to = 255)
fun Int.ensureIsU8() = Util.ensureIsU8(this)

/**
 * Convert unsigned 8-bit value in 0..255 to Bounded.
 */
fun Int.u8toBounded(): Bounded = Util.u8ToBounded(Util.ensureIsU8(this))

/**
 * Check if the value corresponds exactly to an unsigned 8-bit value in 0..255.
 */
fun Bounded.isU8(): Boolean = Util.boundedIsU8(this)

/**
 * Clamp to nearest Bounded that corresponds exactly to an unsigned 8-bit value in 0..255.
 */
fun Bounded.clampToU8Bounded(): Bounded = Util.clampBoundedToU8(this)

