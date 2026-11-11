- [Dependencies](#dependencies)
  - [Debugging missing dependencies](#dependencies-debugging)
- [GPGPU acceleration](#gpu-acceleration)
- [Pre-built binaries](#prebuilt)
- [Building](#building)
- [Testing](#testing)
  - [Usage](#testing-usage)
  - [Examples](#testing-examples)
<hr />

This application is a reference implementation for developers to show how to use the Java API and could
be used to easily check the accuracy. The Java API is a wrapper around the C++ API defined at [https://www.doubango.org/SDKs/anpr/docs/cpp-api.html](https://www.doubango.org/SDKs/anpr/docs/cpp-api.html).

The application accepts path to a JPEG/PNG/BMP file as input.

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

<a name="building"></a>
# Building #

This sample contains [a single Java source file](Recognizer.java).

You have to navigate to the current folder (`ultimateALPR-SDK/samples/java/recognizer` ) before trying the next commands:
```
cd ultimateALPR-SDK/samples/java/recognizer
```

Here is how to build the file using `javac`:
```
javac @sources.txt -d .
```

<a name="testing-usage"></a>
## Usage ##

`Recognizer` is a command line application with the following usage:
```
Recognizer \
      --image <path-to-image-with-plate-to-process> \
      --assets <path-to-assets-folder> \
      [--tokenfile <path-to-license-token-file>] \
      [--tokendata <base64-license-token-data>]
```
Options surrounded with **[]** are optional.
- `--image` Path to the image(JPEG/PNG/BMP) to process. You can use default image at [../../../assets/images/lic_us_1280x720.jpg](../../../assets/images/lic_us_1280x720.jpg).
- `--assets` Path to the [assets](../../../assets) folder containing the configuration files and models. Default value is the current folder.
- `--tokenfile` Path to the file containing the base64 license token if you have one. If not provided then, the application will act like a trial version. Default: *null*.
- `--tokendata` Base64 license token if you have one. If not provided then, the application will act like a trial version. Default: *null*.

<a name="testing-examples"></a>
## Examples ##
You'll need to build the sample as explained [above](#building).

You have to navigate to the current folder (`ultimateALPR-SDK/samples/java/recognizer` ) before trying the next commands:
```
cd ultimateALPR-SDK/samples/java/recognizer
```

- For example, on **Linux aarch64** you may call the recognizer application using the following command:
```
LD_LIBRARY_PATH=../../../binaries/linux/aarch64:$LD_LIBRARY_PATH \
java Recognizer --image ../../../assets/images/lic_us_1280x720.jpg --assets ../../../assets
```
- On **Linux x86_64**, you may use the next command:
```
LD_LIBRARY_PATH=../../../binaries/linux/x86_64:$LD_LIBRARY_PATH \
java Recognizer --image ../../../assets/images/lic_us_1280x720.jpg --assets ../../../assets
```

- On **Windows x86_64**, you may use the next command:
```
setlocal
set PATH=%PATH%;../../../binaries/windows/x86_64
java Recognizer --image ../../../assets/images/lic_us_1280x720.jpg --assets ../../../assets
endlocal
```
To make your life easier, run [Recognizer.bat](Recognizer.bat) to test on Windows. You can edit the file using Notepad to change the parameters.

Please note that if you're cross compiling the application then you've to make sure to copy the application and both the [assets](../../../assets) and [binaries](../../../binaries) folders to the target device.


