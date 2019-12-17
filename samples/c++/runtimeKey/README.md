- [Building](#building)
  - [Windows](#building-windows)
  - [Generic GCC](#building-generic-gcc)
  - [Raspberry Pi (Raspbian OS)](#building-rpi)
- [Testing](#testing)
  - [Usage](#testing-usage)
  - [Examples](#testing-examples)


This application is used as reference code for developers to show how to use the [C++ API](https://www.doubango.org/SDKs/anpr/docs/cpp-api.html) to
generate a runtime key. Once a runtime key is generated it must be [activated to produce a token](https://www.doubango.org/SDKs/LicenseManager/docs/Activation_use_cases.html).

<a name="building"></a>
# Building #

This sample contains [a single C++ source file](runtimeKey.cxx) and is easy to build. The documentation about the C++ API is at [https://www.doubango.org/SDKs/anpr/docs/cpp-api.html](https://www.doubango.org/SDKs/anpr/docs/cpp-api.html).

<a name="building-windows"></a>
## Windows ##
You'll need Visual Studio and the project is at [runtimeKey.vcxproj](runtimeKey.vcxproj).

<a name="building-generic-gcc"></a>
## Generic GCC ##
Next command is a generic GCC command:
```
cd ultimateALPR-SDK/samples/c++/runtimeKey

g++ runtimeKey.cxx -O3 -I../../../c++ -L../../../binaries/<yourOS>/<yourArch> -lultimate_alpr-sdk -o runtimeKey
```
- You've to change `yourOS` and  `yourArch` with the correct values. For example, on Android ARM64 they would be equal to `android` and `jniLibs/arm64-v8a` respectively.
- If you're cross compiling then, you'll have to change `g++` with the correct triplet. For example, on Android ARM64 the triplet would be equal to `aarch64-linux-android-g++`.

<a name="building-rpi"></a>
## Raspberry Pi (Raspbian OS) ##

To build the sample for Raspberry Pi you can either do it on the device itself or cross compile it on [Windows](#cross-compilation-rpi-install-windows), [Linux](#cross-compilation-rpi-install-ubunt) or OSX machines. 
For more information on how to install the toolchain for cross compilation please check [here](../README.md#cross-compilation-rpi).

```
cd ultimateALPR-SDK/samples/c++/runtimeKey

arm-linux-gnueabihf-g++ runtimeKey.cxx -O3 -I../../../c++ -L../../../binaries/raspbian/armv7l -lultimate_alpr-sdk -o runtimeKey
```
- On Windows: replace `arm-linux-gnueabihf-g++` with `arm-linux-gnueabihf-g++.exe`
- If you're building on the device itself: replace `arm-linux-gnueabihf-g++` with `g++` to use the default GCC

<a name="testing"></a>
# Testing #
After [building](#building) the application you can test it on your local machine.

<a name="testing-usage"></a>
## Usage ##

runtimeKey is a command line application with the following usage:
```
runtimeKey \
      [--json <json-output:bool>]
```
Options surrounded with **[]** are optional.
- `--json` Whether to output the runtime license key as JSON string intead of raw string. Default: *true*.

<a name="testing-examples"></a>
## Examples ##

For example, on **Raspberry Pi** you may call the runtimeKey application using the following command:
```
LD_LIBRARY_PATH=../../../binaries/raspbian/armv7l:$LD_LIBRARY_PATH ./runtimeKey \
    --json false
```
On Android ARM64 you may use the next command:
```
LD_LIBRARY_PATH=../../../binaries/android/jniLibs/arm64-v8a:$LD_LIBRARY_PATH ./runtimeKey \
    --json false
```

Please note that if you're cross compiling the application then you've to make sure to copy the application and both the [assets](../../../assets) and [binaries](../../../binaries) folders to the target device.


