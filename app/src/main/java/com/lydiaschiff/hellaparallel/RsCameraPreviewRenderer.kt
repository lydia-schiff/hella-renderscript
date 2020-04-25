package com.lydiaschiff.hellaparallel

import android.graphics.ImageFormat
import android.os.Handler
import android.os.HandlerThread
import android.renderscript.Allocation
import android.renderscript.Allocation.OnBufferAvailableListener
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicYuvToRGB
import android.util.Log
import android.view.Surface
import androidx.annotation.AnyThread
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import com.lydiaschiff.hella.FrameStats
import com.lydiaschiff.hella.RsRenderer
import com.lydiaschiff.hella.RsSurfaceRenderer
import com.lydiaschiff.hella.RsUtil
import com.lydiaschiff.hella.renderer.DefaultRsRenderer

/**
 * Created by lydia on 10/30/17.
 */
@RequiresApi(19)
class RsCameraPreviewRenderer
@JvmOverloads
constructor(
        private val rs: RenderScript,
        private var rsRenderer: RsRenderer,
        x: Int,
        y: Int,
        // guarded by "this"
        private var renderHandler: Handler? = null
) : RsSurfaceRenderer, OnBufferAvailableListener, Runnable {

    private val yuvInAlloc: Allocation =
            RsUtil.createYuvIoInputAlloc(rs, x, y, ImageFormat.YUV_420_888).also {
                it.setOnBufferAvailableListener(this)
            }
    private val rgbInAlloc: Allocation = RsUtil.createRgbAlloc(rs, x, y)
    private val rgbOutAlloc: Allocation = RsUtil.createRgbIoOutputAlloc(rs, x, y)
    private val yuvToRGBScript = ScriptIntrinsicYuvToRGB.create(rs, Element.RGBA_8888(rs)).apply {
        setInput(yuvInAlloc)
    }

    // all vars guarded by "this"
    private var renderThread: HandlerThread? =
            when (renderHandler) {
                null -> HandlerThread(TAG).apply {
                    start()
                    renderHandler = Handler(looper)
                }
                else -> null
            }
    private var droppedFrameLogger: FrameStats? = null
    private var nFramesAvailable = 0
    private var totalFrames = 0
    private var totalDropped = 0
    private var outputSurfaceIsSet = false

    init {
        Log.i(TAG, "Setting up RsCameraPreviewRenderer with ${rsRenderer.name} ($x,$y)")
    }

    constructor(rs: RenderScript, x: Int, y: Int) : this(rs, DefaultRsRenderer(), x, y)

    @AnyThread
    @Synchronized
    override fun setRsRenderer(rsRenderer: RsRenderer) {
        if (isRunning) {
            this.rsRenderer = rsRenderer
            Log.i(TAG, "updating RsRenderer to \"" + rsRenderer.name + "\"")
            totalFrames = 0
            totalDropped = 0
            if (droppedFrameLogger != null) {
                droppedFrameLogger!!.clear()
            }
        }
    }

    @Synchronized
    fun setDroppedFrameLogger(droppedFrameLogger: FrameStats) {
        this.droppedFrameLogger = droppedFrameLogger
    }

    /**
     * Check if this renderer is still running or has been shutdown.
     *
     * @return true if we're running, else false
     */
    @get:Synchronized
    @get:AnyThread
    override val isRunning: Boolean
        get() {
            if (renderHandler == null) {
                Log.w(TAG, "renderer was already shut down")
                return false
            }
            return true
        }

    /**
     * Set the output surface to consume the stream of edited camera frames. This is probably
     * from a SurfaceView or TextureView. Please make sure it's valid.
     *
     * @param surface a valid surface to consume a stream of edited frames from the camera
     */
    @AnyThread
    @Synchronized
    override fun setOutputSurface(surface: Surface) {
        if (isRunning) {
            require(surface.isValid) { "output was invalid" }
            rgbOutAlloc.surface = surface
            outputSurfaceIsSet = true
            Log.d(TAG, "output surface was set")
        }
    }

    /**
     * Get the Surface that the camera will push frames to. This is the Surface from our yuv
     * input allocation. It will recieve a callback when a frame is available from the camera.
     *
     * @return a surface that consumes yuv frames from the camera preview, or null renderer is
     * shutdown
     */
    @get:Synchronized
    @get:AnyThread
    override val inputSurface: Surface?
        get() = if (isRunning) yuvInAlloc.surface else null

    /**
     * Callback for when the camera has a new frame. We want to handle this on the render thread
     * specific thread, so we'll increment nFramesAvailable and post a render request.
     */
    @Synchronized
    override fun onBufferAvailable(a: Allocation) {
        if (isRunning) {
            if (!outputSurfaceIsSet) {
                Log.e(TAG, "We are getting frames from the camera but we never set the view " +
                        "surface to render to")
                return
            }
            nFramesAvailable++
            renderHandler!!.post(this)
        }
    }

    /**
     * Render a frame on the render thread. Everything is async except for ioSend() will block
     * until the rendering completes. If we wanted to time it, make sure to log the time after
     * that call.
     */
    @WorkerThread
    override fun run() {
        var renderer: RsRenderer
        var nFrames: Int
        synchronized(this) {
            if (!isRunning) {
                return
            }
            renderer = rsRenderer
            nFrames = nFramesAvailable
            nFramesAvailable = 0
            logFrames(nFrames)
            renderHandler!!.removeCallbacks(this)
        }
        for (i in 0 until nFrames) {
            yuvInAlloc.ioReceive()
        }
        yuvToRGBScript.forEach(rgbInAlloc)
        renderer.renderFrame(rs, rgbInAlloc, rgbOutAlloc)
        rgbOutAlloc.ioSend()
    }

    private fun logFrames(nFrames: Int) {
        if (droppedFrameLogger != null) {
            totalFrames++
            val droppedFrames = nFrames - 1
            totalDropped += droppedFrames
            droppedFrameLogger!!.logFrame(TAG, droppedFrames, totalDropped, totalFrames)
        }
    }

    /**
     * Shut down the renderer when you're finished.
     */
    @AnyThread
    override fun shutdown() {
        synchronized(this) {
            if (!isRunning) {
                Log.d(TAG, "requesting shutdown...")
                renderHandler!!.removeCallbacks(this)
                renderHandler!!.postAtFrontOfQueue {
                    Log.i(TAG, "shutting down")
                    synchronized(this) {
                        droppedFrameLogger = null
                        yuvInAlloc.destroy()
                        rgbInAlloc.destroy()
                        rgbOutAlloc.destroy()
                        yuvToRGBScript.destroy()
                        renderThread?.quitSafely()
                    }
                }
                renderHandler = null
            }
        }
    }

    companion object {
        private const val TAG = "RsCameraPreviewRenderer"
    }
}