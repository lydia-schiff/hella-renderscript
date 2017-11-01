package com.lydiaschiff.hella;

import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * todo: this needs to be tested
 */
public class RsBitmapRenderer {

    private static final String TAG = "RsBitmapRenderer";

    private final RenderScript rs;
    private final RsRenderer rsRenderer;
    private boolean canModifyInputBitmap = true;

    private static final boolean DO_TIMING = true;
    private long start;

    /**
     * @param rs
     * @param rsRenderer
     */
    public RsBitmapRenderer(RenderScript rs, RsRenderer rsRenderer) {
        this.rs = rs;
        this.rsRenderer = rsRenderer;
    }

    /**
     * @param canModifyInputBitmap
     */
    public void setCanModifyInputBitmap(boolean canModifyInputBitmap) {
        this.canModifyInputBitmap = canModifyInputBitmap;
    }

    /**
     * @param in
     * @return
     */
    public Bitmap apply(Bitmap in) {
        if (canModifyInputBitmap && rsRenderer.canRenderInPlace()) {
            return apply(in, in);
        }
        return apply(in, null);
    }

    /**
     * @param in
     * @param out
     * @return
     */
    public Bitmap apply(Bitmap in, @Nullable Bitmap out) {
        if (DO_TIMING) {
            start = System.currentTimeMillis();
        }

        Allocation inAlloc = Allocation.createFromBitmap(rs, in);
        Allocation outAlloc;
        if (out != null) {
            if (!sameSize(in, out)) {
                throw new IllegalArgumentException(
                        "expected same sized bitmaps, got in=" + bitmapToString(in) + ", out=" +
                                bitmapToString(out));
            }
            outAlloc = Allocation.createFromBitmap(rs, out);
        } else {
            outAlloc = Allocation.createTyped(rs, inAlloc.getType());
        }

        Log.d(TAG, "applying RsRenderer: " + rsRenderer.getName());
        rsRenderer.renderFrame(rs, inAlloc, outAlloc);

        outAlloc.copyTo(out);

        inAlloc.destroy();
        outAlloc.destroy();

        if (DO_TIMING) {
            long duration = System.currentTimeMillis() - start;
            Log.d(TAG, "applied RsRenderer: " + rsRenderer.getName() + " to " + bitmapToString(in) +
                    " (" + duration + " ms)");
        }

        return out;
    }

    private static boolean sameSize(Bitmap b1, Bitmap b2) {
        return !(b1 == null || b2 == null) &&
                (b1 == b2 || b1.getWidth() == b2.getWidth() && b1.getHeight() == b2.getHeight());
    }

    private static String bitmapToString(Bitmap b) {
        return b == null
               ? "null"
               : "Bitmap: w=" + b.getWidth() + ", h=" + b.getHeight() + "," + " config=" +
                       b.getConfig();
    }
}
