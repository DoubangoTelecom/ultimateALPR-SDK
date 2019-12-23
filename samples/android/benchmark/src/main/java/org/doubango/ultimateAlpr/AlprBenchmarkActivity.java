/*
 * Copyright (C) 2016-2019 Doubango AI <https://www.doubango.org>
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
     * pattern: [left, width, top, height]
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#detect-roi
     */
    static final List<Float> CONFIG_DETECT_ROI = Arrays.asList(0.f, 0.f, 0.f, 0.f);

    /**
     * Whether to enable pyramidal search. Pyramidal search is an advanced and experimental feature to accurately detect very small or far away license plates.
     * May not be available if you're using a trial version.
     * JSON name: "detect_pyramidal_search_enabled"
     * Default: false
     * type: bool
     * pattern: true | false
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#detect-pyramidal-search-enabled
     */
    static final boolean CONFIG_DETECT_PYRAMIDAL_SEARCH_ENABLED = false;

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

        // Warm up to prepare for benchmark
        assertIsOk(UltAlprSdkEngine.warmUp(ULTALPR_SDK_IMAGE_TYPE.ULTALPR_SDK_IMAGE_TYPE_RGB24));
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

            config.put("detect_minscore", CONFIG_DETECT_MINSCORE);
            config.put("detect_roi", new JSONArray(CONFIG_DETECT_ROI));
            config.put("detect_pyramidal_search_enabled", CONFIG_DETECT_PYRAMIDAL_SEARCH_ENABLED);

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
