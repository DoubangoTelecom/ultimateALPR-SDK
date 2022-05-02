
- [Operating system](#Operating-system)
- [Enabling NPU acceleration](#Enabling-NPU-acceleration)
- [Benchmarking](#Benchmarking)
  - [Check list](#Benchmarking_Check-list)
  - [Running on Khadas VIM3 Basic edition](#Benchmarking_Running)
    - [Performance numbers](#Benchmarking_Running_perf)

<hr />

We have added support for Amlogic NPUs ([Neural Processing Unit](https://en.wikichip.org/wiki/neural_processor)) acceleration in version v3.9.0.
You'll be amazed to see UltimateALPR **running at up to 64fps** (High Definition[HD/720p] resolution) on a $99 ARM device ([Khadas VIM3](https://www.khadas.com/vim3)).
The engine can **run at up to 90fps** on low resolution images.

This guide will focus on how to use UltimateALPR on Kadas VIM3 but any [SBC (Single Board Computer)](https://en.wikipedia.org/wiki/Single-board_computer) with Amlogic NPU will work fine (e.g. [Banana Pi](https://www.banana-pi.org/)).

<a name="Operating-system"></a>
# Operating system #

Your Khadas VIM3 will likely come with an Android 9 installed on the eMMC. Unfortunately that's a 32-bit Android OS and not suitable for high performance applications.
You'll need to install a Linux AArch64 OS from Khadas website: https://docs.khadas.com/linux/firmware/Vim3UbuntuFirmware.html.
We're using version 4.9 (https://dl.khadas.com/Firmware/VIM3/Ubuntu/SD_USB/VIM3_Ubuntu-server-focal_Linux-4.9_arm64_SD-USB_V1.0.9-211217.img.xz) but any version should work. 
Please note that the **Mainline Kernel images do not support NPU**, make sure to install the right Linux version (see above).

You don't need to override the Android OS from the eMMC, install the Linux OS on an external SD card. Your Khadas will choose the OS on the SD card at boot time. This is the healthiest way to test NPU acceleration on Linux without overwriting the OS on the eMMC. Once you're happy with the result you could install the Linux OS on the eMMC which is faster than the SD card (memory read/write). You just need to remove the SD card for the boot loader to choose Android (on the eMMC) again.

Make sure to upgrade your OS as explained at https://docs.khadas.com/linux/vim3/UpgradeSystem.html

When I run `uname -a` on my device I see `Linux Khadas 4.9.241 #8 SMP PREEMPT Sat Jan 8 09:27:25 CST 2022 aarch64 aarch64 aarch64 GNU/Linux`

<a name="Enabling-NPU-acceleration"></a>
# Enabling NPU acceleration #
To enable NPU acceleration:
- you'll need to set the JSON configuration entry [npu_enabled](https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#npu-enabled) to `true` (by default it's already set to true). This could be done by using command param `--npu_enabled true` when using the [recognizer](samples/c%2B%2B/recognizer) or the [benchmark](samples/c%2B%2B/benchmark) application.
- your hardware name must be listed in [supported_hardware.txt](assets/models.amlogic_npu/supported_hardware.txt) (case insensitive). If that's not the case, then edit the file to add it. To find your hardware name, run `cat /proc/cpuinfo | grep Hardware`

<a name="Benchmarking"></a>
# Benchmarking #
We'll run the [benchmark](samples/c%2B%2B/benchmark) sample application on Khadas VIM3 to see how fast UltimateALPR is on that device. We'll run the benchmark with and without NPU acceleration to see the boost.

<a name="Benchmarking_Check-list"></a>
## Check list ##
- make sure your device has enough power
- make sure your CPU isn't throttling or overheating: `cat /sys/devices/system/cpu/cpu*/cpufreq/cpuinfo_cur_freq`
- make sure that your CPU power management is `Performance` and not `Powersave`: `cat /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor`
- make sure to unplug your device and let it coool down if your performance numbers aren't as good as what we're reporting here

<a name="Benchmarking_Running"></a>
## Running on Khadas VIM3 Basic edition ##
The benchmark application is ran on Khadas VIM3 Basic edition (Linux 4.9) using a [720p (1280x720) image](assets/images/lic_us_1280x720.jpg). This is a large image (1280x720), you can try with smaller image to see how fast the engine would be.
Notice how fast the engine is when [parallel mode](https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html) is enabled. Please note that parallel mode isn't available on Python, you'll have to use C++, Java, C# or any other language.

To run the benchmark application with 0.2 positive rate (20% of the images will have plates) for 100 loops:
```
cd ulatimateALPR-SDK/binaries/linux/aarch64
chmod +x benchmark
LD_LIBRARY_PATH=.:$LD_LIBRARY_PATH ./benchmark \
    --positive ../../../assets/images/lic_us_1280x720.jpg \
    --negative ../../../assets/images/london_traffic.jpg \
    --assets ../../../assets \
    --npu_enabled true \
    --charset latin \
    --loops 100 \
    --rate 0.2 \
    --parallel true
```

- Change `--npu_enabled true` to enable/disable NPU acceleration
- Change `--parallel true` to enable/disable [parallel mode](https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html). `--parallel false` to use sequential mode insteal of parallel mode.

<a name="Benchmarking_Running_perf"></a>
### Performance numbers ###
|  | 0.0 rate | 0.2 rate | 0.5 rate | 0.7 rate | 1.0 rate |
|-------- | --- | --- | --- | --- | --- |
| **Khadas VIM3 Basic<br/> Linux 4.9, NPU, Parallel mode** | 1560 millis <br />**64.08 fps** | 1797 millis <br/> 55.63 fps | 1876 millis <br/> 53.29 fps | 2162 millis <br/> 46.25 fps | 2902 millis <br/> 34.45 fps |
| Khadas VIM3 Basic<br/> Linux 4.9, NPU, Sequential mode | 1776 millis <br />**56.30 fps** | 3443 millis <br/> 29.04 fps | 6009 millis <br/> 16.63 fps | 7705 millis <br/> 12.97 fps | 10275 millis <br/> 9.73 fps |
| Khadas VIM3 Basic<br/> Linux 4.9, CPU, Parallel mode | 4187 millis <br />**23.88 fps** | 4414 millis <br/> 22.65 fps | 4824 millis <br/> 20.72 fps | 5189 millis <br/> 19.26 fps | 5740 millis <br/> 17.42 fps |
| Khadas VIM3 Basic<br/> Linux 4.9, CPU, Sequential mode | 4184 millis <br />**23.89 fps** | 5972 millis <br/> 16.74 fps | 8513 millis <br/> 11.74 fps | 10258 millis <br/> 9.74 fps | 12867 millis <br/> 7.77 fps |


- **When [parallel mode](https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html) is enabled we'll perform detection using the NPU and OCR using the CPU in parallel.**
- **Notice how the [parallel mode](https://www.doubango.org/SDKs/anpr/docs/Parallel_versus_sequential_processing.html) is 4 times faster than the sequential mode when rate=1.0 (all 100 images have plates).**
