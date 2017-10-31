package com.lydiaschiff.hellaparallel.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

import com.lydiaschiff.hella.RsSurfaceRenderer;
import com.lydiaschiff.hellaparallel.BuildConfig;
import com.lydiaschiff.hellaparallel.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Base activity for streaming frames from the camera.
 * Mostly the camera-related code copied from android hrd-viewfinder example app.
 * https://github.com/googlesamples/android-HdrViewfinder
 */
@RequiresApi(21)
public abstract class BaseViewfinderActivity extends AppCompatActivity
        implements CameraOps.ErrorDisplayer, CameraOps.CameraReadyListener, SurfaceHolder.Callback {

    private static final String TAG = "BaseViewfinderActivity";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final String FRAGMENT_DIALOG = "dialog";

    protected CameraManager mCameraManager;
    protected Handler mUiHandler;
    protected CameraOps mCameraOps;
    private CameraCharacteristics mCameraInfo;
    private Surface mPreviewSurface;
    private Surface mProcessingNormalSurface;

    // This is set by the subclass
    protected RsSurfaceRenderer mRenderer;

    protected abstract View getRootView();

    protected abstract RsSurfaceRenderer createNewRendererForCurrentType(Size outputSize);

    protected abstract FixedAspectSurfaceView getViewfinderSurfaceView();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        mUiHandler = new Handler(Looper.getMainLooper());

        // set callback for configuring the view surface
        getViewfinderSurfaceView().getHolder().addCallback(this);
    }

    /**
     * Callbacks for CameraOps {@link CameraOps.CameraReadyListener}.
     */
    @Override
    public void onCameraReady() {
        // Ready to send requests in, so set them up
        try {
            CaptureRequest.Builder previewBuilder =
                    mCameraOps.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewBuilder.addTarget(mProcessingNormalSurface);
            mCameraOps.setRepeatingRequest(previewBuilder.build(), null, mUiHandler);

        } catch (CameraAccessException e) {
            String errorMessage = getErrorString(e);
            showErrorDialog(errorMessage);
        }
    }

    protected void requestCameraPermissions() {
        boolean shouldProvideRationale = ActivityCompat
                .shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying camera permission rationale to provide additional context.");
            Snackbar.make(getRootView(), R.string.camera_permission_rationale, Snackbar
                    .LENGTH_INDEFINITE)
                    .setAction(R.string.ok, view -> {
                        // Request Camera permission
                        ActivityCompat
                                .requestPermissions(BaseViewfinderActivity.this, new String[]{
                                        Manifest.permission.CAMERA },
                                        REQUEST_PERMISSIONS_REQUEST_CODE);
                    }).show();
        } else {
            Log.i(TAG, "Requesting camera permission");
            // Request Camera permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat
                    .requestPermissions(BaseViewfinderActivity.this, new String[]{ Manifest
                            .permission.CAMERA }, REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // When permissions are revoked the app is restarted so onCreate is sufficient to check for
        // permissions core to the Activity's functionality.
        if (!checkCameraPermissions()) {
            requestCameraPermissions();
        } else {
            findAndOpenCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRenderer != null) {
            mRenderer.shutdown();
        }
        // Wait until camera is closed to ensure the next application can open it
        if (mCameraOps != null) {
            mCameraOps.closeCameraAndWait();
            mCameraOps = null;
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                findAndOpenCamera();
            } else {
                // Permission denied.

                // In this Activity we've chosen to notify the user that they
                // have rejected a core permission for the app since it makes the Activity useless.
                // We're communicating this message in a Snackbar since this is a sample app, but
                // core permissions would typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                Snackbar.make(getRootView(), R.string.camera_permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, view -> {
                            // Build intent that displays the App settings screen.
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }).show();
            }
        }
    }

    private void findAndOpenCamera() {
        boolean cameraPermissions = checkCameraPermissions();
        if (cameraPermissions) {
            String errorMessage = "Unknown error";
            boolean foundCamera = false;
            initializeCamera();
            if (cameraPermissions && mCameraOps != null) {
                try {
                    // Find first back-facing camera that has necessary capability.
                    String[] cameraIds = mCameraManager.getCameraIdList();
                    for (String id : cameraIds) {
                        CameraCharacteristics info = mCameraManager.getCameraCharacteristics(id);
                        int facing = info.get(CameraCharacteristics.LENS_FACING);

                        int level = info.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                        boolean hasFullLevel =
                                (level == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);

                        int[] capabilities =
                                info.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
                        int syncLatency = info.get(CameraCharacteristics.SYNC_MAX_LATENCY);
                        boolean hasManualControl =
                                hasCapability(capabilities, CameraCharacteristics
                                        .REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR);
                        boolean hasEnoughCapability = hasManualControl && syncLatency ==
                                CameraCharacteristics.SYNC_MAX_LATENCY_PER_FRAME_CONTROL;

                        // All these are guaranteed by
                        // CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL, but checking
                        // for only the things we care about expands range of devices we can run on.
                        // We want:
                        //  - Back-facing camera
                        //  - Manual sensor control
                        //  - Per-frame synchronization (so that exposure can be changed every
                        // frame)
                        if (facing == CameraCharacteristics.LENS_FACING_BACK &&
                                (hasFullLevel || hasEnoughCapability)) {
                            // Found suitable camera - get info, open, and set up outputs
                            mCameraInfo = info;
                            mCameraOps.openCamera(id);
                            configureSurfaces();
                            foundCamera = true;
                            break;
                        }
                    }
                    if (!foundCamera) {
                        errorMessage = getString(R.string.camera_no_good);
                    }
                } catch (CameraAccessException e) {
                    errorMessage = getErrorString(e);
                }
                if (!foundCamera) {
                    showErrorDialog(errorMessage);
                }
            }
        }
    }

    /**
     * Once camera is open and output surfaces are ready, configure the RS processing
     * and the camera device inputs/outputs.
     */
    private void setupProcessing(Size outputSize) {
        if (mRenderer == null) {
            mRenderer = createNewRendererForCurrentType(outputSize);
        }
        if (mPreviewSurface == null)
            return;

        mRenderer.setOutputSurface(mPreviewSurface);
        mProcessingNormalSurface = mRenderer.getInputSurface();
        List<Surface> cameraOutputSurfaces = new ArrayList<>();
        cameraOutputSurfaces.add(mProcessingNormalSurface);
        mCameraOps.setSurfaces(cameraOutputSurfaces);
    }


    /**
     * Return the current state of the camera permissions.
     */
    private boolean checkCameraPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        // Check if the Camera permission is already available.
        if (permissionState != PackageManager.PERMISSION_GRANTED) {
            // Camera permission has not been granted.
            Log.i(TAG, "CAMERA permission has NOT been granted.");
            return false;
        } else {
            // Camera permissions are available.
            Log.i(TAG, "CAMERA permission has already been granted.");
            return true;
        }
    }

    /**
     * Attempt to initialize the camera.
     */
    private void initializeCamera() {
        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        if (mCameraManager != null) {
            mCameraOps = new CameraOps(mCameraManager,
                /*errorDisplayer*/ this,
                /*readyListener*/ this,
                /*readyHandler*/ mUiHandler);
        } else {
            Log.e(TAG, "Couldn't initialize the camera");
        }
    }

    /**
     * Configure the surfaceview and RS processing.
     */
    private void configureSurfaces() {
        // Find a good size for output - largest 16:9 aspect ratio that's less than 720p
        final int MAX_WIDTH = 1280;
        final float TARGET_ASPECT = 16.f / 9.f;
        final float ASPECT_TOLERANCE = 0.1f;

        StreamConfigurationMap configs =
                mCameraInfo.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        Size[] outputSizes = configs.getOutputSizes(SurfaceHolder.class);

        Size outputSize = outputSizes[0];
        float outputAspect = (float) outputSize.getWidth() / outputSize.getHeight();
        for (Size candidateSize : outputSizes) {
            if (candidateSize.getWidth() > MAX_WIDTH)
                continue;
            float candidateAspect = (float) candidateSize.getWidth() / candidateSize.getHeight();
            boolean goodCandidateAspect =
                    Math.abs(candidateAspect - TARGET_ASPECT) < ASPECT_TOLERANCE;
            boolean goodOutputAspect = Math.abs(outputAspect - TARGET_ASPECT) < ASPECT_TOLERANCE;
            if ((goodCandidateAspect && !goodOutputAspect) ||
                    candidateSize.getWidth() > outputSize.getWidth()) {
                outputSize = candidateSize;
                outputAspect = candidateAspect;
            }
        }
        Log.i(TAG, "Resolution chosen: " + outputSize);

        setupProcessing(outputSize);

        // this will trigger onSurfaceChanged()
        getViewfinderSurfaceView().getHolder()
                .setFixedSize(outputSize.getWidth(), outputSize.getHeight());
        getViewfinderSurfaceView().setAspectRatio(outputAspect);
    }

    private static boolean hasCapability(int[] capabilities, int capability) {
        for (int c : capabilities) {
            if (c == capability)
                return true;
        }
        return false;
    }

    /**
     * Callbacks for the FixedAspectSurfaceView
     */

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mPreviewSurface = holder.getSurface();
        configureSurfaces();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // ignored
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mPreviewSurface = null;
    }

    /**
     * Utility methods
     */
    @Override
    public void showErrorDialog(String errorMessage) {
        MessageDialogFragment.newInstance(errorMessage).show(getFragmentManager(), FRAGMENT_DIALOG);
    }

    @SuppressLint("StringFormatMatches")
    @Override
    public String getErrorString(CameraAccessException e) {
        String errorMessage;
        switch (e.getReason()) {
            case CameraAccessException.CAMERA_DISABLED:
                errorMessage = getString(R.string.camera_disabled);
                break;
            case CameraAccessException.CAMERA_DISCONNECTED:
                errorMessage = getString(R.string.camera_disconnected);
                break;
            case CameraAccessException.CAMERA_ERROR:
                errorMessage = getString(R.string.camera_error);
                break;
            default:
                errorMessage = getString(R.string.camera_unknown, e.getReason());
                break;
        }
        return errorMessage;
    }
}
