/* Copyright (C) 2011-2026 Doubango Telecom <https://www.doubango.org>
* File author: Mamadou DIOP (Doubango Telecom, France).
* License: For non commercial use only.
* Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
* WebSite: https://www.doubango.org/webapps/alpr/
*/
// Include ultimateALPR namespace
using org.doubango.ultimateAlpr.Sdk;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.Runtime.InteropServices;
using System.Web.Script.Serialization;

/*
	https://github.com/DoubangoTelecom/ultimateALPR-SDK/blob/master/samples/c%2B%2B/recognizer/README.md
	Usage: 
		recognizer \
			---image <path-to-image-with-to-recognize> \
            --assets <path-to-assets-folder> \
            [--charset <recognition-charset:latin/korean/chinese>] \
			[--tokenfile <path-to-license-token-file>] \
			[--tokendata <base64-license-token-data>]
	Example:
		recognizer \
			--image "ultimateALPR-SDK/assets/images/lic_us_1280x720.jpg" \
            --assets "ultimateALPR-SDK/assets" \
            --charset "latin" \
			--tokendata "xyz"
*/

namespace recognizer
{
    class Program
    {
        /**
        * Defines the debug level to output on the console. You should use "verbose" for diagnostic, "info" in development stage and "warn" on production.
        * JSON name: "debug_level"
        * Default: "info"
        * type: string
        * pattern: "verbose" | "info" | "warn" | "error" | "fatal"
        * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#debug-level
        */
        const String CONFIG_DEBUG_LEVEL = "info";

        /**
         * Whether to write the transformed input image to the disk. This could be useful for debugging.
         * JSON name: "debug_write_input_image_enabled"
         * Default: false
         * type: bool
         * pattern: true | false
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#debug-write-input-image-enabled
         */
        const bool CONFIG_DEBUG_WRITE_INPUT_IMAGE = false; // must be false unless you're debugging the code

        /**
         * Path to the folder where to write the transformed input image. Used only if "debug_write_input_image_enabled" is true.
         * JSON name: "debug_internal_data_path"
         * Default: ""
         * type: string
         * pattern: folder path
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#debug-internal-data-path
         */
        const String CONFIG_DEBUG_INTERNAL_DATA_PATH = ".";

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
        const int CONFIG_NUM_THREADS = -1;

        /**
         * The parallel processing method could introduce delay/latency in the delivery callback on low-end CPUs. T
         * his parameter controls the maximum latency you can tolerate. The unit is number of frames. The default value is -1 which means auto
         * * JSON name: "max_latency"
         * Default: -1
         * type: int
         * pattern: [0, +inf[
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#max-latency
         */
        const int CONFIG_MAX_LATENCY = -1;        

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
        const bool CONFIG_NIGHT_VISION_ENABLED = false;

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
        const String CONFIG_BATCHING_MODE = "auto";

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
        const String CONFIG_BACKEND = "auto";

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
        const String CONFIG_PLATFORM = "auto";

        /**
         * Whether to enable assembler code to use SIMD acceleration (SSE, AVX, NEON).
         *
         * JSON name: "asm_enabled"
         * Default: true
         * type: bool
         * pattern: true | false
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#asm-enabled
         */
        const bool CONFIG_ASM_ENABLED = true;

        /**
         * Whether to enable intrinsic code to use SIMD acceleration (SSE, AVX, NEON).
         *
         * JSON name: "intrin_enabled"
         * Default: true
         * type: bool
         * pattern: true | false
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#intrin-enabled
         */
        const bool CONFIG_INTRIN_ENABLED = true;

        /**
         * Defines the Region Of Interest (ROI) for the detector. Any pixels outside region of interest will be ignored by the detector.
         * Defining an WxH region of interest instead of resizing the image at WxH is very important as you'll keep the same quality when you define a ROI while you'll lose in quality when using the later.
         * JSON name: "detect_roi"
         * Default: [0.f, 0.f, 0.f, 0.f]
         * type: float[4]
         * pattern: [left, right, top, bottom]
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#detect-roi
         */
        static readonly IList<float> CONFIG_DETECT_ROI = new[] { 0f, 0f, 0f, 0f };

        /**
         * Define a threshold for the detection score. Any detection with a score below that threshold will be ignored. 0.f being poor confidence and 1.f excellent confidence.
         * JSON name: "detect_minscore",
         * Default: 0.4f
         * type: float
         * pattern: ]0.f, 1.f]
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#detect-minscore
         */
        const double CONFIG_DETECT_MINSCORE = 0.4;

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
        const double CONFIG_DETECT_MINIOU = 0.4;

        /**
         * Whether to enable pyramidal search. Pyramidal search is an advanced feature to accurately detect very small or far away license plates.
         * JSON name: "pyramidal_search_enabled"
         * Default: true
         * type: bool
         * pattern: true | false
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#pyramidal-search-enabled
         */
        const bool CONFIG_PYRAMIDAL_SEARCH_ENABLED = true;

        /**
         * Defines a threshold for the detection score associated to the plates retrieved after pyramidal search.
         * Any detection with a score below that threshold will be ignored.
         * 0.f being poor confidence and 1.f excellent confidence.
         * JSON name: "pyramidal_search_minscore"
         * Default: 0.2f
         * type: float
         * pattern: ]0.f, 1.f]
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#pyramidal-search-minscore
         */
        const double CONFIG_PYRAMIDAL_SEARCH_MINSCORE = 0.2; // 20%

        /**
         * Minimum image size (max[width, height]) in pixels to trigger pyramidal search.
         * Pyramidal search will be disabled if the image size is less than this value. Using pyramidal search on small images is useless.
         * JSON name: "pyramidal_search_min_image_size_inpixels"
         * Default: 800
         * type: integer
         * pattern: [0, inf[
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#pyramidal-search-min-image-size-inpixels
         */
        const int CONFIG_PYRAMIDAL_SEARCH_MIN_IMAGE_SIZE_INPIXELS = 800; // pixels

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
        const double CONFIG_PYRAMIDAL_SEARCH_MINIOU = 0.4;

        /**
         * Whether to enable License Plate Country Identification (LPCI) function (https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-country-identification-lpci). 
         * To avoid adding latency to the pipeline only enable this function if you really need it.
         * JSON name: "klass_lpci_enabled"
         * Default: false
         * type: bool
         * pattern: true | false
         * More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-lpci-enabled
         */
        const bool CONFIG_KLASS_LPCI_ENABLED = false;

        /**
         * License Plate Country Identification (LPCI) is a classification problem and the result is the top K most probable classes. This configuration entry defines K.
         * JSON name: "klass_lpci_topk"
         * Default: 5
         * type: integer
         * pattern: [1, inf[
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-lpci-topk
         */
        const int CONFIG_KLASS_LPCI_TOPK = 5;

        /**
         * Whether to enable Vehicle Color Recognition (VCR) function (https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-color-recognition-vcr). 
         * To avoid adding latency to the pipeline only enable this function if you really need it.
         * JSON name: "klass_vcr_enabled"
         * Default: false
         * type: bool
         * pattern: true | false
         * More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vcr-enabled
         */
        const bool CONFIG_KLASS_VCR_ENABLED = false;

        /**
         * Vehicle Color Recognition (VCR) is a classification problem and the result is the top K most probable classes. This configuration entry defines K.
         * JSON name: "klass_vcr_topk"
         * Default: 5
         * type: integer
         * pattern: [1, inf[
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vcr-topk
         */
        const int CONFIG_KLASS_VCR_TOPK = 5;

        /**
         * Whether to enable Vehicle Make Model Recognition (VMMR) function (https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-make-model-recognition-vmmr).
         * To avoid adding latency to the pipeline only enable this function if you really need it.
         * JSON name: "klass_vmmr_enabled"
         * Default: false
         * type: bool
         * pattern: true | false
         * More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vmmr-enabled
         */
        const bool CONFIG_KLASS_VMMR_ENABLED = false;

        /**
         * Vehicle Make Model Recognition (VMMR) is a classification problem and the result is the top K most probable classes. This configuration entry defines K.
         * JSON name: "klass_vmmr_topk"
         * Default: 5
         * type: integer
         * pattern: [1, inf[
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vmmr-topk
         */
        const int CONFIG_KLASS_VMMR_TOPK = 5;

        /**
         * Whether to enable Vehicle Body Style Recognition (VBSR) function (https://www.doubango.org/SDKs/anpr/docs/Features.html#features-vehiclebodystylerecognition).
         * To avoid adding latency to the pipeline only enable this function if you really need it.
         * JSON name: "klass_vbsr_enabled"
         * Default: false
         * type: bool
         * pattern: true | false
         * More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vbsr-enabled
         */
        const bool CONFIG_KLASS_VBSR_ENABLED = false;

        /**
         * Vehicle Body Style Recognition (VBSR)  is a classification problem and the result is the top K most probable classes. This configuration entry defines K.
         * JSON name: "klass_vbsr_topk"
         * Default: 5
         * type: integer
         * pattern: [1, inf[
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vbsr-topk
         */
        const int CONFIG_KLASS_VBSR_TOPK = 5;

        /**
         * Whether to enable License Plate Recognition (LPR) function.
         * JSON name: "recogn_enabled"
         * Default: true
         * type: bool
         * pattern: true | false
         * More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#recogn-enabled
         */
        const bool CONFIG_RECOGN_ENABLED = true;

        /**
         * Defines a charset (Alphabet) to use for the recognizer.
         * JSON name: "recogn_charset"
         * Default: "latin"
         * type: string
         * pattern: "latin" | "korean" | "chinese" | "brazilian" | "israeli"
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#recogn-charset
         */
        const String CONFIG_RECOGN_CHARSET = "latin";

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
        const String CONFIG_RECOGN_SCORE_TYPE = "cumprod";

        /**
         * Define a threshold for the overall recognition score. Any recognition with a score below that threshold will be ignored.
         * The overall score is computed based on "recogn_score_type". 0.f being poor confidence and 1.f excellent confidence.
         * JSON name: "recogn_minscore"
         * Default: 0.2f
         * type: float
         * pattern: ]0.f, 1.f]
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#recogn-minscore
         */
        const double CONFIG_RECOGN_MINSCORE = 0.2; // 20%


        static void Main(String[] args)
        {
            // Parse arguments
            IDictionary<String, String> parameters = ParseArgs(args);

            // Make sur the image is provided using args
            if (!parameters.ContainsKey("--image"))
            {
                Console.Error.WriteLine("--image required");
                throw new Exception("--image required");
            }
            // Extract assets folder
            // https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#assets-folder
            if (!parameters.ContainsKey("--assets"))
            {
                Console.Error.WriteLine("--assets required");
                throw new Exception("--assets required");
            }
            String assetsFolder = parameters["--assets"];

            // Charset - Optional
            // https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#charset
            String charsetAkaAlphabet = parameters.ContainsKey("--charset")
                ? parameters["--charset"] : CONFIG_RECOGN_CHARSET;

            // License data - Optional
            // https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#license-token-data
            String tokenDataBase64 = parameters.ContainsKey("--tokendata")
                ? parameters["--tokendata"] : String.Empty;

            // Initialize the engine: Load deep learning models and init GPU shaders
            // Make sure de disable VS hosting process to see logs from native code: https://social.msdn.microsoft.com/Forums/en-US/5da6cdb2-bc2b-4fff-8adf-752b32143dae/printf-from-dll-in-console-app-in-visual-studio-c-2010-express-does-not-output-to-console-window?forum=Vsexpressvcs
            // This function should be called once.
            // https://www.doubango.org/SDKs/anpr/docs/cpp-api.html#_CPPv4N15ultimateAlprSdk16UltAlprSdkEngine4initEPKcPK34UltAlprSdkParallelDeliveryCallback
            UltAlprSdkResult result = CheckResult("Init", UltAlprSdkEngine.init(BuildJSON(charsetAkaAlphabet, assetsFolder, tokenDataBase64)));

            // Decode the JPEG/PNG/BMP file
            String file = parameters["--image"];
            if (!System.IO.File.Exists(file))
            {
                throw new System.IO.FileNotFoundException("File not found:" + file);
            }
            Bitmap image = new Bitmap(file);
            if (Image.GetPixelFormatSize(image.PixelFormat) == 24 && ((image.Width * 3) & 3) != 0)
            {
                //!\\ Not DWORD aligned -> the stride will be multiple of 4-bytes instead of 3-bytes
                // ultimateMICR requires stride to be in samples unit instead of in bytes
                Console.Error.WriteLine(String.Format("//!\\ The image width ({0}) not a multiple of DWORD.", image.Width));
                image = new Bitmap(image, new Size((image.Width + 3) & -4, image.Height));
            }
            int bytesPerPixel = Image.GetPixelFormatSize(image.PixelFormat) >> 3;
            if (bytesPerPixel != 1 && bytesPerPixel != 3 && bytesPerPixel != 4)
            {
                throw new System.Exception("Invalid BPP:" + bytesPerPixel);
            }

            // Extract Exif orientation
            const int ExifOrientationTagId = 0x112;
            int orientation = 1;
            if (Array.IndexOf(image.PropertyIdList, ExifOrientationTagId) > -1)
            {
                int orientation_ = image.GetPropertyItem(ExifOrientationTagId).Value[0];
                if (orientation_ >= 1 && orientation_ <= 8)
                {
                    orientation = orientation_;
                }
            }

            // Processing: Detection + recognition
            // First inference is expected to be slow (deep learning models mapping to CPU/GPU memory)
            BitmapData imageData = image.LockBits(new Rectangle(0, 0, image.Width, image.Height), ImageLockMode.ReadOnly, image.PixelFormat);
            try
            {
                // For packed formats (RGB-family): https://www.doubango.org/SDKs/anpr/docs/cpp-api.html#_CPPv4N15ultimateAlprSdk16UltAlprSdkEngine7processEK22ULTALPR_SDK_IMAGE_TYPEPKvK6size_tK6size_tK6size_tKi
                // For YUV formats (data from camera): https://www.doubango.org/SDKs/anpr/docs/cpp-api.html#_CPPv4N15ultimateAlprSdk16UltAlprSdkEngine7processEK22ULTALPR_SDK_IMAGE_TYPEPKvPKvPKvK6size_tK6size_tK6size_tK6size_tK6size_tK6size_tKi
                // For compressed data (jpeg, png...): https://www.doubango.org/SDKs/anpr/docs/cpp-api.html#_CPPv4N15ultimateAlprSdk16UltAlprSdkEngine7processEPKvK6size_t
                result = CheckResult("Process", UltAlprSdkEngine.process(
                        (bytesPerPixel == 1) ? ULTALPR_SDK_IMAGE_TYPE.ULTALPR_SDK_IMAGE_TYPE_Y : (bytesPerPixel == 4 ? ULTALPR_SDK_IMAGE_TYPE.ULTALPR_SDK_IMAGE_TYPE_BGRA32 : ULTALPR_SDK_IMAGE_TYPE.ULTALPR_SDK_IMAGE_TYPE_BGR24),
                        imageData.Scan0,
                        (uint)imageData.Width,
                        (uint)imageData.Height,
                        (uint)(imageData.Stride / bytesPerPixel),
                        orientation
                    ));
                // Print result to console
                Console.WriteLine("Result: {0}", result.json());
            }
            finally
            {
                image.UnlockBits(imageData);
            }

            // Write until user press a key
            Console.WriteLine("Press any key to terminate !!");
            Console.Read();

            // Now that you're done, deInit the engine before exiting
            CheckResult("DeInit", UltAlprSdkEngine.deInit());
        }

        static IDictionary<String, String> ParseArgs(String[] args)
        {
            Console.WriteLine("Args: {0}", string.Join(" ", args));

            if ((args.Length & 1) != 0)
            {
                String errMessage = String.Format("Number of args must be even: {0}", args.Length);
                Console.Error.WriteLine(errMessage);
                throw new Exception(errMessage);
            }

            // Parsing
            Dictionary<String, String> values = new Dictionary<String, String>();
            for (int index = 0; index < args.Length; index += 2)
            {
                String key = args[index];
                if (key.Length < 2 || key[0] != '-' || key[1] != '-')
                {
                    String errMessage = String.Format("Invalid key: {0}", key);
                    Console.Error.WriteLine(errMessage);
                    throw new Exception(errMessage);
                }
                values[key] = args[index + 1].Replace("$(ProjectDir)", Properties.Resources.RecognizerProjectDir.Trim()); // Patch path to use project directory
            }
            return values;
        }

        static UltAlprSdkResult CheckResult(String functionName, UltAlprSdkResult result)
        {
            if (!result.isOK())
            {
                String errMessage = String.Format("{0}: Execution failed: {1}", new String[] { functionName, result.json() });
                Console.Error.WriteLine(errMessage);
                throw new Exception(errMessage);
            }
            return result;
        }

        // https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html
        static String BuildJSON(String charsetAkaAlphabet, String assetsFolder = "", String tokenDataBase64 = "")
        {
            return new JavaScriptSerializer().Serialize(new
            {
                debug_level = CONFIG_DEBUG_LEVEL,
                debug_write_input_image_enabled = CONFIG_DEBUG_WRITE_INPUT_IMAGE,
                debug_internal_data_path = CONFIG_DEBUG_INTERNAL_DATA_PATH,

                num_threads = CONFIG_NUM_THREADS,
                max_latency = CONFIG_MAX_LATENCY,
                night_vision_enabled = CONFIG_NIGHT_VISION_ENABLED,
                batching_mode = CONFIG_BATCHING_MODE,
                backend = CONFIG_BACKEND,
                platform = CONFIG_PLATFORM,
                asm_enabled = CONFIG_ASM_ENABLED,
                intrin_enabled = CONFIG_INTRIN_ENABLED,

                detect_roi = CONFIG_DETECT_ROI,
                detect_minscore = CONFIG_DETECT_MINSCORE,
                detect_miniou = CONFIG_DETECT_MINIOU,

                pyramidal_search_enabled = CONFIG_PYRAMIDAL_SEARCH_ENABLED,
                pyramidal_search_minscore = CONFIG_PYRAMIDAL_SEARCH_MINSCORE,
                pyramidal_search_min_image_size_inpixels = CONFIG_PYRAMIDAL_SEARCH_MIN_IMAGE_SIZE_INPIXELS,
                pyramidal_search_miniou = CONFIG_PYRAMIDAL_SEARCH_MINIOU,

                klass_lpci_enabled = CONFIG_KLASS_LPCI_ENABLED,
                klass_lpci_topk = CONFIG_KLASS_LPCI_TOPK,

                klass_vcr_enabled = CONFIG_KLASS_VCR_ENABLED,
                klass_vcr_topk = CONFIG_KLASS_VCR_TOPK,

                klass_vmmr_enabled = CONFIG_KLASS_VMMR_ENABLED,
                klass_vmmr_topk = CONFIG_KLASS_VMMR_TOPK,

                klass_vbsr_enabled = CONFIG_KLASS_VBSR_ENABLED,
                klass_vbsr_topk = CONFIG_KLASS_VBSR_TOPK,

                recogn_enabled = CONFIG_RECOGN_ENABLED,
                recogn_score_type = CONFIG_RECOGN_SCORE_TYPE,
                recogn_minscore = CONFIG_RECOGN_MINSCORE,

                // Value added using command line args
                assets_folder = assetsFolder,
                recogn_charset = charsetAkaAlphabet,
                license_token_data = tokenDataBase64,
            });
        }        
    }
}
