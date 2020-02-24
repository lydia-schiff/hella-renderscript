package com.lydiaschiff.hella.renderer;

import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicConvolve5x5;
import androidx.annotation.RequiresApi;

import com.lydiaschiff.hella.RsRenderer;

/**
 * Apply RGB sharpen to an image allocation using a 5x5 kernel.
 */
@RequiresApi(17)
public class SharpenRenderer implements RsRenderer {

    private static final float SHARPEN_INTENSITY = 1.0f;

    private ScriptIntrinsicConvolve5x5 sharpenScript;

    @Override
    public void renderFrame(RenderScript rs, Allocation in, Allocation out) {
        if (sharpenScript == null) {
            sharpenScript = ScriptIntrinsicConvolve5x5.create(rs, in.getElement());
            sharpenScript.setInput(in);

            float[] kernel = createSharpenKernel5x5(SHARPEN_INTENSITY);
            sharpenScript.setCoefficients(kernel);
        }
        sharpenScript.forEach(out);
    }

    @Override
    public String getName() {
        return "RGB sharpen with ScriptInstrinsic5x5Convolve";
    }

    @Override
    public boolean canRenderInPlace() {
        return false;
    }

    /**
     * Create a 5x5 sharpen convolution kernel to use with {@link ScriptIntrinsicConvolve5x5}.
     *
     * @param intensity sharpen intensity in [0,1]
     * @return new 5x5 sharpen kernel
     */
    private static float[] createSharpenKernel5x5(float intensity) {
        float centralWeightValue = 180.0f - intensity * 130.0f;
        float totalWeight = centralWeightValue - 32.0f;
        float x = -1.0f / totalWeight;
        float y = -2.0f / totalWeight;
        float z = centralWeightValue / totalWeight;
        return new float[] {
                x, x, x, x, x,
                x, y, y, y, x,
                x, y, z, y, x,
                x, y, y, y, x,
                x, x, x, x, x
        };
    }
}
