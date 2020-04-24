package com.lydiaschiff.hella

import android.view.Surface
import androidx.annotation.AnyThread

/**
 * Created by lydia on 10/30/17.
 */
@AnyThread
interface RsSurfaceRenderer {
    /**
     * Set an output Surface, probably from a SurfaceView or TextureView.
     */
    fun setOutputSurface(surface: Surface)

    /**
     * Get the input Surface, usually passed to Camera2 CaptureSession. This Surface is expecting to
     * consume streaming YUV buffers from the camera.
     */
    val inputSurface: Surface?

    val isRunning: Boolean

    fun setRsRenderer(rsRenderer: RsRenderer)

    fun shutdown()
}