- [The issue](#container-issue)
- [The solution](#container-solution)
  - [Pull your image (optional)](#container-solution-pull)
  - [Run a container](#container-solution-run)
  - [Setup (inside the container)](#container-solution-setup)

<hr />

This document explains how to run a licensed version of the SDK inside a container. You can ignore it if you're using the trial version.

As explained at [https://www.doubango.org/pricing.html](https://www.doubango.org/pricing.html) our licensing model is per device/machine. 
Each machine is uniquely identified using the hardware information (CPU model, motherboard, architecture, hard drive serial number...). The hardware information doesn't change even if the OS is (up/down)-graded or reinstalled. We don't use network information like the MAC address to make sure the SDK can work without [NIC](https://en.wikipedia.org/wiki/Network_interface_controller).

[The machine's unique identifier](https://www.doubango.org/SDKs/LicenseManager/docs/Jargon.html#runtime-key) is built from the hardware information and generated as base64 encrypted key using the [runtimeKey](samples/c++/runtimeKey/) application. You don't need to build the application by yourself, use the pre-built [binaries](binaries).
Once you have the [runtime key](https://www.doubango.org/SDKs/LicenseManager/docs/Jargon.html#runtime-key) you can generate the [token (license)](https://www.doubango.org/SDKs/LicenseManager/docs/Jargon.html#token) using the [activation](https://www.doubango.org/SDKs/LicenseManager/docs/Activation_use_cases.html) function. More information about the process at [https://www.doubango.org/SDKs/LicenseManager/docs/](https://www.doubango.org/SDKs/LicenseManager/docs/).

This document is about using [docker](https://www.docker.com/) containers on Ubuntu 18. Please [contact us](https://www.doubango.org/#contact) for any other scenario.

<a name="container-issue"></a>
# The issue #
The problem with a container or any virtual machine is that you don't have access to the devices (hard drive, usb...) attached to the host machine.
It's possible to run a container as super user using `--privileged` option in order to have complet access to the host but we are avoiding it for obvious reasons.

<a name="container-solution"></a>
# The solution #
A container can have access to the CPU information used by our license manager but not to the information related to the hard drive or motherboard. The solution is to give a container the rights to have access to the missing information. 
**We only provide read only access for security reasons and we highly recommend running the container as non root user.**

We consider you already have [docker](https://www.docker.com/) correctly installed. If not, we recommend the guide at https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-on-ubuntu-18-04.

<a name="container-solution-pull"></a>
## Pull your image (optional) ##
Only required if you don't already have your own image. In our case we are using `ubuntu` image.

```
docker pull ubuntu
```
To check that the image is correctly downloaded: `docker images`.

<a name="container-solution-run"></a>
## Run a container ##
To run a container using the previously downloaded image (`ubuntu`) you'll need some additional parameters.
The command:
```
docker run -v /run/udev:/run/udev:ro -v /dev:/dev:ro -it ubuntu
```
- `-v /run/udev:/run/udev:ro`: The format is "name of the volume to mount:the destination:the access rights". In this case we need `ro` (read-only) access rights. More info at https://docs.docker.com/storage/volumes/
- `-v /dev:/dev:ro`: See above

In short, we want to allow the [udev](https://en.wikipedia.org/wiki/Udev) tool running inside a container to get access to the hardware information on the host. [udev](https://en.wikipedia.org/wiki/Udev) runs on userspace and doesn't require root access. The SDK will run `udevadm info --query=property --name=...` to retrieve hardware information. You can run the same command inside the container to check if the SDK will succeed.

<a name="container-solution-setup"></a>
## Setup (inside the container) ##
- Update:
```
apt update
```
- Install [udev](https://en.wikipedia.org/wiki/Udev):
```
apt install udev
```

That's it, now you're ready to run the [runtimeKey](samples/c++/runtimeKey/) application to generate the [runtime key](https://www.doubango.org/SDKs/LicenseManager/docs/Jargon.html#runtime-key) for [activation](https://www.doubango.org/SDKs/LicenseManager/docs/Activation_use_cases.html).
