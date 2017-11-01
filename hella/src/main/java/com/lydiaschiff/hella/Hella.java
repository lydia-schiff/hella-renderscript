package com.lydiaschiff.hella;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.RenderScript;
import android.util.Log;

import com.lydiaschiff.hella.renderer.ScriptC_color_frame;
import com.lydiaschiff.hella.renderer.ScriptC_set_alpha;
import com.lydiaschiff.hella.renderer.ScriptC_to_grey;

/**
 * Created by lydia on 10/30/17.
 */

public final class Hella {

    private static final String TAG = "Hella";

    private Hella() {} // no instances

    /**
     * These are custom kernels that are AoT compiled on the very first launch so we want to make
     * sure that happens outside of a render loop and also not in the UI thread.
     */
    public static void warmUpInBackground(RenderScript rs) {
        new Thread(() -> {
            Log.i(TAG, "RS warmup start...");
            long start = System.currentTimeMillis();
            try {
                ScriptC_color_frame color_frame = new ScriptC_color_frame(rs);
                ScriptC_set_alpha set_alpha = new ScriptC_set_alpha(rs);
                ScriptC_to_grey to_grey = new ScriptC_to_grey(rs);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i(TAG, "RS warmup end, " + (System.currentTimeMillis() - start) + " ms");
        }).start();
    }

    /**
     * @param context
     * @param cls
     * @param in
     * @return
     */
    public static Bitmap renderBitmap(Context context, Class<? extends RsRenderer> cls, Bitmap in) {
        RenderScript rs = RenderScript.create(context);
        try {
            return renderBitmap(rs, cls, in);
        } finally {
            rs.destroy();
        }
    }

    /**
     * @param rs
     * @param cls
     * @param in
     * @return
     */
    public static Bitmap renderBitmap(RenderScript rs, Class<? extends RsRenderer> cls, Bitmap in) {
        try {
            RsRenderer rsRenderer = cls.newInstance();
            return renderBitmap(rs, rsRenderer, in);

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "make sure RsRenderer implementation has a no-arg " + "constructor", e);
        }
    }

    /**
     * @param rs
     * @param rsRenderer
     * @param in
     * @return
     */
    public static Bitmap renderBitmap(RenderScript rs, RsRenderer rsRenderer, Bitmap in) {
        return new RsBitmapRenderer(rs, rsRenderer).apply(in);
    }

}