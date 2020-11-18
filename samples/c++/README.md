- [GPGPU acceleration](#gpu-acceleration)
  - [OpenVINO](#gpu-acceleration-openvino)
  - [Tensorflow libraries](#gpu-acceleration-tensorflow)
    - [Windows](#gpu-acceleration-tensorflow-windows)
    - [Linux](#gpu-acceleration-tensorflow-linux)
    - [NVIDIA Jetson](#gpu-acceleration-tensorflow-jetson)
- [Cross compilation](#cross-compilation)
  - [Raspberry Pi](#cross-compilation-rpi)
    - [Installing the toolchain](#cross-compilation-rpi-install)
      - [Windows](#cross-compilation-rpi-install-windows)
      - [Ubuntu](#cross-compilation-rpi-install-ubuntu)
- [Known issues](#known-issues)

There are 3 C++ samples: [Benchmark](benchmark), [Recognizer](recognizer) and [RuntimeKey](runtimeKey). Check [here](benchmark/README.md), [here](recognizer/README.md) and [here](runtimeKey/README.md) on how to build and use them.

The C++ samples are common to all platforms: **Android**, **Windows**, **iOS**, **Raspberry Pi (3 or 4)**, **Linux**...

For **Android**, we recommend using the Java samples under [android](../android) folder. These are complete samples with support for reatime recognition from video stream (camera) and require Android Studio to build. More info on how to install these samples is available [here](../../README.md#trying-the-samples-android).

<a name="gpu-acceleration"></a>
# GPGPU acceleration #

We use [Tensorflow](https://www.tensorflow.org/) and [OpenVINO](https://docs.openvinotoolkit.org/) as deep learning frameworks. The current repository contains [Tensorflow](https://www.tensorflow.org/) libraries built without GPU functions to reduce the size. Also, few developers need GPGPU accelerated libraries. The GPU libraries will work on your device even if you don't have NVIDIA GPU.

<a name="gpu-acceleration-openvino"></a>
## OpenVINO ##
By default we use the "CPU" device when OpenVINO is enabled. If you have an Intel GPU and want to use it, then change the device type to "GPU" (`--openvino_device="GNA"|"HETERO"|"CPU"|"MULTI"|"GPU"|"MYRIAD"|"HDDL"|"FPGA"` command). More information at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#openvino-device.

<a name="gpu-acceleration-tensorflow"></a>
## Tensorflow libraries ##
The Tensorflow libraries are hosted at:
 - [1] Windows_x86_64_CPU+GPU: https://doubango.org/deep_learning/libtensorflow_r1.15_cpu+gpu_windows_x86-64.zip
 - [2] Windows_x86_64_CPU: https://doubango.org/deep_learning/libtensorflow_r1.15_cpu_windows_x86-64.zip
 - [3] Linux_x86_64_CPU+GPU: https://doubango.org/deep_learning/libtensorflow_r1.14_cpu+gpu_linux_x86-64.tar.gz
 - [4] Linux_x86_64_CPU: https://doubango.org/deep_learning/libtensorflow_r1.14_cpu_linux_x86-64.tar.gz
 - [5] Jetson_aarch64_GPU: https://doubango.org/deep_learning/libtensorflow-1.15.4-jetson-gpu.tar.xz

<a name="gpu-acceleration-tensorflow-window"></a>
### Windows ###
To use the Tensorflow version with GPU funtions you'll need to download [[1]](https://doubango.org/deep_learning/libtensorflow_r1.15_cpu+gpu_windows_x86-64.zip), extract **tensorflow.dll** and override CPU-only [tensorflow.dll](../../binaries/windows/x86_64/tensorflow.dll) in [binaries/windows/x86_64](../../binaries/windows/x86_64)

<a name="gpu-acceleration-tensorflow-linux"></a>
### Linux ###
On Linux x86_64, [libtensorflow.so](../../binaries/linux/x86_64/libtensorflow.so) is missing in the [binaries folder](../../binaries/linux/x86_64). You'll need to download your preferred Tensorflow version ([[3]](https://doubango.org/deep_learning/libtensorflow_r1.14_cpu+gpu_linux_x86-64.tar.gz) or [[4]](https://doubango.org/deep_learning/libtensorflow_r1.14_cpu_linux_x86-64.tar.gz)) and copy the content to [binaries/linux/x86_64](../../binaries/linux/x86_64).

<a name="gpu-acceleration-tensorflow-jetson"></a>
### NVIDIA Jetson ###
On NVIDIA Jetson TX1/TX2/Nano and Xavier AGX/NX [libtensorflow_cc.so](../../binaries/jetson_tftrt/aarch64/libtensorflow_cc.so) is only needed if you're using binaries in [jetson_tftrt](../../binaries/jetson_tftrt). You don't need Tensorflow to use the binaries under [jetson](../../binaries/jetson). The difference between [jetson_tftrt](../../binaries/jetson_tftrt) and [jetson](../../binaries/jetson) binaries is explained [here](../../Jetson.md#getting-started_jetson-versus-jetsontftrt).

If you're using [jetson_tftrt](../../binaries/jetson_tftrt) instead of [jetson](../../binaries/jetson) then, you'll need to run the [./prepare.sh](../../binaries/jetson_tftrt/aarch64/prepare.sh) script as explained [here](../../Jetson.md#getting-started_before-trying-to-use-the-sdk-on-jetson_building-optimized-models) to download Tensorflow C++ libraries.

<a name="cross-compilation"></a>
# Cross compilation #

Every sample contain a single C++ source file and is easy to cross compile. 

<a name="cross-compilation-rpi"></a>
## Raspberry Pi (Raspbian OS) ##

This section explain how to install Raspberry Pi 4 (Raspbian OS) toolchain to cross compile the samples. These instructions can be easily adapted to another target platform (e.g Android).

<a name="cross-compilation-rpi-install"></a>
### Installing the toolchain ###

<a name="cross-compilation-rpi-install-windows"></a>
#### Windows ####
The toolchain for Raspberry Pi 4 could be found at [http://sysprogs.com/getfile/566/raspberry-gcc8.3.0.exe](http://sysprogs.com/getfile/566/raspberry-gcc8.3.0.exe) and more toolchain versions are at [https://gnutoolchains.com/raspberry/](https://gnutoolchains.com/raspberry/).

If you haven't changed the installation dir then, it should be installed at **C:\SysGCC**. To add the toolchain to the **%PATH**:
```
set PATH=%PATH%;C:\SysGCC\raspberry\bin
```

<a name="cross-compilation-rpi-install-ubuntu"></a>
#### Ubuntu ####
```
sudo apt-get update
sudo apt-get install crossbuild-essential-armhf
```

<a name="known-issues"></a>
# Known issues #
- On Linux you may get `[CompVSharedLib] Failed to load library with path=<...>libultimatePluginOpenVINO.so, Error: 0xffffffff`. Make sure to set `LD_LIBRARY_PATH` to add binaries folder to help the loader find all dependencies. You can also run `ldd libultimatePluginOpenVINO.so` to see which libraries are missing.
- On Linux you may get `'GLIBC_2.27' not found (required by <...>)`. This message means you're using an old glibc version. Update glibc or your OS to Ubuntu 18, Debian Buster... You can check your actual version by running `ldd --version`. 
