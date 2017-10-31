package com.lydiaschiff.hellaparallel;

import android.os.Bundle;
import android.renderscript.RenderScript;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Size;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

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

@RequiresApi(21)
public class RsViewfinderActivity extends BaseViewfinderActivity {

    private FixedAspectSurfaceView viewfinderSurfaceView;
    private View rootView;

    private RenderScript rs;
    private RsCameraPreviewRenderer cameraPreviewRenderer;
    private Toast rendererNameToast;
    private String rendererName;

    private int currentRendererIndex = 0;

    private static List<Class<? extends RsRenderer>> rendererTypes;

    static {
        rendererTypes = new ArrayList<>();
        rendererTypes.add(DefaultRsRenderer.class);
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
        rootView = findViewById(R.id.panels);
        viewfinderSurfaceView = findViewById(R.id.preview);
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
    }

    private void cycleRendererType() {
        currentRendererIndex++;
        if (currentRendererIndex == rendererTypes.size()) {
            currentRendererIndex = 0;
        }
    }

    private void updateRsRenderer() {
        try {
            RsRenderer renderer = rendererTypes.get(currentRendererIndex).newInstance();
            rendererName = renderer.getName();
            cameraPreviewRenderer.setRsRenderer(renderer);

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
        }
        updateRsRenderer();
        return cameraPreviewRenderer;
    }


    @Override
    protected FixedAspectSurfaceView getViewfinderSurfaceView() {
        return viewfinderSurfaceView;
    }
}
