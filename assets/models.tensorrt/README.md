This folder contains deep learning models for NVIDIA Jetson devices.

Not all files in this folder are required. It depends on the [features](https://www.doubango.org/SDKs/anpr/docs/Features.html) you want to activate.

**This entire folder is useless if you are not using an NVIDIA Jetson device.**

The models listed here are not usable, they must be built and optimized for your target platform. The final models (plans) will be generated inside the [optimized](optimized) folder.
More info [here](../../Jetson.md#building-optimized-models).

On production you'll only need:
  - [optimized](optimized)/*
  - **ultimateALPR-SDK_recogn1x100_latin.desktop.model.tensorrt.doubango** or **ultimateALPR-SDK_recogn1x100_korean.desktop.model.tensorrt.doubango** depending on your target charset
