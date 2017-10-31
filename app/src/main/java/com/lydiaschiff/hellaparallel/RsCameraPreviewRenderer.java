package com.lydiaschiff.hellaparallel;

import android.graphics.ImageFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.support.annotation.AnyThread;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.WorkerThread;
import android.util.Log;
import android.view.Surface;

import com.lydiaschiff.hella.RsRenderer;
import com.lydiaschiff.hella.RsSurfaceRenderer;
import com.lydiaschiff.hella.RsUtil;
import com.lydiaschiff.hella.renderer.DefaultRsRenderer;


/**
 * Created by lydia on 10/30/17.
 */
@RequiresApi(19)
public class RsCameraPreviewRenderer
        implements RsSurfaceRenderer, Allocation.OnBufferAvailableListener, Runnable {

    private static final String TAG = "RsCameraPreviewRenderer";

    private final RenderScript rs;
    private final Allocation yuvInAlloc;
    private final Allocation rgbInAlloc;
    private final Allocation rgbOutAlloc;
    private final ScriptIntrinsicYuvToRGB yuvToRGBScript;

    @Nullable
    private final HandlerThread renderThread;

    // all guarded by "this"
    private Handler renderHandler;
    private RsRenderer rsRenderer;
    private int nFramesAvailable;
    private boolean outputSurfaceIsSet;

    /**
     * @param rs
     * @param x
     * @param y
     */
    public RsCameraPreviewRenderer(RenderScript rs, int x, int y) {
        this(rs, new DefaultRsRenderer(), x, y);
    }

    /**
     * @param rs
     * @param rsRenderer
     * @param x
     * @param y
     */
    public RsCameraPreviewRenderer(RenderScript rs, RsRenderer rsRenderer, int x, int y) {
        this(rs, rsRenderer, x, y, null);
    }

    /**
     * @param rs
     * @param rsRenderer
     * @param x
     * @param y
     * @param renderHandler
     */
    public RsCameraPreviewRenderer(RenderScript rs, RsRenderer rsRenderer, int x, int y,
            Handler renderHandler) {
        this.rs = rs;
        this.rsRenderer = rsRenderer;

        if (renderHandler == null) {
            this.renderThread = new HandlerThread(TAG);
            this.renderThread.start();
            this.renderHandler = new Handler(renderThread.getLooper());
        } else {
            this.renderThread = null;
            this.renderHandler = renderHandler;
        }

        Log.i(TAG,
                "Setting up RsCameraPreviewRenderer with " + rsRenderer.getName() + " (" + x + "," +
                        y + ")");

        yuvInAlloc = RsUtil.createYuvIoInputAlloc(rs, x, y, ImageFormat.YUV_420_888);
        yuvInAlloc.setOnBufferAvailableListener(this);

        rgbInAlloc = RsUtil.createRgbAlloc(rs, x, y);
        rgbOutAlloc = RsUtil.createRgbIoOutputAlloc(rs, x, y);

        yuvToRGBScript = ScriptIntrinsicYuvToRGB.create(rs, Element.RGBA_8888(rs));
        yuvToRGBScript.setInput(yuvInAlloc);
    }

    @Override
    @AnyThread
    public synchronized void setRsRenderer(RsRenderer rsRenderer) {
        if (isRunning()) {
            this.rsRenderer = rsRenderer;
            Log.i(TAG, "updating RsRenderer to " + rsRenderer.getName());
        }
    }

    /**
     * Check if this renderer is still running or has been shutdown.
     *
     * @return true if we're running, else false
     */
    @Override
    @AnyThread
    public synchronized boolean isRunning() {
        if (renderHandler == null) {
            Log.w(TAG, "renderer was already shut down");
            return false;
        }
        return true;
    }

    /**
     * Set the output surface to consume the stream of edited camera frames. This is probably
     * from a SurfaceView or TextureView. Please make sure it's valid.
     *
     * @param outputSurface a valid surface to consume a stream of edited frames from the camera
     */
    @AnyThread
    @Override
    public synchronized void setOutputSurface(Surface outputSurface) {
        if (isRunning()) {
            if (!outputSurface.isValid()) {
                throw new IllegalArgumentException("output was invalid");
            }
            rgbOutAlloc.setSurface(outputSurface);
            outputSurfaceIsSet = true;
            Log.d(TAG, "output surface was set");
        }
    }

    /**
     * Get the Surface that the camera will push frames to. This is the Surface from our yuv
     * input allocation. It will recieve a callback when a frame is available from the camera.
     *
     * @return a surface that consumes yuv frames from the camera preview, or null renderer is
     * shutdown
     */
    @AnyThread
    @Override
    public synchronized Surface getInputSurface() {
        return isRunning() ? yuvInAlloc.getSurface() : null;
    }

    /**
     * Callback for when the camera has a new frame. We want to handle this on the render thread
     * specific thread, so we'll increment nFramesAvailable and post a render request.
     */
    @Override
    public synchronized void onBufferAvailable(Allocation a) {
        if (isRunning()) {
            if (!outputSurfaceIsSet) {
                Log.e(TAG, "We are getting frames from the camera but we never set the view " +
                        "surface to render to");
                return;
            }
            nFramesAvailable++;
            renderHandler.post(this);
        }
    }

    /**
     * Render a frame on the render thread. Everything is async except for ioSend() will block
     * until the rendering completes. If we wanted to time it, make sure to log the time after
     * that call.
     */
    @WorkerThread
    @Override
    public void run() {
        RsRenderer renderer;
        int nFrames;
        synchronized (this) {
            if (!isRunning()) {
                return;
            }
            renderer = rsRenderer;
            nFrames = nFramesAvailable;
            nFramesAvailable = 0;
            renderHandler.removeCallbacks(this);
        }
        if (nFrames > 1) {
            Log.d(TAG, "renderer is falling behind, dropping " + (nFrames - 1) + "frames");
        }
        for (int i = 0; i < nFrames; i++) {
            yuvInAlloc.ioReceive();
        }
        yuvToRGBScript.forEach(rgbInAlloc);
        renderer.renderFrame(rs, rgbInAlloc, rgbOutAlloc);
        rgbOutAlloc.ioSend();
    }

    /**
     * Shut down the renderer when you're finished.
     */
    @Override
    @AnyThread
    public void shutdown() {
        synchronized (this) {
            if (!isRunning()) {
                Log.d(TAG, "requesting shutdown...");
                renderHandler.removeCallbacks(this);
                renderHandler.postAtFrontOfQueue(() -> {
                    Log.i(TAG, "shutting down");
                    synchronized (this) {
                        yuvInAlloc.destroy();
                        rgbInAlloc.destroy();
                        rgbOutAlloc.destroy();
                        yuvToRGBScript.destroy();
                        if (renderThread != null) {
                            renderThread.quitSafely();
                        }
                    }
                });
                renderHandler = null;
            }
        }
    }
}
