package com.lydiaschiff.hellaparallel.renderers;

import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import androidx.annotation.RequiresApi;

import com.lydiaschiff.hella.RsRenderer;
import com.lydiaschiff.hella.RsUtil;


@RequiresApi(18)
public class AcidRenderer implements RsRenderer {

    private final TrailsRenderer trailsRenderer = new TrailsRenderer();
    private final HueRotationRenderer hueRotationRenderer = new HueRotationRenderer();

    private Allocation tempRgb;

    @Override
    public void renderFrame(RenderScript rs, Allocation in, Allocation out) {
        if (tempRgb == null) {
            tempRgb = RsUtil.createMatchingAlloc(rs, in);
        }
        hueRotationRenderer.renderFrame(rs, in, tempRgb);
        trailsRenderer.renderFrame(rs, tempRgb, out);
    }

    @Override
    public String getName() {
        return trailsRenderer.getName() + " + " + hueRotationRenderer.getName() + " (acid)";
    }

    @Override
    public boolean canRenderInPlace() {
        return false;
    }
}
