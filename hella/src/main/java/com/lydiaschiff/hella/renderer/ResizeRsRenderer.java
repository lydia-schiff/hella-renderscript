package com.lydiaschiff.hella.renderer;

import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicResize;
import android.support.annotation.RequiresApi;

import com.lydiaschiff.hella.RsRenderer;

/**
 * Resize an allocation to the size of the output allocation. Uses bicubic interpolation.
 */
@RequiresApi(20)
public class ResizeRsRenderer implements RsRenderer {

    private ScriptIntrinsicResize resizeScript;

    @Override
    public void renderFrame(RenderScript rs, Allocation in, Allocation out) {
        if (resizeScript == null) {
            resizeScript = ScriptIntrinsicResize.create(rs);
            resizeScript.setInput(in);
        }
        resizeScript.forEach_bicubic(out);
    }

    @Override
    public String getName() {
        return "bicubic resize with ScriptInstrinsicResize";
    }

    @Override
    public boolean canRenderInPlace() {
        // input and output allocations are not the same size
        return false;
    }
}
