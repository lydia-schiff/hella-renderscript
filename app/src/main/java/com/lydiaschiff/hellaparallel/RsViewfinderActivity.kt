package com.lydiaschiff.hellaparallel

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.renderscript.RenderScript
import android.util.Log
import android.util.Size
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import cat.the.lydia.coolalgebralydiathanks.implementation.RsLut3dRenderer
import cat.the.lydia.coolalgebralydiathanks.utils.CubeFileParser
import com.lydiaschiff.hella.FrameStats
import com.lydiaschiff.hella.Hella
import com.lydiaschiff.hella.RsRenderer
import com.lydiaschiff.hella.RsSurfaceRenderer
import com.lydiaschiff.hella.renderer.BlurRsRenderer
import com.lydiaschiff.hella.renderer.DefaultRsRenderer
import com.lydiaschiff.hella.renderer.GreyscaleRsRenderer
import com.lydiaschiff.hella.renderer.SharpenRenderer
import com.lydiaschiff.hellaparallel.camera.BaseViewfinderActivity
import com.lydiaschiff.hellaparallel.camera.FixedAspectSurfaceView
import com.lydiaschiff.hellaparallel.renderers.AcidRenderer
import com.lydiaschiff.hellaparallel.renderers.ColorFrameRenderer
import com.lydiaschiff.hellaparallel.renderers.HueRotationRenderer
import com.lydiaschiff.hellaparallel.renderers.TrailsRenderer
import java.util.*
import kotlin.math.roundToLong

@RequiresApi(21)
class RsViewfinderActivity : BaseViewfinderActivity() {
    private lateinit var viewfinderSurfaceView: FixedAspectSurfaceView
    private lateinit var rootView: View
    private lateinit var textView: TextView
    private lateinit var rs: RenderScript
    private var cameraPreviewRenderer: RsCameraPreviewRenderer? = null
    private lateinit var frameStats: FrameLogger
    private var rendererNameToast: Toast? = null
    private lateinit var rendererName: String
    private var currentRendererIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        frameStats = FrameLogger()
        rootView = findViewById(R.id.panels)
        viewfinderSurfaceView = findViewById(R.id.preview)
        textView = findViewById(R.id.odd_exposure_label)
        findViewById<View>(R.id.next_button).setOnClickListener { view: View ->
            cycleRendererType()
            updateRsRenderer()
            aToast() // !
        }
        rs = RenderScript.create(this)
        Hella.warmUpInBackground(rs)
    }

    fun aToast() { // to you!
        rendererNameToast?.cancel()
        rendererNameToast = Toast.makeText(this, rendererName, Toast.LENGTH_LONG)
                .apply { show() }
    }

    override fun onResume() {
        super.onResume()
        frameStats.clear()
    }

    private fun cycleRendererType() {
        currentRendererIndex++
        if (currentRendererIndex == rendererTypes.size) {
            currentRendererIndex = 0
        }
    }

    private fun updateText(text: String) {
        textView.text = text
    }

    private fun updateRsRenderer() =
            try {
                val renderer = rendererTypes[currentRendererIndex].newInstance()
                // todo: hack!
                if (currentRendererIndex == 2) {
                    (renderer as RsLut3dRenderer).setColorCube(CubeFileParser.loadCubeResource(this, R.raw.fg_cine_drama_17))
                }
                rendererName = renderer.name
                cameraPreviewRenderer?.setRsRenderer(renderer)
                frameStats.clear()
            } catch (e: InstantiationException) {
                throw RuntimeException(
                        "Unable to create renderer for index " + currentRendererIndex +
                                ", make sure it has a no-arg constructor please.", e)
            } catch (e: IllegalAccessException) {
                throw RuntimeException(
                        "Unable to create renderer for index " + currentRendererIndex +
                                ", make sure it has a no-arg constructor please.", e)
            }


    override fun getRootView(): View = rootView

    public override fun createNewRendererForCurrentType(outputSize: Size): RsSurfaceRenderer {
        if (cameraPreviewRenderer == null) {
            cameraPreviewRenderer = RsCameraPreviewRenderer(rs, outputSize.width, outputSize.height)
            cameraPreviewRenderer!!.setDroppedFrameLogger(frameStats)
        }
        updateRsRenderer()
        return cameraPreviewRenderer!!
    }

    override fun getViewfinderSurfaceView(): FixedAspectSurfaceView = viewfinderSurfaceView

    private inner class FrameLogger : FrameStats {
        private val uiHandler = Handler(Looper.getMainLooper())

        @Volatile
        private var start: Long = 0
        override fun logFrame(tag: String, nDropped: Int, totalDropped: Int, total: Int) {
            if (start == 0L) {
                start = System.currentTimeMillis()
            }
            if (total % 10 == 9 || nDropped > 0) {
                val avgDroppedFrames = totalDropped / total.toFloat()
                val elapsed = System.currentTimeMillis() - start
                val fps = (total * 1000 / elapsed.toFloat()).roundToLong()
                if (nDropped > 0) {
                    logDropped(tag, nDropped, avgDroppedFrames, fps.toFloat())
                }
                updateUi(avgDroppedFrames, fps.toFloat())
            }
        }

        override fun clear() {
            start = 0
            updateText("avg dropped frames: 0")
        }

        private fun logDropped(tag: String, nDropped: Int, avgDroppedFrames: Float, fps: Float) {
            Log.d(tag, String.format(
                    "renderer is falling behind, dropping %d frame%s (avg %.2f)",
                    nDropped, if (nDropped > 1) "s" else "", avgDroppedFrames) + "fps = " + fps)
        }

        private fun updateUi(avgDroppedFrames: Float, fps: Float) {
            uiHandler.post {
                updateText("avg dropped frames: " +
                        "${Math.round(avgDroppedFrames * 100) / 100f} fps = $fps")
            }
        }
    }

    companion object {
        private const val TAG = "RsViewfinderActivity"
        private val rendererTypes: List<Class<out RsRenderer>> =
                listOf(
                        DefaultRsRenderer::class.java,
                        RsLut3dRenderer::class.java,
                        RsLut3dRenderer::class.java,
                        GreyscaleRsRenderer::class.java,
                        SharpenRenderer::class.java,
                        BlurRsRenderer::class.java,
                        ColorFrameRenderer::class.java,
                        HueRotationRenderer::class.java,
                        TrailsRenderer::class.java,
                        AcidRenderer::class.java)
    }
}