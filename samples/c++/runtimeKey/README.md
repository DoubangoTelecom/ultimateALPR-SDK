- [Dependencies](#dependencies)
  - [Debugging missing dependencies](#dependencies-debugging)
- [Pre-built binaries](#prebuilt)
- [Building](#building)
- [Testing](#testing)
  - [Usage](#testing-usage)
  - [Examples](#testing-examples)
  - [Amazon Web Services (AWS) and Microsoft Azure](#testing-aws-azure)


This application is used as reference code for developers to show how to use the [C++ API](https://www.doubango.org/SDKs/face-liveness/docs/cpp-api.html) to
generate a runtime key. Once a runtime key is generated it must be [activated to produce a token](https://www.doubango.org/SDKs/LicenseManager/docs/Activation_use_cases.html).

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
 - Windows x86_64: [runtimeKey.exe](../../../binaries/windows/x86_64/runtimeKey.exe) under [binaries/windows/x86_64](../../../binaries/windows/x86_64)
 - Linux x86_64: [runtimeKey](../../../binaries/linux/x86_64/runtimeKey) under [binaries/linux/x86_64](../../../binaries/linux/x86_64).
 - Linux aarch64: [runtimeKey](../../../binaries/linux/aarch64/runtimeKey) under [binaries/linux/x86_64](../../../binaries/linux/aarch64).

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
runtimeKey \
      --assets <path-to-assets-folder> \
      [--json <json-output:bool>] \
      [--type <license-type:string>]
      
```
Options surrounded with **[]** are optional.
- `--assets` Path to the assets folder containing the configuration files and models. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#assets-folder.
- `--json` Whether to output the runtime license key as JSON string intead of raw string. Default: *true*.
- `--type` Defines how the license is attached to the machine/host. Possible values are *aws-instance*, *aws-byol*, *azure-instance* or *azure-byol*. Default: null. More info [here](../../../AWS.md).

<a name="testing-examples"></a>
## Examples ##

- On **Linux x86_64** you may use the next command:
```
LD_LIBRARY_PATH=../../../binaries/linux/x86_64:$LD_LIBRARY_PATH ./runtimeKey \
    --json true \
    --assets ../../../assets
```
- On **Linux aarch64** you may use the next command:
```
LD_LIBRARY_PATH=../../../binaries/linux/aarch64:$LD_LIBRARY_PATH ./runtimeKey \
    --json true \
    --assets ../../../assets
```
- On **Windows x86_64**, you may use the next command:
```
runtimeKey.exe ^
    --json true ^
    --assets ../../../assets
```

<a name="testing-aws-azure"></a>
## Amazon Web Services (AWS) and Microsoft Azure ##

Please read [this](../../../AWS.md) if you're planning to run the SDK on [Amazon AWS](https://aws.amazon.com/) or [Microsoft Azure](https://azure.microsoft.com/en-us/). 

