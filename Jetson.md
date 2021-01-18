- [Getting started](#getting-started)
  - [Requirements](#getting-started_requirements)
  - [Before trying to use the SDK on Jetson](#getting-started_before-trying-to-use-the-sdk-on-jetson)
    - [Building optimized models](#getting-started_before-trying-to-use-the-sdk-on-jetson_building-optimized-models)
  - [**binaries/jetson** versus **binaries/jetson_tftrt**](#getting-started_jetson-versus-jetsontftrt)
    - [Pros and Cons](#getting-started_jetson-versus-jetsontftrt_pros-and-cons)
    - [Recommendations](#getting-started_jetson-versus-jetsontftrt_recommendations)
- [Benchmark](#benchmark)
- [Jetson nano versus Raspberry Pi 4](#jetson-nano-versus-Raspberry-Pi-4)
- [Jetson Xavier NX versus Jetson TX2](#jetson-nx-versus-jetso-tx2)
- [Pre-processing operations](#pre-processing-operations)
- [Coming next](#coming-next)
- [Known issues and possible fixes](#known-issues-and-possible-fixes)
  - [Failed to open file](#known-issues-and-possible-fixes_failed-to-open-file)
  - [Slow load and initialization](#known-issues-and-possible-fixes_slow-load-and-initialization)
  - [High memory usage](#known-issues-and-possible-fixes_high-memory-usage)
  - [High CPU usage](#known-issues-and-possible-fixes_high-cpu-usage)
- [Technical questions](#technical-questions)

<br />

This document is about [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) in general but will focus on [NVIDIA Jetson devices](https://developer.nvidia.com/buy-jetson) (TX1, TX2, Nano, Xavier AGX/NX...).

**Starting version 3.1.0** we support full GPGPU acceleration for [NVIDIA Jetson devices](https://developer.nvidia.com/buy-jetson) using [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) and [TF-TRT](https://docs.nvidia.com/deeplearning/frameworks/tf-trt-user-guide/index.html).

 - The SDK was tested using [JetPack 4.4.1](https://developer.nvidia.com/embedded/jetpack), the latest version from NVIDIA and **we will not provide support for any other version**.
 - This repo contains two set of binaries: [binaries/jetson](binaries/jetson) and [binaries/jetson_tftrt](binaries/jetson_tftrt)

<a name="getting-started"></a>
# Getting started #
As explained above, we use both [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) and [TF-TRT](https://docs.nvidia.com/deeplearning/frameworks/tf-trt-user-guide/index.html).
 * [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) is used for:
    - License plate and car detection
    - [License Plate Country Identification (LPCI)](https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-country-identification-lpci)
    - [Vehicle Color Recognition (VCR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-color-recognition-vcr)
    - [Vehicle Make Model Recognition (VMMR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-make-model-recognition-vmmr)
    - [Vehicle Body Style Recognition (VBSR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-body-style-recognition-vbsr)
    - [Vehicle Direction Tracking (VDT)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-direction-tracking-vdt)
    - [Vehicle Speed Estimation (VSE)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-speed-estimation-vse)
    
 * [TF-TRT](https://docs.nvidia.com/deeplearning/frameworks/tf-trt-user-guide/index.html) is used for:
    - [License Plate Recognition (LPR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-recognition-lpr)
    
[NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) is natively supported by all Jetson devices once flashed with [Jetpack](https://developer.nvidia.com/embedded/jetpack) while [TF-TRT](https://docs.nvidia.com/deeplearning/frameworks/tf-trt-user-guide/index.html) requires [TensorFlow](https://www.tensorflow.org/) binaries with TensorRT support. 
You don't need to worry about building Tensorflow with support for TensorRT by yourself, this repo contains all required binaries.

<a name="getting-started_requirements"></a>
## Requirements ##
**We require CUDA 10.2, cuDNN 8.0 and TensorRT 7+.** To make your life easier, just install [JetPack 4.4.1](https://developer.nvidia.com/embedded/jetpack). As of today (11/16/2020), version 4.4.1 is the latest one.

<a name="getting-started_before-trying-to-use-the-sdk-on-jetson"></a>
## Before trying to use the SDK on Jetson ##
Please note that this repo doesn't contain optimized TensorRT models and **you'll not be able to use the SDK** unless you generate these models. More info about model optimization at https://docs.nvidia.com/deeplearning/tensorrt/developer-guide/index.html. Fortunately we made this task very easy by writing an optimizer using TensorRT C++.

<a name="getting-started_before-trying-to-use-the-sdk-on-jetson_building-optimized-models"></a>
<a name="building-optimized-models"></a>
### Building optimized models ###
This process will write the optimized models (a.k.a plans) to the local disk which means we'll need write permission. We recommend running the next commands as root(#) instead of normal user($).
To generate the optimized models:
 - Navigate to the jetson binaries folder: `cd ultimateALPR-SDK/binaries/jetson/aarch64` or `cd ultimateALPR-SDK/binaries/jetson_tftrt/aarch64`
 - Generate the optimized models: `sudo chmod +x ./prepare.sh && sudo ./prepare.sh`
 
This will build the models using CUDA engine and serialize the optimized models into [assets/models.tensorrt/optimized](assets/models.tensorrt/optimized). Please note that **the task will last several minutes and you must be patient**. Next time you run this task it will be faster as only newest models will be generated. So, you can interrupt the process and next time it will continue from where it ended the last time.

For [binaries/jetson_tftrt](binaries/jetson_tftrt) the [prepare.sh](binaries/jetson_tftrt/aarch64/prepare.sh) script will also download Tensorflow libraries which means you'll need internet connection. You'll only need to run the script once.

Models generated on a Jetson device with [Compute Capabilities](https://developer.nvidia.com/cuda-gpus) X and TensorRT version Y will only be usable on devices matching this configuration. For example, **you'll not be able to use models generated on Jetson TX2 ([Compute Capabilities](https://developer.nvidia.com/cuda-gpus) 6.2) on a Jetson nano ([Compute Capabilities](https://developer.nvidia.com/cuda-gpus) 5.3)**.

<a name="getting-started_jetson-versus-jetsontftrt"></a>
## [binaries/jetson](binaries/jetson) versus [binaries/jetson_tftrt](binaries/jetson_tftrt) ##
If you navigate to the [binaries](binaries) you'll see that there are 2 **'jetson'** folders: [binaries/jetson](binaries/jetson) and [binaries/jetson_tftrt](binaries/jetson_tftrt).

| Feature | [binaries/jetson](binaries/jetson) | [binaries/jetson_tftrt](binaries/jetson_tftrt) |
|---------|--------|------------ |
| License plate and car detection | [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) GPGPU acceleration | [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) GPGPU acceleration |
| [License Plate Country Identification (LPCI)](https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-country-identification-lpci) | [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) GPGPU acceleration | [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) GPGPU acceleration |
| [Vehicle Color Recognition (VCR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-color-recognition-vcr) | [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) GPGPU acceleration | [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) GPGPU acceleration |
| [Vehicle Make Model Recognition (VMMR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-make-model-recognition-vmmr) | [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) GPGPU acceleration | [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) GPGPU acceleration |
| [Vehicle Body Style Recognition (VBSR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-body-style-recognition-vbsr) | [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) GPGPU acceleration | [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) GPGPU acceleration |
| [Vehicle Direction Tracking (VDT)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-direction-tracking-vdt) | [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) GPGPU acceleration | [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) GPGPU acceleration |
| [Vehicle Speed Estimation (VSE)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-speed-estimation-vse) | [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) GPGPU acceleration | [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) GPGPU acceleration |
| [License Plate Recognition (LPR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-recognition-lpr) | **CPU** | [TF-TRT](https://docs.nvidia.com/deeplearning/frameworks/tf-trt-user-guide/index.html) GPGPU acceleration (requires Tensorflow C++ libraries built with CUDA 10.2, cuDNN 8.0 and TensorRT 7+) |

**To make it short:** Both versions use [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) GPGPU acceleration for the detection and classification while only [binaries/jetson_tftrt](binaries/jetson_tftrt) uses GPGPU acceleration for [License Plate Recognition (LPR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-recognition-lpr) (a.k.a OCR).

[binaries/jetson](binaries/jetson) is very fast when [parallel mode](https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html) is enabled as we'll perform the detection and classification on GPU and the recognition/OCR on CPU. [binaries/jetson_tftrt](binaries/jetson_tftrt) is faster as all operations (detection, classification, OCR...) are done on GPU.

For now we have failed to convert the [License Plate Recognition (LPR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-recognition-lpr) model to [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) and this is why we're using [TF-TRT](https://docs.nvidia.com/deeplearning/frameworks/tf-trt-user-guide/index.html) which comes with many issues: large binary size, high memory usage, slow load and initialization... We're working to have [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) for all models and completly remove Tensorflow.

<a name="getting-started_jetson-versus-jetsontftrt_pros-and-cons"></a>
### Pros and Cons ###
 - [binaries/jetson](binaries/jetson)
    - Pros:
       - Low memory usage (~20% on Jetson Nano)
       - Fast load and initialization
    - Cons:
       - High CPU usage (> 300% out of 400% on Jetson Nano)
       - Lower frame rate when there are license plates on the image. [License Plate Recognition (LPR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-recognition-lpr) **NOT** GPGPU accelerated
       
       
 - [binaries/jetson_tftrt](binaries/jetson_tftrt)
     - Pros:
       - Low CPU usage (< 100% out of 400% on Jetson Nano)
       - Higher frame rate when there are license plates on the image. [License Plate Recognition (LPR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-recognition-lpr) **IS** GPGPU accelerated using [TF-TRT](https://docs.nvidia.com/deeplearning/frameworks/tf-trt-user-guide/index.html)
    - Cons:
       - High memory usage (~50% on Jetson Nano). [TF-TRT](https://docs.nvidia.com/deeplearning/frameworks/tf-trt-user-guide/index.html) binaries are >500Mo and this doesn't help.
       - Slow load and initialization. For now we cannot generated the optimized models for the OCR part using [TF-TRT](https://docs.nvidia.com/deeplearning/frameworks/tf-trt-user-guide/index.html), the models are built and optimized at runtime before inference.

To check GPU and CPU usage: `/usr/bin/tegrastats`

<a name="getting-started_jetson-versus-jetsontftrt_recommendations"></a>
### Recommendations ###

We recommend using [binaries/jetson](binaries/jetson) for your devs as it loads very fast and switch to [binaries/jetson_tftrt](binaries/jetson_tftrt) for production. [binaries/jetson_tftrt](binaries/jetson_tftrt) may be slow to load and initialize but once it's done the frame rate is higher.

On Jetson Xavier AGX, [binaries/jetson](binaries/jetson) may be faster than [binaries/jetson_tftrt](binaries/jetson_tftrt). Check [issue #128](https://github.com/DoubangoTelecom/ultimateALPR-SDK/issues/128) on why.

If [binaries/jetson](binaries/jetson) is still slow to load and initialize, then use [binaries/linux/aarch64]([binaries/linux/aarch64) which are a very light binaries using Tensorflow Lite (less than 13Mo total size).

<a name="benchmark"></a>
# Benchmark #
Here are some benchmark numbers to compare the speed. For more information about the **positive rate**, please check https://www.doubango.org/SDKs/anpr/docs/Benchmark.html. The benchmark application is open source and could be found at [samples/c++/benchmark](samples/c%2B%2B/benchmark).

Before running the benchmark application:
 - For Jetson nano, make sure you're using a Barrel Jack (5V-4A) power supply instead of microUSB port (5V-2A)
 - Put the device on maximum performance mode: `sudo nvpmodel -m 0 && sudo jetson_clocks`.


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
| **[binaries/jetson_tftrt](binaries/jetson_tftrt)<br/> (Xavier NX, JetPack 4.4.1)** | 657 millis <br />**152.06 fps** | 967 millis <br/> 103.39 fps | 1280 millis <br/> 78.06 fps | 1539 millis <br/> 64.95 fps | 1849 millis <br/> 54.07 fps |
| **[binaries/jetson](binaries/jetson)<br/> (Xavier NX, JetPack 4.4.1)** | 657 millis <br />**152.02 fps** | 1169 millis <br/> 85.47 fps | 2112 millis <br/> 47.34 fps | 2703 millis <br/> 36.98 fps | 3628 millis <br/> 27.56 fps |
| **[binaries/linux/aarch64](binaries/linux/aarch64)<br/> (Xavier NX, JetPack 4.4.1)** | 7498 millis <br />**13.33 fps** | 8281 millis <br/> 12.07 fps | 9421 millis <br/> 10.61 fps | 10161 millis <br/> 9.84 fps | 11006 millis <br/> 9.08 fps |
| **[binaries/jetson_tftrt](binaries/jetson_tftrt)<br/> (TX2, JetPack 4.4.1)** | 1420 millis <br />**70.38 fps** | 1653 millis <br/> 60.47 fps | 1998 millis <br/> 50.02 fps | 2273 millis <br/> 43.97 fps | 2681 millis <br/> 37.29 fps |
| **[binaries/jetson](binaries/jetson)<br/> (TX2, JetPack 4.4.1)** | 1428 millis <br />**70.01 fps** | 1712 millis <br/> 58.40 fps | 2165 millis <br/> 46.17 fps | 2692 millis <br/> 37.13 fps | 3673 millis <br/> 27.22 fps |
| **[binaries/linux/aarch64](binaries/linux/aarch64)<br/> (TX2, JetPack 4.4.1)** | 4591 millis <br />**21.77 fps** | 4722 millis <br/> 21.17 fps | 5290 millis <br/> 18.90 fps | 7154 millis <br/> 13.97 fps | 10032 millis <br/> 9.96 fps |
| **[binaries/jetson_tftrt](binaries/jetson_tftrt)<br/> (Nano, JetPack 4.4.1)** | 3106 millis <br />**32.19 fps** | 3292 millis <br/> 30.37 fps | 3754 millis <br/> 26.63 fps | 3967 millis <br/> 25.20 fps | 4621 millis <br/> 21.63 fps |
| **[binaries/jetson](binaries/jetson)<br/> (nano, JetPack 4.4.1)** | 2920 millis <br />**34.24 fps** | 3083 millis <br/> 32.42 fps | 3340 millis <br/> 29.93 fps | 3882 millis <br/> 25.75 fps | 5102 millis <br/> 19.59 fps |
| **[binaries/linux/aarch64](binaries/linux/aarch64)<br/> (Nano, JetPack 4.4.1)** | 4891 millis <br />**20.44 fps** | 6950 millis <br/> 14.38 fps | 9928 millis <br/> 10.07 fps | 11892 millis <br/> 8.40 fps | 14870 millis <br/> 6.72 fps |

You can notice that [binaries/jetson](binaries/jetson) and [binaries/jetson_tftrt](binaries/jetson_tftrt) have the same fps when positive rate is 0.0 (no plate in the stream) but the gap widen when the rate increase (more plates in the stream). This can be explained by the fact that both use [NVIDIA TensorRT](https://developer.nvidia.com/tensorrt) to accelerate the license plate and car detection but only [binaries/jetson_tftrt](binaries/jetson_tftrt) uses GPGPU acceleration for [License Plate Recognition (LPR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-recognition-lpr) (thanks to [TF-TRT](https://docs.nvidia.com/deeplearning/frameworks/tf-trt-user-guide/index.html)).

[binaries/linux/aarch64](binaries/linux/aarch64) contains generic Linux binaries for AArch64 (a.k.a ARM64) devices. All operations are done on CPU. The performance boost between this CPU-only version and the Jetson-based ones may not seem impressive but there is a good reason: [binaries/linux/aarch64](binaries/linux/aarch64) uses INT8 inference while the Jetson-based versions use a mix of FP32 and FP16 **which means more accurate**. Providing INT8 models for Jetson devices is on our roadmap with no ETA.

<a name="jetson-nano-versus-Raspberry-Pi-4"></a>
# Jetson nano versus Raspberry Pi 4 #
**On average the SDK is 3 times faster on Jetson nano compared to Raspberry Pi 4** and this may not seem impressive but there is a good reason: [binaries/raspbian/armv7l](binaries/raspbian/armv7l) uses INT8 inference while the Jetson-based binaries ([binaries/jetson](binaries/jetson) and [binaries/jetson_tftrt](binaries/jetson_tftrt)) use a mix of FP32 and FP16 **which means more accurate**. Providing INT8 models for Jetson devices is on our roadmap with no ETA.

<a name="jetson-nx-versus-jetso-tx2"></a>
# Jetson Xavier NX versus Jetson TX2 #
Jetson Xavier NX and Jetson TX2 are proposed at the same price ($399) but **NX has 4.6 times more compute power than TX2 for FP16**: *6 TFLOPS versus 1.3 TFLOPS*.

**We highly recommend using Xavier NX instead of TX2.**

- NX (€342): https://www.amazon.com/NVIDIA-Jetson-Xavier-Developer-812674024318/dp/B086874Q5R
- TX2: (€343): https://www.amazon.com/NVIDIA-945-82771-0000-000-Jetson-TX2-Development/dp/B06XPFH939

<a name="pre-processing-operations"></a>
# Pre-processing operations #
Please note that even when your're using [binaries/jetson_tftrt](binaries/jetson_tftrt) some pre-processing operations are performed on CPU and this why the CPU usage is at 1/5th. You don't need to worry about these operations, they are massively multi-threaded and entirely written in assembler with **SIMD NEON** acceleration. These functions are open source and you can find them at:
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

<a name="known-issues-and-possible-fixes_failed-to-open-file"></a>
## Failed to open file ##
You may receive `[UltAlprSdkTRT] Failed to open file` error after running `./prepare.sh` script if we fail to write to the local disk. We recommend running the script as root(#) instead of normal user($). 

<a name="known-issues-and-possible-fixes_slow-load-and-initialization"></a>
## Slow load and initialization ##
When your're using [binaries/jetson_tftrt](binaries/jetson_tftrt) the OCR models are built using CUDA engines at runtime before running the inference. Building the models is very slow and not suitable in dev stage. We recommend using [binaries/jetson](binaries/jetson) for your devs as it loads very fast and switch to [binaries/jetson_tftrt](binaries/jetson_tftrt) for production. [binaries/jetson_tftrt](binaries/jetson_tftrt) may be slow to load and initialize but once it's done the frame rate is higher.

<a name="known-issues-and-possible-fixes_high-memory-usage"></a>
## High memory usage ##
Disabling [parallel mode](https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html) alone could decrease the memory usage by 50%. Off course disabling [parallel mode](https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html) will slowdown the frame rate by up to 60%.

[binaries/jetson_tftrt](binaries/jetson_tftrt) is the faster version but it depends on [TF-TRT](https://docs.nvidia.com/deeplearning/frameworks/tf-trt-user-guide/index.html) which is very large (>500Mo). Try with [binaries/jetson](binaries/jetson) which is very small (less than 13Mo total size).

<a name="known-issues-and-possible-fixes_high-cpu-usage"></a>
## High CPU usage ##
[binaries/jetson](binaries/jetson) uses the CPU for the OCR part. Use [binaries/jetson_tftrt](binaries/jetson_tftrt) for full GPGPU acceleration to significantly reduce CPU usage.

<a name="technical-questions"></a>
# Technical questions #
Please check our [discussion group](https://groups.google.com/forum/#!forum/doubango-ai) or [twitter account](https://twitter.com/doubangotelecom?lang=en)
