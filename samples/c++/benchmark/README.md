- [Dependencies](#dependencies)
  - [Debugging missing dependencies](#dependencies-debugging)
- [GPGPU acceleration](#gpu-acceleration)
- [Pre-built binaries](#prebuilt)
- [Building](#building)
- [Usage](#testing-usage)
- [Peformance numbers](#peformance-numbers)
  - [Desktop (Linux/Windows X86_64)](#peformance-numbers-desktop)
  - [Mobile (Linux/Android AArch64)](#peformance-numbers-mobile)
    - [NVIDIA Jetson](#peformance-numbers-mobile-jetson)
    - [Raspberry Pi](#peformance-numbers-mobile-rpi)
    - [OrangePi 5+ (rk3588 NPU)](#peformance-numbers-mobile-orangepi)
    - [Radxa rock 5b (rk3588 NPU)](#peformance-numbers-mobile-rock5b)
    - [Radxa Dragon Q6A (Qualcomm Hexagon NPU)](#peformance-numbers-mobile-dragon)
    - [Samsung Galaxy S24](#peformance-numbers-mobile-s24)
    - [Debix A (NXP i.MX 8M Plus NPU)](#peformance-numbers-mobile-debixa)
<hr />

This application is used to check everything is ok and running as fast as expected on Windows and Linux. Check [here](../../android/benchmark) for Android. 
It's open source and doesn't require registration or license key.

<a name="dependencies"></a>
# Dependencies #
**The SDK is developed in C++11** and you'll need **glibc 2.27+** on *Linux* and **[Microsoft Visual C++ 2026 Redistributable(x64)](https://aka.ms/vc14/vc_redist.x64.exe)** (any later version is ok) on *Windows*.  **You most likely already have these dependencies on you machine** as almost every program require it.

If you're planning to use [OpenVINO](https://docs.openvinotoolkit.org/), then you'll need [Intel C++ Compiler Redistributable](https://software.intel.com/en-us/articles/intel-compilers-redistributable-libraries-by-version) (choose newest). Please note that OpenVINO is packaged in the SDK as plugin and loaded (`dlopen`) at runtime. The engine will fail to load the plugin if [Intel C++ Compiler Redistributable](https://software.intel.com/en-us/articles/intel-compilers-redistributable-libraries-by-version) is missing on your machine **but the program will work as expected** with ONNX as fallback. We highly recommend using [OpenVINO](https://docs.openvinotoolkit.org/) to speedup the inference time. See benchmark numbers with/without [OpenVINO](https://docs.openvinotoolkit.org/).

<a name="dependencies-debugging"></a>
## Debugging missing dependencies ##
To check if all dependencies are present:
- **Windows x86_64:** Use [Dependency Walker](https://www.dependencywalker.com/) on [binaries/windows/x86_64/ultimateALPR-SDK.dll](../../../binaries/windows/x86_64/ultimateALPR-SDK.dll) and [binaries/windows/x86_64/plugin_dl_vino.dll](../../../binaries/windows/x86_64/plugin_dl_vino.dll) if you're planning to use [OpenVINO](https://docs.openvinotoolkit.org/).
- **Linux x86_64:** Use `ldd <your-shared-lib>` on [binaries/linux/x86_64/libultimate_alpr-sdk.so](../../../binaries/linux/x86_64/libultimate_alpr-sdk.so) and [binaries/linux/x86_64/libplugin_dl_vino.so](../../../binaries/linux/x86_64/libplugin_dl_vino.so) if you're planning to use [OpenVINO](https://docs.openvinotoolkit.org/).

<a name="gpu-acceleration"></a>
# GPGPU acceleration #
Please read [this](../../../GPGPU.md) if you're planning to use your GPU (CUDA or TensorRT). We recommend using TensorRT which is faster than CUDA.

<a name="prebuilt"></a>
# Pre-built binaries #

If you don't want to build this sample by yourself, then use the pre-built versions:
 - Windows x86_64: [benchmark.exe](../../../binaries/windows/x86_64/benchmark.exe) under [binaries/windows/x86_64](../../../binaries/windows/x86_64).
 - Linux x86_64: [benchmark](../../../binaries/linux/x86_64/benchmark) under [binaries/linux/x86_64](../../../binaries/linux/x86_64).
 - Linux aarch64 (Raspberry Pi, Jetson, Orange Pi...): [benchmark](../../../binaries/linux/aarch64/benchmark) under [binaries/linux/aarch64](../../../binaries/linux/aarch64).
 - Android: check [android](../../android) folder

<a name="building"></a>
# Building #

You'll need [CMake](https://cmake.org/) to build this sample.

- Create build folder and move into it: `mkdir build && cd build`

To generate the build files:
- Windows (Visual Studio files): `cmake .. -DCMAKE_GENERATOR_PLATFORM=x64`
- Linux (Makefile): `cmake .. -G"Unix Makefiles"`

To build the project:
- Windows: Open the VS solution and build the projet
- Linux: Run `make` to build the project

<a name="testing-usage"></a>
# Usage #

```
		benchmark \
			--image <Comma-separated paths to images (JPEG/PNG/BMP) to benchmark> \
			--assets <path-to-assets-folder> \
			[--charset <recognition-charset:latin/korean/chinese>] \
			[--num_threads <number-of-threads:[1, inf]>] \
			[--max_latency <max-latency:]-inf,+inf[>] \
			[--nvision_enabled <whether-to-enable-IENV:true/false>] \
			[--platform <target-platform:auto/mobile/desktop>] \
			[--batching_mode <batching-mode:auto/on/off>] \
			[--backend <backend:auto,onnx,tflite,rknn2,trt,vino...>] \
			[--pysearch_enabled <whether-to-pyramidal-search:true/false>]
			[--recogn_enabled <whether-to-enabled-LPR:true/false>]
			[--klass_lpci_enabled <whether-to-enable-LPCI:true/false>] \
			[--klass_vcr_enabled <whether-to-enable-VCR:true/false>] \
			[--klass_vmmr_enabled <whether-to-enable-VMMR:true/false>] \
			[--klass_vbsr_enabled <whether-to-enable-VBSR:true/false>] \
			[--loops <number-of-times-to-run-the-inference-loop:[1, inf]>] \
            [--loopsw <number-of-times-to-run-the-warmup-loop:[1, inf]>] \
			[--parallel <whether-to-enable-parallel-mode:true/false>] \
			[--tokenfile <path-to-license-token-file>] \
			[--tokendata <base64-license-token-data>]
```
Options surrounded with **[]** are optional.
- `--image` Path to the image(JPEG/PNG/BMP) to benchmark. You can use default image at [../../../assets/images/lic_us_1280x720.jpg](../../../assets/images/lic_us_1280x720.jpg).
- `--assets` Path to the [assets](../../../assets) folder containing the configuration files and models. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#assets-folder.
- `--charset` Defines the recognition charset (a.k.a alphabet) value (latin, korean, chinese...). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#recogn-charset. Default: *latin*.
- `--num_threads` Number of threads to use. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#num-threads. Default: *-1*.
- `--max_latency` Max latency when parallel processing is enabled. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#max-latency. Default: *-1*.
- `--nvision_enabled` Whether to enable Image Enhancement for Night-Vision (IENV). More info about IENV at https://www.doubango.org/SDKs/anpr/docs/Features.html#night-vision-enabled. Default: *false*.
- `--platform` Target platform. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#platform. Default: *auto*.
- `--backend` Target backend. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#backend. Default: *auto*.
- `--pysearch_enabled` Whether to enable Pyramidal search. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#pyramidal-search-enabled. Default: *false*.
- `--recogn_enabled` Whether to enable License Plate Recognition (LPR) / OCR. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#recogn-enabled. Default: *false*.
- `--klass_lpci_enabled` Whether to enable License Plate Country Identification (LPCI). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-lpci-enabled. Default: *false*.
- `--klass_vcr_enabled` Whether to enable Vehicle Color Recognition (VCR). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vcr-enabled. Default: *false*.
- `--klass_vmmr_enabled` Whether to enable Vehicle Make Model Recognition (VMMR). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vmmr-enabled. Default: *false*.
- `--klass_vbsr_enabled` Whether to enable Vehicle Body Style Recognition (VBSR). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vbsr-enabled. Default: *false*.
- `--loops` Number of times to run the inference loop.
- `--loopsw` Number of times to run the warmup loop.
- `--parallel` Whether to enabled the parallel mode. More info about the parallel mode at https://www.doubango.org/SDKs/anpr/docs/Parallel_processing.html. Default: *true*.
- `--tokenfile` Path to the file containing the base64 license token if you have one. If not provided then, the application will act like a trial version. Default: *null*.
- `--tokendata` Base64 license token if you have one. If not provided then, the application will act like a trial version. Default: *null*.

<a name="peformance-numbers"></a>
# Peformance numbers #

You should run the benchmark app at least `#100 times on low-end devices` (e.g. Raspberry Pi) and `#5000 times on on high-end devices` (x86_64, GPUs, NPUs...).

**Next numbers are FPS (frames per second).** `1000/fps` gives you the number of milliseconds it takes to process a single frame.

The columns are the [backend](https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#backend) values.

All benchmark experiments are done with [parallel processing](https://www.doubango.org/SDKs/anpr/docs/Parallel_processing.html) enabled and both detection and recognition (OCR) are evaluated for each frame.

<a name="peformance-numbers-desktop"></a>
## Desktop (Linux/Windows X86_64) ##

- On **Linux x86_64**, you may use the next command:
```
LD_LIBRARY_PATH=../../../binaries/linux/x86_64:$LD_LIBRARY_PATH ./benchmark \
    --image "../../../assets/images/lic_us_1280x720.jpg" \
    --assets ../../../assets \
    --backend "auto" \
    --loops 500 \
    --parallel true
```

- On **Windows x86_64**, you can also use [binaries/windows/x86_64/benchmark.bat](../../../binaries/windows/x86_64/benchmark.bat) to make your life easier, or run:
```
benchmark.exe ^
    --image "../../../assets/images/lic_us_1280x720.jpg" ^
    --assets ../../../assets ^
    --backend "auto" ^
    --loops 500 \
    --parallel true
```

| | trt           | vino[cpu]    | vino[gpu] | onnx[cuda]    |  onnx[none]    | tflite[xnnpack]| tflite[none] |
| --- | --- | --- | --- | --- | --- | --- |--- |
| AMD Ryzen 7 9800X3D 8-Core<br/>(NVIDIA RTX 5060 GPU)<br/>(Windows 11) | 875.17 | 405.65 | -- | 572.17 | 126.32 | 233.66 | 8.92 |
| AMD Ryzen 9 7950X 16-Core<br/>(NVIDIA RTX 4060 GPU)<br/>(Ubuntu 22) | 595.32 | 269.15 | -- | 338.41 | 84.78 | 76.35 | 16.69 |
| Mobile Intel i7-12650H<br/>(Intel GPU)<br/>(Windows 11) | -- | 120.48 | 44.95 | -- | 50.65 | 81.35 | 3.61 |

Important to note that you must [generate the optimized models](../../../GPGPU.md#ggpu-acceleration-tensorrt) if you're planning to use TensorRT (`trt` backend).

<a name="peformance-numbers-mobile"></a>
## Mobile (Linux aarch64) ##

- On **Linux aarch64**, you may use the next command:
```
LD_LIBRARY_PATH=../../../binaries/linux/aarch64:$LD_LIBRARY_PATH ./benchmark \
    --image "../../../assets/images/lic_us_1280x720.jpg" \
    --assets ../../../assets \
    --backend "auto" \
    --loops 500 \
    --parallel true
```

<a name="peformance-numbers-mobile-jetson"></a>
### NVIDIA Jetson ###

Important to note that you must [generate the optimized models](../../../GPGPU.md#gpu-acceleration-tensorrt-install-jetson) if you're planning to use TensorRT TensorRT (`trt` backend).

We recommend putting the device in maximum performance mode (`sudo nvpmodel -m 2` on Jetson Nano Orin or `sudo nvpmodel -m 0` on Jetson Nano B01) before running the script.

To check if your NVIDIA Jetson is running in maximum performance mode, run `sudo nvpmodel -q` to view the active power mode and `sudo jetson_clocks --show` to verify if the clocks are locked at maximum frequency.

| | trt           |tflite[xnnpack]| tflite[none]  | onnx          |
| --- | --- | --- | --- | --- |
| Jetson Nano Orin<br/>(Jetpack 7.2.0) | 472.89 | 49.78 | 20.46 | 22.09 |
| Jetson Nano B01<br/>(Jetpack 4.6.6) | 37.31 | -- | -- | -- |

<a name="peformance-numbers-mobile-rpi"></a>
### Raspberry Pi ###

| | hailo[hailo8l] |tflite[xnnpack]| tflite[none]  | onnx          |
| --- | --- | --- | --- | --- |
| Raspberry Pi 5 | 143.67 | 37.41 | 15.03 | 14.74 |
| Raspberry Pi 4B | -- | 10.65 | 5.89 | -- |
| Raspberry Pi 3B+ | -- | 5.61 | 3.64 | -- |

For Hailo, run `sudo lspci -vv` command and check **"LnkSta"** entry associated to the device to get the communication speed. The speed should be **"8GT/s"** and a lower value means you're using the **"PCI gen 2"** bus instead of **"PCI gen 3"**. To migrate to **"PCI gen 3"**:
 - 1. open the file `/boot/firmware/config.txt` for edit
 - 2. under the line `dtparam=pciex1` add the following line `dtparam=pciex1_gen=3`
 - 3. reboot the device

Please also note that Hailo uses different OCR models and may produce difference results. **OCR models for Hailo are less accurate** (do be improved in next versions).

<a name="peformance-numbers-mobile-orangepi"></a>
### OrangePi 5+ (rk3588 NPU) ###

| | rknpu2[rk3588]|tflite[xnnpack]|tflite[nnapi]  | tflite[gpu]   | tflite[none]  |  onnx          |
| --- | --- | --- | --- | --- | --- | --- |
| OrangePi 5+ (Ubuntu) | 310.21 | 55.08 | -- | -- | 20.19 | 20.56 |
| OrangePi 5+ (Android) | 275.22 | 35.19 | 24.77 | 8.65 | 24.53 | -- |

 - Android and Linux uses the exact same QINT8 models but the latter is faster because it is 100% C++ while the former uses a JNI (Java) wrapper.
 - The Android project contains only TensorflowLite models, you need to change **sdk/build.gradle** to include everything: from `assets.srcDirs += ['../../../assets/models.tflite']` to `assets.srcDirs += ['../../../assets/models']`.

<a name="peformance-numbers-mobile-rock5b"></a>
### Radxa rock 5b (rk3588 NPU) ###

| | rknpu2[rk3588]|tflite[xnnpack]|tflite[nnapi]  | tflite[gpu]   | tflite[none]  |  onnx          |
| --- | --- | --- | --- | --- | --- | --- |
| Radxa rock 5b (Ubuntu) | 311.49 | 64.12 | -- | -- | 23.64 | 22.74 |
| Radxa rock 5b (Android) | 277.89 | 38.12 | 26.09 | 9.78 | 26.54 | -- |

 - Android and Linux uses the exact same QINT8 models but the latter is faster because it is 100% C++ while the former uses a JNI (Java) wrapper.
 - The Android project contains only TensorflowLite models, you need to change **sdk/build.gradle** to include everything: from `assets.srcDirs += ['../../../assets/models.tflite']` to `assets.srcDirs += ['../../../assets/models']`.

<a name="peformance-numbers-mobile-dragon"></a>
### Radxa Dragon Q6A (Qualcomm Hexagon NPU) ###

| | tflite[ext=$QAIRT_HOME/lib/$ABI/libQnnTFLiteDelegate.so] |tflite[xnnpack]| tflite[none]  | onnx          |
| --- | --- | --- | --- | --- |
| Dragon Q6A (QCS6490) | 301.04 | 43.06 | 19.97 | 16.67 |

Check [here](../../../Qualcomm.md) on how to configure the SDK to run on Qualcomm Hexagon Tensor Processor (HTP).

<a name="peformance-numbers-mobile-s24"></a>
### Samsung Galaxy S24 ###

These numbers are generated using [Android benchmark app](../../android/benchmark).

| | tflite[ext=libQnnTFLiteDelegate.so]    |tflite[xnnpack]|tflite[nnapi]  | tflite[gpu]   | tflite[none]  |
| --- | --- | --- | --- | --- | --- |
| Samsung Galaxy S24 (Android) | -- | 91.24 | 51.64 | 6.84 | 54.47 |

The S24 model (`SM-S921B`) used is an European version with a `Exynos 2400` processor. Unlike the S24 models sold in America and China (`Snapdragon 8 Gen 3` processor), ours doesn't have an NPU.

<a name="peformance-numbers-mobile-debixa"></a>
### Debix A (NXP i.MX 8M Plus NPU) ###

| | tflite[ext=/usr/lib/libvx_delegate.so] |tflite[xnnpack]| tflite[none]  | onnx          |
| --- | --- | --- | --- | --- |
| [Debix A](https://debix.io/product/debix-model-a/) | 71.02 | 5.18 | 3.25 | -- |

Use the next code to cache the compiled graph and speedup the startup:

```
export VIV_VX_CACHE_BINARY_GRAPH_DIR=`pwd`
export VIV_VX_ENABLE_CACHE_GRAPH_BINARY="1"
```
