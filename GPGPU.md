- [NVIDIA TensorRT](#gpu-acceleration-tensorrt)
  - [Installation](#gpu-acceleration-tensorrt-install)
      - [Windows/Linux x86_64](#gpu-acceleration-tensorrt-install-x64)
      - [NVIDIA Jetson](#gpu-acceleration-tensorrt-install-jetson)
  - [Building plans](#gpu-acceleration-tensorrt-build)
      - [Windows/Linux x86_64](#gpu-acceleration-tensorrt-build-x64)
      - [NVIDIA Jetson](#gpu-acceleration-tensorrt-build-jetson)
  - [Testing](#gpu-acceleration-tensorrt-testing)
- [CUDA](#gpu-acceleration-cuda)
    - [Installation](#gpu-acceleration-cuda-install)
    - [Testing](#gpu-acceleration-cuda-testing)
<hr />

The SDK supports both CUDA and [TensorRT](https://developer.nvidia.com/tensorrt) for GPU acceleration. The later ([TensorRT](https://developer.nvidia.com/tensorrt)) is faster and preferred.

<a name="gpu-acceleration-tensorrt"></a>
# NVIDIA TensorRT #
This is the preferred framework for GPU acceleration on NVIDIA devices.

<a name="gpu-acceleration-tensorrt-install"></a>
## Installation ##

<a name="gpu-acceleration-tensorrt-install-x64"></a>
### Windows/Linux x86_64 ###
On Linux/Windows x86_64 we require TensorRT 10.x (tested with **TensorRT 10.16.1.11-cuda-12.9**). The NVIDIA website also offers TRT10 for CUDA 13.
We recommend using the `tar` (Linux) or the `zip` (Windows) versions as they don't require installation which means you'll not override your native TensorRT if you have one.
- Download the [tar/zip](https://developer.nvidia.com/downloads/compute/machine-learning/tensorrt/10.16.1/tars/TensorRT-10.16.1.11.Linux.x86_64-gnu.cuda-12.9.tar.gz) from https://developer.nvidia.com/nvidia-tensorrt-10x-download
- Unzip/untar [the file](https://developer.nvidia.com/downloads/compute/machine-learning/tensorrt/10.16.1/tars/TensorRT-10.16.1.11.Linux.x86_64-gnu.cuda-12.9.tar.gz) (e.g. under `/home/TensorRT-10.16.1.11`)

You'll need to use `LD_LIBRARY_PATH` envvar to tell the linker where to find TensorRT libraries required by [libplugin_dl_trt.so](../../binaries/linux/x86_64/libplugin_dl_trt.so). You can avoid using `LD_LIBRARY_PATH` by copying the TensorRT libraries to [binaries/linux/x86_64](../../binaries/linux/x86_64): `cp /home/TensorRT-10.16.1.11/lib/* binaries/linux/x86_64`.

To check if all dependencies can be found: 
  - Linux: `ldd libplugin_dl_trt.so`
  - Windows: Use [Dependency Walker](https://www.dependencywalker.com/) on [binaries/windows/x86_64/plugin_dl_trt.dll](../../../binaries/windows/x86_64/plugin_dl_trt.dll)

<a name="gpu-acceleration-tensorrt-install-jetson"></a>
### NVIDIA Jetson ###
We recommend putting the device in maximum performance mode (`sudo nvpmodel -m 2` on Jetson Nano Orin or `sudo nvpmodel -m 0` on Jetson Nano B01) before running the script.

To check if your NVIDIA Jetson is running in maximum performance mode, run `sudo nvpmodel -q` to view the active power mode and `sudo jetson_clocks --show` to verify if the clocks are locked at maximum frequency.

On Jetson, a single script is used to install and build the plans.
```
cd binaries/linux/aarch64
sudo sh ./prepare_jetson.sh
```

Building the plans will take **several minutes**, you must be patient. The console will give you a feedback `[X/Y]`, `X` being the current step and `Y` the total steps. You can stop the process and restart it later, it'll continue from the latest step.

You can ignore all warnings (`Severity >= 2`) from TensorRT as long as the process doesn't exit with errors (`Severity <= 1`).

You can generate the models on onde device and copy them to all your devices to avoid running the script again. **The devices must be identical** (e.g. models gnerated on Jetson Nano cannot be used on Jetson NX).

<a name="gpu-acceleration-tensorrt-build"></a>
### Building plans ###
The TensorRT optimized models (a.k.a plans) will be generated into [models.tensorrt folder](../../assets/models.tensorrt).

<a name="gpu-acceleration-tensorrt-build-x64"></a>
### Windows/Linux x86_64 ###

To build the plans:
```
cd binaries/linux/x86_64
sudo LD_LIBRARY_PATH=/home/TensorRT-10.16.1.11/lib:.:$LD_LIBRARY_PATH ./trt_optimizer --assets ../../../assets
```
Notice how we use `LD_LIBRARY_PATH` envvar to tell the linker where to find the TensorRT libs. An alternative would be copying the TensorRT libs to [binaries/linux/x86_64](../../binaries/linux/x86_64) as explained above.

This will take **several minutes**, you must be patient.

<a name="gpu-acceleration-tensorrt-build-jetson"></a>
### NVIDIA Jetson ###

Check [previous section](#gpu-acceleration-tensorrt-install-jetson).

<a name="gpu-acceleration-tensorrt-testing"></a>
## Testing ##

Run the [benchmark application](samples/c++/benchmark/README.md) like this:
 - x86_64: `LD_LIBRARY_PATH=../../../binaries/linux/x86_64:/home/TensorRT-10.16.1.11/lib:$LD_LIBRARY_PATH ./benchmark     --image "../../../assets/images/lic_us_1280x720.jpg"     --assets ../../../assets     --backend "trt"     --loops 5000     --parallel true`
 - aarch64 (jetson): `LD_LIBRARY_PATH=../../../binaries/linux/aarch64:$LD_LIBRARY_PATH ./benchmark --image "../../../assets/images/lic_us_1280x720.jpg" --assets ../../../assets --backend "trt" --loops 5000 --parallel true`

<a name="gpu-acceleration-cuda"></a>
# CUDA #

<a name="gpu-acceleration-cuda-install"></a>
## Installation ##

The deep learning models used in this SDK were developped using [Pytorch](https://pytorch.org/). All models were exported to [ONNX](https://onnx.ai/) for inferencing. 
We use [ONNX Runtime 1.21.0](https://github.com/microsoft/onnxruntime/releases/tag/v1.21.0) a.k.a **ONNX RT** as inference engine. The SDK should work with any later version but we'll only provide support if you're using 1.21.0.

This short guide explain how to enable CUDA acceleration for NVIDIA GPUs. We'll focus on **Ubuntu 20.04** but it's easy to adapt the guide for Windows.

The SDK is shipped with the CPU version of [ONNX RT 1.21.0](https://github.com/microsoft/onnxruntime/releases/tag/v1.21.0) in the [binaries folder](binaries). The GPU versions are too large to be pushed on Github. 
You'll have to pull the GPU version of ONNX RT to replace the CPU version.
```
cd binaries/linux/x86_64
wget https://github.com/microsoft/onnxruntime/releases/download/v1.21.0/onnxruntime-linux-x64-gpu-1.21.0.tgz
tar -xf onnxruntime-linux-x64-gpu-1.21.0.tgz
cp -rf onnxruntime-linux-x64-gpu-1.21.0/lib/* .
```

[ONNX Runtime 1.21.0](https://github.com/microsoft/onnxruntime/releases/tag/v1.21.0) requires **CUDA 12.x** and **cuDNN 9.x** according to https://onnxruntime.ai/docs/execution-providers/CUDA-ExecutionProvider.html.
We tested both cuDNN 9.8 and 9.11 and had `segmentation fault` with the later. So, we highly recommend using cuDNN 9.8 and making sure no other version is installed on your machine.

Using Anaconda on **Ubuntu 20.04**:
```
conda create -n test_env_onnx python=3.10
conda activate test_env_onnx

wget https://developer.download.nvidia.com/compute/cuda/12.1.0/local_installers/cuda_12.1.0_530.30.02_linux.run
sudo sh cuda_12.1.0_530.30.02_linux.run

wget https://developer.download.nvidia.com/compute/cudnn/9.8.0/local_installers/cudnn-local-repo-ubuntu2004-9.8.0_1.0-1_amd64.deb
sudo dpkg -i cudnn-local-repo-ubuntu2004-9.8.0_1.0-1_amd64.deb
sudo cp /var/cudnn-local-repo-ubuntu2004-9.8.0/cudnn-*-keyring.gpg /usr/share/keyrings/
sudo apt-get update
sudo apt-get -y install cudnn
```

The SDK will automatically enable CUDA when ONNX [backend](https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#backend) is selected. You can force the SDK to use CUDA by using `onnx[cuda]` as [backend](https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#backend).

<a name="gpu-acceleration-cuda-testing"></a>
## Testing ##

Use the [benchmark application](samples/c++/benchmark/README.md) with command line `--backend onnx[cuda]`.
