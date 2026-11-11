- [Sample applications](#sample-applications-android)
  - [Benchmark](#sample-application-benchmark-android)
  - [VideoParallel](#sample-application-videoparallel-android)
  - [VideoSequential](#sample-application-videosequential-android)
- [Trying the samples](#trying-the-samples-android)
- [Adding the SDK to your project](#adding-the-sdk-to-your-project-android)
- [Using the Java API](#using-the-java-api-android)

<a name="sample-applications-android"></a>
## Sample applications ##
The source code comes with #3 Android sample applications: [Benchmark](benchmark), [VideoParallel](videoparallel) and [VideoSequential](videosequential).
No license is required to run these samples.

<a name="sample-application-benchmark-android"></a>
### Benchmark ###
This application is used to check everything is ok and running as fast as expected. 

<a name="sample-application-videoparallel-android"></a>
### VideoParallel ###
This application should be used as reference code by any developer trying to add ultimateALPR to their products. It shows how to detect and recognize license plates in realtime using live video stream from the camera.
Please check [Parallel versus sequential processing section](https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html#parallelversussequentialprocessing) for more info about parellel mode.

<a name="sample-application-videosequential-android"></a>
### VideoSequential ###
Same as VideoParallel but working on sequential mode which means slower. This application is provided to ease comparing the modes: Parallel versus Sequential.

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
    
      sourceSets {
         main {
             jniLibs.srcDirs += ['path-to-your-ultimateALPR-SDK/binaries/android/jniLibs']
             java.srcDirs += ['path-to-your-ultimateALPR-SDK/java/android']
             assets.srcDirs += ['path-to-your-ultimateALPR-SDK/assets/models.tflite']
         }
      }

      packagingOptions {
        jniLibs {
            useLegacyPackaging = true
        }
     }

	 dependencies {
	    implementation project(":sdk")
	}
}
```

<a name="using-the-java-api-android"></a>
## Using the Java API ##

It's hard to be lost when you try to use the API as there are only 3 useful functions: init, process and deInit.

The C++ API is defined [here](https://www.doubango.org/SDKs/anpr/docs/cpp-api.html).

```java

	import org.doubango.ultimateAlpr.Sdk.ULTALPR_SDK_IMAGE_TYPE;
	import org.doubango.ultimateAlpr.Sdk.UltAlprSdkEngine;
	import org.doubango.ultimateAlpr.Sdk.UltAlprSdkParallelDeliveryCallback;
	import org.doubango.ultimateAlpr.Sdk.UltAlprSdkResult;

  // https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html
	final static String CONFIG = "{" +
		"\"debug_level\": \"info\"," + 
		"\"num_threads\": -1," + 
		"\"night_vision_enabled\": false," +
		"\"backend\": \"auto\"," +
		"\"platform\": \"auto\"," +
		
		"\"pyramidal_search_enabled\": true," +
		"\"recogn_enabled\": true," +
		"\"klass_lpci_enabled\": true," +
		"\"klass_vcr_enabled\": true," +
		"\"klass_vmmr_enabled\": true" +
	"}";

	/**
	* Parallel callback delivery function used to notify about new results.
	* This callback will be called few milliseconds (before next frame is completely processed)
	* after process function is called.
	*/
	static class MyUltAlprSdkParallelDeliveryCallback extends UltAlprSdkParallelDeliveryCallback {
		@Override
		public void onNewResult(UltAlprSdkResult result) {
		   Log.d("Sample", result.json());
		}
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
				    Log.d("Sample", result.json());
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
