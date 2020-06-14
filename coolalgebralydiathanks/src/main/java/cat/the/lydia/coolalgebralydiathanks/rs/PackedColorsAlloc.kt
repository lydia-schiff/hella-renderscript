package cat.the.lydia.coolalgebralydiathanks.rs

import android.renderscript.Allocation
import android.renderscript.RenderScript
import cat.the.lydia.coolalgebralydiathanks.ColorData
import cat.the.lydia.coolalgebralydiathanks.utils.Util
import cat.the.lydia.coolalgebralydiathanks.utils.emptyIntArray

/**
 * A resizable allocation that holds ColorData as packed RGBA_8888 elements.
 */
class PackedColorsAlloc(private val rs: RenderScript) : RsResource {

    private var hasData = false
    private var destroyed = false
    private var count = 0
    private var buffer: IntArray = emptyIntArray()

    var alloc: Allocation = RsFriend.emptyAlloc
        get() {
            check(!destroyed)
            check(hasData)
            return field
        }
        private set

    fun setColorData(data: ColorData) {
        check(!destroyed)
        if (count != data.size) {
            reset()
            count = data.size
            alloc = Allocation.createSized(rs, RsFriend.RGBA_8888, count)
            buffer = IntArray(count)
        }
        Util.copyColorDataAsPackedRsColors(data, buffer)
        alloc.copyFromUnchecked(buffer)
        hasData = true
    }

    override fun reset() {
        if (destroyed) return
        if (alloc != RsFriend.emptyAlloc) {
            alloc.destroy()
            alloc = RsFriend.emptyAlloc
        }
        buffer = emptyIntArray()
        count = 0
        hasData = false
    }

    override fun destroy() {
        reset()
        destroyed = true
    }
}
