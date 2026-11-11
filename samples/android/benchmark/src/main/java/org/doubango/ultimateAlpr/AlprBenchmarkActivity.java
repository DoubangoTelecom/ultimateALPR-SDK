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

import org.doubango.ultimateAlpr.sample.benchmark.R;

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
import java.util.Arrays;
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
     * Image to use for benchmarking
     */
    static final String IMAGE_NAME = "lic_us_1280x720.jpg";

    /**
     * Number of times to try.
     * This number must be high enough (>= 300) to make sure the noise is relatively small.
     */
    static final int BENCHMARK_LOOPS = 300;
    /***
     * Number of loops for the warmup.
     */
    static final int WARMUP_LOOPS = 20;



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
     * The parallel processing method could introduce delay/latency in the delivery callback on low-end CPUs. T
     * his parameter controls the maximum latency you can tolerate. The unit is number of frames. The default value is -1 which means auto
     * * JSON name: "max_latency"
     * Default: -1
     * type: int
     * pattern: [0, +inf[
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#max-latency
     */
    static final int CONFIG_MAX_LATENCY = -1;

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
    static final double CONFIG_DETECT_MINSCORE = 0.4;

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
    static final boolean CONFIG_PYRAMIDAL_SEARCH_ENABLED = false;

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
    static final boolean CONFIG_KLASS_LPCI_ENABLED = false;

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
    static final boolean CONFIG_KLASS_VCR_ENABLED = false;

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
    static final boolean CONFIG_KLASS_VMMR_ENABLED = false;

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
    static final boolean CONFIG_KLASS_VBSR_ENABLED = false;

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

    MyUltAlprSdkParallelDeliveryCallback mCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Sanity check
        if (BENCHMARK_LOOPS <= 0 || WARMUP_LOOPS <= 0) {
            throw new AssertionError("NUM_LOOPS must be > 0");
        }

        mCallback = new MyUltAlprSdkParallelDeliveryCallback(BENCHMARK_LOOPS);

        // Initialize the engine
        UltAlprSdkResult result = assertIsOk(UltAlprSdkEngine.init(
                getAssets(),
                getConfig(),
                PARALLEL_MODE ? mCallback : null
        ));
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();

        final TextView textView = findViewById(R.id.textView);
        textView.setText("*** Preparing... ***");

        // Read the images
        final AlprImage image = readFile(IMAGE_NAME);
        if (image == null) {
            throw new AssertionError("Failed to read file:" + IMAGE_NAME);
        }

        // Warm up to prepare for benchmark
        textView.setText("*** Warmup... ***");
        mCallback.reset(PARALLEL_MODE ? WARMUP_LOOPS : 0);
        for (int i = 0; i < WARMUP_LOOPS; ++i) {
            assertIsOk(UltAlprSdkEngine.process(
                    image.mType,
                    image.mBuffer,
                    image.mWidth,
                    image.mHeight
            ));
        }
        mCallback.waitUntilDone();

        // Processing
        Log.i(TAG, "*** Benchmarking... ***");
        mCallback.reset(PARALLEL_MODE ? BENCHMARK_LOOPS : 0);
        final long startTimeInMillis = SystemClock.uptimeMillis();
        for (int i = 0; i < BENCHMARK_LOOPS; ++i) {
            assertIsOk(UltAlprSdkEngine.process(
                    image.mType,
                    image.mBuffer,
                    image.mWidth,
                    image.mHeight
            ));
        }
        mCallback.waitUntilDone();
        final long endTimeInMillis = SystemClock.uptimeMillis();
        final long elapsedTime = (endTimeInMillis - startTimeInMillis);
        final float estimatedFps = 1000.f / (elapsedTime / (float)BENCHMARK_LOOPS);

        Log.i(TAG, "**** Elapsed time: " + elapsedTime + " millis, FrameRate: " + estimatedFps + " ****");

        textView.setText("Elapsed time: " + (endTimeInMillis - startTimeInMillis) + " millis" + ", Frame rate: " + estimatedFps);
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

            config.put("debug_level", CONFIG_DEBUG_LEVEL);
            config.put("debug_write_input_image_enabled", CONFIG_DEBUG_WRITE_INPUT_IMAGE);

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
            config.put("recogn_score_type", CONFIG_RECOGN_SCORE_TYPE);
            config.put("recogn_minscore", CONFIG_RECOGN_MINSCORE);
            config.put("recogn_charset", CONFIG_RECOGN_CHARSET);
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
        private int mLoops;
        private int mNotifs = 0;
        public MyUltAlprSdkParallelDeliveryCallback(int loops) {
            mLoops = loops;
        }

        public void reset(int loops) {
            mLoops = loops;
            mNotifs = 0;
        }

        public synchronized void waitUntilDone() {
            if (mNotifs != mLoops) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new AssertionError(e.getMessage());
                }
            }
        }

        @Override
        public synchronized void onNewResult(UltAlprSdkResult result) {
            Log.i(TAG, resultToString(result));
            mNotifs++;
            if (mNotifs == mLoops) {
                notifyAll();
            }
        }
    }

    /**
     * Checks if the result is success. Raise an exception if the result is failure.
     * @param result the result to check
     * @return the same result received in param.
     */
    static UltAlprSdkResult assertIsOk(final UltAlprSdkResult result) {
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
    static String resultToString(final UltAlprSdkResult result) {
        return "code: " + result.code() + ", phrase: " + result.phrase() + ", json: " + result.json();
    }


}
