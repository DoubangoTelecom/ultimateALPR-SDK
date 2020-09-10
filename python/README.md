This SDK is developped using C++11 and comes with Python wrappers generated using SWIG.

There is no Python extension in the repository. Generating pre-built extension will force us to choose a specific Python version which means you'll be bound to this decision. 
To avoid being bound to a specific Python version we let the extension build task up to you. Building the extension is very easy and doesn't require any specific skill.

# C++ Compiler #
You'll need a C++ compiler. 

On Windows we recommend Visual Studio 2015 Community or later.

On Linux we recommend recent GCC/G++ version with support for C++11.

# Building #
You'll need Python, all versions are supported but ***we highly recommend 3.0 or later***. You'll also need **Cython** and **python-dev** packages. Other packages may be required but you can easily install them using **pip tool**.

On Windows we recommend using Anaconda.

Before building the extension you have to navigate to the folder containing the [binaries](../binaries):
```
cd ultimateALPR-SDK/binaries/<<os>>/<<arch>>
```
For example:
 - On Windows x86_64: [binaries/windows/x86_64](../binaries/windows/x86_64)
 - On Linux x86_64: [binaries/linux/x86_64](../binaries/linux/x86_64)
 - On Linux aarch64: [binaries/linux/aarch64](../binaries/linux/aarch64)
 - On Raspbian arm32 : [binaries/raspbian/armv7l](../binaries/raspbian/armv7l)
 - ... you got the idea
 
 From the the binaries folder (`ultimateALPR-SDK/binaries/<<os>>/<<arch>>`), call the [setup.py](setup.py) script:
 ```
 python ../../../python/setup.py build_ext --inplace -v
 ```
 If you have multiple python versions installed, then you may use a virtual env, a full path to the executable... Also, you may use `python3` instead of `python` to make sure you're using version 3. The python version used to build the extension should be the same as the one running the samples.
 
 This will build and install the extension in the current folder. The extension is named **_ultimateAlpr.pyd** on Windows and **_ultimateAlpr.so** on Linux.
 
 Now you're ready to run Python scripts using the extension. We highly recommend checking the recognizer sample: [ultimateALPR-SDK/samples/python/recognizer](../samples/python/recognizer)
 
 # Know issues #
 If you get `TypeError: super() takes at least 1 argument (0 given)` error message, then make sure you're using Python 3. We tested the code on version **3.6.9** (Windows 8), **3.6.8** (Ubuntu 18) and **3.7.3** (Raspbian Buster). Run `python --version` to print your Python version. You may use `python3` instead of `python` to make sure you're using version 3.
