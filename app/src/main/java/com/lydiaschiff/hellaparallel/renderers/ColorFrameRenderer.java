package com.lydiaschiff.hellaparallel.renderers;

import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.Short4;
import androidx.annotation.RequiresApi;

import com.lydiaschiff.hella.RsRenderer;
import com.lydiaschiff.hella.RsUtil;
import com.lydiaschiff.hella.renderer.ResizeRsRenderer;
import com.lydiaschiff.hella.renderer.ScriptC_color_frame;

import java.util.Random;

/**
 * Created by lydia on 10/17/17.
 */
@RequiresApi(20)
public class ColorFrameRenderer implements RsRenderer {

    private static final int FRAMES_BEFORE_COLOR_REFRESH = 10;

    private final Random random = new Random();
    private final Short4 rgbaColor = new Short4(); // same as uchar4 in rs script

    private ResizeRsRenderer resizeRsRenderer;
    private ScriptC_color_frame colorFrameScript;
    private Allocation scaledAlloc;
    private int count;

    @Override
    public void renderFrame(RenderScript rs, Allocation in, Allocation out) {
        if (resizeRsRenderer == null) {
            resizeRsRenderer = new ResizeRsRenderer();
            colorFrameScript = new ScriptC_color_frame(rs);

            scaledAlloc = RsUtil.createRgbAlloc(rs, RsUtil.width(in) / 2, RsUtil.height(in) / 2);
            colorFrameScript.invoke_prepare(scaledAlloc);
        }

        if (count++ % FRAMES_BEFORE_COLOR_REFRESH == 0) {
            loadRandomRGBAColor();
            colorFrameScript.set_color(rgbaColor);
        }

        // scale image
        resizeRsRenderer.renderFrame(rs, in, scaledAlloc);

        // draw scaled image and color frame. we use out as input and output because we don't
        // actually use the input value in the calculation, just the rectangle bounds
        colorFrameScript.forEach_frame_image(out, out);
    }

    @Override
    public String getName() {
        return "Color frame with ScriptIntrinsicResize";
    }

    @Override
    public boolean canRenderInPlace() {
        return true;
    }

    /**
     * Load a random RGBA color corresponding to a uchar4 in rs.
     */
    private void loadRandomRGBAColor() {
        rgbaColor.x = (short) random.nextInt(0xff);
        rgbaColor.y = (short) random.nextInt(0xff);
        rgbaColor.z = (short) random.nextInt(0xff);
        rgbaColor.w = (short) 0xff;
    }
}
