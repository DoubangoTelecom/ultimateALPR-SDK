This guide explains how to add support for acceleration on Qualcomm Hexagon Tensor Processor (HTP) hardware. We'll focus on a particular device ([Radxa Dragon Q6A](https://radxa.com/products/dragon/q6a/)) and particular operating system (**Linux**). This guide can be easily used for other devices and OSes. 
For **Android**, you can complement your documentation with information [published by Qualcomm](https://docs.qualcomm.com/nav/home/tutorial_c_application.html?product=924033590759186372#on-device-environment-setup).

- [1. Build and install FastRPC and make sure your hardware is supported](#1_choose_os)
- [2. Create your workspace and download QAIRT SDK `2.42.0.251225`](#2_create_workspace)
- [3. Get/Set your platform version](#3_getset_version)
- [4. Run the Benchmark app](#4_run_benchmark)
<hr />

<a name="1_choose_os"></a>
### 1. Choose you OS image ###

We're using [Radxa Dragon Q6A](https://radxa.com/products/dragon/q6a/) (QCS6490) device and the OS image at [https://github.com/radxa-build/radxa-dragon-q6a/releases/download/rsdk-r2/radxa-dragon-q6a_noble_gnome_r2.output_512.img.xz](https://github.com/radxa-build/radxa-dragon-q6a/releases/download/rsdk-r2/radxa-dragon-q6a_noble_gnome_r2.output_512.img.xz).
Running the command `uname -a` on our device produces `Linux radxa-dragon-q6a 6.18.2-3-qcom #3 SMP PREEMPT_DYNAMIC Sat Feb 14 07:41:12 UTC 2026 aarch64 aarch64 aarch64 GNU/Linux`.

### 1. Build and install FastRPC and make sure your hardware is supported ###
- run (install):
```console
sudo apt update
sudo apt-get install -y autoconf libtool libyaml-dev libbsd-dev
git clone https://github.com/quic/fastrpc
cd fastrpc
git checkout v1.0.9
./gitcompile
sudo make install
sudo ldconfig
sudo systemd-sysusers
```
- run (test):
```console
fastrpc_test
```
should get:
```shell-session
========================================
Test Summary:
  Total tests run:    3
  Passed:             3
  Failed:             0
  Skipped:            0
========================================

RESULT: All applicable tests PASSED
```
if not, it means there are issues in the communication bus with your HTP hardware. **You must fix it before moving to the next steps.**

<a name="2_create_workspace"></a>
### 2. Create your workspace and download QAIRT SDK `2.42.0.251225`  ###
**We highly recommend using the same version (`2.42.0.251225`).**

```console
sudo apt-get install -y unzip
mkdir /home/wsp && cd /home/wsp
export WSP=/home/wsp
export QAIRT_VERSION=2.42.0.251225
wget https://softwarecenter.qualcomm.com/api/download/software/sdks/Qualcomm_AI_Runtime_Community/All/${QAIRT_VERSION}/v${QAIRT_VERSION}.zip
unzip v${QAIRT_VERSION}.zip
export QAIRT_HOME=$WSP/qairt/$QAIRT_VERSION
export ABI=aarch64-oe-linux-gcc11.2
```

<a name="3_getset_version"></a>
### 3. Get/Set your platform version ###
running
```console
$QAIRT_HOME/bin/$ABI/qnn-platform-validator --backend dsp --coreVersion
```
will produce (on [Radxa Dragon Q6A](https://radxa.com/products/dragon/q6a/)):
```shell-session
Core Version      : Hexagon Architecture V68
```
So, our platform version is `V68`, set it:
```console
export HEXV=v68
```

<a name="4_run_benchmark"></a>
### 4. Run the Benchmark app ##
- documentation for the benchmark app is at [samples/c++/benchmark/README.md](samples/c++/benchmark/README.md)
- you can build the app by yourself or use the pre-built binary at [binaries/linux/aarch64/benchmark](binaries/linux/aarch64/benchmark)

It's very important to notice how the env vars `ADSP_LIBRARY_PATH` and `LD_LIBRARY_PATH` are set. You can update `~/.bashrc` file with these env vars if you don't want to set them at each run.
- run:
```console
cd binaries/linux/aarch64
chmod +x ./benchmark
### !!! next code is a single line !!! ###
ADSP_LIBRARY_PATH="$QAIRT_HOME/lib/hexagon-$HEXV/unsigned" \
LD_LIBRARY_PATH=.:$QAIRT_HOME/lib/$ABI:$QAIRT_HOME/lib/hexagon-$HEXV/unsigned:$LD_LIBRARY_PATH \
./benchmark --parallel true --batching_mode auto --backend tflite[ext=$QAIRT_HOME/lib/$ABI/libQnnTFLiteDelegate.so] \
--assets ../../../assets --image ../../../assets/images/lic_us_1280x720.jpg --loops 5000
```
 - you'll have a lot of warnnings when the deeplearning models are loaded:
```shell-session
[2] simple_tiler.cc:668:WARNING:SEAN, beginning for q::DepthwiseConvLayer_valid
[2] simple_tiler.cc:668:WARNING:SEAN, beginning for q::ConvLayer
[2] op_registry_prepare.cc:75:WARNING:Selecting disabled op for 0x592700000029 q::crouton_to_vtcm
[2] concat_opts.cc:200:WARNING:Disabling in place concat 0x5b44000001e4 ... dim 3 of input 1 not a multiple of 32
```
they will desapear after the first inference and you can safely ignore them.
- the result from the benchmark app should look like this:
```shell-session
*[ULTALPR_SDK INFO]: Elapsed time (Benchmark) = [[[ 15763.751637 millis ]]]
*[ULTALPR_SDK INFO]: result: {"frame_id":5029,"latency":3,"duration":0}
*[ULTALPR_SDK INFO]: *** elapsedTimeInMillis: 15763.751637, notified: 5000/5000, estimatedFps: 317.183378 ***
```

`317 fps` is impressive but not enought based on the specs. Qualcomm QCS6490 advertises a 12 TOPS NPU while Rockchip RK3588 has a 6 TOPS. 
Our benchmark numbers show that the SDK runs at [`310 fps`](https://www.doubango.org/SDKs/anpr/docs/Benchmark.html#orangepi-5-rk3588-npu) on Orange Pi5 which means it should reach the double (`600+ fps`) on QCS6490. Two possibilities:
  - 1. the NPU/GPU vendors do not use the same metrics to measure the numer of TOPS
  - 2. or, TfLite delegate isn't that efficient.
  
IMO option [i] is the most likely because Jetson Orin Nano runs at [472 fps](https://www.doubango.org/SDKs/anpr/docs/Benchmark.html#nvidia-jetson) with an advertised 40 TOPS (INT8) GPU and a solid/clean TensorRT optimized plans. FLOAT32 models/plans are used on Orin but we also tested INT8 models and noticed marginal speed up.

At https://aihub.qualcomm.com/iot/models/yolov8_det Qualcomm shows NPU performance numbers using various frameworks (ONNX, Qualcomm AI Runtime, TfLite delegate...) and the results show that TfLite is the fastest. So, we're using the most efficient way to perform inference on Hexagon NPUs. **Disclaimer: we do NOT use YOLO.**

Contact the [developer group](https://groups.google.com/forum/#!forum/doubango-ai) if you have any question.
