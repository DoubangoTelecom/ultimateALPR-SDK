/* Copyright (C) 2011-2020 Doubango Telecom <https://www.doubango.org>
* File author: Mamadou DIOP (Doubango Telecom, France).
* License: For non commercial use only.
* Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
* WebSite: https://www.doubango.org/webapps/alpr/
*/
/**
\file ultimateALPR-SDK-API-PUBLIC.h
ultimateALPR SDK public header
*/
#ifndef _ULTIMATE_ALPR_SDK_API_PUBLIC_H_
#define _ULTIMATE_ALPR_SDK_API_PUBLIC_H_

#include <string>

#define ULTALPR_SDK_VERSION_MAJOR		3
#define ULTALPR_SDK_VERSION_MINOR		10
#define ULTALPR_SDK_VERSION_MICRO		5

// Windows's symbols export
#if defined(SWIG)
# 	define ULTIMATE_ALPR_SDK_PUBLIC_API
#else
#	if (defined(WIN32) || defined(_WIN32) || defined(_WIN32_WCE) || defined(_WIN16) || defined(_WIN64) || defined(__WIN32__) || defined(__TOS_WIN__) || defined(__WINDOWS__)) && !defined(ULTALPR_SDK_STATIC)
#		if defined (ULTIMATE_ALPR_SDK_PUBLIC_EXPORTS)
# 			define ULTIMATE_ALPR_SDK_PUBLIC_API		__declspec(dllexport)
#		else
# 			define ULTIMATE_ALPR_SDK_PUBLIC_API		__declspec(dllimport)
#		endif
#	else
# 		define ULTIMATE_ALPR_SDK_PUBLIC_API			__attribute__((visibility("default")))
#	endif /* WIN32 */
#endif /* SWIG */

// Android OS detection
#if (defined(__ANDROID__) || defined(ANDROID)) && !defined(SWIG)
#	define ULTALPR_SDK_OS_ANDROID	1
#endif /* ULTALPR_SDK_OS_ANDROID */

// Macros to print logs to the console
#if ULTALPR_SDK_OS_ANDROID
#	if !defined(SWIG)
#		include <android/log.h>
#		include <android/asset_manager.h>
#		include <jni.h>
#	endif
#	define ULTALPR_SDK_PRINT_VERBOSE(FMT, ...) __android_log_print(ANDROID_LOG_VERBOSE, "org.doubango.ultimateAlpr.Sdk", "*[ULTALPR_SDK VERBOSE]: " FMT "\n", ##__VA_ARGS__)
#	define ULTALPR_SDK_PRINT_INFO(FMT, ...) __android_log_print(ANDROID_LOG_INFO, "org.doubango.ultimateAlpr.Sdk", "*[ULTALPR_SDK INFO]: " FMT "\n", ##__VA_ARGS__)
#	define ULTALPR_SDK_PRINT_WARN(FMT, ...) __android_log_print(ANDROID_LOG_WARN, "org.doubango.ultimateAlpr.Sdk", "**[ULTALPR_SDK WARN]: function: \"%s()\" \nfile: \"%s\" \nline: \"%u\" \nmessage: " FMT "\n", __FUNCTION__,  __FILE__, __LINE__, ##__VA_ARGS__)
#	define ULTALPR_SDK_PRINT_ERROR(FMT, ...) __android_log_print(ANDROID_LOG_ERROR, "org.doubango.ultimateAlpr.Sdk", "***[ULTALPR_SDK ERROR]: function: \"%s()\" \nfile: \"%s\" \nline: \"%u\" \nmessage: " FMT "\n", __FUNCTION__,  __FILE__, __LINE__, ##__VA_ARGS__)
#	define ULTALPR_SDK_PRINT_FATAL(FMT, ...) __android_log_print(ANDROID_LOG_FATAL, "org.doubango.ultimateAlpr.Sdk", "****[ULTALPR_SDK FATAL]: function: \"%s()\" \nfile: \"%s\" \nline: \"%u\" \nmessage: " FMT "\n", __FUNCTION__,  __FILE__, __LINE__, ##__VA_ARGS__)
#else
#	define ULTALPR_SDK_PRINT_VERBOSE(FMT, ...) fprintf(stderr, "*[ULTALPR_SDK VERBOSE]: " FMT "\n", ##__VA_ARGS__)
#	define ULTALPR_SDK_PRINT_INFO(FMT, ...) fprintf(stderr, "*[ULTALPR_SDK INFO]: " FMT "\n", ##__VA_ARGS__)
#	define ULTALPR_SDK_PRINT_WARN(FMT, ...) fprintf(stderr, "**[ULTALPR_SDK WARN]: function: \"%s()\" \nfile: \"%s\" \nline: \"%u\" \nmessage: " FMT "\n", __FUNCTION__,  __FILE__, __LINE__, ##__VA_ARGS__)
#	define ULTALPR_SDK_PRINT_ERROR(FMT, ...) fprintf(stderr, "***[ULTALPR_SDK ERROR]: function: \"%s()\" \nfile: \"%s\" \nline: \"%u\" \nmessage: " FMT "\n", __FUNCTION__,  __FILE__, __LINE__, ##__VA_ARGS__)
#	define ULTALPR_SDK_PRINT_FATAL(FMT, ...) fprintf(stderr, "****[ULTALPR_SDK FATAL]: function: \"%s()\" \nfile: \"%s\" \nline: \"%u\" \nmessage: " FMT "\n", __FUNCTION__,  __FILE__, __LINE__, ##__VA_ARGS__)
#endif /* !ULTALPR_SDK_OS_ANDROID */

// Assertion function
#if defined(NDEBUG)
#	define ULTALPR_SDK_ASSERT(x) do { bool __ULTALPR_SDK_b_ret = (x); if (!__ULTALPR_SDK_b_ret) { ULTALPR_SDK_PRINT_FATAL("Assertion failed!"); abort(); } } while(0)
#else
#	define ULTALPR_SDK_ASSERT(x) do { bool __ULTALPR_SDK_b_ret = (x); assert(__ULTALPR_SDK_b_ret); } while(0)
#endif /* !NDEBUG */

namespace ultimateAlprSdk  
{
	/*! Defines the image types.
	*/
	enum ULTALPR_SDK_IMAGE_TYPE {
		/*! Each pixel is stored on 3 bytes. Each channel (R, G, B) is stored with 8 bits of precision (256 possible values).
		* Here is how the pixels are packed:
		* \code{.cpp}
		* const int pixel = (B & 0xff) << 16 | (G & 0xff) << 8 | (R & 0xff);
		* \endcode
		*
		* Available since: 2.0.0
		*/
		ULTALPR_SDK_IMAGE_TYPE_RGB24,

		/*! Each pixel is stored on 4 bytes. Each channel (R, G, B, A) is stored with 8 bits (1 byte) of precision (256 possible values).
		* The R channel is stored at the lowest memory address followed by G, B then A channels. If you're using Android then,
		* this is the same as <a href="https://developer.android.com/reference/android/graphics/Bitmap.Config#ARGB_8888">ARGB_8888</a>.
		* Here is how the pixels are packed:
		* \code{.cpp}
		* const int pixel = (A & 0xff) << 24 | (B & 0xff) << 16 | (G & 0xff) << 8 | (R & 0xff);
		* \endcode
		*
		* Available since: 2.1.0
		*/
		ULTALPR_SDK_IMAGE_TYPE_RGBA32,

		/*! Each pixel is stored on 4 bytes. Each channel (B, G, R, A) is stored with 8 bits (1 byte) of precision (256 possible values).
		* The B channel is stored at the lowest memory address followed by G, R then A channels. If you're using iOS then,
		* this is the same as <a href="https://developer.apple.com/documentation/corevideo/1563591-pixel_format_identifiers/kcvpixelformattype_32bgra?language=objc">kCVPixelFormatType_32BGRA</a>.
		* Here is how the pixels are packed:
		* \code{.cpp}
		* const int pixel = (A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
		* \endcode
		*
		* Available since: 2.3.0
		*/
		ULTALPR_SDK_IMAGE_TYPE_BGRA32,

		/*! YUV 4:2:0 image with a plane of 8 bit Y samples followed by an interleaved U/V plane containing 8 bit 2x2 subsampled colour difference samples.
		*	More information at https://www.fourcc.org/pixel-format/yuv-nv12/
		*
		* Available since: 2.0.0
		*/
		ULTALPR_SDK_IMAGE_TYPE_NV12,

		/*! YUV 4:2:0 image with a plane of 8 bit Y samples followed by an interleaved V/U plane containing 8 bit 2x2 subsampled chroma samples. 
		* The same as \ref ULTALPR_SDK_IMAGE_TYPE_NV12 except the interleave order of U and V is reversed.
		*	More information at https://www.fourcc.org/pixel-format/yuv-nv21/
		*
		* Available since: 2.0.0
		*/
		ULTALPR_SDK_IMAGE_TYPE_NV21,

		/*! These formats are identical to YV12 except that the U and V plane order is reversed. 
		* They comprise an NxM Y plane followed by (N/2)x(M/2) U and V planes. 
		* This is the format of choice for many software MPEG codecs.
		* More information at https://www.fourcc.org/pixel-format/yuv-i420/
		*
		* Available since: 2.0.0
		*/
		ULTALPR_SDK_IMAGE_TYPE_YUV420P,

		/*! Same as \ref ULTALPR_SDK_IMAGE_TYPE_YUV420P except the order of U and V is reversed.
		* More information at https://www.fourcc.org/pixel-format/yuv-yv12/
		*
		* Available since: 2.0.0
		*/
		ULTALPR_SDK_IMAGE_TYPE_YVU420P,

		/*! YUV 4:2:2 image with an NxM Y plane followed by (N/2)x(M) V and U planes.
		*
		* Available since: 2.0.0
		*/
		ULTALPR_SDK_IMAGE_TYPE_YUV422P,

		/*! YUV 4:4:4 image with an NxM Y plane followed by NxM V and U planes.
		*
		* Available since: 2.0.0
		*/
		ULTALPR_SDK_IMAGE_TYPE_YUV444P,

		/*! Grayscale image with single channel (luminance only). Each pixel is stored in single byte (8 bit Y samples).
		*
		* Available since: 2.6.2
		*/
		ULTALPR_SDK_IMAGE_TYPE_Y,

		/*! Each pixel is stored on 3 bytes. Each channel (B, G, R) is stored with 8 bits (1 byte) of precision (256 possible values).
		* The B channel is stored at the lowest memory address followed by G then R channels. If you're using C# then,
		* this is the same as <b>PixelFormat.Format24bppRgb</b>.
		* Here is how the pixels are packed:
		* \code{.cpp}
		* const int pixel = (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
		* \endcode
		*
		* Available since: 2.8.0
		*/
		ULTALPR_SDK_IMAGE_TYPE_BGR24,
	};

	/*! Result returned by the \ref UltAlprSdkEngine "engine" at initialization, deInitialization and processing stages.
	*/
	class ULTIMATE_ALPR_SDK_PUBLIC_API UltAlprSdkResult {
	public:
		UltAlprSdkResult();
		UltAlprSdkResult(const int code, const char* phrase, const char* json, const size_t numPlates = 0, const size_t numCars = 0);
		UltAlprSdkResult(const UltAlprSdkResult& other);
		virtual ~UltAlprSdkResult();
#if !defined(SWIG)
		UltAlprSdkResult& operator=(const UltAlprSdkResult& other) { return operatorAssign(other); }
#endif

		/*! The result code. 0 if success, nonzero otherwise.
		*/
		inline int code()const { return code_; }
		/*! Short description for the \ref code.
		*/
		inline const char* phrase()const { return phrase_; }
		/*! The license plates as JSON content string. May be null if no plate found.
		*/
		inline const char* json()const { return json_; }
		/*! Number of license plates in \ref json string. This is a helper function to quickly check whether the result contains license plates
			without parsing the \ref json string.
		*/
		inline const size_t numPlates()const { return numPlates_; }
		/*! Number of cars in \ref json string. This is a helper function to quickly check whether the result contains cars
		without parsing the \ref json string.
		*
		* Available since: 3.2.0
		*/
		inline const size_t numCars()const { return numCars_; }
		/*! Whether the result is success. true if success, false otherwise.
		*/
		inline bool isOK()const { return (code_ == 0); }
#if !defined(SWIG)
		static UltAlprSdkResult bodyless(const int code, const char* phrase) { return UltAlprSdkResult(code, phrase, ""); }
		static UltAlprSdkResult bodylessOK() { return UltAlprSdkResult(0, "OK", ""); }
#endif /* SWIG */

	private:
		void ctor(const int code, const char* phrase, const char* json, const size_t numPlates, const size_t numCars);
#if !defined(SWIG)
		UltAlprSdkResult& operatorAssign(const UltAlprSdkResult& other);
#endif /* SWIG */

	private:
		int code_;
		char* phrase_;
		char* json_;
		size_t numPlates_;
		size_t numCars_;
	};

	/*! Callback function to be used to get asynchronous notifications.
	*/
	struct UltAlprSdkParallelDeliveryCallback 
	{
	protected:
		UltAlprSdkParallelDeliveryCallback() { }
	public:
		virtual ~UltAlprSdkParallelDeliveryCallback() {  }
		/*! Notification function to override in order to receive the results. */
		virtual void onNewResult(const UltAlprSdkResult* newResult) const = 0;
	};

	/*! The Automatic Number/License Plate Recognition (ANPR/ALPR) engine.
	*/
	class ULTIMATE_ALPR_SDK_PUBLIC_API UltAlprSdkEngine
	{
#if !defined(SWIG)
	protected:
		UltAlprSdkEngine() = delete;
#endif /* SWIG */
	public:

#if ULTALPR_SDK_OS_ANDROID
		/*! Initializes the engine. This function must be the first one to call.
			This function is only available on Android.
			\param assetManager AssetManager to use to read the content of the "assets" folder containing the models and configuration files.
			\param jsonConfig JSON string containing configuration entries. May be null. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html
			\param parallelDeliveryCallback Callback function to enable parallel mode. Use null value to use sequential instead of parallel mode. More info at https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html.
			\returns a result
		*/
		static UltAlprSdkResult init(jobject assetManager, const char* jsonConfig = nullptr, const UltAlprSdkParallelDeliveryCallback* parallelDeliveryCallback = nullptr);
#else
		/*! Initializes the engine. This function must be the first one to call.
			\param jsonConfig JSON string containing configuration entries. May be null. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html
			\param parallelDeliveryCallback \ref UltAlprSdkParallelDeliveryCallback "callback" function to enable parallel mode. Use null value to use sequential instead of parallel mode. More info at https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html.
			\returns a \ref UltAlprSdkResult "result"
		*/
		static UltAlprSdkResult init(const char* jsonConfig = nullptr, const UltAlprSdkParallelDeliveryCallback* parallelDeliveryCallback = nullptr);
#endif /* ULTALPR_SDK_OS_ANDROID */

		/*! DeInitialize the engine. This function must be the last one to be call.
			Deallocate all the resources allocated using \ref init function.
			\returns a \ref UltAlprSdkResult "result"
		*/
		static UltAlprSdkResult deInit();

		/*! Performs ANPR detection and recognition operations.
			<br />
			If you're using OpenCV to capture images from the camera or RTSP stream, the function could be used like this:
			\code{.cpp}
			VideoCapture cap(....);

			while (1) {
				Mat frame;
				cap >> frame;

				if (frame.empty()) {
					break;
				}

				ULTALPR_SDK_ASSERT((result = UltAlprSdkEngine::process(
					ULTALPR_SDK_IMAGE_TYPE_BGR24,
					frame.ptr(),
					frame.size().width,
					frame.size().height
				)).isOK());
			}
			\endcode
			\param imageType The image type.
			\param imageData Pointer to the image data.
			\param imageWidthInSamples Image width in samples.
			\param imageHeightInSamples Image height in samples.
			\param imageStrideInSamples Image stride in samples. Should be zero unless your the data is strided.
			\param imageExifOrientation Image EXIF/JPEG orientation. Must be within [1, 8]. More information at https://www.impulseadventure.com/photo/exif-orientation.html. Available since: 2.3.0.
			\returns a \ref UltAlprSdkResult "result"
		*/
		static UltAlprSdkResult process(
			const ULTALPR_SDK_IMAGE_TYPE imageType, 
			const void* imageData, 
			const size_t imageWidthInSamples,
			const size_t imageHeightInSamples,
			const size_t imageStrideInSamples = 0,
			const int imageExifOrientation = 1
		);

		/*! Performs ANPR detection and recognition operations.
			<br />
			If you're using FFmpeg, the function could be used like this:
			\code{.cpp}
			ULTALPR_SDK_ASSERT((result = UltAlprSdkEngine::process(
				ULTALPR_SDK_IMAGE_TYPE_YUV420P,
				frame->data[0], // Y
				frame->data[1], // U
				frame->data[2], // V
				frame->width, // Width
				frame->height, // Height
				frame->linesize[0], // Y-stride
				frame->linesize[1], // U-stride
				frame->linesize[2] // V-stride
			)).isOK());
			\endcode
			\param imageType The image type.
			\param yPtr Pointer to the start of the Y (luma) samples.
			\param uPtr Pointer to the start of the U (chroma) samples.
			\param vPtr Pointer to the start of the V (chroma) samples.
			\param widthInSamples Image width in samples.
			\param heightInSamples Image height in samples.
			\param yStrideInBytes Stride in bytes for the Y (luma) samples.
			\param uStrideInBytes Stride in bytes for the U (chroma) samples.
			\param vStrideInBytes Stride in bytes for the V (chroma) samples.
			\param uvPixelStrideInBytes Pixel stride in bytes for the UV (chroma) samples. Should be 1 for planar and 2 for semi-planar formats. Set to 0 for auto-detect.
			\param exifOrientation Image EXIF/JPEG orientation. Must be within [1, 8]. More information at https://www.impulseadventure.com/photo/exif-orientation.html. Available since: 2.3.0.
			\returns a \ref UltAlprSdkResult "result"
		*/
		static UltAlprSdkResult process(
			const ULTALPR_SDK_IMAGE_TYPE imageType,
			const void* yPtr,
			const void* uPtr,
			const void* vPtr,
			const size_t widthInSamples,
			const size_t heightInSamples,
			const size_t yStrideInBytes,
			const size_t uStrideInBytes,
			const size_t vStrideInBytes,
			const size_t uvPixelStrideInBytes = 0,
			const int exifOrientation = 1
		);

		/*! Retrieve EXIF orientation value from JPEG meta-data.
			\param jpegMetaDataPtr Pointer to the meta-data.
			\param jpegMetaDataSize Size of the meta-data.
			\returns Image's EXIF/JPEG orientation. Must be within [1, 8]. More information at https://www.impulseadventure.com/photo/exif-orientation.html.

			Available since: 3.4.0
		*/
		static int exifOrientation(const void* jpegMetaDataPtr, const size_t jpegMetaDataSize);
		
		/*! Build a unique runtime license key associated to this device.
			You must \ref init "initialize" the engine before calling this function.
			This function doesn't require internet connection.
			The runtime key must be activated to obtain a token. The activation procedure is explained at https://www.doubango.org/SDKs/LicenseManager/docs/Activation_use_cases.html.
			\param rawInsteadOfJSON Whether to output the runtime key as raw string intead of JSON entry. Requesting raw
				string instead of JSON could be helpful for applications without JSON parser to extract the key.
			\returns a \ref UltAlprSdkResult "result"
		*/
		static UltAlprSdkResult requestRuntimeLicenseKey(const bool& rawInsteadOfJSON = false);

		static UltAlprSdkResult warmUp(const ULTALPR_SDK_IMAGE_TYPE imageType);
#if !defined(SWIG)
		static UltAlprSdkResult optimizeTRT(const char* models_folder);
#endif /* !defined(SWIG) */

#if ULTALPR_SDK_OS_ANDROID && !defined(SWIG)
		static void setAssetManager(AAssetManager* assetManager);
		static void setJavaVM(JavaVM* vm);

	private:
		static bool s_bOweAAssetManager;
#endif /* ULTALPR_SDK_OS_ANDROID */
	};

} // namespace ultimateAlprSdk 

#endif // _ULTIMATE_ALPR_SDK_API_PUBLIC_H_
