package cat.the.lydia.coolalgebralydiathanks.implementation

import android.renderscript.RenderScript
import cat.the.lydia.coolalgebralydiathanks.Color
import cat.the.lydia.coolalgebralydiathanks.ColorFunc
import cat.the.lydia.coolalgebralydiathanks.utils.BitmapFriend
import com.lydiaschiff.hella.RsBitmapRenderer
import com.lydiaschiff.hella.RsRenderer

/**
 * A ColorFunc backed by a RsBitmapRenderer. This adapts ColorFunc to use hella-renderscript's API
 * for general rendering of edits using Bitmap-backed RS Allocations.
 */
open class RsColorFunc(
        rs: RenderScript,
        renderer: RsRenderer
) : RsBitmapRenderer(rs, renderer), ColorFunc {

    init {
        canModifyInputBitmap = true
    }

    override fun mapColors(colors: List<Color>): List<Color> {
        // wicked, very landscape
        val inBitmap = BitmapFriend.colorsToBitmap(colors, colors.size, 1)
        return BitmapFriend.bitmapToColors(apply(inBitmap))
    }

    override fun mapColor(color: Color): Color = mapColors(listOf(color)).first()
}