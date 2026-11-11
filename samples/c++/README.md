 - [benchmark](benchmark) -> to measure the application performance (speed, memory usage...) on your CPU/GPU/NPU.
 - [recognizer](recognizer) -> to measure the application accuracy (lpr, lpci, vbsr, vmmr, vcr, night vision...).
 - [runtimeKey](runtimeKey) -> to generate a runtime key for your device in order to get a token/license.
 - [trt_optimizer](trt_optimizer) -> to generate TensorRT optimized models for NVIDIA GPUs (e.g. Jetson)

<a name="known-issues"></a>
# Known issues #
- On Linux you may get `[CompVSharedLib] Failed to load library with path=<...>libplugin_dl_xxx.so, Error: 0xffffffff`. Make sure to set `LD_LIBRARY_PATH` to add binaries folder to help the loader find all dependencies. You can also run `ldd libplugin_dl_xxx.so` to see which libraries are missing.
- On Linux you may get `'GLIBC_2.27' not found (required by <...>)`. This message means you're using an old glibc version. Update glibc or your OS to Ubuntu 18, Debian Buster... You can check your actual version by running `ldd --version`. 
