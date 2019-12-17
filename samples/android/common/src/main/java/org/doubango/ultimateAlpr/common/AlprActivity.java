/*
 * Copyright (C) 2016-2019 Doubango AI <https://www.doubango.org>
 * License: For non-commercial use only
 * Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
 * WebSite: https://www.doubango.org/webapps/alpr/
 */
package org.doubango.ultimateAlpr.common;

import android.graphics.RectF;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.doubango.ultimateAlpr.Sdk.ULTALPR_SDK_IMAGE_TYPE;
import org.doubango.ultimateAlpr.Sdk.UltAlprSdkEngine;
import org.doubango.ultimateAlpr.Sdk.UltAlprSdkParallelDeliveryCallback;
import org.doubango.ultimateAlpr.Sdk.UltAlprSdkResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

/**
 * Base activity to subclass to make our life easier
 */
public abstract class AlprActivity extends AppCompatActivity implements AlprCameraFragment.AlprCameraFragmentSink {

    static final String TAG = AlprActivity.class.getCanonicalName();

    private String mDebugInternalDataPath = null;

    private boolean mIsProcessing = false;

    /**
     * Parallel callback delivery function used by the engine to notify for new deferred results
     */
    static class MyUltAlprSdkParallelDeliveryCallback extends UltAlprSdkParallelDeliveryCallback {
        static final String TAG = MyUltAlprSdkParallelDeliveryCallback.class.getCanonicalName();

        AlprPlateView mAlprPlateView;
        Size mImageSize;

        void setAlprPlateView(@NonNull final AlprPlateView view) {
            mAlprPlateView = view;
        }

        void setImageSize(@NonNull final Size imageSize) {
            mImageSize = imageSize;
        }

        @Override
        public void onNewResult(UltAlprSdkResult result) {
            Log.d(TAG, AlprUtils.resultToString(result));
            if (mAlprPlateView!= null) {
                mAlprPlateView.setResult(result, mImageSize);
            }
        }
        static MyUltAlprSdkParallelDeliveryCallback newInstance() {
            return new MyUltAlprSdkParallelDeliveryCallback();
        }
    }

    /**
     * The parallel delivery callback. Set to null to disable parallel mode
     * and enforce sequential mode.
     */
    private MyUltAlprSdkParallelDeliveryCallback mParallelDeliveryCallback;

    private AlprPlateView mAlprPlateView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Log.i(TAG, "onCreate " + this);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(getLayoutResId());

        // Create folder to dump input images for debugging
        File dummyFile = new File(getExternalFilesDir(null), "dummyFile");
        if (!dummyFile.getParentFile().exists() && !dummyFile.getParentFile().mkdirs()) {
            Log.e(TAG,"mkdir failed: " + dummyFile.getParentFile().getAbsolutePath());
        }
        mDebugInternalDataPath = dummyFile.getParentFile().exists() ? dummyFile.getParent() : Environment.getExternalStorageDirectory().getAbsolutePath();
        dummyFile.delete();

        // Create parallel delivery callback is enabled
        mParallelDeliveryCallback = isParallelDeliveryEnabled() ? MyUltAlprSdkParallelDeliveryCallback.newInstance() : null;

        // Init the engine
        final boolean isActivationPossible = !getActivationServerUrl().isEmpty() && !getActivationMasterOrSlaveKey().isEmpty();
        final JSONObject config = getJsonConfig();
        String tokenFile = "";
        if (isActivationPossible) {
            // Retrieve previously stored key from internal storage
            tokenFile = AlprLicenseActivator.tokenFile(this);
            if (!tokenFile.isEmpty()) {
                try {
                    config.put("license_token_data", AlprLicenseActivator.tokenData(tokenFile));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        final UltAlprSdkResult alprResult = AlprUtils.assertIsOk(UltAlprSdkEngine.init(
                getAssets(),
                config.toString(),
                mParallelDeliveryCallback
        ));
        Log.i(TAG,"ALPR engine initialized: " + AlprUtils.resultToString(alprResult));

        // Activate the license
        if (isActivationPossible && tokenFile.isEmpty()) {
            // Generate the license key and store it to the internal storage for next times
            tokenFile = AlprLicenseActivator.activate(this, getActivationServerUrl(), getActivationMasterOrSlaveKey(), false);
            if (!tokenFile.isEmpty()) {
                try {
                    config.put("license_token_data", AlprLicenseActivator.tokenData(tokenFile));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                AlprUtils.assertIsOk(UltAlprSdkEngine.init(
                        getAssets(),
                        config.toString(),
                        mParallelDeliveryCallback
                ));
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy " + this);
        // DeInitialize the engine. This will stop all threads and cleanup all pending calls.
        // If you're performing a work in a parallel callback thread, then this function will
        // block until the end.
        final UltAlprSdkResult result = AlprUtils.assertIsOk(UltAlprSdkEngine.deInit());
        Log.i(TAG,"ALPR engine deInitialized: " + AlprUtils.resultToString(result));

        super.onDestroy();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

    }

    @Override
    public synchronized void onPause() {

        super.onPause();
    }

    @Override
    public void setAlprPlateView(@NonNull final AlprPlateView view) {
        mAlprPlateView = view;
        if (mParallelDeliveryCallback != null) {
            mParallelDeliveryCallback.setAlprPlateView(view);
        }
        final List<Float> roi = getDetectROI();
        assert(roi.size() == 4);
        mAlprPlateView.setDetectROI(
                new RectF(
                        roi.get(0).floatValue(),
                        roi.get(2).floatValue(),
                        roi.get(1).floatValue(),
                        roi.get(3).floatValue()
                )
        );
    }

    @Override
    public void setImage(@NonNull final Image image) {

        // On sequential mode we just ignore the processing
        if (mIsProcessing) {
            Log.d(TAG, "Inference function not returned yet");
            image.close();
            return;
        }

        mIsProcessing = true;

        final Size imageSize = new Size(image.getWidth(), image.getHeight());

        // Update image for the async callback
        if (mParallelDeliveryCallback != null) {
            mParallelDeliveryCallback.setImageSize(imageSize);
        }

        // The actual ALPR inference is done here
        // Do not worry about the time taken to perform the inference, the caller
        // (most likely the camera fragment) set the current image using a background thread.
        final Image.Plane[] planes = image.getPlanes();
        final long startTimeInMillis = SystemClock.uptimeMillis();
        final UltAlprSdkResult result = /*AlprUtils.assertIsOk*/(UltAlprSdkEngine.process(
                ULTALPR_SDK_IMAGE_TYPE.ULTALPR_SDK_IMAGE_TYPE_YUV420P,
                planes[0].getBuffer(),
                planes[1].getBuffer(),
                planes[2].getBuffer(),
                imageSize.getWidth(),
                imageSize.getHeight(),
                planes[0].getRowStride(),
                planes[1].getRowStride(),
                planes[2].getRowStride(),
                planes[1].getPixelStride()
        ));
        final long durationInMillis = SystemClock.uptimeMillis() - startTimeInMillis;
        //Log.d(TAG, "inferenceTimeInMillis: " + durationInMillis);
        if (mAlprPlateView != null) {
            mAlprPlateView.setInferenceTime(durationInMillis);
        }

        // Release the image and signal the inference process is finished
        image.close();

        mIsProcessing = false;

        if (result.isOK()) {
            Log.d(TAG, AlprUtils.resultToString(result));
        } else {
            Log.e(TAG, AlprUtils.resultToString(result));
        }

        // Display the result if sequential mode. Otherwise, let the parallel callback
        // display the result when provided.
        if (mAlprPlateView != null && (mParallelDeliveryCallback == null || result.numPlates() == 0)) { // means sequential call or no plates to expect from the parallel delivery callback
            mAlprPlateView.setResult(result, imageSize);
        }
    }

    /**
     * Gets the base folder defining a path where the application can write private
     * data.
     * @return The path
     */
    protected String getDebugInternalDataPath() {
        return mDebugInternalDataPath;
    }

    /**
     * Gets the server url used to activate the license. Please contact us to get the correct URL.
     * e.g. https://localhost:3600
     * @return The URL
     */
    protected String getActivationServerUrl() {
        return "";
    }

    /**
     * Gets the master or slave key to use for the activation.
     * You MUST NEVER include your master key in the code or share it with the end user.
     * The master key should be used to generate slaves (one-time activation keys).
     * More information about master/slave keys at https://www.doubango.org/SDKs/LicenseManager/docs/Jargon.html.
     * @return The master of slave key.
     */
    protected String getActivationMasterOrSlaveKey() {
        return "";
    }

    /**
     * Returns the layout Id for the activity
     * @return
     */
    protected abstract int getLayoutResId();

    /**
     * Returns JSON config to be used to initialize the ALPR/ANPR SDK.
     * @return The JSON config
     */
    protected abstract JSONObject getJsonConfig();

    /**
     */
    protected abstract boolean isParallelDeliveryEnabled();

    protected abstract List<Float> getDetectROI();
}