package cat.the.lydia.coolalgebralydiathanks.lut3d

import android.renderscript.RenderScript
import cat.the.lydia.coolalgebralydiathanks.Color
import cat.the.lydia.coolalgebralydiathanks.ColorFunc
import cat.the.lydia.coolalgebralydiathanks.utils.BitmapFriend
import com.lydiaschiff.hella.RsBitmapRenderer
import com.lydiaschiff.hella.RsRenderer

class RsColorFunc(
        rs: RenderScript,
        renderer: RsRenderer
) : RsBitmapRenderer(rs, renderer), ColorFunc {

    init {
        canModifyInputBitmap = true
    }

    override fun mapColors(colors: List<Color>): List<Color> {
        // wicked, like very landscape
        val inBitmap = BitmapFriend.colorsToBitmap(colors, colors.size, 1)
        val outBitmap = apply(inBitmap)
        return BitmapFriend.bitmapToColors(outBitmap).also {
            inBitmap.recycle()
            outBitmap.recycle()
        }
    }

    override fun mapColor(color: Color): Color = mapColors(listOf(color)).first()
}