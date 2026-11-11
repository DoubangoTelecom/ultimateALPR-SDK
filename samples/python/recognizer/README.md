- [Dependencies](#dependencies)
  - [Debugging missing dependencies](#dependencies-debugging)
- [GPGPU acceleration](#gpu-acceleration)
- [Pre-built binaries](#prebuilt)
- [Prerequisite](#prerequisite)
- [Usage](#testing-usage)
- [Examples](#testing-examples)
- [Know issues](#testing-know-issues)
<hr />

This application is used as reference code for developers to show how to use the Python bindings for the [C++ API](https://www.doubango.org/SDKs/anpr/docs/cpp-api.html) and could
be used to easily check the accuracy. The application accepts path to a JPEG/PNG/BMP file as input.

If you don't want to build this sample and is looking for a quick way to check the accuracy then, try
our cloud-based solution at [https://www.doubango.org/webapps/alpr/](https://www.doubango.org/webapps/alpr/).

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

<a name="prerequisite"></a>
# Prerequisite #

[**You must build the Python extension**](../../../python/README.md) before trying to run this sample. More information on how to build the extension could be found [here](../../../python/README.md)

<a name="testing-usage"></a>
# Usage #

`recognizer.py` is a Python command line application with the following usage:
```
recognizer.py \
			--image <path-to-image-with-to-recognize> \
			--assets <path-to-assets-folder> \
			[--parallel <whether-to-enable-parallel-mode:true/false>] \
			[--platform <target platform:auto/mobile/desktop>] \
			[--charset <recognition-charset:latin/korean/chinese>] \
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

## Move to the binaries folder ##
Before trying the next examples you have to navigate to the folder containing the [binaries](../binaries):
```
cd ultimateALPR-SDK/binaries/<<os>>/<<arch>>
```
For example:
 * On Windows x86_64: [ultimateALPR-SDK/binaries/windows/x86_64](../../../binaries/windows/x86_64)
 * On Linux x86_64: [ultimateALPR-SDK/binaries/linux/x86_64](../../../binaries/linux/x86_64)
 * On Linux aarch64: [ultimateALPR-SDK/binaries/linux/aarch64](../../../binaries/linux/aarch64)
 * ... you got the idea

## Try ##

- On **Linux**, you may use the next command:
```
PYTHONPATH=$PYTHONPATH:.:../../../python \
LD_LIBRARY_PATH=.:$LD_LIBRARY_PATH \
python ../../../samples/python/recognizer/recognizer.py --image ../../../assets/images/lic_us_1280x720.jpg --assets ../../../assets
```

- On **Windows x86_64**, you may use the next command:
```
setlocal
set PYTHONPATH=%PYTHONPATH%;.;../../../python
set PATH=%PATH%;%~dp0
python ../../../samples/python/recognizer/recognizer.py --image ../../../assets/images/lic_us_1280x720.jpg --assets ../../../assets
endlocal
```
If you want to make your life easier run [python_recognizer.bat](../../../binaries/windows/x86_64/python_recognizer.bat) to test on Windows. You can edit the file using Notepad to change the parameters.

Please note that if you're cross compiling the application then you've to make sure to copy the application and both the [assets](../../../assets) and [binaries](../../../binaries) folders to the target device.

<a name="testing-know-issues"></a>
# Know issues #
If you get `undefined symbol: PyUnicode_FromFormat` error message, then make sure you're using Python 3 and same version as the one used to buid the extension. We tested the code on version **3.6.9** (Windows 8), **3.6.8** (Ubuntu 18) and **3.7.3** (Raspbian Buster). Run `python --version` to print your Python version. You may use `python3` instead of `python` to make sure you're using version 3.


