- [Prerequisite](#prerequisite)
- [Usage](#testing-usage)
- [Examples](#testing-examples)
- [Know issues](#testing-know-issues)

This application is used as reference code for developers to show how to use the Python bindings for the [C++ API](https://www.doubango.org/SDKs/anpr/docs/cpp-api.html) and could
be used to easily check the accuracy. The application accepts path to a JPEG/PNG/BMP file as input. This **is not the recommended** way to use the API. We recommend reading the data directly from the camera and feeding the SDK with the uncompressed **YUV data** without saving it to a file or converting it to RGB.

If you don't want to build this sample and is looking for a quick way to check the accuracy then, try
our cloud-based solution at [https://www.doubango.org/webapps/alpr/](https://www.doubango.org/webapps/alpr/).

This sample is open source and doesn't require registration or license key.

<a name="prerequisite"></a>
# Prerequisite #

[**You must build the Python extension**](../../../python/README.md) before trying to run this sample. More information on how to build the extension could be found [here](../../../python/README.md)

<a name="testing-usage"></a>
# Usage #

`recognizer.py` is a Python command line application with the following usage:
```
recognizer.py \
      --image <path-to-image-with-plate-to-process> \
      [--assets <path-to-assets-folder>] \
      [--charset <recognition-charset:latin/korean/chinese>] \
      [--tokenfile <path-to-license-token-file>] \
      [--tokendata <base64-license-token-data>]
```
Options surrounded with **[]** are optional.
- `--image` Path to the image(JPEG/PNG/BMP) to process. You can use default image at [../../../assets/images/lic_us_1280x720.jpg](../../../assets/images/lic_us_1280x720.jpg).
- `--assets` Path to the [assets](../../../assets) folder containing the configuration files and models. Default value is the current folder.
- `--charset` Defines the recognition charset (a.k.a alphabet) value (latin, korean, chinese...). Default: *latin*.
- `--tokenfile` Path to the file containing the base64 license token if you have one. If not provided then, the application will act like a trial version. Default: *null*.
- `--tokendata` Base64 license token if you have one. If not provided then, the application will act like a trial version. Default: *null*.

<a name="testing-examples"></a>
# Examples #

You should navigate to the current folder (`ultimateALPR-SDK/samples/python/recognizer` ) before trying the next commands:
```
cd ultimateALPR-SDK/samples/python/recognizer
```

- For example, on **Raspberry Pi** you may call the recognizer application using the following command:
```
PYTHONPATH=../../../binaries/raspbian/armv7l:../../../python \
LD_LIBRARY_PATH=../../../binaries/raspbian/armv7l:$LD_LIBRARY_PATH \
python recognizer.py --image ../../../assets/images/lic_us_1280x720.jpg --assets ../../../assets 
```
- On **Linux x86_64**, you may use the next command:
```
PYTHONPATH=../../../binaries/linux/x86_64:../../../python \
LD_LIBRARY_PATH=../../../binaries/linux/x86_64:$LD_LIBRARY_PATH \
python recognizer.py --image ../../../assets/images/lic_us_1280x720.jpg --assets ../../../assets 
```
Before trying to run the program **you'll need to download libtensorflow.so as explained [here](../../c++/README.md#gpu-acceleration-tensorflow-linux)**

- On **Windows x86_64**, you may use the next command:
```
setlocal
set PYTHONPATH=../../../binaries/windows/x86_64;../../../python
python recognizer.py --image ../../../assets/images/lic_us_1280x720.jpg --assets ../../../assets
endlocal
```

Please note that if you're cross compiling the application then you've to make sure to copy the application and both the [assets](../../../assets) and [binaries](../../../binaries) folders to the target device.

<a name="testing-know-issues"></a>
# Know issues #
If you get `undefined symbol: PyUnicode_FromFormat` error message, then make sure you're using Python 3 and same version as the one used to buid the extension. We tested the code on version **3.6.9** (Windows 8), **3.6.8** (Ubuntu 18) and **3.7.3** (Raspbian Buster). Run `python --version` to print your Python version. You may use `python3` instead of `python` to make sure you're using version 3.


