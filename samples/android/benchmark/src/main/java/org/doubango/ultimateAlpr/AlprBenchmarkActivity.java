/*
 * Copyright (C) 2016-2020 Doubango AI <https://www.doubango.org>
 * License: For non-commercial use only
 * Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
 * WebSite: https://www.doubango.org/webapps/alpr/
 */
package org.doubango.ultimateAlpr;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import org.doubango.ultimateAlpr.Sdk.ULTALPR_SDK_IMAGE_TYPE;
import org.doubango.ultimateAlpr.Sdk.UltAlprSdkEngine;
import org.doubango.ultimateAlpr.Sdk.UltAlprSdkParallelDeliveryCallback;
import org.doubango.ultimateAlpr.Sdk.UltAlprSdkResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AlprBenchmarkActivity extends AppCompatActivity {

    /**
     * TAG used for the debug logs.
     */
    static final String TAG = AlprBenchmarkActivity.class.toString();

    /**
     * Whether to enable the parallel mode. More information about the parallel mode at https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html.
     * Should be true.
     */
    static final boolean PARALLEL_MODE = true;

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
     * Defines the maximum number of threads to use.
     * You should not change this value unless you know what you’re doing. Set to -1 to let the SDK choose the right value.
     * The right value the SDK will choose will likely be equal to the number of virtual cores.
     * For example, on an octa-core device the maximum number of threads will be 8.
     * JSON name: "num_threads"
     * Default: -1
     * type: int
     * pattern: [-inf, +inf]
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#num-threads
     */
    static final int CONFIG_NUM_THREADS = -1;

    /**
     * Whether to enable GPGPU computing. This will enable or disable GPGPU computing on the computer vision and deep learning libraries.
     * On ARM devices this flag will be ignored when fixed-point (integer) math implementation exist for a well-defined function.
     * For example, this function will be disabled for the bilinear scaling as we have a fixed-point SIMD accelerated implementation.
     * Same for many deep learning parts as we’re using QINT8 quantized inference.
     * JSON name: "gpgpu_enabled"
     * Default: true
     * type: bool
     * pattern: true | false
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#gpgpu-enabled
     */
    static final boolean CONFIG_GPGPU_ENABLED = true;

    /**
     * The parallel processing method could introduce delay/latency in the delivery callback on low-end CPUs.
     * This parameter controls the maximum latency you can tolerate. The unit is number of frames.
     * The default value is -1 which means auto.
     * JSON name: "max_latency"
     * Default: -1
     * type: int
     * pattern: [0, +inf[
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#max-latency
     */
    static final int CONFIG_MAX_LATENCY = -1;

    /**
     * Defines a charset (Alphabet) to use for the recognizer.
     * JSON name: "gpgpu_enabled"
     * Default: latin
     * type: string
     * pattern: latin | korean
     * Available since: 2.6.2
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#charset
     */
    static final String CONFIG_CHARSET = "latin";

    /**
     * Whether to use OpenVINO instead of Tensorflow as deep learning backend engine. OpenVINO is used for detection and classification but not for OCR.
     * OpenVINO is always faster than Tensorflow on Intel products (CPUs, VPUs, GPUs, FPGAs…) and we highly recommend using it.
     * We require a CPU with support for both AVX2 and FMA features before trying to load OpenVINO plugin (shared library).
     * OpenVINO will be disabled with a fallback on Tensorflow if these CPU features are not detected.
     * JSON name: "openvino_enabled"
     * Default: true
     * type: bool
     * pattern: true | false
     * Available since: 3.0.0
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#openvino-enabled
     */
    static final boolean CONFIG_OPENVINO_ENABLED = true;

    /**
     * OpenVINO device to use for computations. We recommend using "CPU" which is always correct.
     * If you have an Intel GPU, VPU or FPGA, then you can change this value.
     * If you try to use any other value than "CPU" without having the right device, then OpenVINO will be completely disabled with a fallback on Tensorflow.
     * JSON name: "openvino_device"
     * Default: "CPU"
     * type: string
     * pattern: "GNA" | "HETERO" | "CPU" | "MULTI" | "GPU" | "MYRIAD" | "HDDL " | "FPGA"
     * Available since: 3.0.0
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#openvino-device
     */
    static final String CONFIG_OPENVINO_DEVICE = "CPU";

    /**
     * Define a threshold for the detection score. Any detection with a score below that threshold will be ignored. 0.f being poor confidence and 1.f excellent confidence.
     * JSON name: "detect_minscore",
     * Default: 0.3f
     * type: float
     * pattern: ]0.f, 1.f]
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#detect-minscore
     */
    static final double CONFIG_DETECT_MINSCORE = 0.1; // 10%

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
     * Whether to enable pyramidal search. Pyramidal search is an advanced feature to accurately detect very small or far away license plates.
     * JSON name: "pyramidal_search_enabled"
     * Default: true
     * type: bool
     * pattern: true | false
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#pyramidal_search_enabled
     */
    static final boolean CONFIG_PYRAMIDAL_SEARCH_ENABLED = false;

    /**
     * Defines how sensitive the pyramidal search anchor resolution function should be. The higher this value is, the higher the number of pyramid levels will be.
     * More levels means better accuracy but higher CPU usage and inference time.
     * Pyramidal search will be disabled if this value is equal to 0.
     * JSON name: "pyramidal_search_sensitivity"
     * Default: 0.28f
     * type: float
     * pattern: [0.f, 1.f]
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#pyramidal_search_sensitivity
     */
    static final double CONFIG_PYRAMIDAL_SEARCH_SENSITIVITY= 0.28; // 28%

    /**
     * Defines a threshold for the detection score associated to the plates retrieved after pyramidal search.
     * Any detection with a score below that threshold will be ignored.
     * 0.f being poor confidence and 1.f excellent confidence.
     * JSON name: "pyramidal_search_minscore"
     * Default: 0.8f
     * type: float
     * pattern: ]0.f, 1.f]
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#pyramidal_search_minscore
     */
    static final double CONFIG_PYRAMIDAL_SEARCH_MINSCORE = 0.8; // 80%

    /**
     * Minimum image size (max[width, height]) in pixels to trigger pyramidal search.
     * Pyramidal search will be disabled if the image size is less than this value. Using pyramidal search on small images is useless.
     * JSON name: "pyramidal_search_min_image_size_inpixels"
     * Default: 800
     * type: integer
     * pattern: [0, inf]
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#pyramidal_search_min_image_size_inpixels
     */
    static final int CONFIG_PYRAMIDAL_SEARCH_MIN_IMAGE_SIZE_INPIXELS = 800; // pixels

    /**
     * Whether to enable License Plate Country Identification (LPCI) function (https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-country-identification-lpci).
     * To avoid adding latency to the pipeline only enable this function if you really need it.
     * JSON name: "klass_lpci_enabled"
     * Default: false
     * type: bool
     * pattern: true | false
     * Available since: 3.0.0
     * More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-lpci-enabled
     */
    static final boolean CONFIG_KLASS_LPCI_ENABLED = false;

    /**
     * Whether to enable Vehicle Color Recognition (VCR) function (https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-color-recognition-vcr).
     * To avoid adding latency to the pipeline only enable this function if you really need it.
     * JSON name: "klass_vcr_enabled"
     * Default: false
     * type: bool
     * pattern: true | false
     * Available since: 3.0.0
     * More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vcr-enabled
     */
    static final boolean CONFIG_KLASS_VCR_ENABLED = false;

    /**
     * Whether to enable Vehicle Make Model Recognition (VMMR) function (https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-make-model-recognition-vmmr).
     * To avoid adding latency to the pipeline only enable this function if you really need it.
     * JSON name: "klass_vmmr_enabled"
     * Default: false
     * type: bool
     * pattern: true | false
     * More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vmmr-enabled
     */
    static final boolean CONFIG_KLASS_VMMR_ENABLED = false;

    /**
     * 1/G coefficient value to use for gamma correction operation in order to enhance the car color before applying VCR classification.
     * More information on gamma correction could be found at https://en.wikipedia.org/wiki/Gamma_correction.
     * Values higher than 1.0f mean lighter and lower than 1.0f mean darker. Value equal to 1.0f mean bypass gamma correction operation.
     * This parameter in action: https://www.doubango.org/SDKs/anpr/docs/Improving_the_accuracy.html#gamma-correction
     * * JSON name: "recogn_minscore"
     * Default: 1.5
     * type: float
     * pattern: [0.f, inf[
     * Available since: 3.0.0
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vcr-gamma
     */
    static final double CONFIG_KLASS_VCR_GAMMA = 1.5;

    /**
     * Define a threshold for the overall recognition score. Any recognition with a score below that threshold will be ignored.
     * The overall score is computed based on "recogn_score_type". 0.f being poor confidence and 1.f excellent confidence.
     * JSON name: "recogn_minscore"
     * Default: 0.3f
     * type: float
     * pattern: ]0.f, 1.f]
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#recogn-minscore
     */
    static final double CONFIG_RECOGN_MINSCORE = 0.3; // 30%

    /**
     * Defines the overall score type. The recognizer outputs a recognition score ([0.f, 1.f]) for every character in the license plate.
     * The score type defines how to compute the overall score.
     * - "min": Takes the minimum score.
     * - "mean": Takes the average score.
     * - "median": Takes the median score.
     * - "max": Takes the maximum score.
     * - "minmax": Takes (max + min) * 0.5f.
     * The "min" score is the more robust type as it ensure that every character have at least a certain confidence value.
     * The median score is the default type as it provide a higher recall. In production we recommend using min type.
     * JSON name: "recogn_score_type"
     * Default: "median"
     * Recommended: "min"
     * type: string
     *  More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#recogn-score-type
     */
    static final String CONFIG_RECOGN_SCORE_TYPE = "min";

    /**
     * Whether to add rectification layer between the detector’s output and the recognizer’s input. A rectification layer is used to suppress the distortion.
     * A plate is distorted when it’s skewed and/or slanted. The rectification layer will deslant and deskew the plate to make it straight which make the recognition more accurate.
     * Please note that you only need to enable this feature when the license plates are highly distorted. The implementation can handle moderate distortion without a rectification layer.
     * The rectification layer adds many CPU intensive operations to the pipeline which decrease the frame rate.
     * More info on the rectification layer could be found at https://www.doubango.org/SDKs/anpr/docs/Rectification_layer.html#rectificationlayer
     * JSON name: "recogn_rectify_enabled"
     * Default: false
     * Recommended: false
     * type: string
     * More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#recogn-rectify-enabled
     */
    static final boolean CONFIG_RECOGN_RECTIFY_ENABLED = false;

    /**
     * Number of times to try.
     * This number must be high enough (> 300) to make sure the noise is relatively small.
     */
    static final int NUM_LOOPS = 1000;

    /**
     * The percentage of images with a plate. Within [0, 1] interval.
     * When a camera is filming a street/road most of the time there is no license plate at all.
     * In fact, only 0.03% of the images will likely be positive. In this benchmark we consider
     * 20% of the images have a plate which is a very high guess.
     */
    static final float PERCENT_POSITIVES = .2f; // 20%

    MyUltAlprSdkParallelDeliveryCallback mCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Check some values
        if (PERCENT_POSITIVES < 0 || PERCENT_POSITIVES > 1) {
            throw new AssertionError("PERCENT_POSITIVES must be within [0, 1]");
        }
        if (NUM_LOOPS <= 0) {
            throw new AssertionError("NUM_LOOPS must be > 0");
        }

        mCallback = PARALLEL_MODE ? new MyUltAlprSdkParallelDeliveryCallback((int)(NUM_LOOPS * PERCENT_POSITIVES)) : null;

        // Initialize the engine
        UltAlprSdkResult result = assertIsOk(UltAlprSdkEngine.init(
                getAssets(),
                getConfig(),
                mCallback
        ));
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();

        final TextView textView = findViewById(R.id.textView);
        textView.setText("*** Preparing... ***");

        // Create image indices
        List<Integer> indices = new ArrayList<>(NUM_LOOPS);
        final int numPositives = (int)(NUM_LOOPS * PERCENT_POSITIVES);
        for (int i = 0; i < numPositives; ++i) {
            indices.add(1); // positive index
        }
        for (int i = numPositives; i < NUM_LOOPS; ++i) {
            indices.add(0); // negative index
        }
        Collections.shuffle(indices); // make the indices random

        // Read the images
        final AlprImage images[] = new AlprImage[2];
        images[0] = readFile("london_traffic.jpg");
        if (images[0] == null) {
            throw new AssertionError("Failed to read file");
        }
        images[1] = readFile("lic_us_1280x720.jpg");
        if (images[1] == null) {
            throw new AssertionError("Failed to read file");
        }

        // Warm up to prepare for benchmark
        assertIsOk(UltAlprSdkEngine.warmUp(images[1].mType));

        textView.setText("*** Started timing... ***");

        // Processing
        Log.i(TAG, "*** Started timing... ***");
        final long startTimeInMillis = SystemClock.uptimeMillis();
        for (Integer i : indices) {
            final AlprImage image = images[i];
            assertIsOk(UltAlprSdkEngine.process(
                    image.mType,
                    image.mBuffer,
                    image.mWidth,
                    image.mHeight
            ));
        }
        final long endTimeInMillis = SystemClock.uptimeMillis();
        final long elapsedTime = (endTimeInMillis - startTimeInMillis);
        final float estimatedFps = 1000.f / (elapsedTime / (float)NUM_LOOPS);

        Log.i(TAG, "Elapsed time: " + elapsedTime + " millis, FrameRate: " + estimatedFps);

        textView.setText("Elapsed time: " + (endTimeInMillis - startTimeInMillis) + " millis" + ", Frame rate: " + estimatedFps);

        // To check we really processed all positive frames
        if (mCallback != null) {
            Log.i(TAG, "Num positives: " + mCallback.getNumPositives() + "/" + numPositives);
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        final UltAlprSdkResult result = assertIsOk(UltAlprSdkEngine.deInit());

        super.onDestroy();
    }

    AlprImage readFile(final String name) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(getAssets().open(name), null, options);
        }
        catch(IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
            return null;
        }
        if (bitmap.getRowBytes() < bitmap.getWidth() << 2) {
            throw new AssertionError("Not ARGB");
        }

        final int widthInBytes = bitmap.getRowBytes();
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final ByteBuffer nativeBuffer = ByteBuffer.allocateDirect(widthInBytes * height);
        bitmap.copyPixelsToBuffer(nativeBuffer);
        nativeBuffer.rewind();

        return new AlprImage(ULTALPR_SDK_IMAGE_TYPE.ULTALPR_SDK_IMAGE_TYPE_RGBA32, nativeBuffer, width, height);
    }

    final String getConfig() {
        // More information on the JSON config at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html
        JSONObject config = new JSONObject();
        try {
            config.put("debug_level", CONFIG_DEBUG_LEVEL);
            config.put("debug_write_input_image_enabled", CONFIG_DEBUG_WRITE_INPUT_IMAGE);
            if (CONFIG_DEBUG_WRITE_INPUT_IMAGE) {
                // Create folder to dump input images for debugging
                File dummyFile = new File(getExternalFilesDir(null), "dummyFile");
                if (!dummyFile.getParentFile().exists() && !dummyFile.getParentFile().mkdirs()) {
                    Log.e(TAG, "mkdir failed: " + dummyFile.getParentFile().getAbsolutePath());
                }
                final String debugInternalDataPath = dummyFile.getParentFile().exists() ? dummyFile.getParent() : Environment.getExternalStorageDirectory().getAbsolutePath();
                dummyFile.delete();
                config.put("debug_internal_data_path", debugInternalDataPath);
            }

            config.put("num_threads", CONFIG_NUM_THREADS);
            config.put("gpgpu_enabled", CONFIG_GPGPU_ENABLED);
            config.put("charset", CONFIG_CHARSET);
            config.put("max_latency", CONFIG_MAX_LATENCY);
            config.put("openvino_enabled", CONFIG_OPENVINO_ENABLED);
            config.put("openvino_device", CONFIG_OPENVINO_DEVICE);

            config.put("detect_minscore", CONFIG_DETECT_MINSCORE);
            config.put("detect_roi", new JSONArray(CONFIG_DETECT_ROI));

            config.put("pyramidal_search_enabled", CONFIG_PYRAMIDAL_SEARCH_ENABLED);
            config.put("pyramidal_search_sensitivity", CONFIG_PYRAMIDAL_SEARCH_SENSITIVITY);
            config.put("pyramidal_search_minscore", CONFIG_PYRAMIDAL_SEARCH_MINSCORE);
            config.put("pyramidal_search_min_image_size_inpixels", CONFIG_PYRAMIDAL_SEARCH_MIN_IMAGE_SIZE_INPIXELS);

            config.put("klass_lpci_enabled", CONFIG_KLASS_LPCI_ENABLED);
            config.put("klass_vcr_enabled", CONFIG_KLASS_VCR_ENABLED);
            config.put("klass_vmmr_enabled", CONFIG_KLASS_VMMR_ENABLED);
            config.put("klass_vcr_gamma", CONFIG_KLASS_VCR_GAMMA);

            config.put("recogn_minscore", CONFIG_RECOGN_MINSCORE);
            config.put("recogn_score_type", CONFIG_RECOGN_SCORE_TYPE);
            config.put("recogn_rectify_enabled", CONFIG_RECOGN_RECTIFY_ENABLED);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return config.toString();
    }

    static class AlprImage {
        final ULTALPR_SDK_IMAGE_TYPE mType;
        final ByteBuffer mBuffer;
        final int mWidth;
        final int mHeight;

        AlprImage(final ULTALPR_SDK_IMAGE_TYPE type, final ByteBuffer buffer, final int width, final int height) {
            mType = type;
            mBuffer = buffer;
            mWidth = width;
            mHeight = height;
        }
    }

    /**
     * Parallel callback delivery function used to notify about new results.
     * This callback will be called few milliseconds (before next frame is completely processed)
     * after process function is called.
     */
    static class MyUltAlprSdkParallelDeliveryCallback extends UltAlprSdkParallelDeliveryCallback {
        private int mNumPositives = 0;
        private final int mTotalPositives;

        MyUltAlprSdkParallelDeliveryCallback(int totalPositives) {
            mTotalPositives = totalPositives;
        }

        @Override
        public void onNewResult(UltAlprSdkResult result) {
            ++mNumPositives;
            Log.i(TAG, "Positive: " + mNumPositives + "/" + mTotalPositives + " -> " + resultToString(result));
        }
        public int getNumPositives() { return mNumPositives; }
    }

    /**
     * Checks if the result is success. Raise an exception if the result is failure.
     * @param result the result to check
     * @return the same result received in param.
     */
    static final UltAlprSdkResult assertIsOk(final UltAlprSdkResult result) {
        if (!result.isOK()) {
            throw new AssertionError("Operation failed: " + result.phrase());
        }
        return result;
    }

    /**
     * Converts the result to String for display.
     * @param result the result to convert
     * @return the String representing the result
     */
    static final String resultToString(final UltAlprSdkResult result) {
        return "code: " + result.code() + ", phrase: " + result.phrase() + ", numPlates: " + result.numPlates() + ", json: " + result.json();
    }


}
