package com.lydiaschiff.hellaparallel;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.renderscript.RenderScript;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.lydiaschiff.hella.FrameStats;
import com.lydiaschiff.hella.Hella;
import com.lydiaschiff.hella.RsRenderer;
import com.lydiaschiff.hella.RsSurfaceRenderer;
import com.lydiaschiff.hella.renderer.BlurRsRenderer;
import com.lydiaschiff.hella.renderer.DefaultRsRenderer;
import com.lydiaschiff.hella.renderer.GreyscaleRsRenderer;
import com.lydiaschiff.hella.renderer.SharpenRenderer;
import com.lydiaschiff.hellaparallel.camera.BaseViewfinderActivity;
import com.lydiaschiff.hellaparallel.camera.FixedAspectSurfaceView;
import com.lydiaschiff.hellaparallel.renderers.AcidRenderer;
import com.lydiaschiff.hellaparallel.renderers.ColorFrameRenderer;
import com.lydiaschiff.hellaparallel.renderers.HueRotationRenderer;
import com.lydiaschiff.hellaparallel.renderers.TrailsRenderer;

import java.util.ArrayList;
import java.util.List;

import cat.the.lydia.coolalgebralydiathanks.lut3d.Lut3dRenderer;


@RequiresApi(21)
public class RsViewfinderActivity extends BaseViewfinderActivity {
    private static final String TAG = "RsViewfinderActivity";

    private FixedAspectSurfaceView viewfinderSurfaceView;
    private View rootView;
    private TextView textView;

    private RenderScript rs;
    private RsCameraPreviewRenderer cameraPreviewRenderer;
    private FrameLogger frameStats;
    private Toast rendererNameToast;
    private String rendererName;

    private int currentRendererIndex = 0;

    private static List<Class<? extends RsRenderer>> rendererTypes;

    static {
        rendererTypes = new ArrayList<>();
        rendererTypes.add(DefaultRsRenderer.class);
        rendererTypes.add(Lut3dRenderer.class);
        rendererTypes.add(GreyscaleRsRenderer.class);
        rendererTypes.add(SharpenRenderer.class);
        rendererTypes.add(BlurRsRenderer.class);
        rendererTypes.add(ColorFrameRenderer.class);
        rendererTypes.add(HueRotationRenderer.class);
        rendererTypes.add(TrailsRenderer.class);
        rendererTypes.add(AcidRenderer.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        frameStats = new FrameLogger();

        rootView = findViewById(R.id.panels);
        viewfinderSurfaceView = findViewById(R.id.preview);
        textView = findViewById(R.id.odd_exposure_label);

        findViewById(R.id.next_button).setOnClickListener(view -> {
            cycleRendererType();
            updateRsRenderer();
            if (rendererNameToast != null) {
                rendererNameToast.cancel();
            }
            rendererNameToast =
                    Toast.makeText(RsViewfinderActivity.this, rendererName, Toast.LENGTH_LONG);
            rendererNameToast.show();
        });

        rs = RenderScript.create(this);
        Hella.warmUpInBackground(rs);
    }

    @Override
    protected void onResume() {
        super.onResume();
        frameStats.clear();
    }

    private void cycleRendererType() {
        currentRendererIndex++;
        if (currentRendererIndex == rendererTypes.size()) {
            currentRendererIndex = 0;
        }
    }

    private void updateText(String text) {
        textView.setText(text);
    }

    private void updateRsRenderer() {
        try {
            RsRenderer renderer = rendererTypes.get(currentRendererIndex).newInstance();
            rendererName = renderer.getName();
            cameraPreviewRenderer.setRsRenderer(renderer);
            frameStats.clear();

        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(
                    "Unable to create renderer for index " + currentRendererIndex +
                            ", make sure it has a no-arg constructor please.", e);
        }
    }

    @Override
    protected View getRootView() {
        return rootView;
    }

    @Override
    public RsSurfaceRenderer createNewRendererForCurrentType(Size outputSize) {
        if (cameraPreviewRenderer == null) {
            cameraPreviewRenderer =
                    new RsCameraPreviewRenderer(rs, outputSize.getWidth(), outputSize.getHeight());
            cameraPreviewRenderer.setDroppedFrameLogger(frameStats);
        }
        updateRsRenderer();
        return cameraPreviewRenderer;
    }


    @Override
    protected FixedAspectSurfaceView getViewfinderSurfaceView() {
        return viewfinderSurfaceView;
    }

    private class FrameLogger implements FrameStats {

        private final Handler uiHandler = new Handler(Looper.getMainLooper());
        private volatile long start;

        @Override
        public void logFrame(String tag, int nDropped, int totalDropped, int total) {
            if (start == 0) {
                start = System.currentTimeMillis();
            }
            if (total % 10 == 9 || nDropped > 0) {
                float avgDroppedFrames = totalDropped / (float) total;
                long elapsed = System.currentTimeMillis() - start;
                long fps = (long) (total * 1000 / (float) elapsed);

                if (nDropped > 0) {
                    logDropped(tag, nDropped, avgDroppedFrames, fps);
                }
                updateUi(avgDroppedFrames, fps);
            }
        }

        @Override
        public void clear() {
            start = 0;
            updateText("avg dropped frames: 0");
        }

        private void logDropped(String tag, int nDropped, float avgDroppedFrames, float fps) {
            Log.d(tag, String.format(
                    "renderer is falling behind, dropping %d frame%s (avg " + "%.2f)" +
                            "", nDropped, nDropped > 1 ? "s" : "", avgDroppedFrames) + "fps = " +
                    fps);
        }

        private void updateUi(float avgDroppedFrames, float fps) {
            uiHandler.post(() -> {
                updateText("avg dropped frames: " + Math.round(avgDroppedFrames * 100) / 100.f +
                        "\nfps = " + fps);
            });
        }
    }
}
