- [Dependencies](#dependencies)
  - [Debugging missing dependencies](#dependencies-debugging)
- [GPGPU acceleration](#gpu-acceleration)
- [Pre-built binaries](#prebuilt)
- [Building](#building)
- [Usage](#testing-usage)
- [Examples](#testing-examples)

This application is a reference implementation for developers to show how to use the [C++ API](https://www.doubango.org/SDKs/anpr/docs/cpp-api.html) and could
be used to easily test the accuracy of the SDK. 
If you don't want to build this sample and is looking for a quick way to check the accuracy, then try
our online webapp demo at https://www.doubango.org/webapps/alpr.

This sample is open source and doesn't require registration or license key.

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
 - Windows x86_64: [recognizer.exe](../../../binaries/windows/x86_64/recognizer.exe) under [binaries/windows/x86_64](../../../binaries/windows/x86_64).
 - Linux x86_64: [recognizer](../../../binaries/linux/x86_64/recognizer) under [binaries/linux/x86_64](../../../binaries/linux/x86_64).
 - Linux aarch64 (Raspberry Pi, Jetson, Orange Pi...): [recognizer](../../../binaries/linux/aarch64/recognizer) under [binaries/linux/aarch64](../../../binaries/linux/aarch64).
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
recognizer \
			--image <path-to-image-with-to-recognize> \
			--assets <path-to-assets-folder> \
			[--parallel <whether-to-enable-parallel-mode:true/false>] \
			[--platform <target platform:auto/mobile/desktop>] \
			[--charset <recognition-charset:latin/korean/chinese/brazilian/israeli>] \
			[--nvision_enabled <whether-to-enable-IENV:true/false>] \
			[--backend <backend:auto,onnx,tflite,rknn2,trt,vino...>] \
			[--klass_lpci_enabled <whether-to-enable-LPCI:true/false>] \
			[--klass_vcr_enabled <whether-to-enable-VCR:true/false>] \
			[--klass_vmmr_enabled <whether-to-enable-VMMR:true/false>] \
			[--klass_vbsr_enabled <whether-to-enable-VBSR:true/false>] \
			[--tokenfile <path-to-license-token-file>] \
			[--tokendata <base64-license-token-data>]
```
Options surrounded with **[]** are optional.
- `--image` Path to the image(JPEG/PNG/BMP) to process. You can use default image at [../../../assets/images/lic_us_1280x720.jpg](../../../assets/images/lic_us_1280x720.jpg).
- `--assets` Path to the assets folder containing the configuration files and models. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#assets-folder.
- `--charset` Defines the recognition charset (a.k.a alphabet) value (latin, korean, chinese...). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#recogn-charset. Default: *latin*.
- `--platform` Target platform. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#platform. Default: *auto*.
- `--nvision_enabled` Whether to enable Image Enhancement for Night-Vision (IENV). More info about IENV at https://www.doubango.org/SDKs/anpr/docs/Features.html#night-vision-enabled. Default: *false*.
- `--backend` Target backend. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#backend. Default: *auto*.
- `--klass_lpci_enabled` Whether to enable License Plate Country Identification (LPCI). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-lpci-enabled. Default: *false*.
- `--klass_vcr_enabled` Whether to enable Vehicle Color Recognition (VCR). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vcr-enabled. Default: *false*.
- `--klass_vmmr_enabled` Whether to enable Vehicle Make Model Recognition (VMMR). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vmmr-enabled. Default: *false*.
- `--klass_vbsr_enabled` Whether to enable Vehicle Body Style Recognition (VBSR). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vbsr-enabled. Default: *false*.
- `--parallel` Whether to enabled the parallel mode.More info about the parallel mode at https://www.doubango.org/SDKs/anpr/docs/Parallel_processing.html. Default: *true*.
- `--tokenfile` Path to the file containing the base64 license token if you have one. If not provided then, the application will act like a trial version. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#license-token-file. Default: *null*.
- `--tokendata` Base64 license token if you have one. If not provided then, the application will act like a trial version. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#license-token-data. Default: *null*.

<a name="testing-examples"></a>
# Examples #

- On **Linux x86_64**, you may use the next command:
```
LD_LIBRARY_PATH=../../../binaries/linux/x86_64:$LD_LIBRARY_PATH ./recognizer \
    --image "../../../assets/images/lic_us_1280x720.jpg" \
    --assets ../../../assets \
    --charset latin \
    --platform auto \
    --backend auto \
    --nvision_enabled false \
    --klass_lpci_enabled true \
    --klass_vcr_enabled true \
    --klass_vmmr_enabled true \
    --klass_vbsr_enabled true \
    --parallel false
```

- On **Linux aarch64**, you may use the next command:
```
LD_LIBRARY_PATH=../../../binaries/linux/aarch64:$LD_LIBRARY_PATH ./recognizer \
    --image "../../../assets/images/lic_us_1280x720.jpg" \
    --assets ../../../assets \
    --charset latin \
    --platform auto \
    --backend auto \
    --nvision_enabled false \
    --klass_lpci_enabled true \
    --klass_vcr_enabled true \
    --klass_vmmr_enabled true \
    --klass_vbsr_enabled true \
    --parallel false
```
on jetson, you must generate the optimized models first.

- On **Windows x86_64**, you may use the next command:
```
recognizer.exe ^
    --image ../../../assets/images/lic_us_1280x720.jpg ^
    --assets ../../../assets ^
    --charset latin ^
    --platform auto ^
    --backend auto ^
    --nvision_enabled false ^
    --klass_lpci_enabled true ^
    --klass_vcr_enabled true ^
    --klass_vmmr_enabled true ^
    --klass_vbsr_enabled true ^
    --parallel false
```
you can also use [binaries/windows/x86_64/recognizer-latin.bat](../../../binaries/windows/x86_64/recognizer-latin.bat) to make your life easier.

