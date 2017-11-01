package com.lydiaschiff.hellaparallel.renderers;

import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlend;
import android.support.annotation.RequiresApi;

import com.lydiaschiff.hella.RsRenderer;
import com.lydiaschiff.hella.RsUtil;
import com.lydiaschiff.hella.renderer.ScriptC_set_alpha;

/**
 * Created by lydia on 10/17/17.
 */
@RequiresApi(18)
public class TrailsRenderer implements RsRenderer {

    private ScriptIntrinsicBlend blendScript;
    private ScriptC_set_alpha setAlphaScript;
    private Allocation last;

    @Override
    public void renderFrame(RenderScript rs, Allocation in, Allocation out) {
        if (blendScript == null) {
            blendScript = ScriptIntrinsicBlend.create(rs, in.getElement());
            setAlphaScript = new ScriptC_set_alpha(rs);
            last = RsUtil.createMatchingAlloc(rs, in);
            last.copyFrom(in);
        }
        out.copyFrom(last);

        // setting the alpha here is just to trick ScriptIntrinsicBlend to do linear
        // interpolation for us
        setAlphaScript.set_alpha((short) 200);
        setAlphaScript.forEach_filter(out, out);

        setAlphaScript.set_alpha((short) 55);
        setAlphaScript.forEach_filter(in, in);

        blendScript.forEachSrcAtop(in, out);

        last.copyFrom(out);
    }

    @Override
    public String getName() {
        return "ScriptIntrinsicBlend (trails)";
    }

    @Override
    public boolean canRenderInPlace() {
        return false;
    }
}
