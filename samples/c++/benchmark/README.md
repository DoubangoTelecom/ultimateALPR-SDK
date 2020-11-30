- [Dependencies](#dependencies)
  - [Debugging missing dependencies](#dependencies-debugging)
- [GPGPU acceleration](#gpu-acceleration)
- [Peformance numbers](#peformance-numbers)
- [Pre-built binaries](#prebuilt)
- [Building](#building)
  - [Windows](#building-windows)
  - [Generic GCC](#building-generic-gcc)
  - [Raspberry Pi (Raspbian OS)](#building-rpi)
- [Testing](#testing)
  - [Usage](#testing-usage)
  - [Examples](#testing-examples)


This application is used to check everything is ok and running as fast as expected. 
The information about the maximum frame rate (**237fps** on Intel Xeon, **47fps** on Snapdragon 855 and **12fps** on Raspberry Pi 4) could be checked using this application. 
It's open source and doesn't require registration or license key.

More information about the benchmark rules at [https://www.doubango.org/SDKs/anpr/docs/Benchmark.html](https://www.doubango.org/SDKs/anpr/docs/Benchmark.html).

<a name="dependencies"></a>
# Dependencies #
**The SDK is developed in C++11** and you'll need **glibc 2.27+** on *Linux* and **[Visual C++ Redistributable for Visual Studio 2015](https://www.microsoft.com/en-us/download/details.aspx?id=48145)** (any later version is ok) on *Windows*.  **You most likely already have these dependencies on you machine** as almost every program require it.

If you're planning to use [OpenVINO](https://docs.openvinotoolkit.org/), then you'll need [Intel C++ Compiler Redistributable](https://software.intel.com/en-us/articles/intel-compilers-redistributable-libraries-by-version) (choose newest). Please note that OpenVINO is packaged in the SDK as plugin and loaded (`dlopen`) at runtime. The engine will fail to load the plugin if [Intel C++ Compiler Redistributable](https://software.intel.com/en-us/articles/intel-compilers-redistributable-libraries-by-version) is missing on your machine **but the program will work as expected** with Tensorflow as fallback. We highly recommend using [OpenVINO](https://docs.openvinotoolkit.org/) to speedup the inference time. See benchmark numbers with/without [OpenVINO](https://docs.openvinotoolkit.org/) at https://www.doubango.org/SDKs/anpr/docs/Benchmark.html#core-i7-windows.

<a name="dependencies-debugging"></a>
## Debugging missing dependencies ##
To check if all dependencies are present:
- **Windows x86_64:** Use [Dependency Walker](https://www.dependencywalker.com/) on [binaries/windows/x86_64/ultimateALPR-SDK.dll](../../../binaries/windows/x86_64/ultimateALPR-SDK.dll) and [binaries/windows/x86_64/ultimatePluginOpenVINO.dll](../../../binaries/windows/x86_64/ultimatePluginOpenVINO.dll) if you're planning to use [OpenVINO](https://docs.openvinotoolkit.org/).
- **Linux x86_64:** Use `ldd <your-shared-lib>` on [binaries/linux/x86_64/libultimate_alpr-sdk.so](../../../binaries/linux/x86_64/libultimate_alpr-sdk.so) and [binaries/linux/x86_64/libultimatePluginOpenVINO.so](../../../binaries/linux/x86_64/libultimatePluginOpenVINO.so) if you're planning to use [OpenVINO](https://docs.openvinotoolkit.org/).


<a name="gpu-acceleration"></a>
# GPGPU acceleration #
- On x86-64, GPGPU acceleration is disabled by default. Check [here](../README.md#gpu-acceleration) for more information on how to enable it.
- On NVIDIA Jetson (AArch64), GPGPU acceleration is always enabled. Check [here](../../../Jetson.md) for more information.


<a name="peformance-numbers"></a>
# Peformance numbers #

These performance numbers are obtained using **version 3.0**. You can use any later version. **Please notice the boost when OpenVINO is enabled.**

Some performance numbers on mid-range GPU (**GTX 1070**), high-range ARM CPU (**Galaxy S10+**), low-range ARM CPU (**Raspberry Pi 4**) devices using **720p (1280x720)** images:

|  | 0.0 rate | 0.2 rate | 0.5 rate | 0.7 rate | 1.0 rate |
|-------- | --- | --- | --- | --- | --- |
| **Intel® Xeon® E3 1230v5 + GTX 1070<br/> (Ubuntu 18, OpenVINO disabled)** | 711 millis <br />**140.51 fps** | 828 millis <br/> 120.76 fps | 1004 millis <br/> 99.53 fps | 1127 millis <br/> 88.70 fps | 1292 millis <br/> 77.38 fps |
| **Intel® Xeon® E3 1230v5 + GTX 1070<br/> (Ubuntu 18, OpenVINO enabled)** | 737 millis <br />**135.62 fps** | 809 millis <br/> 123.55 fps | 903 millis <br/> 110.72 fps | 968 millis <br/> 103.22 fps | 1063 millis <br/> 94.07 fps |
| **i7-4790K<br/> (Windows 8, OpenVINO enabled)** | 758 millis <br />**131.78 fps** | 1110 millis <br/> 90.07 fps | 1597 millis <br/> 62.58 fps | 1907 millis <br/> 52.42 fps | 2399 millis <br/> 41.66 fps |
| **i7-4790K<br/> (Windows 8, OpenVINO disabled)** | 2427 millis <br />**41.18 fps** | 2658 millis <br/> 37.60 fps | 2999 millis <br/> 33.34 fps | 3360 millis <br/> 29.75 fps | 3607 millis <br/> 27.72 fps |
| **i7-4770HQ<br/> (Winows 10, OpenVINO enabled)** | 1094 millis <br />**91.35 fps** | 1674 millis <br/> 59.71 fps | 2456 millis <br/> 40.71 fps | 2923 millis <br/> 34.21 fps | 4255 millis <br/> 23.49 fps |
| **i7-4770HQ<br/> (Windows 10, OpenVINO disabled)** | 4129 millis <br />**24.21 fps** | 4486 millis <br/> 22.28 fps | 4916 millis <br/> 20.34 fps | 5460 millis <br/> 18.31 fps | 5740 millis <br/> 17.42 fps |
| **Galaxy S10+<br/> (Android)** | 21344 millis <br/> **46.85 fps** | 25815 millis <br/> 38.73 fps | 29712 millis <br/> 33.65 fps | 33352 millis <br/> 29.98 fps | 37825 millis <br/> 26.43 fps |
| **RockPi 4B <br/> (Ubuntu Server 18.04)** | 7588 millis <br />**13.17 fps** | 8008 millis <br/> 12.48 fps | 8606 millis <br/> 11.61 fps | 9213 millis <br/> 10.85fps | 9798 millis <br/> 10.20 fps |
| **Raspberry Pi 4<br/> (Raspbian Buster)** | 81890 millis <br />**12.21 fps** | 89770 millis <br/> 11.13 fps | 115190 millis <br/> 8.68 fps | 122950 millis <br/> 8.13fps | 141460 millis <br/> 7.06 fps |
| **[binaries/jetson_tftrt](binaries/jetson_tftrt)<br/> (Xavier NX, JetPack 4.4.1)** | 657 millis <br />**152.06 fps** | 967 millis <br/> 103.39 fps | 1280 millis <br/> 78.06 fps | 1539 millis <br/> 64.95 fps | 1849 millis <br/> 54.07 fps |
| **[binaries/jetson](binaries/jetson)<br/> (Xavier NX, JetPack 4.4.1)** | 657 millis <br />**152.02 fps** | 1169 millis <br/> 85.47 fps | 2112 millis <br/> 47.34 fps | 2703 millis <br/> 36.98 fps | 3628 millis <br/> 27.56 fps |
| **[binaries/jetson_tftrt](../../../binaries/jetson_tftrt)<br/> (TX2, JetPack 4.4.1)** | 1420 millis <br />**70.38 fps** | 1653 millis <br/> 60.47 fps | 1998 millis <br/> 50.02 fps | 2273 millis <br/> 43.97 fps | 2681 millis <br/> 37.29 fps |
| **[binaries/jetson](../../../binaries/jetson)<br/> (TX2, JetPack 4.4.1)** | 1428 millis <br />**70.01 fps** | 1712 millis <br/> 58.40 fps | 2165 millis <br/> 46.17 fps | 2692 millis <br/> 37.13 fps | 3673 millis <br/> 27.22 fps |
| **[binaries/jetson_tftrt](../../../binaries/jetson_tftrt)<br/> (Nano, JetPack 4.4.1)** | 3106 millis <br />**32.19 fps** | 3292 millis <br/> 30.37 fps | 3754 millis <br/> 26.63 fps | 3967 millis <br/> 25.20 fps | 4621 millis <br/> 21.63 fps |
| **[binaries/jetson](../../../binaries/jetson)<br/> (nano, JetPack 4.4.1)** | 2920 millis <br />**34.24 fps** | 3083 millis <br/> 32.42 fps | 3340 millis <br/> 29.93 fps | 3882 millis <br/> 25.75 fps | 5102 millis <br/> 19.59 fps |

Some notes:
- **The above numbers show that the best case is 'Intel® Xeon® E3 1230v5 + GTX 1070 + OpenVINO enabled'. In such case the GPU (TensorRT, CUDA) and the CPU(OpenVINO) are used in parallel. The CPU is used for detection and the GPU for recognition/OCR.**
- **Please note that even if Raspberry Pi 4 has a 64-bit CPU [Raspbian OS](https://en.wikipedia.org/wiki/Raspbian>) uses a 32-bit kernel which means we're loosing many SIMD optimizations.**
- **On RockPi 4B the code is 5 times faster when [parallel processing](https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html) is enabled.**
- **On NVIDIA Jetson the code is 3 times faster when [parallel processing](https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html) is enabled.**
- **On Android devices we have noticed that [parallel processing](https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html) can speedup the pipeline by up to 120% on some devices while on Raspberry Pi the gain is marginal.**
- **Both i7 CPUs are 6yr+ old (2014) to make sure everyone can easily find them at the cheapest price possible.**

<a name="prebuilt"></a>
# Pre-built binaries #

If you don't want to build this sample by yourself then, use the pre-built versions:
 - Windows x86_64: [benchmark.exe](../../../binaries/windows/x86_64/benchmark.exe) under [binaries/windows/x86_64](../../../binaries/windows/x86_64)
 - Linux x86_64: [benchmark](../../../binaries/linux/x86_64/benchmark) under [binaries/linux/x86_64](../../../binaries/linux/x86_64). Built on Ubuntu 18. **You'll need to download libtensorflow.so as explained [here](../README.md#gpu-acceleration-tensorflow-linux)**.
 - Linux aarch64: [benchmark](../../../binaries/linux/aarch64/benchmark) under [binaries/linux/aarch64](../../../binaries/linux/aarch64).
 - Raspberry Pi: [benchmark](../../../binaries/raspbian/armv7l/benchmark) under [binaries/raspbian/armv7l](../../../binaries/raspbian/armv7l)
 - Android: check [android](../../android) folder
 - NVIDIA Jetson: [binaries/jetson/aarch64/benchmark](../../../binaries/jetson/aarch64/benchmark) or [binaries/jetson_tftrt/aarch64/benchmark](../../../binaries/jetson_tftrt/aarch64/benchmark). **You'll need to generate the optimized models as explained [here](../../../Jetson.md#getting-started_before-trying-to-use-the-sdk-on-jetson)**.
 
On **Windows**, the easiest way to try this sample is to navigate to [binaries/windows/x86_64](../../../binaries/windows/x86_64/) and run [binaries/windows/x86_64/benchmark.bat](../../../binaries/windows/x86_64/benchmark.bat). You can edit these files to use your own images and configuration options.

<a name="building"></a>
# Building #

This sample contains [a single C++ source file](benchmark.cxx) and is easy to build. The documentation about the C++ API is at [https://www.doubango.org/SDKs/anpr/docs/cpp-api.html](https://www.doubango.org/SDKs/anpr/docs/cpp-api.html).

<a name="building-windows"></a>
## Windows ##
You'll need Visual Studio to build the code. The VS project is at [benchmark.vcxproj](benchmark.vcxproj). Open it.
 1. You will need to change the **"Command Arguments"** like the [below image](../../../VC++_config.jpg). Default value: `--loops 100 --rate 0.2 --positive $(ProjectDir)..\..\..\assets\images\lic_us_1280x720.jpg --negative $(ProjectDir)..\..\..\assets\images\london_traffic.jpg --assets $(ProjectDir)..\..\..\assets --charset latin`
 2. You will need to change the **"Environment"** variable like the [below image](../../../VC++_config.jpg). Default value: `PATH=$(VCRedistPaths)%PATH%;$(ProjectDir)..\..\..\binaries\windows\x86_64`
 
![VC++ config](../../../VCpp_config.jpg)
 
You're now ready to build and run the sample.

<a name="building-generic-gcc"></a>
## Generic GCC ##
Next command is a generic GCC command:
```
cd ultimateALPR-SDK/samples/c++/benchmark

g++ benchmark.cxx -O3 -I../../../c++ -L../../../binaries/<yourOS>/<yourArch> -lultimate_alpr-sdk -o benchmark
```
- You've to change `yourOS` and  `yourArch` with the correct values. For example, on **Linux x86_64** they would be equal to `linux` and `x86_64` respectively. On **Linux aarch64** they would be `linux` and `aarch64` respectively.
- If you're cross compiling then, you'll have to change `g++` with the correct triplet. For example, on Linux host for Android ARM64 target the triplet would be equal to `aarch64-linux-android-g++`.

<a name="building-rpi"></a>
## Raspberry Pi (Raspbian OS) ##

To build the sample for Raspberry Pi you can either do it on the device itself or cross compile it on [Windows](../#cross-compilation-rpi-install-windows), [Linux](../#cross-compilation-rpi-install-ubuntu) or OSX machines. 
For more information on how to install the toolchain for cross compilation please check [here](../README.md#cross-compilation-rpi).

```
cd ultimateALPR-SDK/samples/c++/benchmark

arm-linux-gnueabihf-g++ benchmark.cxx -O3 -I../../../c++ -L../../../binaries/raspbian/armv7l -lultimate_alpr-sdk -o benchmark
```
- On Windows: replace `arm-linux-gnueabihf-g++` with `arm-linux-gnueabihf-g++.exe`
- If you're building on the device itself: replace `arm-linux-gnueabihf-g++` with `g++` to use the default GCC

<a name="testing"></a>
# Testing #
After [building](#building) the application you can test it on your local machine.

<a name="testing-usage"></a>
## Usage ##

Benchmark is a command line application with the following usage:
```
benchmark \
      --positive <path-to-image-with-a-plate> \
      --negative <path-to-image-without-a-plate> \
      [--assets <path-to-assets-folder>] \
      [--charset <recognition-charset:latin/korean/chinese>] \
      [--openvino_enabled <whether-to-enable-OpenVINO:true/false>] \
      [--openvino_device <openvino-device-to-use>] \
      [--klass_lpci_enabled <whether-to-enable-LPCI:true/false>] \
      [--klass_vcr_enabled <whether-to-enable-VCR:true/false>] \
      [--klass_vmmr_enabled <whether-to-enable-VMMR:true/false>] \
      [--loops <number-of-times-to-run-the-loop:[1, inf]>] \
      [--rate <positive-rate:[0.0, 1.0]>] \
      [--parallel <whether-to-enable-parallel-mode:true/false>] \
      [--rectify <whether-to-enable-rectification-layer:true/false>] \
      [--tokenfile <path-to-license-token-file>] \
      [--tokendata <base64-license-token-data>]
```
Options surrounded with **[]** are optional.
- `--positive` Path to an image (JPEG/PNG/BMP) with a license plate. This image will be used to evaluate the recognizer. You can use default image at [../../../assets/images/lic_us_1280x720.jpg](../../../assets/images/lic_us_1280x720.jpg).
- `--negative` Path to an image (JPEG/PNG/BMP) without a license plate. This image will be used to evaluate the decoder. You can use default image at [../../../assets/images/london_traffic.jpg](../../../assets/images/london_traffic.jpg).
- `--assets` Path to the [assets](../../../assets) folder containing the configuration files and models. Default value is the current folder.
- `--charset` Defines the recognition charset (a.k.a alphabet) value (latin, korean, chinese...). Default: *latin*.
- `--openvino_enabled` Whether to enable OpenVINO. Tensorflow will be used when OpenVINO is disabled. Default: *true*.
- `--openvino_device` Defines the OpenVINO device to use (CPU, GPU, FPGA...). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#openvino-device. Default: *CPU*."
- `--klass_lpci_enabled` Whether to enable License Plate Country Identification (LPCI). More info at https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-country-identification-lpci. Default: *false*.
- `--klass_vcr_enabled` Whether to enable Vehicle Color Recognition (VCR). More info at https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-color-recognition-vcr. Default: *false*.
- `--klass_vmmr_enabled` Whether to enable Vehicle Make Model Recognition (VMMR). More info at https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-make-model-recognition-vmmr. Default: *false*.
- `--loops` Number of times to run the processing pipeline.
- `--rate` Percentage value within [0.0, 1.0] defining the positive rate. The positive rate defines the percentage of images with a plate.
- `--parallel` Whether to enabled the parallel mode. More info about the parallel mode at [https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html](https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html). Default: *true*.
- `--rectify` Whether to enable the rectification layer. More info about the rectification layer at [https://www.doubango.org/SDKs/anpr/docs/Rectification_layer.html](https://www.doubango.org/SDKs/anpr/docs/Rectification_layer.html). Always enabled on x86_64 CPUs. Default: *false*.
- `--tokenfile` Path to the file containing the base64 license token if you have one. If not provided then, the application will act like a trial version. Default: *null*.
- `--tokendata` Base64 license token if you have one. If not provided then, the application will act like a trial version. Default: *null*.

The information about the maximum frame rate (**105fps** on GTX 1070, **47fps** on Snapdragon 855 and **12fps** on Raspberry Pi 4) is obtained using `--rate 0.0` which means evaluating the negative (no license plate) image only. The minimum frame rate could be obtained using `--rate 1.0` which means evaluating the positive image only (all images on the video stream have a license plate). In real life, very few frames from a video stream will contain a license plate (`--rate` **< 0.01**).

<a name="testing-examples"></a>
## Examples ##

- For example, on **Raspberry Pi** you may call the benchmark application using the following command:
```
LD_LIBRARY_PATH=../../../binaries/raspbian/armv7l:$LD_LIBRARY_PATH ./benchmark \
    --positive ../../../assets/images/lic_us_1280x720.jpg \
    --negative ../../../assets/images/london_traffic.jpg \
    --assets ../../../assets \
    --charset latin \
    --loops 100 \
    --rate 0.2 \
    --parallel true \
    --rectify false
```

- On **NVIDIA Jetson**, you'll need to generate the models as explained [here](../../../Jetson.md#building-optimized-models), put the device on maximum performance mode (`sudo nvpmodel -m 0 && sudo jetson_clocks`), then run:
```   
LD_LIBRARY_PATH=../../../binaries/jetson/aarch64:$LD_LIBRARY_PATH ./benchmark \
    --positive ../../../assets/images/lic_us_1280x720.jpg \
    --negative ../../../assets/images/london_traffic.jpg \
    --assets ../../../assets \
    --charset latin \
    --loops 100 \
    --rate 0.2 \
    --parallel true \
    --rectify false
```
or 
```    
LD_LIBRARY_PATH=../../../binaries/jetson_tftrt/aarch64:$LD_LIBRARY_PATH ./benchmark \
    --positive ../../../assets/images/lic_us_1280x720.jpg \
    --negative ../../../assets/images/london_traffic.jpg \
    --assets ../../../assets \
    --charset latin \
    --loops 100 \
    --rate 0.2 \
    --parallel true \
    --rectify false
```
The difference between [jetson_tftrt](../../../binaries/jetson_tftrt) and [jetson](../../../binaries/jetson) binaries is explained [here](../../../Jetson.md#getting-started_jetson-versus-jetsontftrt).

- On **Linux x86_64**, you may use the next command:
```
LD_LIBRARY_PATH=../../../binaries/linux/x86_64:$LD_LIBRARY_PATH ./benchmark \
    --positive ../../../assets/images/lic_us_1280x720.jpg \
    --negative ../../../assets/images/london_traffic.jpg \
    --assets ../../../assets \
    --charset latin \
    --loops 100 \
    --rate 0.2 \
    --parallel true
```

- On **Linux aarch64**, you may use the next command:
```
LD_LIBRARY_PATH=../../../binaries/linux/aarch64:$LD_LIBRARY_PATH ./benchmark \
    --positive ../../../assets/images/lic_us_1280x720.jpg \
    --negative ../../../assets/images/london_traffic.jpg \
    --assets ../../../assets \
    --charset latin \
    --loops 100 \
    --rate 0.2 \
    --parallel true
```

- On **Windows x86_64**, you may use the next command:
```
benchmark.exe ^
    --positive ../../../assets/images/lic_us_1280x720.jpg ^
    --negative ../../../assets/images/london_traffic.jpg ^
    --assets ../../../assets ^
    --charset latin ^
    --loops 100 ^
    --rate 0.2 ^
    --parallel true
```

Please note that if you're cross compiling the application then you've to make sure to copy the application and both the [assets](../../../assets) and [binaries](../../../binaries) folders to the target device.


