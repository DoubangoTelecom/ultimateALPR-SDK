- [AWS issue](#aws-issue)
  - [Binding the license to the instance](#aws-solution-instance)
  - [Binding the license to the hardware](#aws-solution-byol)
- [Note about Microsoft Azure](#azure)

<hr />

This document explains how to run a licensed version of the SDK on [AWS (Amazon Web Services)](https://aws.amazon.com/) or [Microsoft Azure](https://azure.microsoft.com/en-us/). You can ignore it if you're using the trial version.

As explained at [https://www.doubango.org/pricing.html](https://www.doubango.org/pricing.html) our licensing model is per device/machine. 
Each machine is uniquely identified using the hardware information (CPU model, motherboard, architecture, hard drive serial number...). The hardware information doesn't change even if the OS is (up/down)graded or reinstalled. We don't use network information like the MAC address to make sure the SDK can work without [NIC](https://en.wikipedia.org/wiki/Network_interface_controller).

[The machine's unique identifier](https://www.doubango.org/SDKs/LicenseManager/docs/Jargon.html#runtime-key) is built from the hardware information and generated as base64 encrypted key using the [runtimeKey](samples/c++/runtimeKey/) application. You don't need to build the application by yourself, use the pre-built [binaries](binaries).
Once you have the [runtime key](https://www.doubango.org/SDKs/LicenseManager/docs/Jargon.html#runtime-key) you can generate the [token (license)](https://www.doubango.org/SDKs/LicenseManager/docs/Jargon.html#token) using the [activation](https://www.doubango.org/SDKs/LicenseManager/docs/Activation_use_cases.html) function. More information about the process at [https://www.doubango.org/SDKs/LicenseManager/docs/](https://www.doubango.org/SDKs/LicenseManager/docs/).

<a name="aws-issue"></a>
# AWS issue #
The problem with AWS or any virtual machine is that you don't control on which machine your instance will be launched. If your [license (Token)](https://www.doubango.org/SDKs/LicenseManager/docs/Jargon.html#token) was generated using a [runtime key](https://www.doubango.org/SDKs/LicenseManager/docs/Jargon.html#runtime-key) from **machine A** and your instance is launched on **machine B**, then the hardware information will not match.
To fix this issue we propose two possible solutions:
- 1/ Binding the license to the instance
- 2/ Binding the license to the hardware

<a name="aws-solution-instance"></a>
## Binding the license to the instance ##
The license is bound to the instance identifier and other information attached to it. Your license will remain valid even if the hardware change. 
The information attached to the instance will not change even if it is restarted but **it'll be lost if you terminate the instance.**

**/!\\IMPORTANT: The license will be definitely lost if you terminate the instance.**

To generate a [runtime key](https://www.doubango.org/SDKs/LicenseManager/docs/Jargon.html#runtime-key) for this option you need to run the [runtimeKey](samples/c++/runtimeKey/) sample application like this: 
```
./runtimeKey --type aws-instance --assets ../../../assets
```

The SDK uses [libcurl](https://curl.haxx.se/libcurl/) hunder the hood to query the information associated to the instance. [libcurl](https://curl.haxx.se/libcurl/) is loaded at runtime to avoid liking.

On Linux, install [libcurl](https://curl.haxx.se/libcurl/) like this: `sudo apt-get install libcurl-dev`


On Windows, copy [libcurl.dll](https://github.com/DoubangoTelecom/LicenseManager-SDK/blob/master/binaries/windows/x64/libcurl.dll) and [zlib1.dll](https://github.com/DoubangoTelecom/LicenseManager-SDK/blob/master/binaries/windows/x64/zlib1.dll) from https://github.com/DoubangoTelecom/LicenseManager-SDK/blob/master/binaries/windows/x64/ to your application's root folder or [binaries](binaries) folder.

<a name="aws-solution-byol"></a>
## Binding the license to the hardware ##
This is implemented through [Bring Your Own Licensing (BYOL)](https://aws.amazon.com/blogs/mt/simplified-byol-experience-using-aws-license-manager/) offer from Amazon. 
You'll need a dedicated host subscription. More information at [https://aws.amazon.com/blogs/mt/simplified-byol-experience-using-aws-license-manager/](https://aws.amazon.com/blogs/mt/simplified-byol-experience-using-aws-license-manager/).
There are addition subscription costs for this method but it guarantee the hardware information will never change.

To generate a [runtime key](https://www.doubango.org/SDKs/LicenseManager/docs/Jargon.html#runtime-key) for this option you need to run the [runtimeKey](samples/c++/runtimeKey/) sample application like this: 
```
./runtimeKey --type aws-byol --assets ../../../assets
```

<a name="azure"></a>
# Note about Microsoft Azure #
Everything explained above about Amazon AWS applies to Microsoft Azure. The only difference is how the runtime key is generated.

To attach the license to the Azure VM instead of the hardware (recommended):

```
./runtimeKey --type azure-instance --assets ../../../assets
```

To attach the license to the hardware instead of the VM:

```
./runtimeKey --type azure-byol --assets ../../../assets
```

You'll need libcurl. See above on how to install it.
