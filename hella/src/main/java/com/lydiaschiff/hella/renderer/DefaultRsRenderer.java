package com.lydiaschiff.hella.renderer;

import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import androidx.annotation.RequiresApi;

import com.lydiaschiff.hella.RsRenderer;

/**
 * Render an image allocation without modification.
 */
@RequiresApi(18)
public class DefaultRsRenderer implements RsRenderer {

    @Override
    public void renderFrame(RenderScript rs, Allocation in, Allocation out) {
        out.copyFrom(in);
    }

    @Override
    public String getName() {
        return "default (no edits applied)";
    }

    @Override
    public boolean canRenderInPlace() {
        return true;
    }
}
