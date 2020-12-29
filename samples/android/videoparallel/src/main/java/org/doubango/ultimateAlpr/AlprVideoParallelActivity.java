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
public class AlprVideoParallelActivity extends AlprActivity {

    /**
     * TAG used for the debug logs.
     */
    static final String TAG = AlprVideoParallelActivity.class.getCanonicalName();

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
     * Whether to enable Image Enhancement for Night-Vision (IENV).
     * IENV is explained at https://www.doubango.org/SDKs/anpr/docs/Features.html#features-imageenhancementfornightvision.
     *
     * JSON name: "ienv_enabled"
     * Default: false
     * type: bool
     * pattern: true | false
     * Available since: 3.2.0
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#ienv-enabled
     */
    static final boolean CONFIG_IENV_ENABLED = false;

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
     * JSON name: "detect_minscore"
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
     * Whether to return cars with no plate. By default any car without plate will be silently ignored.
     * To filter false-positives: https://www.doubango.org/SDKs/anpr/docs/Known_issues.html#false-positives-for-cars-with-no-plate
     * JSON name: "car_noplate_detect_enabled"
     * Default: false
     * type: bool
     * pattern: true | false
     * Available since: 3.2.0
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#car-noplate-detect-enabled
     */
    static final boolean CONFIG_CAR_NOPLATE_DETECT_ENABLED = false;

    /**
     * Defines a threshold for the detection score for cars with no plate. Any detection with a score below that threshold will be ignored. 0.f being poor confidence and 1.f excellent confidence.
     * JSON name: "car_noplate_detect_min_score",
     * Default: 0.8f
     * type: float
     * pattern: [0.f, 1.f]
     * Available since: 3.2.0
     * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#car-noplate-detect-min-score
     */
    static final double CONFIG_CAR_NOPLATE_DETECT_MINSCORE = 0.8; // 80%

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
     * Default: 0.3f
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
     * Whether to enable License Plate Country Identification (LPCI) function (https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-country-identification-lpci).
     * To avoid adding latency to the pipeline only enable this function if you really need it.
     * JSON name: "klass_lpci_enabled"
     * Default: false
     * type: bool
     * pattern: true | false
     * Available since: 3.0.0
     * More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-lpci-enabled
     */
    static final boolean CONFIG_KLASS_LPCI_ENABLED = true;

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
    static final boolean CONFIG_KLASS_VCR_ENABLED = true;

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
     * Whether to enable Vehicle Body Style Recognition (VBSR) function (https://www.doubango.org/SDKs/anpr/docs/Features.html#features-vehiclebodystylerecognition).
     * To avoid adding latency to the pipeline only enable this function if you really need it.
     * JSON name: "klass_vbsr_enabled"
     * Default: false
     * type: bool
     * pattern: true | false
     * Available since: 3.2.0
     * More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vbsr-enabled
     */
    static final boolean CONFIG_KLASS_VBSR_ENABLED = false;

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
    static final double CONFIG_RECOGN_MINSCORE = 0.4; // 40%

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
     * A plate is distorted when it's skewed and/or slanted. The rectification layer will deslant and deskew the plate to make it straight which makes the recognition more accurate.
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
            config.put("max_latency", CONFIG_MAX_LATENCY);
            config.put("ienv_enabled", CONFIG_IENV_ENABLED);
            config.put("openvino_enabled", CONFIG_OPENVINO_ENABLED);
            config.put("openvino_device", CONFIG_OPENVINO_DEVICE);

            config.put("detect_minscore", CONFIG_DETECT_MINSCORE);
            config.put("detect_roi", new JSONArray(getDetectROI()));

            config.put("car_noplate_detect_enabled", CONFIG_CAR_NOPLATE_DETECT_ENABLED);
            config.put("car_noplate_detect_min_score", CONFIG_CAR_NOPLATE_DETECT_MINSCORE);

            config.put("pyramidal_search_enabled", CONFIG_PYRAMIDAL_SEARCH_ENABLED);
            config.put("pyramidal_search_sensitivity", CONFIG_PYRAMIDAL_SEARCH_SENSITIVITY);
            config.put("pyramidal_search_minscore", CONFIG_PYRAMIDAL_SEARCH_MINSCORE);
            config.put("pyramidal_search_min_image_size_inpixels", CONFIG_PYRAMIDAL_SEARCH_MIN_IMAGE_SIZE_INPIXELS);

            config.put("klass_lpci_enabled", CONFIG_KLASS_LPCI_ENABLED);
            config.put("klass_vcr_enabled", CONFIG_KLASS_VCR_ENABLED);
            config.put("klass_vmmr_enabled", CONFIG_KLASS_VMMR_ENABLED);
            config.put("klass_vbsr_enabled", CONFIG_KLASS_VBSR_ENABLED);
            config.put("klass_vcr_gamma", CONFIG_KLASS_VCR_GAMMA);

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
    protected boolean isParallelDeliveryEnabled() { return true; /* we want to activated parallel instead of sequential delivery */ }

    @Override
    protected List<Float> getDetectROI() { return CONFIG_DETECT_ROI; }

    @Override
    protected String getActivationServerUrl() { return ACTIVATION_SERVER_URL; }

    @Override
    protected String getActivationMasterOrSlaveKey() { return ACTIVATION_MASTER_OR_SLAVE_KEY; }
}
