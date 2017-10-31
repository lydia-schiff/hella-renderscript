package com.lydiaschiff.hella.renderer;

import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicColorMatrix;
import android.support.annotation.RequiresApi;

import com.lydiaschiff.hella.RsRenderer;

/**
 * Render an image allocation in greyscale.
 */
@RequiresApi(17)
public class GreyscaleRsRenderer implements RsRenderer {

    private ScriptIntrinsicColorMatrix greyscaleScript;

    @Override
    public void renderFrame(RenderScript rs, Allocation in, Allocation out) {
        if (greyscaleScript == null) {
            greyscaleScript = createScript(rs, in.getElement());
            greyscaleScript.setGreyscale();
        }
        greyscaleScript.forEach(in, out);
    }

    @Override
    public String getName() {
        return "Greyscale with ScriptIntrinsicColorMatrix";
    }

    @Override
    public boolean canRenderInPlace() {
        return true;
    }

    private static ScriptIntrinsicColorMatrix createScript(RenderScript rs, Element e) {
        if (Build.VERSION.SDK_INT >= 19) {
            return ScriptIntrinsicColorMatrix.create(rs);
        } else {
            return ScriptIntrinsicColorMatrix.create(rs, e);
        }
    }
}
