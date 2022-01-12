- Not all files in this folder are required. It depends on your CPU type.

- All models listed here (**.model.doubango**) are useless on NVIDIA Jetson (ARM). The models for the NVIDIA Jetson devices are under [models.tensorrt](../models.tensorrt).

# All charsets #

|  | ARM | x86 |comment
|-------- | --- | --- | ---|
| ultimateALPR-SDK_detect_main.desktop.model.doubango | No | **Yes** | **Always required** |
| ultimateALPR-SDK_detect_pysearch.desktop.model.doubango | No | **Yes** | **Always required** |
| ultimateALPR-SDK_detecti_main.mobile.model.doubango | **Yes** | No | **Always required** |
| ultimateALPR-SDK_detecti_pysearch.mobile.model.doubango | **Yes** | No | **Always required** |
| ultimateALPR-SDK_klass_labels_lpci.txt.doubango | **Yes** | **Yes** | Only if you want [License Plate Country Identification (LPCI)](https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-country-identification-lpci) |
| ultimateALPR-SDK_klass_lpci.desktop.model.doubango | No | **Yes** | Only if you want [License Plate Country Identification (LPCI)](https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-country-identification-lpci) |
| ultimateALPR-SDK_klassi_lpci.mobile.model.doubango | **Yes** | No | Only if you want [License Plate Country Identification (LPCI)](https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-country-identification-lpci) |
| ultimateALPR-SDK_klass_labels_vcr.txt.doubango | **Yes** | **Yes** | Only if you want [Vehicle Color Recognition (VCR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-color-recognition-vcr) |
| ultimateALPR-SDK_klass_vcr.desktop.model.doubango | No | **Yes** | Only if you want [Vehicle Color Recognition (VCR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-color-recognition-vcr) |
| ultimateALPR-SDK_klassi_vcr.mobile.model.doubango | **Yes** | No | Only if you want [Vehicle Color Recognition (VCR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-color-recognition-vcr) |
| ultimateALPR-SDK_klass_labels_vmmr.txt.doubango | **Yes** | **Yes** | Only if you want [Vehicle Make Model Recognition (VMMR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-make-model-recognition-vmmr) |
| ultimateALPR-SDK_klass_vmmr.desktop.model.doubango | No | **Yes** | Only if you want [Vehicle Make Model Recognition (VMMR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-make-model-recognition-vmmr) |
| ultimateALPR-SDK_klassi_vmmr.mobile.model.doubango | **Yes** | No | Only if you want [Vehicle Make Model Recognition (VMMR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-make-model-recognition-vmmr) |
| ultimateALPR-SDK_klass_labels_vbsr.txt.doubango | **Yes** | **Yes** | Only if you want [Vehicle Body Style Recognition (VBSR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-body-style-recognition-vbsr) |
| ultimateALPR-SDK_klass_vbsr.desktop.model.doubango | No | **Yes** | Only if you want [Vehicle Body Style Recognition (VBSR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-body-style-recognition-vbsr) |
| ultimateALPR-SDK_klassi_vbsr.mobile.model.doubango | **Yes** | No | Only if you want [Vehicle Body Style Recognition (VBSR)](https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-body-style-recognition-vbsr) |


# Latin ([A-Z0-9]) charsets #

|  | ARM | x86 |
|-------- | --- | --- |
| charset_anpr_latin_size=37.txt | **Yes** | **Yes** |
| ultimateALPR-SDK_recogn2x150_latin.desktop.model.doubango | No | **Yes** |
| ultimateALPR-SDK_recogn1x100_latin.desktop.model.doubango | **Yes** | No |

# Korean charsets #

|  | ARM | x86 |
|-------- | --- | --- |
| charset_anpr_korean_size=78.txt | **Yes** | **Yes** |
| ultimateALPR-SDK_recogn2x150_korean.desktop.model.doubango | No | **Yes** |
| ultimateALPR-SDK_recogn1x100_korean.desktop.model.doubango | **Yes** | No |

# Chinese charsets #

|  | ARM | x86 |
|-------- | --- | --- |
| charset_anpr_chinese_size=73.txt | **Yes** | **Yes** |
| ultimateALPR-SDK_recogn2x150_chinese.desktop.model.doubango | No | **Yes** |
| ultimateALPR-SDK_recogn1x100_chinese.desktop.model.doubango | **Yes** | No |

