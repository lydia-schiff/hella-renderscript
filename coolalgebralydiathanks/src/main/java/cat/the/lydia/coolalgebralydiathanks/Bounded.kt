package cat.the.lydia.coolalgebralydiathanks

/**
 * Just what it says. It's an immutable float value between 0 and 1.
 */
data class Bounded(val value: Float) {
    init {
        require(value in 0f..1f) { "expected value in [0,1] got value=$value" }
    }
}
