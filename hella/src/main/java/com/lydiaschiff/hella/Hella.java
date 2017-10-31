package com.lydiaschiff.hella;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.RenderScript;

/**
 * Created by lydia on 10/30/17.
 */

public final class Hella {

    private Hella() {} // no instances

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