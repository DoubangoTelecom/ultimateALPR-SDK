#if !defined(_ULTIMATE_ALPR_SDK_SAMPLES_ANDROID_UTILS_H_)
#define _ULTIMATE_ALPR_SDK_SAMPLES_ANDROID_UTILS_H_

#include <ultimateALPR-SDK-API-PUBLIC.h>

#if ULTALPR_SDK_OS_ANDROID // below code come from https://github.com/DoubangoTelecom/compv/blob/master/api/include/compv/compv_api.h

using namespace ultimateAlprSdk;

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

static int sdk_android_asset_fread(void* cookie, char* buf, int size) {
	return AAsset_read((AAsset*)cookie, buf, size);
}
static int sdk_android_asset_fwrite(void* cookie, const char* buf, int size) {
	return EACCES;
}
static fpos_t sdk_android_asset_fseek(void* cookie, fpos_t offset, int whence) {
	return AAsset_seek((AAsset*)cookie, offset, whence);
}
static int sdk_android_asset_fclose(void* cookie) {
	AAsset_close((AAsset*)cookie);
	return 0;
}
static FILE* sdk_android_asset_fopen(const char* fname, const char* mode, size_t* size = nullptr) {
	AAsset* asset = AAssetManager_open(__sdk_android_assetmgr, fname, 0);
	if (!asset) {
		return nullptr;
	}
	if (size) {
		*size = static_cast<size_t>(AAsset_getLength(asset));
	}
	return funopen(asset, sdk_android_asset_fread, sdk_android_asset_fwrite, sdk_android_asset_fseek, sdk_android_asset_fclose);
}

#endif /* ULTALPR_SDK_OS_ANDROID */

#endif /* _ULTIMATE_ALPR_SDK_SAMPLES_ANDROID_UTILS_H_ */
