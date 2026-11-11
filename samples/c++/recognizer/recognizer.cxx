/* Copyright (C) 2011-2026 Doubango Telecom <https://www.doubango.org>
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
			--assets <path-to-assets-folder> \
			[--parallel <whether-to-enable-parallel-mode:true/false>] \
			[--platform <target platform:auto/mobile/desktop>] \
			[--charset <recognition-charset:latin/korean/chinese>] \
			[--nvision_enabled <whether-to-enable-IENV:true/false>] \
			[--backend <backend:auto,onnx,tflite,rknn2,trt,vino...>] \
			[--klass_lpci_enabled <whether-to-enable-LPCI:true/false>] \
			[--klass_vcr_enabled <whether-to-enable-VCR:true/false>] \
			[--klass_vmmr_enabled <whether-to-enable-VMMR:true/false>] \
			[--klass_vbsr_enabled <whether-to-enable-VBSR:true/false>] \
			[--tokenfile <path-to-license-token-file>] \
			[--tokendata <base64-license-token-data>]

	Example:
		recognizer \
			--image C:/Projects/GitHub/ultimate/ultimateALPR/SDK_dist/assets/images/lic_us_1280x720.jpg \
			--parallel true \
			--assets C:/Projects/GitHub/ultimate/ultimateALPR/SDK_dist/assets \
			--charset latin \
			--tokenfile C:/Projects/GitHub/ultimate/ultimateALPR/SDK_dev/tokens/windows-iMac.lic
		
*/

#include <ultimateALPR-SDK-API-PUBLIC.h>

#include <iostream> // std::cout
#include <sys/stat.h>
#include <random>
#include <mutex>
#include <condition_variable>
#include <map>
#if defined(_WIN32)
#	include <Windows.h> // SetConsoleOutputCP
#	include <algorithm> // std::replace
#endif


using namespace ultimateAlprSdk;

// Asset manager used on Android to files in "assets" folder
#if ULTALPR_SDK_OS_ANDROID 
#	define ASSET_MGR_PARAM() __sdk_android_assetmgr, 
#else
#	define ASSET_MGR_PARAM() 
#endif /* ULTALPR_SDK_OS_ANDROID */

struct FileInfo {
	void* compressedDataPtr = nullptr;
	size_t compressedDataSize = 0;
	FILE* filePtr = nullptr;
	virtual ~FileInfo() {
		if (compressedDataPtr) free(compressedDataPtr), compressedDataPtr = nullptr;
		if (filePtr) fclose(filePtr), filePtr = nullptr;
	}
	bool isValid() const {
		return compressedDataPtr && compressedDataSize > 0;
	}
};

/*
* Parallel callback function used for notification. Not mandatory.
* More info about parallel delivery: https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html
*/
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
		ULTALPR_SDK_PRINT_INFO("MyUltAlprSdkParallelDeliveryCallback::onNewResult(%d, %s, %zu): %s",
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


static void printUsage(const std::string& message = "");
static bool parseArgs(int argc, char *argv[], std::map<std::string, std::string >& values);
static bool readFile(const std::string& path, FileInfo& file);

// Configuration for ANPR deep learning engine
static const char* __jsonConfig =
"{"
"\"debug_level\": \"info\","
"\"debug_write_input_image_enabled\": false,"
"\"debug_internal_data_path\": \".\","
""
"\"num_threads\": -1,"
"\"max_latency\": -1,"
"\"asm_enabled\": true,"
"\"intrin_enabled\": true,"
""
"\"detect_roi\": [0, 0, 0, 0],"
"\"detect_minscore\": 0.4,"
"\"detect_miniou\": 0.4,"
""
"\"pyramidal_search_enabled\": true,"
"\"pyramidal_search_minscore\": 0.3,"
"\"pyramidal_search_miniou\": 0.4,"
"\"pyramidal_search_min_image_size_inpixels\": 600,"
""
"\"recogn_enabled\": true,"
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
	std::string assetsFolder, licenseTokenData, licenseTokenFile;
	bool isParallelDeliveryEnabled = false; // Single image -> no need for parallel processing
	bool isNightVisionEnabled = false;
	bool isKlassLPCI_Enabled = false;
	bool isKlassVCR_Enabled = false;
	bool isKlassVMMR_Enabled = false;
	bool isKlassVBSR_Enabled = false;
	std::string platform = "auto";
	std::string charset = "latin";
	std::string backend = "auto"; // auto, onnx, onnx[cuda], tflite, tflite[xnnpack], trt, vino...
	std::string pathFileImage;

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
	pathFileImage = args["--image"];
		
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
	if (args.find("--nvision_enabled") != args.end()) {
		isNightVisionEnabled = (args["--nvision_enabled"].compare("true") == 0);
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
	if (!platform.empty()) {
		jsonConfig += std::string(",\"platform\": \"") + platform + std::string("\"");
	}
	if (!backend.empty()) {
		jsonConfig += std::string(",\"backend\": \"") + backend + std::string("\"");
	}
	if (!charset.empty()) {
		jsonConfig += std::string(",\"recogn_charset\": \"") + charset + std::string("\"");
	}
	jsonConfig += std::string(",\"night_vision_enabled\": ") + (isNightVisionEnabled ? "true" : "false");
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

	// Read input file
	FileInfo file;
	if (!readFile(pathFileImage, file) || !file.isValid()) {
		ULTALPR_SDK_PRINT_FATAL("Can't read %s", pathFileImage.c_str());
		return -1;
	}

	// Init
	ULTALPR_SDK_PRINT_INFO("Starting recognizer...");
	MyUltAlprSdkParallelDeliveryCallback parallelDeliveryCallbackCallback(nullptr);
	ULTALPR_SDK_ASSERT(UltAlprSdkEngine::init(
		ASSET_MGR_PARAM()
		jsonConfig.c_str(),
		isParallelDeliveryEnabled ? &parallelDeliveryCallbackCallback : nullptr
	).isOK());

	// Recognize/Process
	// We load the models when this function is called for the first time. This make the first inference slow.
	// Use benchmark application to compute the average inference time: https://github.com/DoubangoTelecom/ultimateALPR-SDK/tree/master/samples/c%2B%2B/benchmark
	ULTALPR_SDK_ASSERT((result = UltAlprSdkEngine::process(
		file.compressedDataPtr,
		file.compressedDataSize
	)).isOK());
	ULTALPR_SDK_PRINT_INFO("Processing done.");

	// Printing to the console is very slow and use a low priority thread.
	// Wait until all results are displayed.
	if (isParallelDeliveryEnabled) {
		static std::mutex parallelNotifMutex;
		std::unique_lock<std::mutex > lk(parallelNotifMutex);
		parallelNotifCondVar.wait_for(lk,
			std::chrono::milliseconds(1500), // maximum number of millis to wait for before giving up, must never wait this long
			[] { return (parallelNotifCount == 1); }
		);
	}

	// Print latest result
	ULTALPR_SDK_PRINT_INFO("result: %s", result.json());

	// Wait for user input
	ULTALPR_SDK_PRINT_INFO("Press ENTER key to terminate !!");
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
		"\t--assets <path-to-assets-folder> \n"
		"\t[--charset <recognition-charset:latin/korean/chinese/brazilian/israeli>] \n"
		"\t[--platform <target platform:auto/mobile/desktop>] \n"
		"\t[--nvision_enabled <whether-to-enable-IENV:true/false>] \n"
		"\t[--backend <backend:auto,onnx,tflite,rknn2,trt,vino...>] \n"
		"\t[--klass_lpci_enabled <whether-to-enable-LPCI:true/false>] \n"
		"\t[--klass_vcr_enabled <whether-to-enable-VCR:true/false>] \n"
		"\t[--klass_vmmr_enabled <whether-to-enable-VMMR:true/false>] \n"
		"\t[--klass_vbsr_enabled <whether-to-enable-VBSR:true/false>] \n"
		"\t[--parallel <whether-to-enable-parallel-mode:true / false>] \n"
		"\t[--tokenfile <path-to-license-token-file>] \n"
		"\t[--tokendata <base64-license-token-data>] \n"
		"\n"
		"Options surrounded with [] are optional.\n"
		"\n"
		"--image: Path to the image(JPEG/PNG/BMP) to process. You can use default image at ../../../assets/images/lic_us_1280x720.jpg.\n\n"
		"--assets: Path to the assets folder containing the configuration files and models. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#assets-folder. \n\n"
		"--charset: Defines the recognition charset (a.k.a alphabet) value (latin, korean, chinese...). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#recogn-charset. Default: latin.\n\n"
		"--platform: Target platform. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#platform. Default: auto.\n\n"
		"--nvision_enabled: Whether to enable Image Enhancement for Night-Vision (IENV). More info about IENV at https://www.doubango.org/SDKs/anpr/docs/Features.html#night-vision-enabled. Default: false.\n\n"
		"--backend: Target backend. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#backend. Default: auto.\n\n"
		"--klass_lpci_enabled: Whether to enable License Plate Country Identification (LPCI). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-lpci-enabled. Default: false.\n\n"
		"--klass_vcr_enabled: Whether to enable Vehicle Color Recognition (VCR). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vcr-enabled. Default: false.\n\n"
		"--klass_vmmr_enabled: Whether to enable Vehicle Make Model Recognition (VMMR). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vmmr-enabled. Default: false.\n\n"
		"--klass_vbsr_enabled: Whether to enable Vehicle Body Style Recognition (VBSR). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vbsr-enabled. Default: false.\n\n"
		"--parallel: Whether to enabled the parallel mode.More info about the parallel mode at https://www.doubango.org/SDKs/anpr/docs/Parallel_processing.html. Default: true.\n\n"
		"--tokenfile: Path to the file containing the base64 license token if you have one. If not provided then, the application will act like a trial version. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#license-token-file. Default: null.\n\n"
		"--tokendata: Base64 license token if you have one. If not provided then, the application will act like a trial version. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#license-token-data. Default: null.\n\n"
		"********************************************************************************\n"
	);
}

static bool parseArgs(int argc, char *argv[], std::map<std::string, std::string >& values)
{
	ULTALPR_SDK_ASSERT(argc > 0 && argv != nullptr);

	values.clear();

	// Make sure the number of arguments is even
	if ((argc - 1) & 1) {
		ULTALPR_SDK_PRINT_ERROR("Number of args must be even");
		return false;
	}

	// Parsing
	for (int index = 1; index < argc; index += 2) {
		std::string key = argv[index];
		if (key.size() < 2 || key[0] != '-' || key[1] != '-') {
			ULTALPR_SDK_PRINT_ERROR("Invalid key: %s", key.c_str());
			return false;
		}
		values[key] = argv[index + 1];
	}

	return true;
}

static bool readFile(const std::string& path, FileInfo& file)
{
	// Open the file
	if ((file.filePtr = fopen(path.c_str(), "rb")) == nullptr) {
		ULTALPR_SDK_PRINT_INFO("Can't open %s", path.c_str());
		return false;
	}

	// Retrieve file size
	struct stat st_;
	if (stat(path.c_str(), &st_) != 0) {
		ULTALPR_SDK_PRINT_INFO("File is empty %s", path.c_str());
	}
	file.compressedDataSize = static_cast<size_t>(st_.st_size);

	// Alloc memory and read data
	file.compressedDataPtr = ::malloc(file.compressedDataSize);
	if (!file.compressedDataPtr) {
		ULTALPR_SDK_PRINT_INFO("Failed to alloc mem with size = %zu", file.compressedDataSize);
		return false;
	}
	size_t read_;
	if (file.compressedDataSize != (read_ = fread(file.compressedDataPtr, 1, file.compressedDataSize, file.filePtr))) {
		ULTALPR_SDK_PRINT_INFO("fread(%s) returned %zu instead of %zu", path.c_str(), read_, file.compressedDataSize);
		return false;
	}

	return file.isValid();
}
