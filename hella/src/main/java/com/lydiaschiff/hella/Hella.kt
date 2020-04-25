package com.lydiaschiff.hella

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.RenderScript
import android.util.Log
import com.lydiaschiff.hella.renderer.ScriptC_color_frame
import com.lydiaschiff.hella.renderer.ScriptC_set_alpha
import com.lydiaschiff.hella.renderer.ScriptC_to_grey

/**
 * Created by lydia on 10/30/17.
 */
object Hella {

    /**
     * Initialize the custom kernels that are AoT compiled on the very first launch. We prefer that
     * happens outside of a render loop and also not in the UI thread.
     */
    @JvmStatic
    fun warmUpInBackground(rs: RenderScript) = let {
        Thread(Runnable {
            Log.i(TAG, "RS warmup start...")
            val start = System.currentTimeMillis()
            try {
                val color_frame_script = ScriptC_color_frame(rs)
                Log.d(TAG, "initialized ScriptC_color_frame.")
                val set_alpha_script = ScriptC_set_alpha(rs)
                Log.d(TAG, "initialized ScriptC_set_alpha.")
                val to_grey_script = ScriptC_to_grey(rs)
                Log.d(TAG, "initialized ScriptC_to_grey.")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Log.i(TAG, "RS warmup end, " + (System.currentTimeMillis() - start) + " ms")
        })
    }.start()

    @JvmStatic
    fun renderBitmap(context: Context, cls: Class<out RsRenderer>, inBitmap: Bitmap): Bitmap {
        val rs = RenderScript.create(context)
        return renderBitmap(rs, cls, inBitmap).also { rs.destroy() }
    }

    @JvmStatic
    fun renderBitmap(rs: RenderScript, cls: Class<out RsRenderer>, inBitmap: Bitmap): Bitmap {
        return try {
            val rsRenderer = cls.newInstance()
            renderBitmap(rs, rsRenderer, inBitmap)
        } catch (e: Exception) {
            throw IllegalArgumentException(
                    "make sure RsRenderer implementation has a no-arg constructor", e)
        }
    }

    @JvmStatic
    fun renderBitmap(rs: RenderScript, rsRenderer: RsRenderer, inBitmap: Bitmap): Bitmap =
            RsBitmapRenderer(rs, rsRenderer).apply(inBitmap)

    private const val TAG = "Hella"
}
