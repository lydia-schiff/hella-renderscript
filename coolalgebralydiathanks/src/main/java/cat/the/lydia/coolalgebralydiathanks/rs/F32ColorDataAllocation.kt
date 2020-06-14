package cat.the.lydia.coolalgebralydiathanks.rs

import android.renderscript.Allocation
import android.renderscript.RenderScript
import cat.the.lydia.coolalgebralydiathanks.ColorData
import cat.the.lydia.coolalgebralydiathanks.utils.N_CHANNELS_RGB
import cat.the.lydia.coolalgebralydiathanks.utils.Util
import cat.the.lydia.coolalgebralydiathanks.utils.emptyFloatArray

/**
 * A resizable allocation that holds ColorData as RGB F32_3 elements.
 */
class F32ColorDataAllocation(private val rs: RenderScript) : RsResource {

    private var hasData = false
    private var destroyed = false
    private var count = 0

    private var buffer: FloatArray = emptyFloatArray()

    var alloc: Allocation = RsFriend.emptyAlloc
        private set
        get() {
            check(!destroyed)
            check(hasData)
            return field
        }

    fun setColorData(data: ColorData) {
        check(!destroyed)
        if (count != data.size) {
            reset()
            count = data.size
            alloc = Allocation.createSized(rs, RsFriend.F32_3, count).apply { setAutoPadding(true) }
            buffer = FloatArray(count * N_CHANNELS_RGB)
        }
        Util.copyColorDataIntoFloatArray(data, buffer)
        alloc.copyFrom(buffer)
        hasData = true
    }

    override fun reset() {
        if (destroyed) return
        if (alloc !== RsFriend.emptyAlloc) {
            alloc.destroy()
            alloc = RsFriend.emptyAlloc
        }
        buffer = emptyFloatArray()
        count = 0
        hasData = false
    }

    override fun destroy() {
        reset()
        destroyed = true
    }
}
