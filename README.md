<hr />

 - Online web demo at https://www.doubango.org/webapps/alpr/
 - Full documentation for the SDK at https://www.doubango.org/SDKs/anpr/docs/
 - Supported languages (API): **C++**, **C#**, **Java** and **Python**
 - Open source Computer Vision Library: https://github.com/DoubangoTelecom/compv
  
<hr />

 - [Trying samples (**C++**, **C#**, **Java** and **Python**)](#trying-samples)
 - [Benchmark](#benchmark)
 - [Getting help](#technical-questions)

<hr />
<hr />

**UltimateALPR** is a highly optimized [ANPR / ALPR (Automatic Number/License Plate Recognition)](https://en.wikipedia.org/wiki/Automatic_number-plate_recognition>) SDK designed to run offline on embedded devices (cameras, **ARM64** mobile devices...) and servers (**X86_64**). The SDK focuses on speed and accuracy with a particular touch on low memory usage thanks to quantized models (QUINT8).

The SDK supports multiple deeplearning frameworks ([Tensorflow Lite](https://www.tensorflow.org/lite/guide), [OpenVINO](https://docs.openvino.ai), [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt), [Hailo SDK](https://hailo.ai/products/hailo-software/hailo-ai-software-suite), [RKNPU2](https://github.com/rockchip-linux/rknpu2), [ONNX Runtime](https://onnxruntime.ai)...) to allow targeting various types of **GPUs** and **NPUs**.

The latest version (**v4.0.0**) is a complete re-design and focuses on broad support for different **NPUs** and **GPUs** to maximize speed:

 - NVIDIA GPUs (Desktop, Jetson...) via ONNX Runtime (cuda) and TensorRT 
 - Intel GPUs, NPUs and FPGAs via OpenVINO
 - Rockchip NPUs (radxa rock 4b, orange pi...) via rknpu2 framework
 - Hailo NPUs
 - Qualcomm, Verisilicon (**NXP i.MX 8M Plus**, **Khadas**, **STMicroelectronics**...) and many others NPUs thanks to Tensorflow Lite external delegates.

With a minimum frame rate as high as [**37 fps on Raspberry Pi 5**, **91 fps on Galaxy S24**, **310 fps on Orange Pi 5** and **472 fps on Jetson Nano Orin**](samples/c++/benchmark/README.md#peformance-numbers), this is the fastest ANPR/ALPR implementation you'll find on the market. Being fast is important but being accurate is crucial. We use state of the art deep learning techniques (multi-head attention, transformers...) to offer unmatched accuracy and precision.

The next [video](https://doubango.org/videos/anpr/highway-x264.mp4) shows the [Recognizer sample](#sample-application-recognizer-others) running on Windows: <br />
[![Recognizer Running on Windows](https://www.doubango.org/SDKs/anpr/docs/_images/vlcsnap-2020-09-10-03h27m56s176.jpg)](https://doubango.org/videos/anpr/highway-x264.mp4)
<hr />

In addition to [License Plate Recognition (LPR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#features-licenseplaterecognition) we support [Image Enhancement for Night-Vision (IENV)](https://www.doubango.org/SDKs/anpr/docs/Features.html#image-enhancement-for-night-vision-ienv), [License Plate Country Identification (LPCI)](https://www.doubango.org/SDKs/anpr/docs/Features.html#features-licenseplatecountryidentification), [Vehicle Color Recognition (VCR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#features-vehiclecolorrecognition), [Vehicle Make Model Recognition (VMMR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#features-vehiclemakemodelrecognition), [Vehicle Body Style Recognition (VBSR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-body-style-recognition-vbsr), [Vehicle Direction Tracking (VDT)](https://www.doubango.org/SDKs/anpr/docs/Features.html#features-vehicledirectiontracking) and [Vehicle Speed Estimation (VSE)](https://www.doubango.org/SDKs/anpr/docs/Features.html#features-vehiclespeedestimation).

Don't take our word for it, come check our implementation. **No registration, license key or internet connection is required**, just clone the code and start coding/testing. Everything runs on the device, no data is leaving your computer. 

You can also check our online [cloud-based implementation](https://www.doubango.org/webapps/alpr/) (*no registration required*) to check out the accuracy and precision before starting to play with the SDK.

<a name="trying-samples"></a>
## Trying samples (**C++**, **C#**, **Java** and **Python**) ##
Go to the [samples folder](samples) and choose your prefered language. You don't need to build the samples by yourself, the [binaries](binaries) folder contains pre-built apps to quickly test the SDK.

<a name="benchmark"></a>
## Benchmark ##
This SDK is the fastest you'll find on the market, we provide pre-built [binaries](binaries) to check the speed on your device. Here are some benchmark numbers on most commonly used ARM64 edge devices:

| ARM64 edge Device  | Frame rate 
|--- | --- |
| Jetson Nano Orin (TensorRT GPU) | 472.89 |
| Orange Pi 5 (RKNPU2) | 310.21 |
| Debix A (NXP i.MX 8M Plus NPU) | 71.02 |
| Raspberry Pi 5 (Hailo-8L NPU) | 143.67 |
| Raspberry Pi 5 (TfLite CPU) | 37.41 |
| Samsung Galaxy S24 (TfLite CPU) | 91.24 |

More benchmark numbers could be found at [samples/c++/benchmark](samples/c++/benchmark/README.md#peformance-numbers).

<a name="technical-questions"></a>
 # Technical questions #
 Please check our [discussion group](https://groups.google.com/forum/#!forum/doubango-ai) or [twitter account](https://twitter.com/doubangotelecom?lang=en)
