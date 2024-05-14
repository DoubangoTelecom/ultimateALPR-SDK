- [GPGPU acceleration](#gpu-acceleration)
  - [OpenVINO](#gpu-acceleration-openvino)
    - [Myriad](#gpu-acceleration-openvino-myriad)
  - [NVIDIA TensorRT](#gpu-acceleration-tensorrt)
    - [Installation](#gpu-acceleration-tensorrt-install)
    - [Building plans](#gpu-acceleration-tensorrt-build)
  - [Tensorflow libraries](#gpu-acceleration-tensorflow)
    - [Windows](#gpu-acceleration-tensorflow-windows)
    - [Linux](#gpu-acceleration-tensorflow-linux)
- [Migration to Tensorflow 2.x and CUDA 11.x](#migration-tf2)
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

We use [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt), [Tensorflow](https://www.tensorflow.org/) and [OpenVINO](https://docs.openvinotoolkit.org/) as deep learning frameworks. The current repository contains [Tensorflow](https://www.tensorflow.org/) libraries built without GPU functions to reduce the size. Also, few developers need GPGPU accelerated libraries. The GPU libraries will work on your device even if you don't have NVIDIA GPU.

<a name="gpu-acceleration-openvino"></a>
## OpenVINO ##
By default we use the "CPU" device when OpenVINO is enabled. If you have an Intel GPU and want to use it, then change the device type to "GPU" (`--openvino_device="GNA"|"HETERO"|"CPU"|"MULTI"|"GPU"|"MYRIAD"|"HDDL"|"FPGA"` command). More information at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#openvino-device.

<a name="gpu-acceleration-openvino-myriad"></a>
### Myriad ###
To run UltimateALPR on Myriad VPU you'll need version **v3.3.5 or later**. **Windows 10+ is also required.**

You have to run the sample applications with the following options: `--openvino_enabled true --openvino_device MYRIAD`. The device name is case-sensitive.
- If you get `Can not init Myriad device: NC_MVCMD_NOT_FOUND`, make sure the driver is correctly installed as explained [here](https://docs.openvinotoolkit.org/2018_R5/_docs_install_guides_installing_openvino_windows.html#usb-myriad). You can find these driver files in the [binaries](../../binaries/windows/x86_64) folder but we recommend using yours.
- If you get `Can not init Myriad device: NC_ERROR`, make sure you're using **v3.3.5 or later**. Check [issue #133](https://github.com/DoubangoTelecom/ultimateALPR-SDK/issues/133) for more info.

<a name="gpu-acceleration-tensorrt"></a>
## NVIDIA TensorRT ##
Starting **UltimateALPR v3.13** we recommend using [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) instead of Tensorflow to run the models on GPU. You still need to download Tensorflow libraries as they are used as fallback when TensorRT plugin fail to load.
**This section is about using TensorRT on Windows/Linux x64, check [here](../../Jetson.md) for NVIDIA Jetson.**

<a name="gpu-acceleration-tensorrt-install"></a>
### Installation ###
We recommend **TensorRT 8.6.1.6** on Windows/Linux x64 and **require TensorRT 8.x**. That version work on CUDA 11 or later. For now we don't support CUDA 10.
We recommend using the `tar` (Linux) or the `zip` (Windows) versions as they don't require installation which means you'll not override your native TensorRT if you have one.
- Download the tar/zip from https://developer.nvidia.com/nvidia-tensorrt-8x-download
- Unzip/untar the file (e.g. under `/home/TensorRT-8.6.1.6`)

You'll need to use `LD_LIBRARY_PATH` envvar to tell the linker where to find TensorRT libraries required by [libultimatePluginTensorRT.so](../../binaries/linux/x86_64/libultimatePluginTensorRT.so). You can avoid using `LD_LIBRARY_PATH` by copying the TensorRT libraries to [binaries/linux/x86_64](../../binaries/linux/x86_64): `cp /home/TensorRT-8.6.1.6/lib/* binaries/linux/x86_64`.

To check if all dependencies can found: `ldd libultimatePluginTensorRT.so`

<a name="gpu-acceleration-tensorrt-build"></a>
### Building plans ###
The [models folder](../../assets/models.tensorrt) contains ONNX models, you need to generate TensorRT optimized models (a.k.a plans):
```
cd binaries/linux/x86_64
sudo LD_LIBRARY_PATH=/home/TensorRT-8.6.1.6/lib:$LD_LIBRARY_PATH ./trt_optimizer --assets ../../../assets
```
Notice how we use `LD_LIBRARY_PATH` envvar to tell the linker where to find the TensorRT libs. An alternative would be copying the TensorRT libs to [binaries/linux/x86_64](../../binaries/linux/x86_64) as explained above.

This will take several minutes, you must be patient.

That's it. You're ready to use TensorRT models.

<a name="gpu-acceleration-tensorflow"></a>
## Tensorflow libraries ##
[NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) is preferred to Tensorflow if you're using **UltimateALPR v3.13 or later**.

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

<a name="migration-tf2"></a>
# Migration to Tensorflow 2.x and CUDA 11.x #

You don't need to migrate to Tensorflow 2.x if you're planning to use [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) (**recommend**).

Our SDK is built and shipped with Tensorflow 1.x to make it work on oldest NVIDIA GPUs. If you want to use newest NVIDIA GPUs (e.g. RTX3060) which requires CUDA 11.x, then you'll need to upgrade the Tensorflow version. Check https://www.tensorflow.org/install/source#gpu to know which CUDA version is required for your Tensorflow version.

***This section is about Tensorflow 2.6, Ubuntu 20.04.2 LTS, NVIDIA RTX3060 GPU and cuda_11.1.TC455_06.29190527_0***. Tensorflow 2.6 is the latest (**11/29/2021**) public version published at https://www.tensorflow.org/install/lang_c. Please note that we use CUDA 11.1 instead of 11.2 as suggested at https://www.tensorflow.org/install/source#gpu but both will work.

- Links:
  - Linux CPU only:	https://storage.googleapis.com/tensorflow/libtensorflow/libtensorflow-cpu-linux-x86_64-2.6.0.tar.gz
  - Linux GPU support:	https://storage.googleapis.com/tensorflow/libtensorflow/libtensorflow-gpu-linux-x86_64-2.6.0.tar.gz
  - Windows CPU only:	https://storage.googleapis.com/tensorflow/libtensorflow/libtensorflow-cpu-windows-x86_64-2.6.0.zip
  - Windows GPU support:	https://storage.googleapis.com/tensorflow/libtensorflow/libtensorflow-gpu-windows-x86_64-2.6.0.zip

- Download and uzip Tensorflow 2.6 inside the binaries folder
```
cd ultimateALPR-SDK/binaries/linux/x86_64
wget https://storage.googleapis.com/tensorflow/libtensorflow/libtensorflow-gpu-linux-x86_64-2.6.0.tar.gz
tar -xf libtensorflow-gpu-linux-x86_64-2.6.0.tar.gz
cp lib/* .
```
make sure you don't have older Tensorflow binaries in that directory.
`ls` from the current folder will give you `libtensorflow_framework.so  libtensorflow_framework.so.2  libtensorflow_framework.so.2.6.0  libtensorflow.so  libtensorflow.so.2  libtensorflow.so.2.6.0` 

- Checking dependencies and workaround

Now when you run `ldd libultimate_alpr-sdk.so` you'll see `libtensorflow.so.1 => not found`. That's normal because the SDK is built for Tensorflow 1.x. Do not worry, we use the [C-API](https://github.com/tensorflow/tensorflow/blob/master/tensorflow/c/c_api.h) which is the same for all Tensorflow versions.

The litte trick is to duplicate and rename the symbolic link: `cp libtensorflow.so.2 libtensorflow.so.1`

**That's it, you're ready to use the SDK**

Check the [benchmark numbers](benchmark/README.md#peformance-numbers) if you want to know how fast the SDK runs on RTX3060.

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
