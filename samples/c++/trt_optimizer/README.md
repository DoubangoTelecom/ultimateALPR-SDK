- [Dependencies](#dependencies)
  - [Debugging missing dependencies](#dependencies-debugging)
- [Pre-built binaries](#prebuilt)
- [Building](#building)
- [Testing](#testing)
  - [Usage](#testing-usage)
  - [Examples](#testing-examples)


This application is used to generate TensorRT optimized models.

<a name="dependencies"></a>
# Dependencies #
**The SDK is developed in C++11** and you'll need **glibc 2.27+** on *Linux* and **[Microsoft Visual C++ 2026 Redistributable(x64)](https://aka.ms/vc14/vc_redist.x64.exe)** (any later version is ok) on *Windows*.  **You most likely already have these dependencies on you machine** as almost every program require it.

<a name="dependencies-debugging"></a>
## Debugging missing dependencies ##
To check if all dependencies are present:
- **Windows x86_64:** Use [Dependency Walker](https://www.dependencywalker.com/) on [binaries/windows/x86_64/ultimateALPR-SDK.dll](../../../binaries/windows/x86_64/ultimateALPR-SDK.dll).
- **Linux x86_64:** Use `ldd <your-shared-lib>` on [binaries/linux/x86_64/libultimate_alpr-sdk.so](../../../binaries/linux/x86_64/libultimate_alpr-sdk.so).
- 
<a name="prebuilt"></a>
# Pre-built binaries #

If you don't want to build this sample by yourself, then use the pre-built versions:
 - Windows x86_64: [trt_optimizer.exe](../../../binaries/windows/x86_64/trt_optimizer.exe) under [binaries/windows/x86_64](../../../binaries/windows/x86_64)
 - Linux x86_64: [trt_optimizer](../../../binaries/linux/x86_64/trt_optimizer) under [binaries/linux/x86_64](../../../binaries/linux/x86_64).
 - Linux aarch64: [trt_optimizer](../../../binaries/linux/aarch64/trt_optimizer) under [binaries/linux/x86_64](../../../binaries/linux/aarch64).

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

<a name="testing"></a>
# Testing #
After [building](#building) the application you can test it on your local machine.

<a name="testing-usage"></a>
## Usage ##

runtimeKey is a command line application with the following usage:
```
trt_optimizer \
      --assets <path-to-assets-folder>
      
```
Options surrounded with **[]** are optional.
- `--assets` Path to the assets folder containing the configuration files and models. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#assets-folder.

<a name="testing-examples"></a>
## Examples ##

- On **Linux x86_64** you may use the next command:
```
LD_LIBRARY_PATH=../../../binaries/linux/x86_64:$LD_LIBRARY_PATH ./trt_optimizer \
    --assets ../../../assets
```
- On **Linux aarch64** you may use the next command:
```
LD_LIBRARY_PATH=../../../binaries/linux/aarch64:$LD_LIBRARY_PATH ./trt_optimizer \
    --assets ../../../assets
```
- On **Windows x86_64**, you may use the next command:
```
trt_optimizer.exe ^
    --assets ../../../assets
```
