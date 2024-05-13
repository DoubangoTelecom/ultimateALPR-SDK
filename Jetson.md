- [Getting started](#getting-started)
  - [Requirements](#getting-started_requirements)
  - [Before trying to use the SDK on Jetson](#getting-started_before-trying-to-use-the-sdk-on-jetson)
    - [Building optimized models](#getting-started_before-trying-to-use-the-sdk-on-jetson_building-optimized-models)
- [Benchmark](#benchmark)
- [Jetson Nano A01/B01 versus Raspberry Pi 4](#jetson-nano-versus-Raspberry-Pi-4)
- [Pre-processing operations](#pre-processing-operations)
- [Coming next](#coming-next)
- [Known issues and possible fixes](#known-issues-and-possible-fixes)
  - [Warnings you can safely ignore](#known-issues-and-possible-fixes_warnings-to-ignore)
  - [Failed to open file](#known-issues-and-possible-fixes_failed-to-open-file)
- [Technical questions](#technical-questions)

<hr />

This document is about [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) in general but will focus on [NVIDIA Jetson devices](https://developer.nvidia.com/buy-jetson) (TX1, TX2, Nano, Xavier AGX/NX...).

**Starting version 3.13.0** we support full GPGPU acceleration for [NVIDIA Jetson devices](https://developer.nvidia.com/buy-jetson) using [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) (no longer need [TF-TRT](https://docs.nvidia.com/deeplearning/frameworks/tf-trt-user-guide/index.html)).

 - The SDK was tested using [JetPack 4.4.1](https://developer.nvidia.com/embedded/jetpack) and [JetPack 5.1.0](https://developer.nvidia.com/embedded/jetpack), the latest version from NVIDIA and **we will not provide technical support if you're using any other version**.
 - The binaries for Jetson are under [binaries/jetson](binaries/jetson)

<a name="getting-started"></a>
# Getting started #
As explained above, we use [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) to run the deep learning models on GPU.
 * [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) is used for:
    - License plate and car detection
    - [License Plate Country Identification (LPCI)](https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-country-identification-lpci)
    - [Vehicle Color Recognition (VCR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-color-recognition-vcr)
    - [Vehicle Make Model Recognition (VMMR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-make-model-recognition-vmmr)
    - [Vehicle Body Style Recognition (VBSR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-body-style-recognition-vbsr)
    - [Vehicle Direction Tracking (VDT)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-direction-tracking-vdt)
    - [Vehicle Speed Estimation (VSE)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-speed-estimation-vse)
    - [License Plate Recognition (LPR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-recognition-lpr)

<a name="getting-started_requirements"></a>
## Requirements ##
We require [JetPack 4.4.1](https://developer.nvidia.com/embedded/jetpack) or [JetPack 5.1.0](https://developer.nvidia.com/embedded/jetpack). As of today (***February 20, 2023***), version 5.1.0 is the latest one.
If you run `apt-cache show nvidia-jetpack | grep "Version:"`, you'll have:
  - `Version: 4.4.1-b50\nVersion: 4.4-b186\nVersion: 4.4-b144` if you're using Jetpack 4.4.1
  - `Version: 5.1-b147` if you're using Jetpack 5.1.0

Supported devices (check https://developer.nvidia.com/embedded/jetpack for up to date info):
  - `Jetpack 5.1.0:` Jetson AGX Orin 32 GB production module, Jetson AGX Orin Developer Kit, Jetson Orin NX 16GB production module, Jetson AGX Xavier series, Jetson Xavier NX series modules, Jetson AGX Xavier Developer Kit and Jetson Xavier NX Developer Kit.
  - `Jetpack 4.4.1:` Jetson Nano, Jetson Xavier NX, Jetson TX1 and Jetson TX2.

<a name="getting-started_before-trying-to-use-the-sdk-on-jetson"></a>
## Before trying to use the SDK on Jetson ##
Please note that this repo doesn't contain optimized TensorRT models and **you'll not be able to use the SDK** unless you generate these models. More info about model optimization at https://docs.nvidia.com/deeplearning/tensorrt/developer-guide/index.html. Fortunately we made this task very easy by writing an optimizer using TensorRT C++.

<a name="getting-started_before-trying-to-use-the-sdk-on-jetson_building-optimized-models"></a>
<a name="building-optimized-models"></a>
### Building optimized models ###
This process will write the optimized models (a.k.a plans) to the local disk which means we'll need write permission. We recommend running the next commands as root(#) instead of normal user($).
To generate the optimized models:
 - Navigate to the jetson binaries folder: `cd ultimateALPR-SDK/binaries/jetson/aarch64`
 - Generate the optimized models: `sudo chmod +x ./prepare.sh && sudo ./prepare.sh`
 
This will build the models using CUDA engine and serialize the optimized models into [assets/models.tensorrt/optimized](assets/models.tensorrt/optimized). Please note that **the task will last several minutes and you must be patient**. Next time you run this task it will be faster as only newest models will be generated. So, you can interrupt the process and next time it will continue from where it ended the last time.

Models generated on a Jetson device with [Compute Capabilities](https://developer.nvidia.com/cuda-gpus) X and TensorRT version Y will only be usable on devices matching this configuration. For example, **you'll not be able to use models generated on Jetson TX2 ([Compute Capabilities](https://developer.nvidia.com/cuda-gpus) 6.2) on a Jetson nano ([Compute Capabilities](https://developer.nvidia.com/cuda-gpus) 5.3)**.

<a name="benchmark"></a>
# Benchmark #
Here are some benchmark numbers to compare the speed. For more information about the **positive rate**, please check https://www.doubango.org/SDKs/anpr/docs/Benchmark.html. The benchmark application is open source and could be found at [samples/c++/benchmark](samples/c%2B%2B/benchmark).

Before running the benchmark application:
 - For Jetson nano, make sure you're using a Barrel Jack (5V-4A) power supply instead of microUSB port (5V-2A)
 - Put the device on maximum performance mode: `sudo nvpmodel -m 2 && sudo jetson_clocks`.
 - Make sure all CPU cores are online: `cat /sys/devices/system/cpu/online`

To run the benchmark application for [binaries/jetson](binaries/jetson) with 0.2 positive rate for 100 loops:
```
cd ulatimateALPR-SDK/binaries/jetson/aarch64
chmod +x benchmark
LD_LIBRARY_PATH=.:$LD_LIBRARY_PATH ./benchmark \
    --positive ../../../assets/images/lic_us_1280x720.jpg \
    --negative ../../../assets/images/london_traffic.jpg \
    --assets ../../../assets \
    --charset latin \
    --loops 100 \
    --rate 0.2 \
    --parallel true
```

|  | 0.0 rate | 0.2 rate | 0.5 rate | 0.7 rate | 1.0 rate |
|-------- | --- | --- | --- | --- | --- |
| **[binaries/jetson](binaries/jetson)<br/> (Xavier NX, JetPack 5.1.0)** | 657 millis <br />**152 fps** | 744 millis <br/> 134 fps | 837 millis <br/> 119 fps | 961 millis <br/> 104 fps | 1068 millis <br/> 93 fps |
| **[binaries/linux/aarch64](binaries/linux/aarch64)<br/> (Xavier NX, JetPack 5.1.0)** | 7498 millis <br />**13.33 fps** | 8281 millis <br/> 12.07 fps | 9421 millis <br/> 10.61 fps | 10161 millis <br/> 9.84 fps | 11006 millis <br/> 9.08 fps |
| **[binaries/jetson](binaries/jetson)<br/> (Nano B01, JetPack 4.4.1)** | 2920 millis <br />**34.24 fps** | 3102 millis <br/> 32.23 fps | 3274 millis <br/> 30.53 fps | 3415 millis <br/> 29.27 fps | 3727 millis <br/> 26.82 fps |
| **[binaries/linux/aarch64](binaries/linux/aarch64)<br/> (Nano B01, JetPack 4.4.1)** | 4891 millis <br />**20.44 fps** | 6950 millis <br/> 14.38 fps | 9928 millis <br/> 10.07 fps | 11892 millis <br/> 8.40 fps | 14870 millis <br/> 6.72 fps |

[binaries/linux/aarch64](binaries/linux/aarch64) contains generic Linux binaries for AArch64 (a.k.a ARM64) devices. All operations are done on CPU. The performance boost between this CPU-only version and the Jetson-based ones may not seem impressive but there is a good reason: [binaries/linux/aarch64](binaries/linux/aarch64) uses INT8 inference while the Jetson-based versions use a mix of FP32 and FP16 **which means more accurate**. Providing INT8 models for Jetson devices is on our roadmap with no ETA.

<a name="jetson-nano-versus-Raspberry-Pi-4"></a>
# Jetson Nano B01/A01 versus Raspberry Pi 4 #
**On average the SDK is 3 times faster on Jetson nano compared to Raspberry Pi 4** and this may not seem impressive but there is a good reason: [binaries/raspbian/armv7l](binaries/raspbian/armv7l) uses INT8 inference while the Jetson-based binaries ([binaries/jetson](binaries/jetson) uses a mix of FP32 and FP16 **which means more accurate**. Providing INT8 models for Jetson devices is on our roadmap with no ETA.

<a name="pre-processing-operations"></a>
# Pre-processing operations #
Please note that some pre-processing operations are performed on CPU and this why the CPU usage is at 1/5th. You don't need to worry about these operations, they are massively multi-threaded and entirely written in assembler with **SIMD NEON** acceleration. These functions are open source and you can find them at:
  - Normalization: [compv_math_op_sub_arm64_neon.S](https://github.com/DoubangoTelecom/compv/blob/e09cdf22801574d322e023872eb0b0a4ceef01b6/base/math/asm/arm/compv_math_op_sub_arm64_neon.S#L141)
  - Chroma Conversion (YUV -> RGB): [compv_image_conv_to_rgbx_arm64_neon.S](https://github.com/DoubangoTelecom/compv/blob/e09cdf22801574d322e023872eb0b0a4ceef01b6/base/image/asm/arm/compv_image_conv_to_rgbx_arm64_neon.S#L34)
  - Type conversion (UINT8 -> FLOAT32): [compv_math_cast_arm64_neon.S](https://github.com/DoubangoTelecom/compv/blob/e09cdf22801574d322e023872eb0b0a4ceef01b6/base/math/asm/arm/compv_math_cast_arm64_neon.S#L23)
  - Packing/Unpacking: [compv_mem_arm64_neon.S](https://github.com/DoubangoTelecom/compv/blob/e09cdf22801574d322e023872eb0b0a4ceef01b6/base/asm/arm/compv_mem_arm64_neon.S#L262)
  - Scaling: [compv_image_scale_bilinear_arm64_neon.S](https://github.com/DoubangoTelecom/compv/blob/e09cdf22801574d322e023872eb0b0a4ceef01b6/base/image/asm/arm/compv_image_scale_bilinear_arm64_neon.S#L25)
  - ...

<a name="coming-next"></a>
# Coming next #
Version 3.1.0 is the first release to support NVIDIA Jetson and there is room for optimizations. Adding support for full INT8 inference could **improve the speed by up to 700%**. We're also planing to move the NMS layer from the GPU to the CPU and rewrite the code in assembler with **NEON SIMD**.

<a name="known-issues-and-possible-fixes"></a>
# Known issues and possible fixes #

<a name="known-issues-and-possible-fixes_warnings-to-ignore"></a>
## Warnings you can safely ignore ##
All warnings from NVIDIA logger will be logged as errors on model optimization process. You can safely ignore the following messages:
  - `Your ONNX model has been generated with INT64 weights, while TensorRT does not natively support INT64. Attempting to cast down to INT32.`
  - `53 weights are affected by this issue: Detected subnormal FP16 values.`
  - `The implicit batch dimension mode has been deprecated. Please create the network with NetworkDefinitionCreationFlag::kEXPLICIT_BATCH flag whenever possible.`

<a name="known-issues-and-possible-fixes_failed-to-open-file"></a>
## Failed to open file ##
You may receive `[UltAlprSdkTRT] Failed to open file` error after running `./prepare.sh` script if we fail to write to the local disk. We recommend running the script as root(#) instead of normal user($). 

<a name="technical-questions"></a>
# Technical questions #
Please check our [discussion group](https://groups.google.com/forum/#!forum/doubango-ai) or [twitter account](https://twitter.com/doubangotelecom?lang=en)
