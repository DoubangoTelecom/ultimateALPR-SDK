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
The information about the maximum frame rate (**105fps** on GTX 1070, **47fps** on Snapdragon 855 and **12fps** on Raspberry Pi 4) could be checked using this application. 
It's open source and doesn't require registration or license key.

More information about the benchmark rules at [https://www.doubango.org/SDKs/anpr/docs/Benchmark.html](https://www.doubango.org/SDKs/anpr/docs/Benchmark.html).

<a name="gpu-acceleration"></a>
# GPGPU acceleration #
By default GPGPU acceleration is disabled. Check [here](../README.md#gpu-acceleration) for more information on how to enable.

<a name="peformance-numbers"></a>
# Peformance numbers #

Some performance numbers on mid-range GPU (**GTX 1070**), high-range ARM CPU (**Galaxy S10+**), low-range ARM CPU (**Raspberry Pi 4**) devices using **720p (1280x720)** images:

|  | 0.0 rate | 0.2 rate | 0.5 rate | 0.7 rate | 1.0 rate |
|-------- | --- | --- | --- | --- | --- |
| **GTX 1070 (Ubuntu 18)** | 9516 millis <br />**105.07 fps** | 9963 millis <br/> 100.36 fps | 10701 millis <br/> 93.44 fps | 11109 millis <br/> 90.01 fps | 11704 millis <br/> 85.43 fps |
| **i7-4790K (Windows 7)** | 4251 millis <br />**23.52 fps** | 4598 millis <br/> 21.74 fps | 4851 millis <br/> 20.61 fps | 5117 millis <br/> 19.54 fps | 5553 millis <br/> 18.00 fps |
| **i7-4770HQ (Windows 10)** | 6040 millis <br />**16.55 fps** | 6342 millis <br/> 15.76 fps | 7065 millis <br/> 14.15 fps | 7279 millis <br/> 13.73 fps | 7965 millis <br/> 12.55 fps |
| **Galaxy S10+ (Android)** | 21344 millis <br/> **46.85 fps** | 25815 millis <br/> 38.73 fps | 29712 millis <br/> 33.65 fps | 33352 millis <br/> 29.98 fps | 37825 millis <br/> 26.43 fps |
| **Raspberry Pi 4 (Raspbian Buster)** | 81890 millis <br />**12.21 fps** | 89770 millis <br/> 11.13 fps | 115190 millis <br/> 8.68 fps | 122950 millis <br/> 8.13fps | 141460 millis <br/> 7.06 fps |

Some notes:
- **Please note that even if Raspberry Pi 4 has a 64-bit CPU [Raspbian OS](https://en.wikipedia.org/wiki/Raspbian>) uses a 32-bit kernel which means we're loosing many SIMD optimizations.**
- **On Android devices we have noticed that [parallel processing](https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html) can speedup the pipeline by up to 120% on some devices while on Raspberry pi the gain is marginal.**
- **Both i7 CPUs are 6yr+ old (2014) to make sure everyone can easily find them at the cheapest price possible.**

<a name="prebuilt"></a>
# Pre-built binaries #

If you don't want to build this sample by yourself then, use the pre-built versions:
 - Windows: [benchmark.exe](../../../binaries/windows/x86_64/benchmark.exe) under [binaries/windows/x86_64](../../../binaries/windows/x86_64)
 - Linux: [benchmark](../../../binaries/linux/x86_64/benchmark) under [binaries/linux/x86_64](../../../binaries/linux/x86_64). Built on Ubuntu 18. **You'll need to download libtensorflow.so as explained [here](../README.md#gpu-acceleration-tensorflow-linux)**.
 - Raspberry Pi: [benchmark](../../../binaries/raspbian/armv7l/benchmark) under [binaries/raspbian/armv7l](../../../binaries/raspbian/armv7l)
 - Android: check [android](../../android) folder
 
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
- You've to change `yourOS` and  `yourArch` with the correct values. For example, on **Linux x86_64** they would be equal to `linux` and `x86_64` respectively.
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
- `--loops` Number of times to run the processing pipeline.
- `--rate` Percentage value within [0.0, 1.0] defining the positive rate. The positive rate defines the percentage of images with a plate.
- `--parallel` Whether to enabled the parallel mode. More info about the parallel mode at [https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html](https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html). Default: *true*.
- `--rectify` Whether to enable the rectification layer. More info about the rectification layer at [https://www.doubango.org/SDKs/anpr/docs/Rectification_layer.html](https://www.doubango.org/SDKs/anpr/docs/Rectification_layer.html). Always enabled on x86_64 CPUs. Default: *false*.
- `--tokenfile` Path to the file containing the base64 license token if you have one. If not provided then, the application will act like a trial version. Default: *null*.
- `--tokendata` Base64 license token if you have one. If not provided then, the application will act like a trial version. Default: *null*.

The information about the maximum frame rate (**105fps** on GTX 1070, **47fps** on Snapdragon 855 and **12fps** on Raspberry Pi 4) is obtained using `--rate 0.0` which means evaluating the negative (no license plate) image only. The minimum frame rate could be obtained using `--rate 1.0` which means evaluating the positive image only (all images on the video stream have a license plate). In real life, very few frames from a video stream will contain a license plate (`--rate` **< 0.01**).

<a name="testing-examples"></a>
## Examples ##

For example, on **Raspberry Pi** you may call the benchmark application using the following command:
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
On **Linux x86_64**, you may use the next command:
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
On **Windows x86_64**, you may use the next command:
```
benchmark.exe ^
    --positive ../../../assets/images/lic_us_1280x720.jpg ^
    --negative ../../../assets/images/traffic_1280x720.jpg ^
    --assets ../../../assets ^
    --format e13b+cmc7 ^
    --loops 100 ^
    --rate 0.2 ^
    --parallel true
```

Please note that if you're cross compiling the application then you've to make sure to copy the application and both the [assets](../../../assets) and [binaries](../../../binaries) folders to the target device.


