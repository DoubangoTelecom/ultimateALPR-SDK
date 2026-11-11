/*
 * Copyright (C) 2016-2019 Doubango AI <https://www.doubango.org>
 * License: For non-commercial use only
 * Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
 * WebSite: https://www.doubango.org/webapps/alpr/
 */
package org.doubango.ultimateAlpr.common;

import android.graphics.RectF;
import android.media.ExifInterface;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Base activity to subclass to make our life easier
 */
public abstract class AlprActivity extends AppCompatActivity implements AlprCameraFragment.AlprCameraFragmentSink {

    static final String TAG = AlprActivity.class.getCanonicalName();

    /**
     * Defines the debug level to output on the console. You should use "verbose" for diagnostic, "info" in development stage and "warn" on production.
     * JSON name: "debug_level"
     * Default: "info"
     * type: string
     * pattern: "verbose" | "info" | "warn" | "error" | "fatal"
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#debug-level
     */
    static final String CONFIG_DEBUG_LEVEL = "info";

    /**
     * Whether to write the transformed input image to the disk. This could be useful for debugging.
     * JSON name: "debug_write_input_image_enabled"
     * Default: false
     * type: bool
     * pattern: true | false
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#debug-write-input-image-enabled
     */
    static final boolean CONFIG_DEBUG_WRITE_INPUT_IMAGE = false; // must be false unless you're debugging the code

    /**
     * Path to the folder where to write the transformed input image. Used only if "debug_write_input_image_enabled" is true.
     * JSON name: "debug_internal_data_path"
     * Default: ""
     * type: string
     * pattern: folder path
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#debug-internal-data-path
     */
    static final String CONFIG_DEBUG_INTERNAL_DATA_PATH = ".";

    /**
     * Defines the maximum number of threads to use.
     * You should not change this value unless you know what you’re doing. Set to -1 to let the SDK choose the right value.
     * The right value the SDK will choose will likely be equal to the number of virtual core.
     * For example, on an octa-core device the maximum number of threads will be 8.
     * JSON name: "num_threads"
     * Default: -1
     * type: int
     * pattern: ]-inf, +inf[
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#num-threads
     */
    static final int CONFIG_NUM_THREADS = -1;

    /**
     * The parallel processing method could introduce delay/latency in the delivery callback on low-end CPUs.
     * This parameter controls the maximum latency you can tolerate. The unit is number of frames. The default value is -1 which means auto.
     * You should not set this entry unless you know what you're doing. You could end with very bad performance numbers.
     * * JSON name: "max_latency"
     * Default: -1
     * type: int
     * pattern: [0, +inf[
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#max-latency
     */
    static final int CONFIG_MAX_LATENCY = 9; //!\\ setting a value (different than "-1") to enforce "max_latency" and avoid large delay in videoparallel. Do NOT do it if you're not displaying the video result. In short, use "-1" for faster inference.

    /**
     * Whether to enable Image Enhancement for Night-Vision (IENV).
     * IENV is explained at https://www.doubango.org/SDKs/anpr/docs/Features.html#features-imageenhancementfornightvision.
     *
     * JSON name: "night_vision_enabled"
     * Default: false
     * type: bool
     * pattern: true | false
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#night-vision-enabled
     */
    static final boolean CONFIG_NIGHT_VISION_ENABLED = false;

    /**
     * Defines the activation type for the batching mode. |br| *auto* will let the SDK choose the appropriate value.
     * "on" will activate the batching while "off" will disable it.
     * The SDK would activate batching when the target hardware is a GPU, NPU or FPGA and deactivate the option on CPU.
     * Deactivating the batching means the parallel processing is used using CPU multi-threading.
     *
     * JSON name: "batching_mode"
     * Default: "auto"
     * type: string
     * pattern: "auto" | "on" | "off"
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#batching-mode
     */
    static final String CONFIG_BATCHING_MODE = "auto";

    /**
     * The backend string is a tuple defining the deeplearning framework to utilize and the acceleration hardware/software to activate.
     * Examples: "auto", "tflite[nnapi]", "onnx[cuda]", "vino[fpga]", "hailo[hailo8l]", "rknpu2[rk3588]", "trt"...
     * Check https://www.doubango.org/SDKs/anpr/docs/Architecture_overview.html#supported-backends for more information.
     *
     * JSON name: "backend"
     * Default: "auto"
     * type: string
     * pattern: "framework"["acceleration"]
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#backend
     */
    static final String CONFIG_BACKEND = "auto";

    /**
     * Defines the target platform. "mobile" will configure the SDK to focus on speed while "desktop" would focus on accuracy.
     * "auto" translates to "mobile" on ARM and "desktop" on x86_64.
     *
     * JSON name: "platform"
     * Default: "auto"
     * type: string
     * pattern: "auto" | "mobile" | "desktop"
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#platform
     */
    static final String CONFIG_PLATFORM = "auto";

    /**
     * Whether to enable assembler code to use SIMD acceleration (SSE, AVX, NEON).
     *
     * JSON name: "asm_enabled"
     * Default: true
     * type: bool
     * pattern: true | false
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#asm-enabled
     */
    static final boolean CONFIG_ASM_ENABLED = true;

    /**
     * Whether to enable intrinsic code to use SIMD acceleration (SSE, AVX, NEON).
     *
     * JSON name: "intrin_enabled"
     * Default: true
     * type: bool
     * pattern: true | false
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#intrin-enabled
     */
    static final boolean CONFIG_INTRIN_ENABLED = true;

    /**
     * Defines the Region Of Interest (ROI) for the detector. Any pixels outside region of interest will be ignored by the detector.
     * Defining an WxH region of interest instead of resizing the image at WxH is very important as you'll keep the same quality when you define a ROI while you'll lose in quality when using the later.
     * JSON name: "detect_roi"
     * Default: [0.f, 0.f, 0.f, 0.f]
     * type: float[4]
     * pattern: [left, right, top, bottom]
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#detect-roi
     */
    static final List<Float> CONFIG_DETECT_ROI = Arrays.asList(0.f, 0.f, 0.f, 0.f);

    /**
     * Define a threshold for the detection score. Any detection with a score below that threshold will be ignored. 0.f being poor confidence and 1.f excellent confidence.
     * JSON name: "detect_minscore",
     * Default: 0.4f
     * type: float
     * pattern: ]0.f, 1.f]
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#detect-minscore
     */
    static final double CONFIG_DETECT_MINSCORE = 0.5;

    /**
     * Defines a threshold for the detection IoU (Intersection over union) for the boxes.
     * This threshold is used in the `NMS (Non-Maximum Suppression) step. |br|
     * The value range is ]0.f, 1.f], 0.f being no overlap and 1.f perfect overlap.
     * JSON name: "detect_miniou",
     * Default: 0.4f
     * type: float
     * pattern: ]0.f, 1.f]
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#detect-miniou
     */
    static final double CONFIG_DETECT_MINIOU = 0.4;

    /**
     * Whether to enable pyramidal search. Pyramidal search is an advanced feature to accurately detect very small or far away license plates.
     * JSON name: "pyramidal_search_enabled"
     * Default: true
     * type: bool
     * pattern: true | false
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#pyramidal-search-enabled
     */
    static final boolean CONFIG_PYRAMIDAL_SEARCH_ENABLED = true;

    /**
     * Defines a threshold for the detection score associated to the plates retrieved after pyramidal search.
     * Any detection with a score below that threshold will be ignored.
     * 0.f being poor confidence and 1.f excellent confidence.
     * JSON name: "pyramidal_search_minscore"
     * Default: 0.3f
     * type: float
     * pattern: ]0.f, 1.f]
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#pyramidal-search-minscore
     */
    static final double CONFIG_PYRAMIDAL_SEARCH_MINSCORE = 0.3; // 30%

    /**
     * Minimum image size (max[width, height]) in pixels to trigger pyramidal search.
     * Pyramidal search will be disabled if the image size is less than this value. Using pyramidal search on small images is useless.
     * JSON name: "pyramidal_search_min_image_size_inpixels"
     * Default: 800
     * type: integer
     * pattern: [0, inf[
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#pyramidal-search-min-image-size-inpixels
     */
    static final int CONFIG_PYRAMIDAL_SEARCH_MIN_IMAGE_SIZE_INPIXELS = 600; // pixels

    /**
     * Defines a threshold for the pyramidal search detection IoU (Intersection over union) for the boxes.
     * This threshold is used in the `NMS (Non-Maximum Suppression) step. |br|
     * The value range is ]0.f, 1.f], 0.f being no overlap and 1.f perfect overlap.
     * JSON name: "pyramidal_search_miniou",
     * Default: 0.4f
     * type: float
     * pattern: ]0.f, 1.f]
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#pyramidal-search-miniou
     */
    static final double CONFIG_PYRAMIDAL_SEARCH_MINIOU = 0.4;

    /**
     * Whether to enable License Plate Country Identification (LPCI) function (https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-country-identification-lpci).
     * To avoid adding latency to the pipeline only enable this function if you really need it.
     * JSON name: "klass_lpci_enabled"
     * Default: false
     * type: bool
     * pattern: true | false
     * More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-lpci-enabled
     */
    static final boolean CONFIG_KLASS_LPCI_ENABLED = true;

    /**
     * License Plate Country Identification (LPCI) is a classification problem and the result is the top K most probable classes. This configuration entry defines K.
     * JSON name: "klass_lpci_topk"
     * Default: 5
     * type: integer
     * pattern: [1, inf[
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-lpci-topk
     */
    static final int CONFIG_KLASS_LPCI_TOPK = 5;

    /**
     * Whether to enable Vehicle Color Recognition (VCR) function (https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-color-recognition-vcr).
     * To avoid adding latency to the pipeline only enable this function if you really need it.
     * JSON name: "klass_vcr_enabled"
     * Default: false
     * type: bool
     * pattern: true | false
     * More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vcr-enabled
     */
    static final boolean CONFIG_KLASS_VCR_ENABLED = true;

    /**
     * Vehicle Color Recognition (VCR) is a classification problem and the result is the top K most probable classes. This configuration entry defines K.
     * JSON name: "klass_vcr_topk"
     * Default: 5
     * type: integer
     * pattern: [1, inf[
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vcr-topk
     */
    static final int CONFIG_KLASS_VCR_TOPK = 5;

    /**
     * Whether to enable Vehicle Make Model Recognition (VMMR) function (https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-make-model-recognition-vmmr).
     * To avoid adding latency to the pipeline only enable this function if you really need it.
     * JSON name: "klass_vmmr_enabled"
     * Default: false
     * type: bool
     * pattern: true | false
     * More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vmmr-enabled
     */
    static final boolean CONFIG_KLASS_VMMR_ENABLED = true;

    /**
     * Vehicle Make Model Recognition (VMMR) is a classification problem and the result is the top K most probable classes. This configuration entry defines K.
     * JSON name: "klass_vmmr_topk"
     * Default: 5
     * type: integer
     * pattern: [1, inf[
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vmmr-topk
     */
    static final int CONFIG_KLASS_VMMR_TOPK = 5;

    /**
     * Whether to enable Vehicle Body Style Recognition (VBSR) function (https://www.doubango.org/SDKs/anpr/docs/Features.html#features-vehiclebodystylerecognition).
     * To avoid adding latency to the pipeline only enable this function if you really need it.
     * JSON name: "klass_vbsr_enabled"
     * Default: false
     * type: bool
     * pattern: true | false
     * More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vbsr-enabled
     */
    static final boolean CONFIG_KLASS_VBSR_ENABLED = true;

    /**
     * Vehicle Body Style Recognition (VBSR)  is a classification problem and the result is the top K most probable classes. This configuration entry defines K.
     * JSON name: "klass_vbsr_topk"
     * Default: 5
     * type: integer
     * pattern: [1, inf[
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vbsr-topk
     */
    static final int CONFIG_KLASS_VBSR_TOPK = 5;

    /**
     * Whether to enable License Plate Recognition (LPR) function.
     * JSON name: "recogn_enabled"
     * Default: true
     * type: bool
     * pattern: true | false
     * More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#recogn-enabled
     */
    static final boolean CONFIG_RECOGN_ENABLED = true;

    /**
     * Defines a charset (Alphabet) to use for the recognizer.
     * JSON name: "recogn_charset"
     * Default: "latin"
     * type: string
     * pattern: "latin" | "korean" | "chinese" | "brazilian" | "israeli"
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#recogn-charset
     */
    static final String CONFIG_RECOGN_CHARSET = "latin";

    /**
     * Defines the overall score type. The recognizer outputs a recognition score ([0.f, 1.f]) for every character in the license plate.
     * The score type defines how to compute the overall score.
     * - "min": Takes the minimum score.
     * - "mean": Takes the average score.
     * - "median": Takes the median score.
     * - "max": Takes the maximum score.
     * - "minmax": Takes (max + min) * 0.5f.
     * - "cumprod": Cumulative product(https://en.cppreference.com/cpp/algorithm/accumulate).
     * The "min" score is the more robust type as it ensure that every character have at least a certain confidence value.
     * The median score is the default type as it provide a higher recall. In production we recommend using min type.
     * JSON name: "recogn_score_type"
     * Default: "cumprod"
     * Recommended: "min"
     * type: string
     * pattern: "min" | "mean" | "median" | "max" | "minmax" | "cumprod"
     *  More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#recogn-score-type
     */
    static final String CONFIG_RECOGN_SCORE_TYPE = "cumprod";

    /**
     * Define a threshold for the overall recognition score. Any recognition with a score below that threshold will be ignored.
     * The overall score is computed based on "recogn_score_type". 0.f being poor confidence and 1.f excellent confidence.
     * JSON name: "recogn_minscore"
     * Default: 0.2f
     * type: float
     * pattern: ]0.f, 1.f]
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#recogn-minscore
     */
    static final double CONFIG_RECOGN_MINSCORE = 0.2; // 20%

    private String mDebugInternalDataPath = null;

    private boolean mIsProcessing = false;
    private boolean mIsPaused = true;

    /**
     * Parallel callback delivery function used by the engine to notify for new deferred results
     */
    static class MyUltAlprSdkParallelDeliveryCallback extends UltAlprSdkParallelDeliveryCallback {
        static final String TAG = MyUltAlprSdkParallelDeliveryCallback.class.getCanonicalName();

        AlprPlateView mAlprPlateView;
        Size mImageSize;
        long mTotalDuration = 0;
        int mOrientation = 0;

        void setAlprPlateView(@NonNull final AlprPlateView view) {
            mAlprPlateView = view;
        }

        void setImageSize(@NonNull final Size imageSize, @NonNull final int orientation) {
            mImageSize = imageSize;
            mOrientation = orientation;
        }

        void setDurationTime(final long totalDuration) {
            mTotalDuration = totalDuration;
        }

        @Override
        public void onNewResult(UltAlprSdkResult result) {
            Log.d(TAG, AlprUtils.resultToString(result));
            if (mAlprPlateView!= null) {
                mAlprPlateView.setResult(result, mImageSize, mTotalDuration, mOrientation);
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
        setContentView(getResources().getIdentifier("activity_main", "layout", getPackageName()));

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
        final JSONObject config = getJsonConfig();
        // Retrieve previously stored key from internal storage
        String tokenFile = AlprLicenseActivator.tokenFile(this);
        if (!tokenFile.isEmpty()) {
            try {
                config.put("license_token_data", AlprLicenseActivator.tokenData(tokenFile));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        final UltAlprSdkResult alprResult = AlprUtils.assertIsOk(UltAlprSdkEngine.init(
                getAssets(),
                config.toString(),
                mParallelDeliveryCallback
        ));
        Log.i(TAG,"ALPR engine initialized: " + AlprUtils.resultToString(alprResult));

        // Activate the license
        final boolean isActivationPossible = !getActivationServerUrl().isEmpty() && !getActivationMasterOrSlaveKey().isEmpty();
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

        mIsPaused = false;
    }

    @Override
    public synchronized void onPause() {
        mIsPaused = true;

        super.onPause();
    }

    @Override
    public void setAlprPlateView(@NonNull final AlprPlateView view) {
        mAlprPlateView = view;
        if (mParallelDeliveryCallback != null) {
            mParallelDeliveryCallback.setAlprPlateView(view);
        }
        assert(CONFIG_DETECT_ROI.size() == 4);
        mAlprPlateView.setDetectROI(
                new RectF(
                        CONFIG_DETECT_ROI.get(0).floatValue(),
                        CONFIG_DETECT_ROI.get(2).floatValue(),
                        CONFIG_DETECT_ROI.get(1).floatValue(),
                        CONFIG_DETECT_ROI.get(3).floatValue()
                )
        );
    }

    @Override
    public void setImage(@NonNull final Image image, final int jpegOrientation) {

        // On sequential mode we just ignore the processing
        if (mIsProcessing || mIsPaused) {
            Log.d(TAG, "Inference function not returned yet: Processing or paused");
            image.close();
            return;
        }

        mIsProcessing = true;

        final Size imageSize = new Size(image.getWidth(), image.getHeight());

        // Orientation
        // Convert from degree to real EXIF orientation
        int exifOrientation;
        switch (jpegOrientation) {
            case 90: exifOrientation = ExifInterface.ORIENTATION_ROTATE_90; break;
            case 180: exifOrientation = ExifInterface.ORIENTATION_ROTATE_180; break;
            case 270: exifOrientation = ExifInterface.ORIENTATION_ROTATE_270; break;
            case 0: default: exifOrientation = ExifInterface.ORIENTATION_NORMAL; break;
        }

        // Update image for the async callback
        if (mParallelDeliveryCallback != null) {
            mParallelDeliveryCallback.setImageSize((jpegOrientation % 180) == 0 ? imageSize : new Size(imageSize.getHeight(), imageSize.getWidth()), jpegOrientation);
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
                planes[1].getPixelStride(),
                exifOrientation
        ));
        final long durationInMillis = SystemClock.uptimeMillis() - startTimeInMillis; // Total time: Inference + image processing (chroma conversion, rotation...)

        if (mParallelDeliveryCallback != null) {
            mParallelDeliveryCallback.setDurationTime(durationInMillis);
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
        // Starting version 3.2 the callback will be called even if the result is empty
        if (mAlprPlateView != null && mParallelDeliveryCallback == null ) { // means sequential call or no plate/car to expect from the parallel delivery callback
            mAlprPlateView.setResult(result, (jpegOrientation % 180) == 0 ? imageSize : new Size(imageSize.getHeight(), imageSize.getWidth()), durationInMillis, jpegOrientation);
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
     * Returns JSON config to be used to initialize the ALPR/ANPR SDK.
     * @return The JSON config
     */
    protected JSONObject getJsonConfig() {
        // More information on the JSON config at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html
        JSONObject config = new JSONObject();
        try {
            config.put("debug_level", CONFIG_DEBUG_LEVEL);
            config.put("debug_write_input_image_enabled", CONFIG_DEBUG_WRITE_INPUT_IMAGE);
            config.put("debug_internal_data_path", getDebugInternalDataPath());

            config.put("num_threads", CONFIG_NUM_THREADS);
            config.put("max_latency", CONFIG_MAX_LATENCY);
            config.put("night_vision_enabled", CONFIG_NIGHT_VISION_ENABLED);
            config.put("batching_mode", CONFIG_BATCHING_MODE);
            config.put("backend", CONFIG_BACKEND);
            config.put("platform", CONFIG_PLATFORM);
            config.put("asm_enabled", CONFIG_ASM_ENABLED);
            config.put("intrin_enabled", CONFIG_INTRIN_ENABLED);

            config.put("detect_roi", new JSONArray(CONFIG_DETECT_ROI));
            config.put("detect_minscore", CONFIG_DETECT_MINSCORE);
            config.put("detect_miniou", CONFIG_DETECT_MINIOU);

            config.put("pyramidal_search_enabled", CONFIG_PYRAMIDAL_SEARCH_ENABLED);
            config.put("pyramidal_search_minscore", CONFIG_PYRAMIDAL_SEARCH_MINSCORE);
            config.put("pyramidal_search_min_image_size_inpixels", CONFIG_PYRAMIDAL_SEARCH_MIN_IMAGE_SIZE_INPIXELS);
            config.put("pyramidal_search_miniou", CONFIG_PYRAMIDAL_SEARCH_MINIOU);

            config.put("klass_lpci_enabled", CONFIG_KLASS_LPCI_ENABLED);
            config.put("klass_lpci_topk", CONFIG_KLASS_LPCI_TOPK);

            config.put("klass_vcr_enabled", CONFIG_KLASS_VCR_ENABLED);
            config.put("klass_vcr_topk", CONFIG_KLASS_VCR_TOPK);

            config.put("klass_vmmr_enabled", CONFIG_KLASS_VMMR_ENABLED);
            config.put("klass_vmmr_topk", CONFIG_KLASS_VMMR_TOPK);

            config.put("klass_vbsr_enabled", CONFIG_KLASS_VBSR_ENABLED);
            config.put("klass_vbsr_topk", CONFIG_KLASS_VBSR_TOPK);

            config.put("recogn_enabled", CONFIG_RECOGN_ENABLED);
            config.put("recogn_charset", CONFIG_RECOGN_CHARSET);
            config.put("recogn_score_type", CONFIG_RECOGN_SCORE_TYPE);
            config.put("recogn_minscore", CONFIG_RECOGN_MINSCORE);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return config;
    }

    /**
     * Gets the server url used to activate the license. Please contact us to get the correct URL.
     * e.g. https://localhost:3600
     * @return The URL
     */
    protected abstract String getActivationServerUrl();

    /**
     * Gets the master or slave key to use for the activation.
     * You MUST NEVER include your master key in the code or share it with the end user.
     * The master key should be used to generate slaves (one-time activation keys).
     * More information about master/slave keys at https://www.doubango.org/SDKs/LicenseManager/docs/Jargon.html.
     * @return The master of slave key.
     */
    protected abstract String getActivationMasterOrSlaveKey();

    /**
     * Whether to enable parallel processing.
     * More at https://www.doubango.org/SDKs/anpr/docs/Parallel_processing.html
     */
    protected abstract boolean isParallelDeliveryEnabled();
}