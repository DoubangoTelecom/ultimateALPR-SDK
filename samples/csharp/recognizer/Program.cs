/* Copyright (C) 2011-2020 Doubango Telecom <https://www.doubango.org>
* File author: Mamadou DIOP (Doubango Telecom, France).
* License: For non commercial use only.
* Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
* WebSite: https://www.doubango.org/webapps/alpr/
*/
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.Web.Script.Serialization;
// Include ultimateALPR namespace
using org.doubango.ultimateAlpr.Sdk;

/*
	https://github.com/DoubangoTelecom/ultimateALPR-SDK/blob/master/samples/c%2B%2B/recognizer/README.md
	Usage: 
		recognizer \
			---image <path-to-image-with-to-recognize> \
            [--assets <path-to-assets-folder>] \
			[--rectify <whether-to-enable-rectification-layer:true/false>] \
            [--charset <recognition-charset:latin/korean/chinese>] \
			[--tokenfile <path-to-license-token-file>] \
			[--tokendata <base64-license-token-data>]
	Example:
		recognizer \
			--image "ultimateALPR-SDK/assets/images/lic_us_1280x720.jpg" \
            --assets "ultimateALPR-SDK/assets" \
			--rectify false \
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
        const String CONFIG_DEBUG_DEBUG_INTERNAL_DATA_PATH = ".";

        /**
         * Defines the maximum number of threads to use.
         * You should not change this value unless you know what you’re doing. Set to -1 to let the SDK choose the right value.
         * The right value the SDK will choose will likely be equal to the number of virtual core.
         * For example, on an octa-core device the maximum number of threads will be 8.
         * JSON name: "num_threads"
         * Default: -1
         * type: int
         * pattern: [-inf, +inf]
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#num-threads
         */
        const int CONFIG_NUM_THREADS = -1;

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
        const bool CONFIG_GPGPU_ENABLED = true;

        /**
         * Define a threshold for the detection score. Any detection with a score below that threshold will be ignored. 0.f being poor confidence and 1.f excellent confidence.
         * JSON name: "detect_minscore",
         * Default: 0.3f
         * type: float
         * pattern: ]0.f, 1.f]
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#detect-minscore
         */
        const double CONFIG_DETECT_MINSCORE = 0.3;

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
         * Defines a charset (Alphabet) to use for the recognizer.
         * JSON name: "charset"
         * Default: "latin"
         * type: string
         * pattern: "latin" | "koran"
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#charset
         */
        const String CONFIG_CHARSET = "latin";

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
         * Defines how sensitive the pyramidal search anchor resolution function should be. The higher this value is, the higher the number of pyramid levels will be.
         * More levels means better accuracy but higher CPU usage and inference time.
         * Pyramidal search will be disabled if this value is equal to 0.
         * JSON name: "pyramidal_search_sensitivity"
         * Default: 0.28f
         * type: float
         * pattern: [0.f, 1.f]
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#pyramidal-search-sensitivity
         */
        const double CONFIG_PYRAMIDAL_SEARCH_SENSITIVITY = 0.33; // 33%

        /**
         * Defines a threshold for the detection score associated to the plates retrieved after pyramidal search.
         * Any detection with a score below that threshold will be ignored.
         * 0.f being poor confidence and 1.f excellent confidence.
         * JSON name: "pyramidal_search_minscore"
         * Default: 0.8f
         * type: float
         * pattern: ]0.f, 1.f]
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#pyramidal-search-minscore
         */
        const double CONFIG_PYRAMIDAL_SEARCH_MINSCORE = 0.3; // 30%

        /**
         * Minimum image size (max[width, height]) in pixels to trigger pyramidal search.
         * Pyramidal search will be disabled if the image size is less than this value. Using pyramidal search on small images is useless.
         * JSON name: "pyramidal_search_min_image_size_inpixels"
         * Default: 800
         * type: integer
         * pattern: [0, inf]
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#pyramidal-search-min-image-size-inpixels
         */
        const int CONFIG_PYRAMIDAL_SEARCH_MIN_IMAGE_SIZE_INPIXELS = 800; // pixels

        /**
         * Define a threshold for the overall recognition score. Any recognition with a score below that threshold will be ignored.
         * The overall score is computed based on "recogn_score_type". 0.f being poor confidence and 1.f excellent confidence.
         * JSON name: "recogn_minscore"
         * Default: 0.3f
         * type: float
         * pattern: ]0.f, 1.f]
         * More info: https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#recogn-minscore
         */
        const double CONFIG_RECOGN_MINSCORE = 0.2; // 20%

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
        const String CONFIG_RECOGN_SCORE_TYPE = "min";

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
        const bool CONFIG_RECOGN_RECTIFY_ENABLED = false;

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
            String assetsFolder = parameters.ContainsKey("--assets")
                ? parameters["--assets"] : String.Empty;

            // Charset - Optional
            // https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#charset
            String charsetAkaAlphabet = parameters.ContainsKey("--charset")
                ? parameters["--charset"] : CONFIG_CHARSET;

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
                debug_internal_data_path = CONFIG_DEBUG_DEBUG_INTERNAL_DATA_PATH,

                num_threads = CONFIG_NUM_THREADS,
                gpgpu_enabled = CONFIG_GPGPU_ENABLED,

                detect_minscore = CONFIG_DETECT_MINSCORE,
                detect_roi = CONFIG_DETECT_ROI,

                pyramidal_search_enabled = CONFIG_PYRAMIDAL_SEARCH_ENABLED,
                pyramidal_search_sensitivity = CONFIG_PYRAMIDAL_SEARCH_SENSITIVITY,
                pyramidal_search_minscore = CONFIG_PYRAMIDAL_SEARCH_MINSCORE,
                pyramidal_search_min_image_size_inpixels = CONFIG_PYRAMIDAL_SEARCH_MIN_IMAGE_SIZE_INPIXELS,

                recogn_minscore = CONFIG_RECOGN_MINSCORE,
                recogn_score_type = CONFIG_RECOGN_SCORE_TYPE,
                recogn_rectify_enabled = CONFIG_RECOGN_RECTIFY_ENABLED,

                // Value added using command line args
                assets_folder = assetsFolder,
                charset = charsetAkaAlphabet,
                license_token_data = tokenDataBase64,
            });
        }        
    }
}
