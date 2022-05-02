
- [Files](#files)
  - [supported_hardware.txt](#files-supported_hardware)
  - [pid_serial_mapping.txt](#pid_serial_mapping)
- [Models-requirement](#models-requirement)

<hr />

Not all files in this folder are required. It depends on the [features](https://www.doubango.org/SDKs/anpr/docs/Features.html) you want to activate.

**This entire folder is useless if your hardware isn't Amlogic or you've disabled NPU ([Neural Processing Unit](https://en.wikichip.org/wiki/neural_processor)) acceleration.**

<a name="files"></a>
# Files

<a name="files-supported_hardware"></a>
## supported_hardware.txt
[This file](supported_hardware.txt) contains the list of hardware names (case insensitive) for which NPU ([Neural Processing Unit](https://en.wikichip.org/wiki/neural_processor)) acceleration will be enabled. Your hardware must be listed in this file. 
You can edit this file to include your hardware name. To get the name of your hardware: `cat /proc/cpuinfo | grep Hardware`.

<a name="pid_serial_mapping"></a>
## pid_serial_mapping.txt
[This file](pid_serial_mapping.txt) contains the mapping between the PID of the models and the 4-first digits of your serial number. More at [Model Transcoding and Running User Guide (1.0).pdf](https://github.com/khadas/aml_npu_sdk/blob/master/docs/en/Model%20Transcoding%20and%20Running%20User%20Guide%20(1.0).pdf) page 9.

| PID | serial (4-first digits) |
|-------- | --- |
|0x7D|290a|
|**0x88**|**290b**|
|0x99|2b0a|
|0xA1|300a|
|0xA1|300b|
|0x99|2f0a|
|0xB9|2f0b|
|0xBE|330a|
|0xBE|330b|
|0xE8|380a|
|0xE8|380b|

When I run `cat /proc/cpuinfo | grep Serial` on my Khadas VIM3 I get `Serial          : 290b100001111500001731343258****`, that means my 4-first digits are `290b` which means my PID is `0x88` which means my models will be inside [PID0x88](PID0x88) folder.
Please contact us via the [dev-group](https://groups.google.com/g/doubango-ai) if the folder correspoding to your PID is empty.

<a name="models-requirement"></a>
# Models requirement
| Folder | Requirement |
|-------- | --- |
| ultimateALPR-SDK_detect_main.mobile.model.amlogic.doubango | **Always required when NPU acceleration is enabled** |
| ultimateALPR-SDK_detect_pysearch.mobile.model.amlogic.doubango | **Always required when NPU acceleration is enabled** |
| ultimateALPR-SDK_klass_lpci.mobile.model.amlogic.doubango | Only if NPU acceleration is enabled and you want [License Plate Country Identification (LPCI)](https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-country-identification-lpci) |
| ultimateALPR-SDK_klass_vbsr.mobile.model.amlogic.doubango | Only if NPU acceleration is enabled and you want [Vehicle Color Recognition (VCR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-color-recognition-vcr) |
| ultimateALPR-SDK_klass_vcr.mobile.model.amlogic.doubango | Only if NPU acceleration is enabled and you want [Vehicle Make Model Recognition (VMMR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-make-model-recognition-vmmr) |
| ultimateALPR-SDK_klass_vmmr.mobile.model.amlogic.doubango | Only if NPU acceleration is enabled and you want [Vehicle Body Style Recognition (VBSR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-body-style-recognition-vbsr) |
