#include <ultimateALPR-SDK-API-PUBLIC.h>
#include <assert.h>
#include <stdlib.h>
#include <chrono>
#include <sys/stat.h>

using namespace ultimateAlprSdk;

#if ULTALPR_SDK_OS_ANDROID // below code come from https://github.com/DoubangoTelecom/compv/blob/master/api/include/compv/compv_api.h
#	define ASSET_MGR_PARAM() nullptr, 
#	include <android/native_activity.h>
#	include <errno.h>
extern "C" void ANativeActivity_onCreatePriv(ANativeActivity* activity, void* savedState, size_t savedStateSize, void(*android_main)(struct android_app* app)); // CompVbase: https://github.com/DoubangoTelecom/compv/blob/master/base/android/compv_android_native_activity.cxx

static AAssetManager* __sdk_android_assetmgr = nullptr;

static void android_main(struct android_app* state) {
	extern int main();
	main();
}

__attribute__((visibility("default"))) void ANativeActivity_onCreate(ANativeActivity* activity, void* savedState, size_t savedStateSize) {
	ANativeActivity_onCreatePriv(activity, savedState, savedStateSize, android_main);
	__sdk_android_assetmgr = activity->assetManager;
	UltAlprSdkEngine::setAssetManager(activity->assetManager);
}

static int sdk_android_asset_fread(void* cookie, char* buf, int size){
	return AAsset_read((AAsset*)cookie, buf, size);
}
static int sdk_android_asset_fwrite(void* cookie, const char* buf, int size){
	return EACCES;
}
static fpos_t sdk_android_asset_fseek(void* cookie, fpos_t offset, int whence){
	return AAsset_seek((AAsset*)cookie, offset, whence);
}
static int sdk_android_asset_fclose(void* cookie){
	AAsset_close((AAsset*)cookie);
	return 0;
}
static FILE* sdk_android_asset_fopen(const char* fname, const char* mode, size_t* size = nullptr){
	AAsset* asset = AAssetManager_open(__sdk_android_assetmgr, fname, 0);
	if (!asset) {
		return nullptr;
	}
	if (size) {
		*size = static_cast<size_t>(AAsset_getLength(asset));
	}
	return funopen(asset, sdk_android_asset_fread, sdk_android_asset_fwrite, sdk_android_asset_fseek, sdk_android_asset_fclose);
}
#	define DEBUG_INTERNAL_DATA_PATH /storage/emulated/0/Android/data/org.doubango.ultimateAlpr/files
#else
#	define ASSET_MGR_PARAM() 
#	define DEBUG_INTERNAL_DATA_PATH .
#endif /* ULTALPR_SDK_OS_ANDROID */

#define STRING_QUOTES__(string_) # string_
#define STRING_QUOTES_(string_) STRING_QUOTES__(string_)
#define STRING_QUOTES(string_) STRING_QUOTES_(STRING_QUOTES_(string_))

#define TEST_PARALLEL					1

#define DEBUG_WRITE_INPUT_IMAGE			false
#define DEBUG_DEBUG_LEVEL				info

#define DETECT_ROI						"[0, 0, 0, 0]" /* left, right, top, bottom: 645, 1241, 301, 711 */

#define RECOGN_SCORE_TYPE				median
#define RECOGN_MINSCORE					0.5
#define RECOGN_RECTIFY_ENABLED			false

#define TEST_JSON_CONFIG				"{ " STRING_QUOTES(debug_level) ":" STRING_QUOTES(DEBUG_DEBUG_LEVEL) \
	", " STRING_QUOTES(debug_write_input_image_enabled) ":" STRING_QUOTES_(DEBUG_WRITE_INPUT_IMAGE) \
	", " STRING_QUOTES(debug_internal_data_path) ":" STRING_QUOTES(DEBUG_INTERNAL_DATA_PATH) \
	\
	", " STRING_QUOTES(detect_roi) ":" STRING_QUOTES_(DETECT_ROI) \
	\
	", " STRING_QUOTES(recogn_score_type) ":" STRING_QUOTES(RECOGN_SCORE_TYPE) \
	", " STRING_QUOTES(recogn_minscore) ":" STRING_QUOTES_(RECOGN_MINSCORE) \
	", " STRING_QUOTES(recogn_rectify_enabled) ":" STRING_QUOTES_(RECOGN_RECTIFY_ENABLED) \
	" }"

#define TEST_LOOP_COUNT					1

#define TEST_FILE_NAME					"lic_us_1280x720_yuv420p.yuv"
#define TEST_FILE_PATH					"C:/Projects/GitHub/data/alpr/" TEST_FILE_NAME // These files are freely available at https://github.com/DoubangoTelecom/data
#define TEST_FILE_TYPE					ULTALPR_SDK_IMAGE_TYPE_YUV420P
#define TEST_FILE_WIDTH					1280
#define TEST_FILE_HEIGHT				720

static void* readFile(const char* path);

#if TEST_PARALLEL
class MyUltAlprSdkParallelDeliveryCallback : public UltAlprSdkParallelDeliveryCallback {
	virtual void onNewResult(const UltAlprSdkResult* result) const override {
		static size_t numParallelDeliveryResults = 0;
		ULTALPR_SDK_ASSERT(result != nullptr);
		const std::string& json = result->json();
		ULTALPR_SDK_PRINT_INFO("MyUltAlprSdkParallelDeliveryCallback::onNewResult(%d, %s, %zu): %s", 
			result->code(), 
			result->phrase().c_str(),
			++numParallelDeliveryResults,
			!json.empty() ? json.c_str() : "{}"
		);
	}
};
#endif /* TEST_PARALLEL */

int main()
{
	UltAlprSdkResult result(0, "OK", "{}");
#if TEST_PARALLEL
	MyUltAlprSdkParallelDeliveryCallback _parallelDeliveryCallbackCallback;
	const MyUltAlprSdkParallelDeliveryCallback* parallelDeliveryCallbackCallback = &_parallelDeliveryCallbackCallback;
#else
	const UltAlprSdkParallelDeliveryCallback* parallelDeliveryCallbackCallback = nullptr;
#endif /* TEST_PARALLEL */

	// Init
	ULTALPR_SDK_PRINT_INFO("Starting tests...");
	ULTALPR_SDK_ASSERT((result = UltAlprSdkEngine::init(
		ASSET_MGR_PARAM()
		TEST_JSON_CONFIG, parallelDeliveryCallbackCallback
	)).isOK());

	// read file data
	void* data = readFile(
#if ULTALPR_SDK_OS_ANDROID 
		TEST_FILE_NAME
#else
		TEST_FILE_PATH
#endif
	);
	ULTALPR_SDK_ASSERT(data != nullptr);

	// Warm up:
	// First time the SDK is called we'll be loading the models into CPU or GPU and initializing
	// some internal variables -> do not include this part in te timing.
	// The warm up function will make fake inference to force the engine to load the models and init the vars.
	if (TEST_LOOP_COUNT > 1) {
		ULTALPR_SDK_ASSERT((result = UltAlprSdkEngine::warmUp(
			TEST_FILE_TYPE
		)).isOK());
	}

	// Recognize
	const std::chrono::high_resolution_clock::time_point timeStart = std::chrono::high_resolution_clock::now();
	for (size_t i = 0; i < TEST_LOOP_COUNT; ++i) {
		ULTALPR_SDK_ASSERT((result = UltAlprSdkEngine::process(
			TEST_FILE_TYPE,
			data,
			TEST_FILE_WIDTH,
			TEST_FILE_HEIGHT
		)).isOK());
	}
	const std::chrono::high_resolution_clock::time_point timeEnd = std::chrono::high_resolution_clock::now();
	ULTALPR_SDK_PRINT_INFO("Elapsed time (ALPR) = [[[ %lf millis ]]]", std::chrono::duration_cast<std::chrono::duration<double > >(timeEnd - timeStart).count() * 1000.0);

	// Print result
	const std::string& json_ = result.json();
	if (!json_.empty()) {
		ULTALPR_SDK_PRINT_INFO("result: %s", json_.c_str());
	}

	// free memory
	free(data);

	ULTALPR_SDK_PRINT_INFO("Press any key to terminate !!");
	getchar();

	// DeInit
	ULTALPR_SDK_PRINT_INFO("Ending tests...");
	ULTALPR_SDK_ASSERT((result = UltAlprSdkEngine::deInit()).isOK());


	return 0;
}

static void* readFile(const char* path)
{
#if ULTALPR_SDK_OS_ANDROID
	size_t size;
	FILE* file = sdk_android_asset_fopen(path, "rb", &size);
	ULTALPR_SDK_ASSERT(file != nullptr && size > 0);
#else
	FILE* file = fopen(path, "rb");
	ULTALPR_SDK_ASSERT(file != nullptr);

	struct stat st_;
	ULTALPR_SDK_ASSERT(stat(path, &st_) == 0);
	const size_t size = static_cast<size_t>(st_.st_size);
	ULTALPR_SDK_ASSERT(size > 0);
#endif

	void* data = malloc(size);
	ULTALPR_SDK_ASSERT(data != nullptr);
	ULTALPR_SDK_ASSERT(size == fread(data, 1, size, file));

	fclose(file);

	return data;
}

