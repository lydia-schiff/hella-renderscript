package cat.the.lydia.coolalgebralydiathanks

import cat.the.lydia.coolalgebralydiathanks.utils.Util

interface ColorData {
    val colors: List<Color>
    val size: Int
        get() = colors.size

    /**
     * Returns true if ColorData has the same colors as another ColorData instance.
     */
    fun contentEquals(other: ColorData): Boolean = Util.sameColorData(this, other)
}
