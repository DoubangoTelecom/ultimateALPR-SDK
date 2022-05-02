'''
    * Copyright (C) 2011-2020 Doubango Telecom <https://www.doubango.org>
    * File author: Mamadou DIOP (Doubango Telecom, France).
    * License: For non commercial use only.
    * Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
    * WebSite: https://www.doubango.org/webapps/alpr/


    https://github.com/DoubangoTelecom/ultimateALPR/blob/master/SDK_dist/samples/c++/recognizer/README.md
	Usage: 
		recognizer.py \
			--image <path-to-image-with-plate-to-recognize> \
			[--assets <path-to-assets-folder>] \
            [--charset <recognition-charset:latin/korean/chinese>] \
			[--tokenfile <path-to-license-token-file>] \
			[--tokendata <base64-license-token-data>]
	Example:
		recognizer.py \
			--image C:/Projects/GitHub/ultimate/ultimateALPR/SDK_dist/assets/images/lic_us_1280x720.jpg \
            --charset "latin" \
			--assets C:/Projects/GitHub/ultimate/ultimateALPR/SDK_dist/assets \
			--tokenfile C:/Projects/GitHub/ultimate/ultimateALPR/SDK_dev/tokens/windows-iMac.lic
'''

import ultimateAlprSdk
import argparse
import json
import os.path

TAG = "[PythonRecognizer] "

# Defines the default JSON configuration. More information at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html
JSON_CONFIG = {
    "debug_level": "info",
    "debug_write_input_image_enabled": False,
    "debug_internal_data_path": ".",
    
    "num_threads": -1,
    "gpgpu_enabled": True,
    "max_latency": -1,

    "klass_vcr_gamma": 1.5,
    
    "detect_roi": [0, 0, 0, 0],
    "detect_minscore": 0.1,

    "car_noplate_detect_min_score": 0.8,
    
    "pyramidal_search_enabled": True,
    "pyramidal_search_sensitivity": 0.28,
    "pyramidal_search_minscore": 0.3,
    "pyramidal_search_min_image_size_inpixels": 800,
    
    "recogn_rectify_enabled": True,
    "recogn_minscore": 0.3,
    "recogn_score_type": "min"
}

IMAGE_TYPES_MAPPING = { 
        'RGB': ultimateAlprSdk.ULTALPR_SDK_IMAGE_TYPE_RGB24,
        'RGBA': ultimateAlprSdk.ULTALPR_SDK_IMAGE_TYPE_RGBA32,
        'L': ultimateAlprSdk.ULTALPR_SDK_IMAGE_TYPE_Y
}

# Load image
def load_pil_image(path):
    from PIL import Image, ExifTags, ImageOps
    import traceback
    pil_image = Image.open(path)
    img_exif = pil_image.getexif()
    ret = {}
    orientation  = 1
    try:
        if img_exif:
            for tag, value in img_exif.items():
                decoded = ExifTags.TAGS.get(tag, tag)
                ret[decoded] = value
            orientation  = ret["Orientation"]
    except Exception as e:
        print(TAG + "An exception occurred: {}".format(e))
        traceback.print_exc()

    if orientation > 1:
        pil_image = ImageOps.exif_transpose(pil_image)

    if pil_image.mode in IMAGE_TYPES_MAPPING:
        imageType = IMAGE_TYPES_MAPPING[pil_image.mode]
    else:
        raise ValueError(TAG + "Invalid mode: %s" % pil_image.mode)

    return pil_image, imageType

# Check result
def checkResult(operation, result):
    if not result.isOK():
        print(TAG + operation + ": failed -> " + result.phrase())
        assert False
    else:
        print(TAG + operation + ": OK -> " + result.json())

# Entry point
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="""
    This is the recognizer sample using python language
    """)

    parser.add_argument("--image", required=True, help="Path to the image with ALPR data to recognize")
    parser.add_argument("--assets", required=False, default="../../../assets", help="Path to the assets folder")
    parser.add_argument("--charset", required=False, default="latin", help="Defines the recognition charset (a.k.a alphabet) value (latin, korean, chinese...)")
    parser.add_argument("--car_noplate_detect_enabled", required=False, default=False, help="Whether to detect and return cars with no plate")
    parser.add_argument("--ienv_enabled", required=False, default=False, help="Whether to enable Image Enhancement for Night-Vision (IENV). More info about IENV at https://www.doubango.org/SDKs/anpr/docs/Features.html#image-enhancement-for-night-vision-ienv. Default: true for x86-64 and false for ARM.")
    parser.add_argument("--openvino_enabled", required=False, default=True, help="Whether to enable OpenVINO. Tensorflow will be used when OpenVINO is disabled")
    parser.add_argument("--openvino_device", required=False, default="CPU", help="Defines the OpenVINO device to use (CPU, GPU, FPGA...). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#openvino-device")
    parser.add_argument("--npu_enabled", required=False, default=True, help="Whether to enable NPU (Neural Processing Unit) acceleration")
    parser.add_argument("--klass_lpci_enabled", required=False, default=False, help="Whether to enable License Plate Country Identification (LPCI). More info at https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-country-identification-lpci")
    parser.add_argument("--klass_vcr_enabled", required=False, default=False, help="Whether to enable Vehicle Color Recognition (VCR). More info at https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-color-recognition-vcr")
    parser.add_argument("--klass_vmmr_enabled", required=False, default=False, help="Whether to enable Vehicle Make Model Recognition (VMMR). More info at https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-make-model-recognition-vmmr")
    parser.add_argument("--klass_vbsr_enabled", required=False, default=False, help="Whether to enable Vehicle Body Style Recognition (VBSR). More info at https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-body-style-recognition-vbsr")
    parser.add_argument("--tokenfile", required=False, default="", help="Path to license token file")
    parser.add_argument("--tokendata", required=False, default="", help="Base64 license token data")

    args = parser.parse_args()

    # Check if image exist
    if not os.path.isfile(args.image):
        raise OSError(TAG + "File doesn't exist: %s" % args.image)

     # Decode the image and extract type
    image, imageType = load_pil_image(args.image)
    width, height = image.size

    # Update JSON options using values from the command args
    JSON_CONFIG["assets_folder"] = args.assets
    JSON_CONFIG["charset"] = args.charset
    JSON_CONFIG["car_noplate_detect_enabled"] = (args.car_noplate_detect_enabled == "True")
    JSON_CONFIG["ienv_enabled"] = (args.ienv_enabled == "True")
    JSON_CONFIG["openvino_enabled"] = (args.openvino_enabled == "True")
    JSON_CONFIG["openvino_device"] = args.openvino_device
    JSON_CONFIG["npu_enabled"] = (args.npu_enabled == "True")
    JSON_CONFIG["klass_lpci_enabled"] = (args.klass_lpci_enabled == "True")
    JSON_CONFIG["klass_vcr_enabled"] = (args.klass_vcr_enabled == "True")
    JSON_CONFIG["klass_vmmr_enabled"] = (args.klass_vmmr_enabled == "True")
    JSON_CONFIG["klass_vbsr_enabled"] = (args.klass_vbsr_enabled == "True")
    JSON_CONFIG["license_token_file"] = args.tokenfile
    JSON_CONFIG["license_token_data"] = args.tokendata

    # Initialize the engine
    checkResult("Init", 
                ultimateAlprSdk.UltAlprSdkEngine_init(json.dumps(JSON_CONFIG))
               )

    # Recognize/Process
    # Please note that the first time you call this function all deep learning models will be loaded 
    # and initialized which means it will be slow. In your application you've to initialize the engine
    # once and do all the recognitions you need then, deinitialize it.
    checkResult("Process",
                ultimateAlprSdk.UltAlprSdkEngine_process(
                    imageType,
                    image.tobytes(), # type(x) == bytes
                    width,
                    height,
                    0, # stride
                    1 # exifOrientation (already rotated in load_image -> use default value: 1)
                )
        )

    # Press any key to exit
    input("\nPress Enter to exit...\n") 

    # DeInit the engine
    checkResult("DeInit", 
                ultimateAlprSdk.UltAlprSdkEngine_deInit()
               )
    
    