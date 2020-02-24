package com.lydiaschiff.hellaparallel.renderers;

import android.renderscript.Allocation;
import android.renderscript.Matrix3f;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicColorMatrix;
import androidx.annotation.RequiresApi;

import com.lydiaschiff.hella.RsRenderer;


/**
 * Created by lydia on 10/17/17.
 */
@RequiresApi(17)
public class HueRotationRenderer implements RsRenderer {

    // determines speed of rotation
    private static final float INCREMENT = 0.15f;

    private final Matrix3f colorMatrix =  new Matrix3f();

    private ScriptIntrinsicColorMatrix colorMatrixScript;
    private float hueOffset;

    @Override
    public void renderFrame(RenderScript rs, Allocation in, Allocation out) {
        if (colorMatrixScript == null) {
            colorMatrixScript = ScriptIntrinsicColorMatrix.create(rs);
        }

        // change the hue a bit each frame
        hueOffset += INCREMENT;
        setColorMatrix3f(colorMatrix, hueOffset);
        colorMatrixScript.setColorMatrix(colorMatrix);

        colorMatrixScript.forEach(in, out);
    }

    @Override
    public String getName() {
        return "hue rotation with ScriptIntrinsicColorMatrix";
    }

    @Override
    public boolean canRenderInPlace() {
        return true;
    }

    /**
     * Load a color matrix with a hue offset. Can be used with {@link ScriptIntrinsicColorMatrix}.
     * from https://github.com/googlesamples/android-RenderScriptIntrinsic
     *
     * @param mat       3x3 color matrix
     * @param hueOffset offset for hue, any value
     */
    private static void setColorMatrix3f(Matrix3f mat, float hueOffset) {
        float cos = (float) Math.cos(hueOffset);
        float sin = (float) Math.sin(hueOffset);
        mat.set(0, 0, .299f + .701f * cos + .168f * sin);
        mat.set(1, 0, .587f - .587f * cos + .330f * sin);
        mat.set(2, 0, .114f - .114f * cos - .497f * sin);
        mat.set(0, 1, .299f - .299f * cos - .328f * sin);
        mat.set(1, 1, .587f + .413f * cos + .035f * sin);
        mat.set(2, 1, .114f - .114f * cos + .292f * sin);
        mat.set(0, 2, .299f - .300f * cos + 1.25f * sin);
        mat.set(1, 2, .587f - .588f * cos - 1.05f * sin);
        mat.set(2, 2, .114f + .886f * cos - .203f * sin);
    }
}
