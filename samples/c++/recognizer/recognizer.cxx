/* Copyright (C) 2011-2024 Doubango Telecom <https://www.doubango.org>
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
			[--charset <recognition-charset:latin/korean/chinese>] \
			[--car_noplate_detect_enabled <whether-to-enable-detecting-cars-with-no-plate:true/false>] \
			[--ienv_enabled <whether-to-enable-IENV:true/false>] \
			[--openvino_enabled <whether-to-enable-OpenVINO:true/false>] \
			[--openvino_device <openvino_device-to-use>] \
			[--npu_enabled <whether-to-enable-NPU-acceleration:true/false>] \
			[--trt_enabled <whether-to-enable-TensorRT-acceleration:true/false>] \
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
			--rectify false \
			--assets C:/Projects/GitHub/ultimate/ultimateALPR/SDK_dist/assets \
			--charset latin \
			--tokenfile C:/Projects/GitHub/ultimate/ultimateALPR/SDK_dev/tokens/windows-iMac.lic
		
*/

#include <ultimateALPR-SDK-API-PUBLIC.h>

#include <iostream> // std::cout
#include <sys/stat.h>
#include <map>
#if defined(_WIN32)
#	include <Windows.h> // SetConsoleOutputCP
#	include <algorithm> // std::replace
#endif

// Not part of the SDK, used to decode images -> https://github.com/nothings/stb
#define STB_IMAGE_IMPLEMENTATION
#define STB_IMAGE_STATIC
#include "../stb_image.h"

using namespace ultimateAlprSdk;

// Asset manager used on Android to files in "assets" folder
#if ULTALPR_SDK_OS_ANDROID 
#	define ASSET_MGR_PARAM() __sdk_android_assetmgr, 
#else
#	define ASSET_MGR_PARAM() 
#endif /* ULTALPR_SDK_OS_ANDROID */

struct AlprFile {
	int width = 0, height = 0, channels = 0;
	stbi_uc* uncompressedDataPtr = nullptr;
	void* compressedDataPtr = nullptr;
	size_t compressedDataSize = 0;
	FILE* filePtr = nullptr;
	virtual ~AlprFile() {
		if (uncompressedDataPtr) free(uncompressedDataPtr), uncompressedDataPtr = nullptr;
		if (compressedDataPtr) free(compressedDataPtr), compressedDataPtr = nullptr;
		if (filePtr) fclose(filePtr), filePtr = nullptr;
	}
	bool isValid() const {
		return width > 0 && height > 0 && (channels == 1 || channels == 3 || channels == 4) && uncompressedDataPtr && compressedDataPtr && compressedDataSize > 0;
	}
	ULTALPR_SDK_IMAGE_TYPE type() const {
		return channels == 4 ? ULTALPR_SDK_IMAGE_TYPE_RGBA32 : (channels == 1 ? ULTALPR_SDK_IMAGE_TYPE_Y : ULTALPR_SDK_IMAGE_TYPE_RGB24);
	}
};

/*
* Parallel callback function used for notification. Not mandatory.
* More info about parallel delivery: https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html
*/
class MyUltAlprSdkParallelDeliveryCallback : public UltAlprSdkParallelDeliveryCallback {
public:
	MyUltAlprSdkParallelDeliveryCallback(const std::string& charset) : m_strCharset(charset) {}
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
private:
	std::string m_strCharset;
};

static void printUsage(const std::string& message = "");
static bool parseArgs(int argc, char *argv[], std::map<std::string, std::string >& values);
static bool readFile(const std::string& path, AlprFile& file);

// Configuration for ANPR deep learning engine
static const char* __jsonConfig =
"{"
"\"debug_level\": \"info\","
"\"debug_write_input_image_enabled\": false,"
"\"debug_internal_data_path\": \".\","
""
"\"num_threads\": -1,"
"\"max_jobs\": -1,"
"\"gpgpu_enabled\": true,"
"\"asm_enabled\": true,"
"\"intrin_enabled\": true,"
""
"\"klass_vcr_gamma\": 1.5,"
""
"\"detect_roi\": [0, 0, 0, 0],"
"\"detect_minscore\": 0.1,"
""
"\"car_noplate_detect_min_score\": 0.8,"
""
"\"pyramidal_search_enabled\": true,"
"\"pyramidal_search_sensitivity\": 1.0,"
"\"pyramidal_search_minscore\": 0.3,"
"\"pyramidal_search_min_image_size_inpixels\": 800,"
""
"\"recogn_minscore\": 0.3,"
"\"recogn_score_type\": \"min\""
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
	bool isRectificationEnabled = true;
	bool isCarNoPlateDetectEnabled = false;
	bool isIENVEnabled = false;
	bool isOpenVinoEnabled = 
#if defined(__arm__) || defined(__thumb__) || defined(__TARGET_ARCH_ARM) || defined(__TARGET_ARCH_THUMB) || defined(_ARM) || defined(_M_ARM) || defined(_M_ARMT) || defined(__arm) || defined(__aarch64__)
		false;
#else // x86-64
		true;
#endif
	bool isNpuEnabled = true; // Amlogic, NXP...
	bool isTensorRTEnabled = false; // NVIDIA TensorRT
	bool isKlassLPCI_Enabled = false;
	bool isKlassVCR_Enabled = false;
	bool isKlassVMMR_Enabled = false;
	bool isKlassVBSR_Enabled = false;
	std::string charset = "latin";
	std::string openvinoDevice = "CPU";
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
	if (args.find("--assets") != args.end()) {
		assetsFolder = args["--assets"];
#if defined(_WIN32)
		std::replace(assetsFolder.begin(), assetsFolder.end(), '\\', '/');
#endif
	}
	if (args.find("--charset") != args.end()) {
		charset = args["--charset"];
	}
	if (args.find("--rectify") != args.end()) {
		isRectificationEnabled = (args["--rectify"].compare("true") == 0);
	}	
	if (args.find("--car_noplate_detect_enabled") != args.end()) {
		isCarNoPlateDetectEnabled = (args["--car_noplate_detect_enabled"].compare("true") == 0);
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
	jsonConfig += std::string(",\"recogn_rectify_enabled\": ") + (isRectificationEnabled ? "true" : "false");	
	jsonConfig += std::string(",\"car_noplate_detect_enabled\": ") + (isCarNoPlateDetectEnabled ? "true" : "false");
	jsonConfig += std::string(",\"ienv_enabled\": ") + (isIENVEnabled ? "true" : "false");
	jsonConfig += std::string(",\"openvino_enabled\": ") + (isOpenVinoEnabled ? "true" : "false");
	if (!openvinoDevice.empty()) {
		jsonConfig += std::string(",\"openvino_device\": \"") + openvinoDevice + std::string("\"");
	}
	jsonConfig += std::string(",\"npu_enabled\": ") + (isNpuEnabled ? "true" : "false");
	jsonConfig += std::string(",\"trt_enabled\": ") + (isTensorRTEnabled ? "true" : "false");
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

	// Decode the file
	AlprFile file;
	if (!readFile(pathFileImage, file)) {
		ULTALPR_SDK_PRINT_ERROR("Can't process %s", pathFileImage.c_str());
		return -1;
	}
	ULTALPR_SDK_ASSERT(file.isValid());

	// Init
	ULTALPR_SDK_PRINT_INFO("Starting recognizer...");
	MyUltAlprSdkParallelDeliveryCallback parallelDeliveryCallbackCallback(charset);
	ULTALPR_SDK_ASSERT((result = UltAlprSdkEngine::init(
		ASSET_MGR_PARAM()
		jsonConfig.c_str(),
		isParallelDeliveryEnabled ? &parallelDeliveryCallbackCallback : nullptr
	)).isOK());

	// Recognize/Process
	// We load the models when this function is called for the first time. This make the first inference slow.
	// Use benchmark application to compute the average inference time: https://github.com/DoubangoTelecom/ultimateALPR-SDK/tree/master/samples/c%2B%2B/benchmark
	ULTALPR_SDK_ASSERT((result = UltAlprSdkEngine::process(
		file.type(), // If you're using data from your camera then, the type would be YUV-family instead of RGB-family. https://www.doubango.org/SDKs/anpr/docs/cpp-api.html#_CPPv4N15ultimateAlprSdk22ULTALPR_SDK_IMAGE_TYPEE
		file.uncompressedDataPtr,
		static_cast<size_t>(file.width),
		static_cast<size_t>(file.height),
		0, // stride
		UltAlprSdkEngine::exifOrientation(file.compressedDataPtr, file.compressedDataSize)
	)).isOK());
	ULTALPR_SDK_PRINT_INFO("Processing done.");

	// Print latest result
	if (!isParallelDeliveryEnabled && result.json()) { // for parallel delivery the result will be printed by the callback function
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
		"\t[--charset <recognition-charset:latin/korean/chinese>] \n"
		"\t[--car_noplate_detect_enabled <whether-to-enable-detecting-cars-with-no-plate:true/false>] \n"
		"\t[--ienv_enabled <whether-to-enable-IENV:true/false>] \n"
		"\t[--openvino_enabled <whether-to-enable-OpenVINO:true/false>] \n"
		"\t[--openvino_device <openvino_device-to-use>] \n"
		"\t[--klass_lpci_enabled <whether-to-enable-LPCI:true/false>] \n"
		"\t[--klass_vcr_enabled <whether-to-enable-VCR:true/false>] \n"
		"\t[--klass_vmmr_enabled <whether-to-enable-VMMR:true/false>] \n"
		"\t[--klass_vbsr_enabled <whether-to-enable-VBSR:true/false>] \n"
		"\t[--parallel <whether-to-enable-parallel-mode:true / false>] \n"
		"\t[--rectify <whether-to-enable-rectification-layer:true / false>] \n"
		"\t[--tokenfile <path-to-license-token-file>] \n"
		"\t[--tokendata <base64-license-token-data>] \n"
		"\n"
		"Options surrounded with [] are optional.\n"
		"\n"
		"--image: Path to the image(JPEG/PNG/BMP) to process. You can use default image at ../../../assets/images/lic_us_1280x720.jpg.\n\n"
		"--assets: Path to the assets folder containing the configuration files and models. Default value is the current folder.\n\n"
		"--charset: Defines the recognition charset (a.k.a alphabet) value (latin, korean, chinese...). Default: latin.\n\n"
		"--charset: Defines the recognition charset value (latin, korean, chinese...). Default: latin.\n\n"
		"--car_noplate_detect_enabled: Whether to detect and return cars with no plate. Default: false.\n\n"
		"--ienv_enabled: Whether to enable Image Enhancement for Night-Vision (IENV). More info about IENV at https://www.doubango.org/SDKs/anpr/docs/Features.html#image-enhancement-for-night-vision-ienv. Default: true for x86-64 and false for ARM.\n\n"
		"--openvino_enabled: Whether to enable OpenVINO. Tensorflow will be used when OpenVINO is disabled. Default: true.\n\n"
		"--openvino_device: Defines the OpenVINO device to use (CPU, GPU, FPGA...). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#openvino_device. Default: CPU.\n\n"
		"--npu_enabled: Whether to enable NPU acceleration (Amlogic, NXP...). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#npu-enabled. Default: true.\n\n"
		"--trt_enabled: Whether to enable NVIDIA TensorRT acceleration. This will disable OpenVINO More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#trt-enabled. Default: false.\n\n"
		"--klass_lpci_enabled: Whether to enable License Plate Country Identification (LPCI). More info at https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-country-identification-lpci. Default: false.\n\n"
		"--klass_vcr_enabled: Whether to enable Vehicle Color Recognition (VCR). More info at https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-color-recognition-vcr. Default: false.\n\n"
		"--klass_vmmr_enabled: Whether to enable Vehicle Make Model Recognition (VMMR). More info at https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-make-model-recognition-vmmr. Default: false.\n\n"
		"--klass_vbsr_enabled: Whether to enable Vehicle Body Style Recognition (VBSR). More info at https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-make-model-recognition-vbsr. Default: false.\n\n"
		"--parallel: Whether to enabled the parallel mode.More info about the parallel mode at https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html. Default: true.\n\n"
		"--rectify: Whether to enable the rectification layer. More info about the rectification layer at https ://www.doubango.org/SDKs/anpr/docs/Rectification_layer.html. Default: true.\n\n"
		"--tokenfile: Path to the file containing the base64 license token if you have one. If not provided then, the application will act like a trial version. Default: null.\n\n"
		"--tokendata: Base64 license token if you have one. If not provided then, the application will act like a trial version. Default: null.\n\n"
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

static bool readFile(const std::string& path, AlprFile& file)
{
	// Open the file
	if ((file.filePtr = fopen(path.c_str(), "rb")) == nullptr) {
		ULTALPR_SDK_PRINT_ERROR("Can't open %s", path.c_str());
		return false;
	}

	// Retrieve file size
	struct stat st_;
	if (stat(path.c_str(), &st_) != 0) {
		ULTALPR_SDK_PRINT_ERROR("File is empty %s", path.c_str());
	}
	file.compressedDataSize = static_cast<size_t>(st_.st_size);

	// Alloc memory and read data
	file.compressedDataPtr = ::malloc(file.compressedDataSize);
	if (!file.compressedDataPtr) {
		ULTALPR_SDK_PRINT_ERROR("Failed to alloc mem with size = %zu", file.compressedDataSize);
		return false;
	}
	size_t read_;
	if (file.compressedDataSize != (read_ = fread(file.compressedDataPtr, 1, file.compressedDataSize, file.filePtr))) {
		ULTALPR_SDK_PRINT_ERROR("fread(%s) returned %zu instead of %zu", path.c_str(), read_, file.compressedDataSize);
		return false;
	}

	// Decode image
	file.uncompressedDataPtr = stbi_load_from_memory(
		reinterpret_cast<stbi_uc const *>(file.compressedDataPtr), static_cast<int>(file.compressedDataSize),
		&file.width, &file.height, &file.channels, 0
	);

	return file.isValid();
}