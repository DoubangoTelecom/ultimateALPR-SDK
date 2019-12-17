/* Copyright (C) 2011-2019 Doubango Telecom <https://www.doubango.org>
* File author: Mamadou DIOP (Doubango Telecom, France).
* License: For non commercial use only.
* Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
* WebSite: https://www.doubango.org/webapps/alpr/
*/

// More info about benchmark application: https://www.doubango.org/SDKs/anpr/docs/Benchmark.html
/*
	https://github.com/DoubangoTelecom/ultimateALPR/blob/master/SDK_dist/samples/c++/benchmark/README.md
	Usage: 
		benchmark \
			--positive <path-to-image-with-a-plate> \
			--negative <path-to-image-without-a-plate> \
			[--assets <path-to-assets-folder>] \
			[--loops <number-of-times-to-run-the-loop:[1, inf]>] \
			[--rate <positive-rate:[0.0, 1.0]>] \
			[--parallel <whether-to-enable-parallel-mode:true/false>] \
			[--rectify <whether-to-enable-rectification-layer:true/false>] \
			[--tokenfile <path-to-license-token-file>] \
			[--tokendata <base64-license-token-data>]

	Example:
		benchmark \
			--positive C:/Projects/GitHub/ultimate/ultimateALPR/SDK_dist/assets/images/lic_us_1280x720.jpg \
			--negative C:/Projects/GitHub/ultimate/ultimateALPR/SDK_dist/assets/images/london_traffic.jpg \
			--loops 100 \
			--rate 0.2 \
			--parallel true \
			--rectify false \
			--assets C:/Projects/GitHub/ultimate/ultimateALPR/SDK_dist/assets \
			--tokenfile C:/Projects/GitHub/ultimate/ultimateALPR/SDK_dev/tokens/windows-iMac.lic
		
*/

#include <ultimateALPR-SDK-API-PUBLIC.h>
#include "../alpr_utils.h"
#include <chrono>
#include <vector>
#include <algorithm>
#include <random>
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
"\"detect_pyramidal_search_enabled\": false,"
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
	bool isParallelDeliveryEnabled = true;
	bool isRectificationEnabled = false;
	size_t loopCount = 100;
	double percentPositives = .2; // 20%
	std::string pathFilePositive;
	std::string pathFileNegative;

	// Parsing args
	std::map<std::string, std::string > args;
	if (!alprParseArgs(argc, argv, args)) {
		printUsage();
		return -1;
	}
	if (args.find("--positive") == args.end()) {
		printUsage("--positive required");
		return -1;
	}
	if (args.find("--negative") == args.end()) {
		printUsage("--negative required");
		return -1;
	}
	pathFilePositive = args["--positive"];
	pathFileNegative = args["--negative"];
	if (args.find("--rate") != args.end()) {
		const double rate = std::atof(args["--rate"].c_str());
		if (rate > 1.0 || rate < 0.0) {
			printUsage("--rate must be within [0.0, 1.0]");
			return -1;
		}
		percentPositives = rate;
	}
	if (args.find("--loops") != args.end()) {
		const int loops = std::atoi(args["--loops"].c_str());
		if (loops < 1) {
			printUsage("--loops must be within [1, inf]");
			return -1;
		}
		loopCount = static_cast<size_t>(loops);
	}
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

	// Read files
	// Positive: the file contains at least one plate
	// Negative: the file doesn't contain a plate
	// Change positive rates to evaluate the detector versus recognizer
	AlprFile filePositive, fileNegative;
	if (!alprDecodeFile(pathFilePositive, filePositive)) {
		ULTALPR_SDK_PRINT_INFO("Failed to read positive file: %s", pathFilePositive.c_str());
		return -1;
	}
	if (!alprDecodeFile(pathFileNegative, fileNegative)) {
		ULTALPR_SDK_PRINT_INFO("Failed to read positive file: %s", pathFilePositive.c_str());
		return -1;
	}

	// Create image indices
	std::vector<size_t> indices(loopCount, 0);
	const int numPositives = (int)std::max(loopCount * percentPositives, 1.);
	for (int i = 0; i < numPositives; ++i) {
		indices[i] = 1; // positive index
	}
	std::shuffle(std::begin(indices), std::end(indices), std::default_random_engine{}); // make the indices random

	// Init
	ULTALPR_SDK_PRINT_INFO("Starting benchmark...");
	ULTALPR_SDK_ASSERT((result = UltAlprSdkEngine::init(
		ASSET_MGR_PARAM()
		jsonConfig.c_str(),
		isParallelDeliveryEnabled ? &parallelDeliveryCallbackCallback : nullptr
	)).isOK());

	// Warm up:
	// First time the SDK is called we'll be loading the models into CPU or GPU and initializing
	// some internal variables -> do not include this part in te timing.
	// The warm up function will make fake inference to force the engine to load the models and init the vars.
	if (loopCount > 1) {
		ULTALPR_SDK_ASSERT((result = UltAlprSdkEngine::warmUp(
			filePositive.type
		)).isOK());
	}

	// Recognize/Process
	const std::chrono::high_resolution_clock::time_point timeStart = std::chrono::high_resolution_clock::now();
	const AlprFile* files[2] = { &fileNegative, &filePositive };
	for (const auto& indice : indices) {
		const AlprFile* file = files[indice];
		ULTALPR_SDK_ASSERT((result = UltAlprSdkEngine::process(
			file->type,
			file->uncompressedData,
			file->width,
			file->height
		)).isOK());
	}
	const std::chrono::high_resolution_clock::time_point timeEnd = std::chrono::high_resolution_clock::now();
	const double elapsedTimeInMillis = std::chrono::duration_cast<std::chrono::duration<double >>(timeEnd - timeStart).count() * 1000.0;
	ULTALPR_SDK_PRINT_INFO("Elapsed time (ALPR) = [[[ %lf millis ]]]", elapsedTimeInMillis);

	// Print latest result
	const std::string& json_ = result.json();
	if (!json_.empty()) {
		ULTALPR_SDK_PRINT_INFO("result: %s", json_.c_str());
	}

	// Print estimated frame rate
	const double estimatedFps = 1000.f / (elapsedTimeInMillis / (double)loopCount);
	ULTALPR_SDK_PRINT_INFO("*** elapsedTimeInMillis: %lf, estimatedFps: %lf ***", elapsedTimeInMillis, estimatedFps);

	ULTALPR_SDK_PRINT_INFO("Press any key to terminate !!");
	getchar();

	// DeInit
	ULTALPR_SDK_PRINT_INFO("Ending benchmark...");
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
		"benchmark\n"
		"\t--positive <path-to-image-with-a-plate> \n"
		"\t--negative <path-to-image-without-a-plate> \n"
		"\t[--assets <path-to-assets-folder>] \n"
		"\t[--loops <number-of-times-to-run-the-loop:[1, inf]>] \n"
		"\t[--rate <positive-rate:[0.0, 1.0]>] \n"
		"\t[--parallel <whether-to-enable-parallel-mode:true / false>] \n"
		"\t[--rectify <whether-to-enable-rectification-layer:true / false>]\n"
		"\t[--tokenfile <path-to-license-token-file>] \n"
		"\t[--tokendata <base64-license-token-data>] \n"
		"\n"
		"Options surrounded with [] are optional.\n"
		"\n"
		"--positive: Path to an image(JPEG/PNG/BMP) with a license plate.This image will be used to evaluate the recognizer. You can use default image at ../../../assets/images/lic_us_1280x720.jpg.\n\n"
		"--negative: Path to an image(JPEG/PNG/BMP) without a license plate.This image will be used to evaluate the decoder. You can use default image at ../../../assets/images/london_traffic.jpg.\n\n"
		"--assets: Path to the assets folder containing the configuration files and models.Default value is the current folder.\n\n"
		"--loops: Number of times to run the processing pipeline.\n\n"
		"--rate: Percentage value within[0.0, 1.0] defining the positive rate. The positive rate defines the percentage of images with a plate.\n\n"
		"--parallel: Whether to enabled the parallel mode. More info about the parallel mode at https ://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html. Default: true.\n\n"
		"--rectify: Whether to enable the rectification layer.More info about the rectification layer at https://www.doubango.org/SDKs/anpr/docs/Rectification_layer.html. Default: false.\n\n"
		"--tokenfile: Path to the file containing the base64 license token if you have one. If not provided then, the application will act like a trial version. Default: null.\n\n"
		"--tokendata: Base64 license token if you have one. If not provided then, the application will act like a trial version. Default: null.\n\n"
		"********************************************************************************\n"
	);
}
