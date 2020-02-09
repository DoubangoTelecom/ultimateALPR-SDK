/* Copyright (C) 2011-2019 Doubango Telecom <https://www.doubango.org>
* File author: Mamadou DIOP (Doubango Telecom, France).
* License: For non commercial use only.
* Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
* WebSite: https://www.doubango.org/webapps/alpr/
*/

/*
	https://github.com/DoubangoTelecom/ultimateALPR/blob/master/SDK_dist/samples/c++/recognizer/README.md
	Usage: 
		recognizer \
			--image <path-to-image-with-to-recognize> \
			[--parallel <whether-to-enable-parallel-mode:true/false>] \
			[--rectify <whether-to-enable-rectification-layer:true/false>] \
			[--assets <path-to-assets-folder>] \
			[--tokenfile <path-to-license-token-file>] \
			[--tokendata <base64-license-token-data>]

	Example:
		recognizer \
			--image C:/Projects/GitHub/ultimate/ultimateALPR/SDK_dist/assets/images/lic_us_1280x720.jpg \
			--parallel true \
			--rectify false \
			--assets C:/Projects/GitHub/ultimate/ultimateALPR/SDK_dist/assets \
			--tokenfile C:/Projects/GitHub/ultimate/ultimateALPR/SDK_dev/tokens/windows-iMac.lic
		
*/

#include <ultimateALPR-SDK-API-PUBLIC.h>
#include "../alpr_utils.h"
#if defined(_WIN32)
#include <algorithm> // std::replace
#endif

using namespace ultimateAlprSdk;

// Configuration for ANPR deep learning engine
static const char* __jsonConfig =
"{"
"\"debug_level\": \"info\","
"\"debug_write_input_image_enabled\": false,"
"\"debug_internal_data_path\": \".\","
""
"\"num_threads\": -1,"
"\"gpgpu_enabled\": true,"
""
"\"detect_roi\": [0, 0, 0, 0],"
"\"detect_minscore\": 0.1,"
""
"\"pyramidal_search_enabled\": true,"
"\"pyramidal_search_sensitivity\": 0.28,"
"\"pyramidal_search_minscore\": 0.5,"
"\"pyramidal_search_min_image_size_inpixels\": 800,"
""
"\"recogn_minscore\": 0.3,"
"\"recogn_score_type\": \"min\""
"";

// Asset manager used on Android to files in "assets" folder
#if ULTALPR_SDK_OS_ANDROID 
#	define ASSET_MGR_PARAM() __sdk_android_assetmgr, 
#else
#	define ASSET_MGR_PARAM() 
#endif /* ULTALPR_SDK_OS_ANDROID */

/*
* Parallel callback function used for notification. Not mandatory.
* More info about parallel delivery: https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html
*/
class MyUltAlprSdkParallelDeliveryCallback : public UltAlprSdkParallelDeliveryCallback {
	virtual void onNewResult(const UltAlprSdkResult* result) const override {
		static size_t numParallelDeliveryResults = 0;
		ULTALPR_SDK_ASSERT(result != nullptr);
		const std::string& json = result->json();
		ULTALPR_SDK_PRINT_INFO("MyUltAlprSdkParallelDeliveryCallback::onNewResult(%d, %s, %zu): %s",
			result->code(),
			result->phrase(),
			++numParallelDeliveryResults,
			!json.empty() ? json.c_str() : "{}"
		);
	}
};

static void printUsage(const std::string& message = "");

/*
* Entry point
*/
int main(int argc, char *argv[])
{
	// local variables
	UltAlprSdkResult result(0, "OK", "{}");
	MyUltAlprSdkParallelDeliveryCallback parallelDeliveryCallbackCallback;
	std::string assetsFolder, licenseTokenData, licenseTokenFile;
	bool isParallelDeliveryEnabled = false; // Single image -> no need for parallel processing
	bool isRectificationEnabled = false;
	std::string pathFileImage;

	// Parsing args
	std::map<std::string, std::string > args;
	if (!alprParseArgs(argc, argv, args)) {
		printUsage();
		return -1;
	}
	if (args.find("--image") == args.end()) {
		printUsage("--image required");
		return -1;
	}
	pathFileImage = args["--image"];
		
	if (args.find("--parallel") != args.end()) {
		isParallelDeliveryEnabled = (args["--parallel"].compare("true") == 0);
	}
	if (args.find("--assets") != args.end()) {
		assetsFolder = args["--assets"];
#if defined(_WIN32)
		std::replace(assetsFolder.begin(), assetsFolder.end(), '\\', '/');
#endif
	}
	if (args.find("--rectify") != args.end()) {
		isRectificationEnabled = (args["--rectify"].compare("true") == 0);
	}	
	if (args.find("--tokenfile") != args.end()) {
		licenseTokenFile = args["--tokenfile"];
#if defined(_WIN32)
		std::replace(licenseTokenFile.begin(), licenseTokenFile.end(), '\\', '/');
#endif
	}
	if (args.find("--tokendata") != args.end()) {
		licenseTokenData = args["--tokendata"];
	}

	// Update JSON config
	std::string jsonConfig = __jsonConfig;
	if (!assetsFolder.empty()) {
		jsonConfig += std::string(",\"assets_folder\": \"") + assetsFolder + std::string("\"");
	}
	jsonConfig += std::string(",\"recogn_rectify_enabled\": ") + (isRectificationEnabled ? "true" : "false");
	if (!licenseTokenFile.empty()) {
		jsonConfig += std::string(",\"license_token_file\": \"") + licenseTokenFile + std::string("\"");
	}
	if (!licenseTokenData.empty()) {
		jsonConfig += std::string(",\"license_token_data\": \"") + licenseTokenData + std::string("\"");
	}
	
	jsonConfig += "}"; // end-of-config

	// Decode image
	AlprFile fileImage;
	if (!alprDecodeFile(pathFileImage, fileImage)) {
		ULTALPR_SDK_PRINT_INFO("Failed to read image file: %s", pathFileImage.c_str());
		return -1;
	}

	// Init
	ULTALPR_SDK_PRINT_INFO("Starting recognizer...");
	ULTALPR_SDK_ASSERT((result = UltAlprSdkEngine::init(
		ASSET_MGR_PARAM()
		jsonConfig.c_str(),
		isParallelDeliveryEnabled ? &parallelDeliveryCallbackCallback : nullptr
	)).isOK());

	// Recognize/Process
	ULTALPR_SDK_ASSERT((result = UltAlprSdkEngine::process(
		fileImage.type, // If you're using data from your camera then, the type would be YUV-family instead of RGB-family. https://www.doubango.org/SDKs/anpr/docs/cpp-api.html#_CPPv4N15ultimateAlprSdk22ULTALPR_SDK_IMAGE_TYPEE
		fileImage.uncompressedData,
		fileImage.width,
		fileImage.height
	)).isOK());
	ULTALPR_SDK_PRINT_INFO("Processing done.");

	// Print latest result
	if (!isParallelDeliveryEnabled) { // for parallel delivery the result will be printed by the callback function
		const std::string& json_ = result.json();
		if (!json_.empty()) {
			ULTALPR_SDK_PRINT_INFO("result: %s", json_.c_str());
		}
	}

	ULTALPR_SDK_PRINT_INFO("Press any key to terminate !!");
	getchar();

	// DeInit
	ULTALPR_SDK_PRINT_INFO("Ending recognizer...");
	ULTALPR_SDK_ASSERT((result = UltAlprSdkEngine::deInit()).isOK());

	return 0;
}

/*
* Print usage
*/
static void printUsage(const std::string& message /*= ""*/)
{
	if (!message.empty()) {
		ULTALPR_SDK_PRINT_ERROR("%s", message.c_str());
	}

	ULTALPR_SDK_PRINT_INFO(
		"\n********************************************************************************\n"
		"recognizer\n"
		"\t--image <path-to-image-with-to-recognize> \n"
		"\t[--assets <path-to-assets-folder>] \n"
		"\t[--parallel <whether-to-enable-parallel-mode:true / false>] \n"
		"\t[--rectify <whether-to-enable-rectification-layer:true / false>] \n"
		"\t[--tokenfile <path-to-license-token-file>] \n"
		"\t[--tokendata <base64-license-token-data>] \n"
		"\n"
		"Options surrounded with [] are optional.\n"
		"\n"
		"--image: Path to the image(JPEG/PNG/BMP) to process. You can use default image at ../../../assets/images/lic_us_1280x720.jpg.\n\n"
		"--assets: Path to the assets folder containing the configuration files and models. Default value is the current folder.\n\n"
		"--parallel: Whether to enabled the parallel mode.More info about the parallel mode at https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html. Default: true.\n\n"
		"--rectify: Whether to enable the rectification layer. More info about the rectification layer at https ://www.doubango.org/SDKs/anpr/docs/Rectification_layer.html. Default: false.\n\n"
		"--tokenfile: Path to the file containing the base64 license token if you have one. If not provided then, the application will act like a trial version. Default: null.\n\n"
		"--tokendata: Base64 license token if you have one. If not provided then, the application will act like a trial version. Default: null.\n\n"
		"********************************************************************************\n"
	);
}
