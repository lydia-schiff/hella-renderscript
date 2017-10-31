package com.lydiaschiff.hella;

import android.renderscript.Allocation;
import android.renderscript.RenderScript;

/**
 * Created by lydia on 10/30/17.
 */

public interface RsRenderer {
    void renderFrame(RenderScript rs, Allocation in, Allocation out);
    String getName();
    boolean canRenderInPlace();
}
