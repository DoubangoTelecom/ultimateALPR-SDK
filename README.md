  - [Getting started](#getting-started)
  - [Android](#android)
  	- [Sample applications](#sample-applications-android)
		- [Benchmark](#sample-application-benchmark-android) (**Java**)
		- [VideoParallel](#sample-application-videoparallel-android) (**Java**)
		- [VideoSequential](#sample-application-videosequential-android) (**Java**)
		- [ImageSnap](#sample-application-imagesnap-android) (**Java**)
	- [Trying the samples](#trying-the-samples-android)
	- [Adding the SDK to your project](#adding-the-sdk-to-your-project-android)
	- [Using the Java API](#using-the-java-api-android)
 - [Raspberry Pi (Raspbian OS), Linux, NVIDIA Jetson, Windows and others](#others)
 	- [Sample applications](#sample-applications-others)
		- [Benchmark](#sample-application-benchmark-others) (**C++**)
		- [Recognizer](#sample-application-recognizer-others) (**C++**, **C#**, **Java** and **Python**)
	- [Using the C++ API](#using-the-cpp-api-others)
 - [Getting help](#technical-questions)
  
 - Online web demo at https://www.doubango.org/webapps/alpr/
 - Full documentation for the SDK at https://www.doubango.org/SDKs/anpr/docs/
 - Supported languages (API): **C++**, **C#**, **Java** and **Python**
 - Open source Computer Vision Library: https://github.com/DoubangoTelecom/compv
  
<hr />

**Keywords:** `Image Enhancement for Night-Vision (IENV)`, `License Plate Recognition (LPR)`, `License Plate Country Identification (LPCI)`, `Vehicle Color Recognition (VCR)`, `Vehicle Make Model Recognition (VMMR)`, `Vehicle Body Style Recognition (VBSR)`, `Vehicle Direction Tracking (VDT)` and `Vehicle Speed Estimation (VSE)`

<hr />
  
Have you ever seen a deep learning based [ANPR/ALPR (Automatic Number/License Plate Recognition)](https://en.wikipedia.org/wiki/Automatic_number-plate_recognition) engine running at **47fps on ARM device** (Android, Snapdragon 855, 720p video resolution)? <br />

With an average frame rate as high as **47 fps on ARM** devices (Snapdragon 855) this is the fastest ANPR/ALPR implementation you'll find on the market. 
Being fast is important but being accurate is crucial. 
We use state of the art deep learning techniques to offer unmatched accuracy and precision. As a comparison this is **#33 times faster than** [OpenALPR on Android](https://github.com/SandroMachado/openalpr-android).
(see [benchmark section](https://www.doubango.org/SDKs/anpr/docs/Benchmark.html) for more information).

No need for special or dedicated GPUs, everything is running on CPU with **SIMD ARM NEON** optimizations, fixed-point math operations and multithreading.
This opens the doors for the possibilities of running fully featured [ITS (Intelligent Transportation System)](https://en.wikipedia.org/wiki/Intelligent_transportation_system) solutions on a camera without soliciting a cloud. 
Being able to run all ITS applications on the device **will significantly lower the cost to acquire, deploy and maintain** such systems. 
Please check [Device-based versus Cloud-based solution](https://www.doubango.org/SDKs/anpr/docs/Device-based_versus_Cloud-based_solution.html) section for more information about how this would reduce the cost.

The next [video](https://youtu.be/xQO7ABHTg1w) ([https://youtu.be/xQO7ABHTg1w](https://youtu.be/xQO7ABHTg1w)) shows the [Recognizer sample](#sample-application-recognizer-others) running on Windows: <br />
[![Recognizer Running on Windows](https://www.doubango.org/SDKs/anpr/docs/_images/vlcsnap-2020-09-10-03h27m56s176.jpg)](https://www.youtube.com/watch?v=xQO7ABHTg1w)
<hr />

The next [video](https://youtu.be/QCkLPP1ix-c) ([https://youtu.be/QCkLPP1ix-c](https://youtu.be/QCkLPP1ix-c)) shows [Image Enhancement for Night-Vision (IENV)](https://www.doubango.org/SDKs/anpr/docs/Features.html#image-enhancement-for-night-vision-ienv) result on video stream captured at night: <br />
[![Image Enhancement for Night-Vision (IENV)](https://www.doubango.org/SDKs/anpr/docs/_images/vlcsnap-2020-12-26-22h55m21s103.jpg)](https://www.youtube.com/watch?v=QCkLPP1ix-c)
<hr />

The code is accelerated on **CPU**, **GPU**, **VPU** and **FPGA**, thanks to [CUDA](https://developer.nvidia.com/cuda-toolkit), [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) and [Intel OpenVINO](https://software.intel.com/content/www/us/en/develop/tools/openvino-toolkit/hardware.html).

In addition to [License Plate Recognition (LPR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#features-licenseplaterecognition) we support [Image Enhancement for Night-Vision (IENV)](https://www.doubango.org/SDKs/anpr/docs/Features.html#image-enhancement-for-night-vision-ienv), [License Plate Country Identification (LPCI)](https://www.doubango.org/SDKs/anpr/docs/Features.html#features-licenseplatecountryidentification), [Vehicle Color Recognition (VCR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#features-vehiclecolorrecognition), [Vehicle Make Model Recognition (VMMR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#features-vehiclemakemodelrecognition), [Vehicle Body Style Recognition (VBSR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-body-style-recognition-vbsr), [Vehicle Direction Tracking (VDT)](https://www.doubango.org/SDKs/anpr/docs/Features.html#features-vehicledirectiontracking) and [Vehicle Speed Estimation (VSE)](https://www.doubango.org/SDKs/anpr/docs/Features.html#features-vehiclespeedestimation).


On high-end NVIDIA GPUs like the **Tesla V100 the frame rate is 315 fps which means 3.17 millisecond inference time**. On high-end CPUs like **Intel Xeon the maximum frame rate could be up to 237fps**, thanks to [OpenVINO](https://software.intel.com/content/www/us/en/develop/tools/openvino-toolkit/hardware.html). On low-end CPUs like the **Raspberry Pi 4 the average frame rate is 12fps**.

Don't take our word for it, come check our implementation. **No registration, license key or internet connection is required**, just clone the code and start coding/testing. Everything runs on the device, no data is leaving your computer. 
The code released here comes with many ready-to-use samples for [Android](#sample-applications-android), [Raspberry Pi](#sample-applications-others), [Linux](#sample-applications-others) and [Windows](#sample-applications-others) to help you get started easily. 

You can also check our online [cloud-based implementation](https://www.doubango.org/webapps/alpr/) (*no registration required*) to check out the accuracy and precision before starting to play with the SDK.

Please check full documentation at https://www.doubango.org/SDKs/anpr/docs/

<a name="getting-started"></a>
# Getting started # 
The SDK works on [many platforms](https://www.doubango.org/SDKs/anpr/docs/Architecture_overview.html#supportedoperatingsystems) and comes with support for many [programming languages](https://www.doubango.org/SDKs/anpr/docs/Architecture_overview.html#supportedprogramminglanguages) but the next sections focus on [Android](#android), [Raspberry Pi, Linux and Windows](#others). 

<a name="android"></a>
# Android #

The next sections are about Android and Java API.

<a name="sample-applications-android"></a>
## Sample applications (Android) ##
The source code comes with #4 Android sample applications: [Benchmark](#sample-application-benchmark-android), [VideoParallel](#sample-application-videoparallel-android), [VideoSequential](sample-application-videosequential-android) and [ImageSnap](sample-application-imagesnap-android).

<a name="sample-application-benchmark-android"></a>
### Benchmark (Android) ###
This application is used to check everything is ok and running as fast as expected. 
The information about the maximum frame rate (**237fps** on Intel Xeon, **47fps** on Snapdragon 855 and **12fps** on Raspberry Pi 4) could be checked using this application. 
It's open source and doesn't require registration or license key.

<a name="sample-application-videoparallel-android"></a>
### VideoParallel (Android) ###
This application should be used as reference code by any developer trying to add ultimateALPR to their products. It shows how to detect and recognize license plates in realtime using live video stream from the camera.
Please check [Parallel versus sequential processing section](https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html#parallelversussequentialprocessing) for more info about parellel mode.

<a name="sample-application-videosequential-android"></a>
### VideoSequential (Android) ###
Same as VideoParallel but working on sequential mode which means slower. This application is provided to ease comparing the modes: Parallel versus Sequential.

<a name="sample-application-imagesnap"></a>
### ImageSnap (Android) ###
This application reads and display the live video stream from the camera but only recognize an image from the stream on demand.

<a name="trying-the-samples-android"></a>
## Trying the samples (Android) ##
To try the sample applications on Android:
 1. Open Android Studio and select "Open an existing Android Studio project"
![alt text](https://www.doubango.org/SDKs/anpr/docs/_images/android_studio_open_existing_project.jpg "Open an existing Android Studio project")

 2. Navigate to **ultimateALPR-SDK/samples**, select **android** folder and click **OK**
![alt text](https://www.doubango.org/SDKs/anpr/docs/_images/android_studio_select_samples_android.jpg "Select project")

 3. Select the sample you want to try (e.g. **videoparallel**) and press **run**. Make sure to have the device on **landscape mode** for better experience.
![alt text](https://www.doubango.org/SDKs/anpr/docs/_images/android_studio_select_samples_videoparallel.jpg "Select sample")
            
<a name="adding-the-sdk-to-your-project-android"></a>
## Adding the SDK to your project (Android) ##
The SDK is distributed as an Android Studio module and you can add it as reference or you can also build it and add the AAR to your project. But, the easiest way to add the SDK to your project is by directly including the source.

In your *build.gradle* file add:

```python
android {

      # This is the block to add within "android { } " section
      sourceSets {
         main {
             jniLibs.srcDirs += ['path-to-your-ultimateALPR-SDK/binaries/android/jniLibs']
             java.srcDirs += ['path-to-your-ultimateALPR-SDK/java/android']
             assets.srcDirs += ['path-to-your-ultimateALPR-SDK/assets/models']
         }
      }
}
```

<a name="using-the-java-api-android"></a>
## Using the Java API (Android) ##

It's hard to be lost when you try to use the API as there are only 3 useful functions: init, process and deInit.

The C++ API is defined [here](https://www.doubango.org/SDKs/anpr/docs/cpp-api.html).

```java

	import org.doubango.ultimateAlpr.Sdk.ULTALPR_SDK_IMAGE_TYPE;
	import org.doubango.ultimateAlpr.Sdk.UltAlprSdkEngine;
	import org.doubango.ultimateAlpr.Sdk.UltAlprSdkParallelDeliveryCallback;
	import org.doubango.ultimateAlpr.Sdk.UltAlprSdkResult;

	final static String CONFIG = "{" +
		"\"debug_level\": \"info\"," + 
		"\"gpgpu_enabled\": true," + 
		"\"openvino_enabled\": true," +
		"\"openvino_device\": \"CPU\"," +

		"\"detect_minscore\": 0.1," + 
		"\"detect_quantization_enabled\": true," + 
		
		"\"pyramidal_search_enabled\": true," +
		"\"pyramidal_search_sensitivity\": 0.28," +
		"\"pyramidal_search_minscore\": 0.5," +
		"\"pyramidal_search_quantization_enabled\": true," +

		"\"klass_lpci_enabled\": true," +
		"\"klass_vcr_enabled\": true," +
		"\"klass_vmm_enabled\": true," +

		"\"recogn_score_type\": \"min\"," + 
		"\"recogn_minscore\": 0.3," + 
		"\"recogn_rectify_enabled\": false," + 
		"\"recogn_quantization_enabled\": true" + 
	"}";

	/**
	* Parallel callback delivery function used to notify about new results.
	* This callback will be called few milliseconds (before next frame is completely processed)
	* after process function is called.
	*/
	static class MyUltAlprSdkParallelDeliveryCallback extends UltAlprSdkParallelDeliveryCallback {
		@Override
		public void onNewResult(UltAlprSdkResult result) { }
	}

	final MyUltAlprSdkParallelDeliveryCallback mCallback = new MyUltAlprSdkParallelDeliveryCallback(); // set to null to disable parallel mode

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		// Initialize the engine
		assert UltAlprSdkEngine.init(
				getAssets(),
				CONFIG,
				mCallback
		).isOK();
	}

	// Camera listener: https://developer.android.com/reference/android/media/ImageReader.OnImageAvailableListener
	final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {

		@Override
		public void onImageAvailable(ImageReader reader) {
				try {
				    final Image image = reader.acquireLatestImage();
				    if (image == null) {
				        return;
				    }

				    // ANPR/ALPR recognition
				    final Image.Plane[] planes = image.getPlanes();
				    final UltAlprSdkResult result = UltAlprSdkEngine.process(
				        ULTALPR_SDK_IMAGE_TYPE.ULTALPR_SDK_IMAGE_TYPE_YUV420P,
				        planes[0].getBuffer(),
				        planes[1].getBuffer(),
				        planes[2].getBuffer(),
				        image.getWidth(),
				        image.getHeight(),
				        planes[0].getRowStride(),
				        planes[1].getRowStride(),
				        planes[2].getRowStride(),
				        planes[1].getPixelStride()
				    );
				    assert result.isOK();

				    image.close();

				} catch (final Exception e) {
				   e.printStackTrace();
				}
		}
	};

	@Override
	public void onDestroy() {
		// DeInitialize the engine
		assert UltAlprSdkEngine.deInit().isOK();

		super.onDestroy();
	}
```

Again, please check the sample applications for [Android](#sample-applications-android), [Raspberry Pi, Linux and Windows](#sample-applications-others) and [full documentation](https://www.doubango.org/SDKs/anpr/docs/) for more information.

<a name="others"></a>
# Raspberry Pi (Raspbian OS), Linux, NVIDIA Jetson, Windows and others #

<a name="sample-applications-others"></a>
## Sample applications (Raspberry Pi, Linux, NVIDIA Jetson, Windows and others) ##
The source code comes with #2 [C++ sample applications](samples/c++): [Benchmark](#sample-application-benchmark-others) and [Recognizer](#sample-application-recognizer-others). These sample applications can be used on all supported platforms: **Android**, **Windows**, **Raspberry Pi**, **iOS**, **OSX**, **Linux**...

<a name="sample-application-benchmark-others"></a>
### Benchmark (Raspberry Pi, Linux, NVIDIA Jetson, Windows and others) ###
This application is used to check everything is ok and running as fast as expected. 
The information about the maximum frame rate (**237fps** on Intel Xeon, **47fps** on Snapdragon 855, **152fps** on Jetson NX, **30fps** on Jetson nano and **12fps** on Raspberry Pi 4) could be checked using this application. 
It's open source and doesn't require registration or license key.

For more information on how to build and run this sample please check [samples/c++/benchmark](samples/c++/benchmark/README.md).

<a name="sample-application-recognizer-others"></a>
### Recognizer (Raspberry Pi, Linux, NVIDIA Jetson, Windows and others) ###
This is a command line application used to detect and recognize a license plate from any JPEG/PNG/BMP image.

For more information on how to build and run this sample please check:
 - C++: [samples/c++/recognizer](samples/c++/recognizer/README.md).
 - C#: [samples/csharp/recognizer](samples/csharp/recognizer/README.md).
 - Java: [samples/java/recognizer](samples/java/recognizer/README.md).
 - Python: [samples/python/recognizer](samples/python/recognizer/README.md).

<a name="using-the-cpp-api-others"></a>
## Using the C++ API ##
The C++ API is defined at https://www.doubango.org/SDKs/anpr/docs/cpp-api.html.

```cpp
	#include <ultimateALPR-SDK-API-PUBLIC.h> // Include the API header file

	// JSON configuration string
	// More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html
	static const char* __jsonConfig =
	"{"
	"\"debug_level\": \"info\","
	"\"debug_write_input_image_enabled\": false,"
	"\"debug_internal_data_path\": \".\","
	""
	"\"num_threads\": -1,"
	"\"gpgpu_enabled\": true,"
	"\"openvino_enabled\": true,"
	"\"openvino_device\": \"CPU\","
	""
	"\"detect_roi\": [0, 0, 0, 0],"
	"\"detect_minscore\": 0.1,"
	""
	"\"pyramidal_search_enabled\": true,"
	"\"pyramidal_search_sensitivity\": 0.28,"
	"\"pyramidal_search_minscore\": 0.3,"
	"\"pyramidal_search_min_image_size_inpixels\": 800,"
	""
	"\"klass_lpci_enabled\": true,"
	"\"klass_vcr_enabled\": true,"
	"\"klass_vmm_enabled\": true,"
	""
	"\"recogn_minscore\": 0.3,"
	"\"recogn_score_type\": \"min\""
	"}";

	// Local variable
	UltAlprSdkResult result;

	// Initialize the engine (should be done once)
	ULTALPR_SDK_ASSERT((result = UltAlprSdkEngine::init(
		__jsonConfig
	)).isOK());

	// Processing (detection + recognition)
	// Call this function for every video frame
	const void* imageData = nullptr;
	ULTALPR_SDK_ASSERT((result = UltAlprSdkEngine::process(
			ULTMICR_SDK_IMAGE_TYPE_RGB24,
			imageData,
			imageWidth,
			imageHeight
		)).isOK());

	// DeInit
	// Call this function before exiting the app to free the allocate resources
	// You must not call process() after calling this function
	ULTALPR_SDK_ASSERT((result = UltAlprSdkEngine::deInit()).isOK());
```

Again, please check the [sample applications](#Sample-applications) for more information on how to use the API.

<a name="technical-questions"></a>
 # Technical questions #
 Please check our [discussion group](https://groups.google.com/forum/#!forum/doubango-ai) or [twitter account](https://twitter.com/doubangotelecom?lang=en)
