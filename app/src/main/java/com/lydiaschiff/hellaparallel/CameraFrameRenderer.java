//package com.lydiaschiff.hellaparallel;
//
//import android.graphics.ImageFormat;
//import android.os.Handler;
//import android.renderscript.Allocation;
//import android.renderscript.Element;
//import android.renderscript.RenderScript;
//import android.renderscript.ScriptIntrinsicYuvToRGB;
//import android.support.annotation.RequiresApi;
//import android.util.Log;
//import android.view.Surface;
//
//import com.lydiaschiff.hella.RsBitmapRenderer;
//import com.lydiaschiff.hella.RsSurfaceRenderer;
//import com.lydiaschiff.hella.RsUtil;
//import com.lydiaschiff.hellaparallel.camera.FixedAspectSurfaceView;
//import com.lydiaschiff.hellaparallel.renderers.RsRenderer;
//
///**
// * This is the the actual rendering pipeline implementation!
// * <p>
// * Listens for {@link Allocation.OnBufferAvailableListener} callbacks, which indicate a new frame is
// * available from the camera, increments the pending frame count, and posts a runnable (this)
// * to the render thread to handle the new frame.
// * <p>
// * On the render thread (in {@link #run()}), we latch the most recent frame, and reset
// * the frame count.
// * <p>
// * It's possible that there was more than one frame waiting for us, which indicates that we are
// * rendering our edits slower than the camera is pushing frames. If so, we log it and skip over any
// * older frames and just take the newest. Dropping frames is fine for the camera viewfinder,
// * since it's supposed to be showing what we're looking at now and we don't want to lag. If we
// * were recording then it might be more involved.
// * <p>
// * We have a frame! After calling {@link Allocation#ioReceive()}, the YUV pixels are now
// * available in yuv Allocation. Our first script will convert the frame from YUV to RGBA_8888,
// * which makes it more like a Bitmap. Our {@link ScriptIntrinsicYuvToRGB} has
// * {@link #yuvIoInputAlloc} as an input and {@link #rgbInputAlloc} as an output.
// * <p>
// * Run it! The call will return right away, since "forEach()" calls are async. We just sent a
// * message to the internal context's internal thread to get started.
// * <p>
// * Next, call {@link RsRenderer#renderFrame(RenderScript, Allocation, Allocation)} on the current
// * RsRenderer instance. It applies it's filter and writes the result pixels into
// * {@link #rgbIoOutputAlloc}. This is the final RGBA_8888 Allocation we created with {@value
// * Allocation#USAGE_IO_OUTPUT}. We've given it the Surface that draws to our
// * {@link FixedAspectSurfaceView}, so we just call {@link Allocation#ioSend()}, which sends the
// * newly edited frame data to the screen.
// */
//@RequiresApi(19)
//public class CameraFrameRenderer
//        implements Allocation.OnBufferAvailableListener, Runnable, RsSurfaceRenderer {
//
//    private static final String TAG = "CameraFrameRenderer";
//    private static final int CAMERA_2_YUV_FORMAT = ImageFormat.YUV_420_888;
//
//    private final RenderScript rs;
//    private final Handler renderHandler;
//
//    private final Allocation yuvIoInputAlloc;
//    private final Allocation rgbInputAlloc;
//    private final Allocation rgbIoOutputAlloc;
//
//    private final ScriptIntrinsicYuvToRGB yuvToRgbConvertScript;
//
//    private final Stats stats = new Stats();
//
//    private int numPendingFrames;
//    private RsRenderer rsRendererImplementation;
//    private RsBitmapRenderer rsBitmapRenderer;
//
//    /**
//     * Create a new CameraFrameRenderer to handle camera frames in a background thread, apply
//     * edits using the current {@link RsRenderer} instance, and draw to the screen.
//     *
//     * @param rs            RenderScript context
//     * @param x             width of the image data in pixels
//     * @param y             height of the image data in pixels
//     * @param renderHandler handler to a background thread to queue and do work on
//     */
//    public CameraFrameRenderer(RenderScript rs, int x, int y, Handler renderHandler) {
//        this.rs = rs;
//        this.renderHandler = renderHandler;
//
//        // the allocation that gets camera frames in YUV
//        yuvIoInputAlloc = RsUtil.createYuvIoInputAlloc(rs, x, y, CAMERA_2_YUV_FORMAT);
//        yuvIoInputAlloc.setOnBufferAvailableListener(this);
//
//        // the allocation that gets the camera frames after converting to RGB
//        rgbInputAlloc = RsUtil.createRgbAlloc(rs, x, y);
//
//        // set up the yuv converter that runs first on each frame
//        yuvToRgbConvertScript = ScriptIntrinsicYuvToRGB.create(rs, Element.RGBA_8888(rs));
//        yuvToRgbConvertScript.setInput(yuvIoInputAlloc);
//
//        // the allocation that gets the edited frame and sends it to the view
//        rgbIoOutputAlloc = RsUtil.createRgbIoOutputAlloc(rs, x, y);
//    }
//
//    @Override
//    public void setOutputSurface(Surface surface) {
//        rgbIoOutputAlloc.setSurface(surface);
//    }
//
//    @Override
//    public Surface getInputSurface() {
//        return yuvIoInputAlloc.getSurface();
//    }
//
//    @Override
//    public void setRsRenderer(com.lydiaschiff.hella.RsRenderer rsRenderer) {
//
//    }
//
//    @Override
//    public boolean isRunning() {
//        return ;
//    }
//
//    @Override
//    public void shutdown() {}
//
//    public void setRsRendererImplementation(final RsRenderer rsRenderer) {
//        renderHandler.post(() -> {
//            synchronized (CameraFrameRenderer.this) {
//                Log.i(TAG, "Switching to renderer: " + rsRenderer.getName());
//                rsRendererImplementation = rsRenderer;
//            }
//        });
//    }
//
//    @Override
//    public void onBufferAvailable(Allocation allocation) {
//        synchronized (this) {
//            numPendingFrames++;
//            renderHandler.post(this);
//        }
//    }
//
//    @Override
//    public void run() {
//        int nFrames;
//        synchronized (this) {
//            nFrames = numPendingFrames;
//            numPendingFrames = 0;
//            renderHandler.removeCallbacks(this);
//        }
//
//        stats.logPendingFrameCount(nFrames);
//
//        // latch the most recently queued frame
//        for (int i = 0; i < nFrames; i++) {
//            yuvIoInputAlloc.ioReceive();
//        }
//
//        // convert yuv to rgb
//        yuvToRgbConvertScript.forEach(rgbInputAlloc);
//
//        // render the input rgb alloc to the output rgb alloc
//        // this is where we apply our filter!
//        rsRendererImplementation.renderFrame(rs, rgbInputAlloc, rgbIoOutputAlloc);
//
//        // send the frame to the screen
//        rgbIoOutputAlloc.ioSend();
//    }
//
//    private static class Stats {
//        private long startTimeMs;
//        private int numFramesTotal;
//        private int numDroppedFramesTotal;
//
//        void logPendingFrameCount(int nFrames) {
//            numFramesTotal++;
//            if (startTimeMs == 0) {
//                startTimeMs = System.currentTimeMillis();
//            }
//            if (nFrames > 1) {
//                int droppedFrames = nFrames - 1;
//                numDroppedFramesTotal += droppedFrames;
//                Log.d(TAG,
//                        "falling behind, dropping " + droppedFrames + " frames [" + numFramesTotal +
//                                "/ " + numDroppedFramesTotal + " total]");
//            }
//        }
//    }
//}
