package com.lydiaschiff.hella;

import androidx.annotation.AnyThread;
import android.view.Surface;

/**
 * Created by lydia on 10/30/17.
 */
@AnyThread
public interface RsSurfaceRenderer {
    /**
     * Set an output Surface, probably from a SurfaceView or TextureView.
     */
    void setOutputSurface(Surface surface);

    /**
     * Get the input Surface, usually passed to Camera2 CaptureSession. This Surface is expecting to
     * consume streaming YUV buffers from the camera.
     */
    Surface getInputSurface();

    void setRsRenderer(RsRenderer rsRenderer);

    boolean isRunning();

    void shutdown();
}
