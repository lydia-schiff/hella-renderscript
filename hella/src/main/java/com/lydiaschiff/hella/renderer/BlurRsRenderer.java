package com.lydiaschiff.hella.renderer;

import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import androidx.annotation.RequiresApi;

import com.lydiaschiff.hella.RsRenderer;

/**
 * Apply a gaussian blur to an image allocation.
 */
@RequiresApi(17)
public class BlurRsRenderer implements RsRenderer {

    private static final float BLUR_RADIUS = 10; // in range [0, 25]

    private ScriptIntrinsicBlur blurScript;

    @Override
    public void renderFrame(RenderScript rs, Allocation in, Allocation out) {
        if (blurScript == null) {
            blurScript = ScriptIntrinsicBlur.create(rs, in.getElement());
            blurScript.setRadius(BLUR_RADIUS);
        }
        blurScript.setInput(in);
        blurScript.forEach(out);
    }

    @Override
    public String getName() {
        return "Gaussian blur (radius " + BLUR_RADIUS + ") with ScriptIntrinsicBlur";
    }

    @Override
    public boolean canRenderInPlace() {
        return false;
    }
}
