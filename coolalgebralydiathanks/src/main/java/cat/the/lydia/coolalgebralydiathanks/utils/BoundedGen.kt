package cat.the.lydia.coolalgebralydiathanks.utils

import cat.the.lydia.coolalgebralydiathanks.Bounded
import cat.the.lydia.coolalgebralydiathanks.CoolAlgebra
import kotlin.random.Random

object BoundedGen : () -> Bounded {
    override fun invoke() = Bounded(Random.nextFloat())

    fun nextBounded(r : Random = Random) = Bounded(r.nextFloat())

    fun nextBoundedLatticeValue(r : Random = Random): Bounded = latticePointValues.random()

    /**
     * The CoolAlgebra.N Bounded values in each cube dimension.
     */
    val latticePointValues: List<Bounded> =
            (0 until CoolAlgebra.N).map { Bounded(it / (CoolAlgebra.N - 1f)) }
}
