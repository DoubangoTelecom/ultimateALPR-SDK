/* Copyright (C) 2011-2026 Doubango Telecom <https://www.doubango.org>
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
			--image <comma-separated-paths-to-images> \
			--assets <path-to-assets-folder> \
			[--charset <recognition-charset:latin/korean/chinese>] \
			[--num_threads <number-of-threads:[1, inf]>] \
			[--nvision_enabled <whether-to-enable-IENV:true/false>] \
			[--platform <target-platform:auto/mobile/desktop>] \
			[--batching_mode <batching-mode:auto/on/off>] \
			[--backend <backend:auto,onnx,tflite,rknn2,trt,vino...>] \
			[--pysearch_enabled <whether-to-pyramidal-search:true/false>]
			[--recogn_enabled <whether-to-enabled-LPR:true/false>]
			[--klass_lpci_enabled <whether-to-enable-LPCI:true/false>] \
			[--klass_vcr_enabled <whether-to-enable-VCR:true/false>] \
			[--klass_vmmr_enabled <whether-to-enable-VMMR:true/false>] \
			[--klass_vbsr_enabled <whether-to-enable-VBSR:true/false>] \
			[--loops <number-of-times-to-run-the-inference-loop:[1, inf]>] \
			[--loopsw <number-of-times-to-run-the-warmup-loop:[1, inf]>] \
			[--parallel <whether-to-enable-parallel-mode:true/false>] \
			[--tokenfile <path-to-license-token-file>] \
			[--tokendata <base64-license-token-data>]

	Example:
		benchmark \
			--image C:/Projects/GitHub/ultimate/ultimateALPR/SDK_dist/assets/images/lic_us_1280x720.jpg \
			--loops 100 \
			--parallel true \
			--assets C:/Projects/GitHub/ultimate/ultimateALPR/SDK_dist/assets \
			--charset latin \
			--tokenfile C:/Projects/GitHub/ultimate/ultimateALPR/SDK_dev/tokens/windows-iMac.lic
*/

#include <ultimateALPR-SDK-API-PUBLIC.h>

#include <chrono>
#include <vector>
#include <algorithm>
#include <random>
#include <mutex>
#include <sstream>
#include <map>
#include <sys/stat.h>
#include <condition_variable>
#if defined(_WIN32)
#	include <Windows.h> // SetConsoleOutputCP
#endif

// Not part of the SDK, used to decode images -> https://github.com/nothings/stb
#define STB_IMAGE_IMPLEMENTATION
#define STB_IMAGE_STATIC
#include "stb_image.h"

using namespace ultimateAlprSdk;

// Configuration for ANPR deep learning engine
static const char* __jsonConfig =
"{"
"\"debug_level\": \"info\","
"\"debug_write_input_image_enabled\": false,"
"\"debug_internal_data_path\": \".\","
""
"\"asm_enabled\": true,"
"\"intrin_enabled\": true,"
""
"\"detect_roi\": [0, 0, 0, 0],"
"\"detect_minscore\": 0.4,"
"\"detect_miniou\": 0.4,"
""
"\"pyramidal_search_minscore\": 0.2,"
"\"pyramidal_search_miniou\": 0.4,"
"\"pyramidal_search_min_image_size_inpixels\": 800,"
""
"\"recogn_minscore\": 0.1,"
"\"recogn_score_type\": \"cumprod\","
""
"\"klass_lpci_topk\": 5,"
""
"\"klass_vcr_topk\": 5,"
""
"\"klass_vmmr_topk\": 5,"
""
"\"klass_vbsr_topk\": 5"
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
static std::string parallelState;
static size_t parallelNotifCount = 0;
static std::condition_variable parallelNotifCondVar;
class MyUltAlprSdkParallelDeliveryCallback : public UltAlprSdkParallelDeliveryCallback {
public:
	MyUltAlprSdkParallelDeliveryCallback(const void* userData = nullptr) : m_pMyDummyData(userData) {}
	virtual void onNewResult(const UltAlprSdkResult* result) const override {
		// Use m_pMyDummyData here if you want
		ULTALPR_SDK_ASSERT(result != nullptr);
		const std::string& json = result->json();
		// Printing to the console could be very slow and delayed -> stop displaying the result as soon as all faces are processed
		ULTALPR_SDK_PRINT_INFO("[%s] MyUltAlprSdkParallelDeliveryCallback::onNewResult(%d, %s, %zu): %s",
			parallelState.c_str(),
			result->code(),
			result->phrase(),
			++parallelNotifCount,
			!json.empty() ? json.c_str() : "{}"
		);
		parallelNotifCondVar.notify_one();
	}
private:
	const void* m_pMyDummyData;
};

struct FileInfo {
	void* uncompressedData = nullptr;
	size_t width = 0;
	size_t height = 0;
	ULTALPR_SDK_IMAGE_TYPE type;
	FileInfo() {}
	FileInfo(const FileInfo& fi) = delete;
	FileInfo(FileInfo&& fi) noexcept // std::move
		: uncompressedData(fi.uncompressedData)
		, width(fi.width)
		, height(fi.height)
		, type(fi.type)
	{ 
		fi.uncompressedData = nullptr;
	}
	virtual ~FileInfo() {
		release();
	}
	void release() {
		if (uncompressedData) {
			free(uncompressedData);
			uncompressedData = nullptr;
		}
	}
	inline bool isValid() const {
		return (uncompressedData != nullptr && width && height);
	}
};

static void printUsage(const std::string& message = "");
static bool parseArgs(int argc, char* argv[], std::map<std::string, std::string >& values);
static bool readFile(const std::string& path, FileInfo& file);
static bool checkResult(const UltAlprSdkResult& result);

/*
* Entry point
*/
int main(int argc, char *argv[])
{
	// Activate UT8 display
#if defined(_WIN32)
	SetConsoleOutputCP(CP_UTF8);
#endif

	// local variables
	UltAlprSdkResult result;
	std::string assetsFolder, licenseTokenData, licenseTokenFile, pluginOptions;
	bool isRecognEnabled = true;
	bool isNightVisionEnabled = false;
	bool isPySearchEnabled = false;
	bool isParallelDeliveryEnabled = true;
	bool isKlassLPCI_Enabled = false;
	bool isKlassVCR_Enabled = false;
	bool isKlassVMMR_Enabled = false;
	bool isKlassVBSR_Enabled = false;
	std::string platform = "auto";
	std::string charset = "latin";
	std::string backend = "auto"; // auto, onnx, onnx[cuda], tflite, tflite[xnnpack], trt, vino...
	std::string batching_mode = "auto"; // auto, on, off
#if defined(__arm__) || defined(__thumb__) || defined(__TARGET_ARCH_ARM) || defined(__TARGET_ARCH_THUMB) || defined(_ARM) || defined(_M_ARM) || defined(_M_ARMT) || defined(__arm) || defined(__aarch64__)
	size_t loopsCount = 100;
#else
	size_t loopsCount = 500;
#endif
	size_t loopsCountW = 30;
	int numThreads = -1;
	int maxLatency = -1;
	std::string images;

	// Parsing args
	std::map<std::string, std::string > args;
	if (!parseArgs(argc, argv, args)) {
		printUsage();
		return -1;
	}
	if (args.find("--image") == args.end()) {
		printUsage("--image required");
		return -1;
	}
	images = args["--image"];

	if (args.find("--parallel") != args.end()) {
		isParallelDeliveryEnabled = (args["--parallel"].compare("true") == 0);
	}
	if (args.find("--assets") == args.end()) {
		printUsage("--assets required");
		return -1;
	}
	assetsFolder = args["--assets"];
#if defined(_WIN32)
	std::replace(assetsFolder.begin(), assetsFolder.end(), '\\', '/');
#endif

	if (args.find("--platform") != args.end()) {
		platform = args["--platform"];
	}
	if (args.find("--charset") != args.end()) {
		charset = args["--charset"];
	}
	if (args.find("--backend") != args.end()) {
		backend = args["--backend"];
	}
	if (args.find("--batching_mode") != args.end()) {
		batching_mode = args["--batching_mode"];
	}
	if (args.find("--recogn_enabled") != args.end()) {
		isRecognEnabled = (args["--recogn_enabled"].compare("true") == 0);
	}
	if (args.find("--nvision_enabled") != args.end()) {
		isNightVisionEnabled = (args["--nvision_enabled"].compare("true") == 0);
	}
	if (args.find("--pysearch_enabled") != args.end()) {
		isPySearchEnabled = (args["--pysearch_enabled"].compare("true") == 0);
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
	if (args.find("--plugin_options") != args.end()) {
		pluginOptions = args["--plugin_options"];
	}
	if (args.find("--num_threads") != args.end()) {
		numThreads = std::atoi(args["--num_threads"].c_str());
	}
	if (args.find("--max_latency") != args.end()) {
		maxLatency = std::atoi(args["--max_latency"].c_str());
	}
	for (const std::string name : { "--loops", "--loopsw" }) {
		if (args.find(name) != args.end()) {
			const int loops = std::atoi(args[name].c_str());
			if (loops < 1 && (name == "--loops")) {
				printUsage("--loops must be within [1, inf]");
				return -1;
			}
			*((name == "--loops") ? &loopsCount : &loopsCountW) = static_cast<size_t>(loops < 0 ? 0 : loops);
		}
	}

	// Update JSON config
	std::string jsonConfig = __jsonConfig;
	if (!assetsFolder.empty()) {
		jsonConfig += std::string(",\"assets_folder\": \"") + assetsFolder + std::string("\"");
	}
	if (!platform.empty()) {
		jsonConfig += std::string(",\"platform\": \"") + platform + std::string("\"");
	}
	if (!backend.empty()) {
		jsonConfig += std::string(",\"backend\": \"") + backend + std::string("\"");
	}
	if (!batching_mode.empty()) {
		jsonConfig += std::string(",\"batching_mode\": \"") + batching_mode + std::string("\"");
	}
	if (!charset.empty()) {
		jsonConfig += std::string(",\"recogn_charset\": \"") + charset + std::string("\"");
	}
	jsonConfig += std::string(",\"recogn_enabled\": ") + (isRecognEnabled ? "true" : "false");
	jsonConfig += std::string(",\"night_vision_enabled\": ") + (isNightVisionEnabled ? "true" : "false");
	jsonConfig += std::string(",\"pyramidal_search_enabled\": ") + (isPySearchEnabled ? "true" : "false");
	jsonConfig += std::string(",\"klass_lpci_enabled\": ") + (isKlassLPCI_Enabled ? "true" : "false");
	jsonConfig += std::string(",\"klass_vcr_enabled\": ") + (isKlassVCR_Enabled ? "true" : "false");
	jsonConfig += std::string(",\"klass_vmmr_enabled\": ") + (isKlassVMMR_Enabled ? "true" : "false");
	jsonConfig += std::string(",\"klass_vbsr_enabled\": ") + (isKlassVBSR_Enabled ? "true" : "false");
	jsonConfig += std::string(",\"num_threads\": ") + std::to_string(numThreads);
	jsonConfig += std::string(",\"max_latency\": ") + std::to_string(maxLatency);
	if (!licenseTokenFile.empty()) {
		jsonConfig += std::string(",\"license_token_file\": \"") + licenseTokenFile + std::string("\"");
	}
	if (!licenseTokenData.empty()) {
		jsonConfig += std::string(",\"license_token_data\": \"") + licenseTokenData + std::string("\"");
	}
	if (!pluginOptions.empty()) {
		jsonConfig += std::string(",\"plugin_options\": \"") + pluginOptions + std::string("\"");
	}

	jsonConfig += "}"; // end-of-config

	// Read input files (comma-separated)
	std::istringstream iss(images);
	ULTALPR_SDK_PRINT_INFO("images:%s", images.c_str());
	std::string image;
	std::vector<FileInfo> files;
	while (std::getline(iss, image, ',')) {
		FileInfo file;
		if (!readFile(image, file) || !file.isValid()) {
			ULTALPR_SDK_PRINT_FATAL("Can't read %s", image.c_str());
			return -1;
		}
		files.push_back(std::move(file));
	}

	// Function to wait until parallel callback is called
	static const bool kParallelNotifCountResetTrue = true;
	static const bool kParallelNotifCountResetFalse = false;
	auto funcWaitPtr = [&](const size_t& count, const bool& reset = false) -> void {
		if (isParallelDeliveryEnabled) {
			static std::mutex parallelNotifMutex;
			std::unique_lock<std::mutex > lk(parallelNotifMutex);
			parallelNotifCondVar.wait_for(lk,
				std::chrono::milliseconds(3 * 60 * 1000), // maximum number of millis to wait for before giving up, must never wait this long
				[&count] { return (parallelNotifCount >= count); }
			);
		}
		else {
			parallelNotifCount = count;
		}
		if (reset == kParallelNotifCountResetTrue) {
			parallelNotifCount = 0;
		}
	};

	// Init
	ULTALPR_SDK_PRINT_INFO("Starting benchmark...");
	MyUltAlprSdkParallelDeliveryCallback parallelDeliveryCallbackCallback(nullptr);
	const std::chrono::high_resolution_clock::time_point timeStartInit = std::chrono::high_resolution_clock::now();
	ULTALPR_SDK_ASSERT(checkResult(UltAlprSdkEngine::init(
		ASSET_MGR_PARAM()
		jsonConfig.c_str(),
		isParallelDeliveryEnabled ? &parallelDeliveryCallbackCallback : nullptr
	)));
	const std::chrono::high_resolution_clock::time_point timeEndInit = std::chrono::high_resolution_clock::now();
	ULTALPR_SDK_PRINT_INFO("Engine init duration: %lf millis",
		std::chrono::duration_cast<std::chrono::duration<double >>(timeEndInit - timeStartInit).count() * 1000.0);

	// WarmUp
	// We load the models into the memory the first time the inference is called which
	// means it'll be very slow.
	ULTALPR_SDK_PRINT_INFO("Starting warmup...");
	parallelState = "WARMUP";
	for (size_t i = 0; i < loopsCountW; ++i) {
		const auto& file = files[rand() % files.size()];
		ULTALPR_SDK_ASSERT(checkResult(result = UltAlprSdkEngine::process(
			file.type, file.uncompressedData, file.width, file.height
		)));
	}
	funcWaitPtr(loopsCountW, kParallelNotifCountResetTrue);
	ULTALPR_SDK_PRINT_INFO("Warmup done.");

	// Processing
	ULTALPR_SDK_PRINT_INFO("Starting benchmarking...");
	parallelState = "BENCH";
	const std::chrono::high_resolution_clock::time_point timeStartBench = std::chrono::high_resolution_clock::now();
	for (size_t i = 0; i < loopsCount; ++i) {
		const auto& file = files[rand() % files.size()];
		ULTALPR_SDK_ASSERT(checkResult(result = UltAlprSdkEngine::process(
			file.type, file.uncompressedData, file.width, file.height
		)));
	}
	funcWaitPtr(loopsCount, kParallelNotifCountResetFalse);

	// Compute the estimated frame rate.
	// At this step all frames are already processed but the result could be still on the delivery
	// queue due to the console display latency. You can move here the code used to wait until all
	// messages are displayed to include the delivery latency.
	const std::chrono::high_resolution_clock::time_point timeEndBench = std::chrono::high_resolution_clock::now();
	const double elapsedTimeInMillis = std::chrono::duration_cast<std::chrono::duration<double >>(timeEndBench - timeStartBench).count() * 1000.0;
	ULTALPR_SDK_PRINT_INFO("Elapsed time (Benchmark) = [[[ %lf millis ]]]", elapsedTimeInMillis);

	// Print latest result
	const std::string& json_ = result.json();
	if (!json_.empty()) {
		ULTALPR_SDK_PRINT_INFO("result: %s", json_.c_str());
	}

	// Print estimated frame rate
	const double estimatedFps = 1000.f / (elapsedTimeInMillis / static_cast<double>(parallelNotifCount));
	ULTALPR_SDK_PRINT_INFO("*** elapsedTimeInMillis: %lf, notified: %zu/%zu, estimatedFps: %lf ***", elapsedTimeInMillis, parallelNotifCount, loopsCount, estimatedFps);

	ULTALPR_SDK_PRINT_INFO("Press ENTER key to terminate !!");
	getchar();

	// DeInit
	ULTALPR_SDK_PRINT_INFO("Ending benchmark...");
	ULTALPR_SDK_ASSERT(checkResult(UltAlprSdkEngine::deInit()));

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
		"\t--image <comma-separated-paths-to-images> \n"
		"\t--assets <path-to-assets-folder> \n"
		"\t[--charset <recognition-charset:latin/korean/chinese/brazilian/israeli>] \n"
		"\t[--num_threads <number of threads : [1, inf]>] \n"
		"\t[--max_latency <max-latency:]-inf,+inf[>] \n"
		"\t[--nvision_enabled <whether-to-enable-IENV:true/false>] \n"
		"\t[--platform <target platform:auto/mobile/desktop>] \n"
		"\t[--backend <backend:auto,onnx,tflite,rknn2,trt,vino...>] \n"
		"\t[--pysearch_enabled <whether-to-enable-pyramidal-search:true/false>] \n"
		"\t[--recogn_enabled <whether-to-enable-LPR:true/false>] \n"
		"\t[--klass_lpci_enabled <whether-to-enable-LPCI:true/false>] \n"
		"\t[--klass_vcr_enabled <whether-to-enable-VCR:true/false>] \n"
		"\t[--klass_vmmr_enabled <whether-to-enable-VMMR:true/false>] \n"
		"\t[--klass_vbsr_enabled <whether-to-enable-VBSR:true/false>] \n"
		"\t[--loops <number-of-times-to-run-the-inference-loop:[1, inf]>] \n"
		"\t[--loopsw <number-of-times-to-run-the-warmup-loop:[1, inf]>] \n"
		"\t[--parallel <whether-to-enable-parallel-mode:true / false>] \n"
		"\t[--tokenfile <path-to-license-token-file>] \n"
		"\t[--tokendata <base64-license-token-data>] \n"
		"\n"
		"Options surrounded with [] are optional.\n"
		"\n"
		"--image: Comma-separated paths to images (JPEG/PNG/BMP) to benchmark. You can use default image at ../../../assets/images/lic_us_1280x720.jpg.\n\n"
		"--assets: Path to the assets folder containing the configuration files and models. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#assets-folder. \n\n"
		"--charset: Defines the recognition charset (a.k.a alphabet) value (latin, korean, chinese...). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#recogn-charset. Default: latin.\n\n"
		"--num_threads: Number of threads to use. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#num-threads. Default: -1.\n\n"
		"--max_latency: Max latency when parallel processing is enabled. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#max-latency. Default: -1.\n\n"
		"--nvision_enabled: Whether to enable Image Enhancement for Night-Vision (IENV). More info about IENV at https://www.doubango.org/SDKs/anpr/docs/Features.html#night-vision-enabled. Default: false.\n\n"
		"--platform: Target platform. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#platform. Default: auto.\n\n"
		"--backend: Target backend. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#backend. Default: auto.\n\n"
		"--pysearch_enabled: Whether to enable Pyramidal search. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#pyramidal-search-enabled. Default: false.\n\n"
		"--recogn_enabled: Whether to enable License Plate Recognition (LPR) / OCR. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#recogn-enabled. Default: false.\n\n"
		"--klass_lpci_enabled: Whether to enable License Plate Country Identification (LPCI). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-lpci-enabled. Default: false.\n\n"
		"--klass_vcr_enabled: Whether to enable Vehicle Color Recognition (VCR). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vcr-enabled. Default: false.\n\n"
		"--klass_vmmr_enabled: Whether to enable Vehicle Make Model Recognition (VMMR). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vmmr-enabled. Default: false.\n\n"
		"--klass_vbsr_enabled: Whether to enable Vehicle Body Style Recognition (VBSR). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vbsr-enabled. Default: false.\n\n"
		"--loops: Number of times to run the inference loop.\n\n"
		"--loopsw: Number of times to run the warmup loop.\n\n"
		"--parallel: Whether to enabled the parallel mode. More info about the parallel mode at https://www.doubango.org/SDKs/anpr/docs/Parallel_processing.html. Default: true.\n\n"
		"--tokenfile: Path to the file containing the base64 license token if you have one. If not provided then, the application will act like a trial version. Default: null.\n\n"
		"--tokendata: Base64 license token if you have one. If not provided then, the application will act like a trial version. Default: null.\n\n"
		"********************************************************************************\n"
	);
}

static bool parseArgs(int argc, char* argv[], std::map<std::string, std::string >& values)
{
	ULTALPR_SDK_ASSERT(argc > 0 && argv != nullptr);

	values.clear();

	// Make sure the number of arguments is even
	if ((argc - 1) & 1) {
		ULTALPR_SDK_PRINT_INFO("Number of args must be even");
		return false;
	}

	// Parsing
	for (int index = 1; index < argc; index += 2) {
		std::string key = argv[index];
		if (key.size() < 2 || key[0] != '-' || key[1] != '-') {
			ULTALPR_SDK_PRINT_INFO("Invalid key: %s", key.c_str());
			return false;
		}
		values[key] = argv[index + 1];
	}

	return true;
}

static bool readFile(const std::string& path, FileInfo& fileInfo)
{
	if (path.empty()) {
		ULTALPR_SDK_PRINT_ERROR("Path is empty");
		return false;
	}

	// Open file
	FILE* file =
#	if ULTALPR_SDK_OS_ANDROID
		sdk_android_asset_fopen(path.c_str(), "rb");
#	else
		fopen(path.c_str(), "rb");
#	endif
	if (!file) {
		ULTALPR_SDK_PRINT_ERROR("Failed to open file at: %s", path.c_str());
		return false;
	}

	// Decode the file
	int width, height, channels;
	stbi_uc* uncompressedData = stbi_load_from_file(file, &width, &height, &channels, 0);
	fclose(file);
	if (!uncompressedData || width <= 0 || height <= 0 || (channels != 1 && channels != 3 && channels != 4)) {
		ULTALPR_SDK_PRINT_ERROR("Invalid file(%s, %d, %d, %d)", path.c_str(), width, height, channels);
		if (uncompressedData) {
			free(uncompressedData);
		}
		return false;
	}

	// We expect RGB-family data from the JPEG/PNG/BMP file
	// If you're using data from your camera then, it should be YUV-family and you don't need
	// to convert to RGB-family.
	// List of supported types: https://www.doubango.org/SDKs/anpr/docs/cpp-api.html#_CPPv4N15ultimateAlprSdk22ULTALPR_SDK_IMAGE_TYPEE
	fileInfo.type = (channels == 3) ? ULTALPR_SDK_IMAGE_TYPE_RGB24 : (channels == 1 ? ULTALPR_SDK_IMAGE_TYPE_Y : ULTALPR_SDK_IMAGE_TYPE_RGBA32);
	fileInfo.uncompressedData = uncompressedData;
	fileInfo.width = static_cast<size_t>(width);
	fileInfo.height = static_cast<size_t>(height);

	return fileInfo.isValid();
}

static bool checkResult(const UltAlprSdkResult& result)
{
	if (!result.isOK()) {
		ULTALPR_SDK_PRINT_ERROR("Failed: phrase: %s, json: %s", result.phrase(), result.json());
		return false;
	}
	return true;
}