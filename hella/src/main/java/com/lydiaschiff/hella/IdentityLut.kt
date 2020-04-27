package com.lydiaschiff.hella

import android.renderscript.Allocation
import kotlin.math.roundToInt

object IdentityLut {

    /**
     * Set a 3D LUT Allocation to represent the identity function.
     */
    fun setIdentityLutData(lutAlloc: Allocation): Allocation {
        val t = lutAlloc.type
        val identity = rsIdentityColorLattice(t.x, t.y, t.z, RsUtil.PooledBuffer.acquire(t.count))
        lutAlloc.copyFromUnchecked(identity)
        return lutAlloc
    }

    fun rsIdentityColorLattice(
            x: Int,
            y: Int = x,
            z: Int = x,
            buffer: IntArray = IntArray(x * y * z)
    ): IntArray {
        require(x > 1 && y > 1 && z > 1)
        require(buffer.size == x * y * z)
        var i = 0
        for (b in 0 until z) {
            for (g in 0 until y) {
                for (r in 0 until x) {
                    buffer[i++] = RsUtil.rsPackedColor8888(
                            ((r / (x - 1f)) * 255f).roundToInt() and 0xff,
                            ((g / (y - 1f)) * 255f).roundToInt() and 0xff,
                            ((b / (z - 1f)) * 255f).roundToInt() and 0xff
                    )
                }
            }
        }
        return buffer
    }
}