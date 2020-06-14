package cat.the.lydia.coolalgebralydiathanks

// if true the library will be aware of identity lut data being the identity function and is able
// to be skipped. Everything should work if this is true or false.
internal var ENABLE_IDENTITY_SPECIAL_CASES = true

/**
 * The identity function as a ColorCube. Isomorphic to IdentityColorFunc.
 */
internal object IdentityCube : ColorCube(generateIdentityColorLattice())

/**
 * Generate a list of Colors that acts as the identity function when interpreted as a 3d-lattice of
 * Colors in a ColorCube.
 *
 * We are able to programmatically create this for any 3D dimensions as it is just a uniform
 * sampling of RGB space, including boundaries.
 */
internal fun generateIdentityColorLattice(): List<Color> {
    val id = ArrayList<Color>(ColorCube.N_COLORS)
    val step = 1 / (ColorCube.N - 1f)
    for (z in 0 until ColorCube.N) {
        val b = Bounded(z * step)
        for (y in 0 until ColorCube.N) {
            val g = Bounded(y * step)
            for (x in 0 until ColorCube.N) {
                val r = Bounded(x * step)
                id.add(Color(r, g, b))
            }
        }
    }
    return id
}

/**
 * The identity function as a simple ColorFunc without a backing ColorCube. Isomorphic to
 * IdentityCube.
 */
internal object IdentityColorFunc : ColorFunc {
    override fun apply(color: Color): Color = color
    override fun apply(colors: List<Color>): List<Color> = colors
    override fun isIdentity(): Boolean = ENABLE_IDENTITY_SPECIAL_CASES
    override fun toColorCube(): ColorCube =
            if (ENABLE_IDENTITY_SPECIAL_CASES) IdentityCube
            else IdentityColorFunc * IdentityCube
}
