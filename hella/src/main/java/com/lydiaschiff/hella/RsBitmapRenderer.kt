package com.lydiaschiff.hella

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.util.Log

/**
 * Use a RsRenderer to apply edits to Bitmaps. This version is simple and not optimized for multiple
 * repeated renders.
 *
 * todo: this needs to be tested
 */
open class RsBitmapRenderer(private val rs: RenderScript, private val rsRenderer: RsRenderer) {
    /**
     * If true then the RsBitmapRenderer may choose to reuse the input bitmap as a buffer or render
     * the effects in place if possible.
     */
    var canModifyInputBitmap = true

    /**
     * If true, inBitmap is allowed to be the same as outBitmap, else false.
     */
    fun canRenderInPlace() = canModifyInputBitmap && rsRenderer.canRenderInPlace()

    /**
     * Apply RsRenderer to a Bitmap. If provided the output bitmap will be written to. The input
     * bitmap will not be written to.
     *
     * @param inBitmap input image
     * @param outBitmap optional output image to reuse, must be the same size as input
     * @return the output bitmap
     */
    @JvmOverloads
    fun apply(inBitmap: Bitmap, reuse: Bitmap? = null): Bitmap {
        val start = if (DO_TIMING) System.currentTimeMillis() else 0L

        val inAlloc = Allocation.createFromBitmap(rs, inBitmap)
        val outAlloc: Allocation
        var outBitmap = reuse

        if (outBitmap == null && canRenderInPlace()) {
            outBitmap = inBitmap
            outAlloc = Allocation.createTyped(rs, inAlloc.type)
        } else {
            if (outBitmap == null) {
                outBitmap = Bitmap.createBitmap(inBitmap.width, inBitmap.height, Config.ARGB_8888)
            }
            outAlloc = Allocation.createFromBitmap(rs, outBitmap)
        }

        checkInputAndOutput(inBitmap, requireNotNull(outBitmap), canRenderInPlace())

        Log.d(TAG, "applying RsRenderer: " + rsRenderer.name)

        rsRenderer.renderFrame(rs, inAlloc, outAlloc)

        outAlloc.copyTo(outBitmap)

        inAlloc.destroy()
        outAlloc.destroy()

        if (DO_TIMING) {
            val duration = System.currentTimeMillis() - start
            Log.d(TAG, "applied RsRenderer: " + rsRenderer.name + " to " +
                    bitmapToString(inBitmap) + " (" + duration + " ms)")
        }

        return outBitmap!!
    }

    override fun toString(): String {
        return "RsBitmapRenderer{" +
                ", rsRenderer=" + rsRendererToString(rsRenderer) +
                ", canModifyInputBitmap=" + canModifyInputBitmap +
                '}'
    }

    companion object {
        private const val TAG = "RsBitmapRenderer"
        private const val DO_TIMING = true

        private fun checkInputAndOutput(
                inBitmap: Bitmap,
                outBitmap: Bitmap,
                canRenderInPlace: Boolean
        ) {
            require(!(inBitmap === outBitmap && !canRenderInPlace)) {
                "in and out are the same bitmap, but we are not " +
                        "allowed to render in place. " + this
            }
            require(sameSize(inBitmap, outBitmap)) {
                "expected same sized bitmaps, got in=" + bitmapToString(inBitmap) + ", out=" +
                        bitmapToString(outBitmap) + ". " + this
            }
        }

        private fun sameSize(b1: Bitmap, b2: Bitmap): Boolean =
                b1 == b2 || b1.width == b2.width && b1.height == b2.height

        private fun bitmapToString(b: Bitmap?) =
                if (b == null) "null"
                else "Bitmap: w=${b.width}, h=${b.height}, config=${b.config}"

        private fun rsRendererToString(rsRenderer: RsRenderer): String =
                "RsRenderer{name=${rsRenderer.name}, " +
                        "canRenderInPlace=${rsRenderer.canRenderInPlace()}"
    }

}