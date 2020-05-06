/*
 * Copyright (C) 2016-2020 Doubango AI <https://www.doubango.org>
 * License: For non-commercial use only
 * Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
 * WebSite: https://www.doubango.org/webapps/alpr/
 */
package org.doubango.ultimateAlpr;

import android.os.Bundle;
import android.util.Log;
import android.util.Size;

import org.doubango.ultimateAlpr.common.AlprActivity;
import org.doubango.ultimateAlpr.common.AlprCameraFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Main activity
 */
public class AlprVideoSequentialActivity extends AlprActivity {

    /**
     * TAG used for the debug logs.
     */
    static final String TAG = AlprVideoSequentialActivity.class.getCanonicalName();

    /**
     * Preferred size for the video stream. Will select the
     * closest size from the camera capabilities.
     */
    static final Size PREFERRED_SIZE = new Size(1280, 720);

    /**
     * The server url used to activate the license. Please contact us to get the real URL.
     * e.g. https://localhost:3600
     */
    static final String ACTIVATION_SERVER_URL = "";

    /**
     * The master or slave key to use for the activation.
     * You MUST NEVER include your master key in the code or share it with the end user.
     * The master key should be used to generate slaves (one-time activation keys).
     * More information about master/slave keys at https://www.doubango.org/SDKs/LicenseManager/docs/Jargon.html.
     */
    static final String ACTIVATION_MASTER_OR_SLAVE_KEY = "";

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
     * You should not change this value unless you know what you're doing. Set to -1 to let the SDK choose the right value.
     * The right value the SDK will choose will likely be equal to the number of virtual core.
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
     * Same for many deep learning parts as we're using QINT8 quantized inference.
     * JSON name: "gpgpu_enabled"
     * Default: true
     * type: bool
     * pattern: true | false
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#gpgpu-enabled
     */
    static final boolean CONFIG_GPGPU_ENABLED = true;

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
    static final boolean CONFIG_PYRAMIDAL_SEARCH_ENABLED = true;

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
    static final double CONFIG_PYRAMIDAL_SEARCH_MINSCORE = 0.5; // 50%

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
     * Whether to add rectification layer between the detector's output and the recognizer's input. A rectification layer is used to suppress the distortion.
     * A plate is distorted when it's skewed and/or slanted. The rectification layer will deslant and deskew the plate to make it straight which make the recognition more accurate.
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate " + this);
        super.onCreate(savedInstanceState);

        // At this step, the base class (AlprActivity) already initialized the engine (thanks to "super.onCreate()").
        // Do not try to create the parallel delivery callback in this method. Do it
        // in the constructor or at the declaration (see above). If the engine is initialized without
        // a parallel delivery callback, then it'll run in sequential mode.

        // Add camera fragment to the layout
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, AlprCameraFragment.newInstance(PREFERRED_SIZE, this))
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy " + this);
        super.onDestroy();

        // At this step, the engine is already deInitialized (thanks to "super.onDestroy()").
        // Any call using the engine (UltAlprSdkEngine.*) will miserably fail.
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected JSONObject getJsonConfig() {
        // More information on the JSON config at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html
        JSONObject config = new JSONObject();
        try {
            config.put("debug_level", CONFIG_DEBUG_LEVEL);
            config.put("debug_write_input_image_enabled", CONFIG_DEBUG_WRITE_INPUT_IMAGE);
            config.put("debug_internal_data_path", getDebugInternalDataPath());

            config.put("num_threads", CONFIG_NUM_THREADS);
            config.put("gpgpu_enabled", CONFIG_GPGPU_ENABLED);
            config.put("charset", CONFIG_CHARSET);

            config.put("detect_minscore", CONFIG_DETECT_MINSCORE);
            config.put("detect_roi", new JSONArray(getDetectROI()));

            config.put("pyramidal_search_enabled", CONFIG_PYRAMIDAL_SEARCH_ENABLED);
            config.put("pyramidal_search_sensitivity", CONFIG_PYRAMIDAL_SEARCH_SENSITIVITY);
            config.put("pyramidal_search_minscore", CONFIG_PYRAMIDAL_SEARCH_MINSCORE);
            config.put("pyramidal_search_min_image_size_inpixels", CONFIG_PYRAMIDAL_SEARCH_MIN_IMAGE_SIZE_INPIXELS);

            config.put("recogn_minscore", CONFIG_RECOGN_MINSCORE);
            config.put("recogn_score_type", CONFIG_RECOGN_SCORE_TYPE);
            config.put("recogn_rectify_enabled", CONFIG_RECOGN_RECTIFY_ENABLED);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return config;
    }


    @Override
    protected boolean isParallelDeliveryEnabled() { return false; /* we want to deactivated parallel and use sequential delivery*/ }

    @Override
    protected List<Float> getDetectROI() { return CONFIG_DETECT_ROI; }

    @Override
    protected String getActivationServerUrl() { return ACTIVATION_SERVER_URL; }

    @Override
    protected String getActivationMasterOrSlaveKey() { return ACTIVATION_MASTER_OR_SLAVE_KEY; }
}
