/* Copyright (C) 2011-2020 Doubango Telecom <https://www.doubango.org>
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
			[--charset <recognition-charset:latin/korean/chinese>] \
			[--num_threads <number of threads:[1, inf]>] \
			[--ienv_enabled <whether-to-enable-IENV:true/false>] \
			[--openvino_enabled <whether-to-enable-OpenVINO:true/false>] \
			[--openvino_device <openvino_device-to-use>] \
			[--npu_enabled <whether-to-enable-NPU-acceleration:true/false>] \
			[--trt_enabled <whether-to-enable-NPU-acceleration:true/false>] \
			[--simd_enabled <whether-to-enable-SIMD-acceleration:true/false>] \
			[--klass_lpci_enabled <whether-to-enable-LPCI:true/false>] \
			[--klass_vcr_enabled <whether-to-enable-VCR:true/false>] \
			[--klass_vmmr_enabled <whether-to-enable-VMMR:true/false>] \
			[--klass_vbsr_enabled <whether-to-enable-VBSR:true/false>] \
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
			--charset latin \
			--tokenfile C:/Projects/GitHub/ultimate/ultimateALPR/SDK_dev/tokens/windows-iMac.lic
*/

#include <ultimateALPR-SDK-API-PUBLIC.h>
#include "../alpr_utils.h"
#include <chrono>
#include <vector>
#include <algorithm>
#include <random>
#include <mutex>
#include <condition_variable>
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
"\"gpgpu_enabled\": true,"
"\"asm_enabled\": true,"
"\"intrin_enabled\": true,"
"\"max_latency\": -1,"
""
"\"klass_vcr_gamma\": 1.5,"
""
"\"detect_tf_gpu_memory_alloc_max_percent\": 1.0,"
"\"recogn_tf_gpu_memory_alloc_max_percent\": 1.0,"
"\"pyramidal_search_tf_gpu_memory_alloc_max_percent\": 1.0,"
""
"\"detect_roi\": [0, 0, 0, 0],"
"\"detect_minscore\": 0.1,"
""
"\"pyramidal_search_enabled\": false,"
"\"pyramidal_search_sensitivity\": 0.28,"
"\"pyramidal_search_minscore\": 0.8,"
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

// Including <Windows.h> add clashes between "std::max" and "::max"
#define ULTAPR_MAX(a, b) (((a) > (b)) ? (a) : (b))

/*
* Parallel callback function used for notification. Not mandatory.
* More info about parallel delivery: https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html
*/
static size_t parallelNotifCount = 0;
static std::condition_variable parallelNotifCondVar;
class MyUltAlprSdkParallelDeliveryCallback : public UltAlprSdkParallelDeliveryCallback {
	virtual void onNewResult(const UltAlprSdkResult* result) const override {
		ULTALPR_SDK_ASSERT(result != nullptr);
		const std::string& json = result->json();
		// Printing to the console could be very slow and delayed -> stop displaying the result as soon as all plates are processed
		ULTALPR_SDK_PRINT_INFO("MyUltAlprSdkParallelDeliveryCallback::onNewResult(%d, %s, %zu): %s",
			result->code(),
			result->phrase(),
			++parallelNotifCount,
			!json.empty() ? json.c_str() : "{}"
		);
		parallelNotifCondVar.notify_one();
	}
};

static void printUsage(const std::string& message = "");

/*
* Entry point
*/
int main(int argc, char *argv[])
{
	// local variables
	UltAlprSdkResult result;
	MyUltAlprSdkParallelDeliveryCallback parallelDeliveryCallbackCallback;
	std::string assetsFolder, licenseTokenData, licenseTokenFile;
	bool isParallelDeliveryEnabled = true;
	std::string numThreads = "-1";
	bool isRectificationEnabled = false;
	bool isIENVEnabled = false;
	bool isOpenVinoEnabled =
#if defined(__arm__) || defined(__thumb__) || defined(__TARGET_ARCH_ARM) || defined(__TARGET_ARCH_THUMB) || defined(_ARM) || defined(_M_ARM) || defined(_M_ARMT) || defined(__arm) || defined(__aarch64__)
		false;
#else // x86-64
		true;
#endif
	bool isNpuEnabled = true; // Amlogic, NXP...
	bool isTensorRTEnabled = false; // NVIDIA TensorRT
	bool isSimdEnabled = true; // Assembler and Intrinsics (SSE, AVX, MMX, NEON...)
	bool isKlassLPCI_Enabled = false;
	bool isKlassVCR_Enabled = false;
	bool isKlassVMMR_Enabled = false;
	bool isKlassVBSR_Enabled = false;
	std::string charset = "latin";
	std::string openvinoDevice = "CPU";
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
	if (args.find("--charset") != args.end()) {
		charset = args["--charset"];
	}
	if (args.find("--num_threads") != args.end()) {
		numThreads = args["--num_threads"];
	}
	if (args.find("--rectify") != args.end()) {
		isRectificationEnabled = (args["--rectify"].compare("true") == 0);
	}
	if (args.find("--ienv_enabled") != args.end()) {
		isIENVEnabled = (args["--ienv_enabled"].compare("true") == 0);
	}
	if (args.find("--openvino_enabled") != args.end()) {
		isOpenVinoEnabled = (args["--openvino_enabled"].compare("true") == 0);
	}
	if (args.find("--openvino_device") != args.end()) {
		openvinoDevice = args["--openvino_device"];
	}
	if (args.find("--npu_enabled") != args.end()) {
		isNpuEnabled = (args["--npu_enabled"].compare("true") == 0);
	}
	if (args.find("--trt_enabled") != args.end()) {
		isTensorRTEnabled = (args["--trt_enabled"].compare("true") == 0);
	}
	if (args.find("--simd_enabled") != args.end()) {
		isSimdEnabled = (args["--simd_enabled"].compare("true") == 0);
	}
	if (args.find("--klass_lpci_enabled") != args.end()) {
		isKlassLPCI_Enabled = (args["--klass_lpci_enabled"].compare("true") == 0);
	}
	if (args.find("--klass_vcr_enabled") != args.end()) {
		isKlassVCR_Enabled = (args["--klass_vcr_enabled"].compare("true") == 0);
	}
	if (args.find("--klass_vmmr_enabled") != args.end()) {
		isKlassVMMR_Enabled = (args["--klass_vmmr_enabled"].compare("true") == 0);
	}
	if (args.find("--klass_vbsr_enabled") != args.end()) {
		isKlassVBSR_Enabled = (args["--klass_vbsr_enabled"].compare("true") == 0);
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
	if (!charset.empty()) {
		jsonConfig += std::string(",\"charset\": \"") + charset + std::string("\"");
	}
	jsonConfig += std::string(",\"num_threads\": ") + (numThreads);
	jsonConfig += std::string(",\"recogn_rectify_enabled\": ") + (isRectificationEnabled ? "true" : "false");	
	jsonConfig += std::string(",\"ienv_enabled\": ") + (isIENVEnabled ? "true" : "false");
	jsonConfig += std::string(",\"openvino_enabled\": ") + (isOpenVinoEnabled ? "true" : "false");
	if (!openvinoDevice.empty()) {
		jsonConfig += std::string(",\"openvino_device\": \"") + openvinoDevice + std::string("\"");
	}
	jsonConfig += std::string(",\"npu_enabled\": ") + (isNpuEnabled ? "true" : "false");
	jsonConfig += std::string(",\"trt_enabled\": ") + (isTensorRTEnabled ? "true" : "false");
	jsonConfig += std::string(",\"asm_enabled\": ") + (isSimdEnabled ? "true" : "false");
	jsonConfig += std::string(",\"intrin_enabled\": ") + (isSimdEnabled ? "true" : "false");
	jsonConfig += std::string(",\"klass_lpci_enabled\": ") + (isKlassLPCI_Enabled ? "true" : "false");
	jsonConfig += std::string(",\"klass_vcr_enabled\": ") + (isKlassVCR_Enabled ? "true" : "false");
	jsonConfig += std::string(",\"klass_vmmr_enabled\": ") + (isKlassVMMR_Enabled ? "true" : "false");
	jsonConfig += std::string(",\"klass_vbsr_enabled\": ") + (isKlassVBSR_Enabled ? "true" : "false");
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

	const int numPositives = static_cast<int>(loopCount * percentPositives);
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
	// Compute the estimated frame rate.
	// At this step all frames are already processed but the result could be still on the delivery
	// queue due to the console display latency. You can move here the code used to wait until all
	// messages are displayed to include the delivery latency.
	const std::chrono::high_resolution_clock::time_point timeEnd = std::chrono::high_resolution_clock::now();
	const double elapsedTimeInMillis = std::chrono::duration_cast<std::chrono::duration<double >>(timeEnd - timeStart).count() * 1000.0;
	ULTALPR_SDK_PRINT_INFO("Elapsed time (ALPR) = [[[ %lf millis ]]]", elapsedTimeInMillis);

	// Printing to the console is very slow and use a low priority thread.
	// Wait until all results are displayed.
	if (isParallelDeliveryEnabled) {
		static std::mutex parallelNotifMutex;
		std::unique_lock<std::mutex > lk(parallelNotifMutex);
		parallelNotifCondVar.wait_for(lk, 
			std::chrono::milliseconds(1500), // maximum number of millis to wait for before giving up, must never wait this long unless your positive image doesn't contain a plate at all
			[&numPositives] { return (parallelNotifCount == numPositives); }
		);
	}

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
		"\t[--charset <recognition-charset:latin/korean/chinese>] \n"
		"\t[--num_threads <number of threads : [1, inf]>] \n"
		"\t[--ienv_enabled <whether-to-enable-IENV:true/false>] \n"
		"\t[--openvino_enabled <whether-to-enable-OpenVINO:true/false>] \n"
		"\t[--openvino_device <openvino_device-to-use>] \n"
		"\t[--npu_enabled <whether-to-enable-NPU-acceleration:true/false>] \n"
		"\t[--trt_enabled <whether-to-enable-TensorRT-acceleration:true/false>] \n"
		"\t[--simd_enabled <whether-to-enable-SIMD-acceleration:true/false>] \n"
		"\t[--klass_lpci_enabled <whether-to-enable-LPCI:true/false>] \n"
		"\t[--klass_vcr_enabled <whether-to-enable-VCR:true/false>] \n"
		"\t[--klass_vmmr_enabled <whether-to-enable-VMMR:true/false>] \n"
		"\t[--klass_vbsr_enabled <whether-to-enable-VBSR:true/false>] \n"
		"\t[--loops <number-of-times-to-run-the-loop:[1, inf]>] \n"
		"\t[--rate <positive-rate:[0.0, 1.0]>] \n"
		"\t[--parallel <whether-to-enable-parallel-mode:true / false>] \n"
		"\t[--rectify <whether-to-enable-rectification-layer:true / false>]\n"
		"\t[--tokenfile <path-to-license-token-file>] \n"
		"\t[--tokendata <base64-license-token-data>] \n"
		"\n"
		"Options surrounded with [] are optional.\n"
		"\n"
		"--positive: Path to an image(JPEG/PNG/BMP) with a license plate. This image will be used to evaluate the recognizer. You can use default image at ../../../assets/images/lic_us_1280x720.jpg.\n\n"
		"--negative: Path to an image(JPEG/PNG/BMP) without a license plate. This image will be used to evaluate the detector. You can use default image at ../../../assets/images/london_traffic.jpg.\n\n"
		"--assets: Path to the assets folder containing the configuration files and models. Default value is the current folder.\n\n"
		"--charset: Defines the recognition charset value (latin, korean, chinese...). Default: latin.\n\n"
		"--num_threads: Number of threads to use. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#num-threads. Default: -1.\n\n"
		"--ienv_enabled: Whether to enable Image Enhancement for Night-Vision (IENV). More info about IENV at https://www.doubango.org/SDKs/anpr/docs/Features.html#image-enhancement-for-night-vision-ienv. Default: false.\n\n"
		"--openvino_enabled: Whether to enable OpenVINO. Tensorflow will be used when OpenVINO is disabled. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#openvino-enabled. Default: true.\n\n"
		"--openvino_device: Defines the OpenVINO device to use (CPU, GPU, FPGA...). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#openvino_device. Default: CPU.\n\n"
		"--npu_enabled: Whether to enable NPU acceleration (Amlogic, NXP...). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#npu-enabled. Default: true.\n\n"
		"--trt_enabled: Whether to enable NVIDIA TensorRT acceleration. This will disable OpenVINO. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#trt-enabled. Default: false.\n\n"
		"--simd_enabled: Whether to enable SIMD acceleration -Assembler and Intrinsics- (SSE, AVX, MMX, NEON...). More info at https://en.wikipedia.org/wiki/Single_instruction,_multiple_data. Default: true.\n\n"
		"--klass_lpci_enabled: Whether to enable License Plate Country Identification (LPCI). More info at https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-country-identification-lpci. Default: false.\n\n"
		"--klass_vcr_enabled: Whether to enable Vehicle Color Recognition (VCR). More info at https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-color-recognition-vcr. Default: false.\n\n"
		"--klass_vmmr_enabled: Whether to enable Vehicle Make Model Recognition (VMMR). More info at https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-make-model-recognition-vmmr. Default: false.\n\n"
		"--klass_vbsr_enabled: Whether to enable Vehicle Body Style Recognition (VBSR). More info at https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-make-model-recognition-vbsr. Default: false.\n\n"
		"--loops: Number of times to run the processing pipeline.\n\n"
		"--rate: Percentage value within[0.0, 1.0] defining the positive rate. The positive rate defines the percentage of images with a plate.\n\n"
		"--parallel: Whether to enabled the parallel mode. More info about the parallel mode at https ://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html. Default: true.\n\n"
		"--rectify: Whether to enable the rectification layer. More info about the rectification layer at https://www.doubango.org/SDKs/anpr/docs/Rectification_layer.html. Default: false.\n\n"
		"--tokenfile: Path to the file containing the base64 license token if you have one. If not provided then, the application will act like a trial version. Default: null.\n\n"
		"--tokendata: Base64 license token if you have one. If not provided then, the application will act like a trial version. Default: null.\n\n"
		"********************************************************************************\n"
	);
}
