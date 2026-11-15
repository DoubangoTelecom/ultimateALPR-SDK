'''
    * Copyright (C) 2011-2026 Doubango Telecom <https://www.doubango.org>
    * File author: Mamadou DIOP (Doubango Telecom, France).
    * License: For non commercial use only.
    * Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
    * WebSite: https://www.doubango.org/webapps/alpr/


    https://github.com/DoubangoTelecom/ultimateALPR/blob/master/SDK_dist/samples/c++/recognizer/README.md
	Usage: 
		recognizer.py \
			--image <path-to-image-with-plate-to-recognize> \
			--assets <path-to-assets-folder> \
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
import os

TAG = "[PythonRecognizer] "

# Defines the default JSON configuration. More information at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html
JSON_CONFIG = {
    "debug_level": "info",
    "debug_write_input_image_enabled": False,
    "debug_internal_data_path": ".",
    
    "num_threads": -1,
    "max_latency": -1,
    "night_vision_enabled": False,
    "batching_mode": "auto",
    "backend": "auto",
    "platform": "auto",
    "asm_enabled": True,
    "intrin_enabled": True,

    "detect_roi": [0, 0, 0, 0],
    "detect_minscore": 0.4,
    "detect_miniou": 0.4,

    "pyramidal_search_enabled": True,
    "pyramidal_search_minscore": 0.2,
    "pyramidal_search_min_image_size_inpixels": 800,
    "pyramidal_search_miniou": 0.4,

    "klass_lpci_enabled": False,
    "klass_lpci_topk": 5,

    "klass_vcr_enabled": False,
    "klass_vcr_topk": 5,

    "klass_vmmr_enabled": False,
    "klass_vmmr_topk": 5,

    "klass_vbsr_enabled": False,
    "klass_vbsr_topk: 5,"
    
    "recogn_enabled": True,
    "recogn_charset": "latin",
    "recogn_score_type": "cumprod",
    "recogn_minscore": 0.1,
}

# Check result
def checkResult(operation :str, result :ultimateAlprSdk.UltAlprSdkResult):
    if not result.isOK():
        print(f'{TAG} {operation}: failed -> {result.phrase()}')
        raise Exception(f'Error: {result.phrase()}')
    else:
        print(f'{TAG} {operation}: ok -> {result.json()}')
    
    return result

# Async result callback for // processing: https://www.doubango.org/SDKs/anpr/docs/Parallel_processing.html
class MyParallelDeliveryCallback(ultimateAlprSdk.UltAlprSdkParallelDeliveryCallback):
    def onNewResult(self, result :ultimateAlprSdk.UltAlprSdkResult):
        checkResult("Async Process", result)
        return 0

# Entry point
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="""
    This is the recognizer sample using python language
    """)

    parser.add_argument("--image", required=True, help="Path to the image(JPEG/PNG/BMP) to process. You can use default image at ../../../assets/images/lic_us_1280x720.jpg.")
    parser.add_argument("--assets", required=True, help="Path to the assets folder containing the configuration files and models. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#assets-folder.")
    parser.add_argument("--charset", required=False, default="latin", help="Defines the recognition charset (a.k.a alphabet) value (latin, korean, chinese...). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#recogn-charset. Default: latin.")
    parser.add_argument("--recogn_enabled", required=False, default=True, help="Whether to enable License Plate Recognition (LPR) / OCR. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#recogn-enabled.")
    parser.add_argument("--nvision_enabled", required=False, default=False, help="Whether to enable Image Enhancement for Night-Vision (IENV). More info about IENV at https://www.doubango.org/SDKs/anpr/docs/Features.html#image-enhancement-for-night-vision-ienv. Default: true for x86-64 and false for ARM.")
    parser.add_argument("--platform", required=False, default="auto", help="Target platform. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#platform")
    parser.add_argument("--backend", required=False, default="auto", help="Target backend. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#backend")
    parser.add_argument("--klass_lpci_enabled", required=False, default=False, help="Whether to enable License Plate Country Identification (LPCI). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-lpci-enabled")
    parser.add_argument("--klass_vcr_enabled", required=False, default=False, help="Whether to enable Vehicle Color Recognition (VCR). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vcr-enabled")
    parser.add_argument("--klass_vmmr_enabled", required=False, default=False, help="Whether to enable Vehicle Make Model Recognition (VMMR). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vmmr-enabled")
    parser.add_argument("--klass_vbsr_enabled", required=False, default=False, help="Whether to enable Vehicle Body Style Recognition (VBSR). More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#klass-vbsr-enabled")
    parser.add_argument("--parallel", required=False, default=False, help="Whether to enabled the parallel mode.More info about the parallel mode at https://www.doubango.org/SDKs/anpr/docs/Parallel_processing.html")
    parser.add_argument("--tokenfile", required=False, default="", help="Path to license token file")
    parser.add_argument("--tokendata", required=False, default="", help="Base64 license token data")

    args = parser.parse_args()

    # Check if image exist
    if not os.path.isfile(args.image):
        raise OSError(f"{TAG} File doesn't exist: {args.image}")

    # Check if assets exist
    if not os.path.isdir(args.assets):
        raise OSError(f"{TAG} Folder doesn't exist: {args.assets}")

    # Update JSON options using values from the command args
    JSON_CONFIG["assets_folder"] = args.assets
    JSON_CONFIG["recogn_charset"] = args.charset
    JSON_CONFIG["recogn_enabled"] = (str(args.recogn_enabled).lower() == "true")
    JSON_CONFIG["night_vision_enabled"] = (str(args.nvision_enabled).lower() == "true")
    JSON_CONFIG["platform"] = args.platform
    JSON_CONFIG["backend"] = args.backend
    JSON_CONFIG["klass_lpci_enabled"] = (str(args.klass_lpci_enabled).lower() == "true")
    JSON_CONFIG["klass_vcr_enabled"] = (str(args.klass_vcr_enabled).lower() == "true")
    JSON_CONFIG["klass_vmmr_enabled"] = (str(args.klass_vmmr_enabled).lower() == "true")
    JSON_CONFIG["klass_vbsr_enabled"] = (str(args.klass_vbsr_enabled).lower() == "true")
    JSON_CONFIG["license_token_file"] = args.tokenfile
    JSON_CONFIG["license_token_data"] = args.tokendata

    # Initialize the engine
    callback = MyParallelDeliveryCallback() if (str(args.parallel).lower() == "true") else None
    checkResult("Init", 
                ultimateAlprSdk.UltAlprSdkEngine.init(json.dumps(JSON_CONFIG), callback)
               )

    # Recognize/Process
    # Please note that the first time you call this function all deep learning models will be loaded 
    # and initialized which means it will be slow. In your application you've to initialize the engine
    # once and do all the recognitions you need then, deinitialize it.
    # For packed formats (RGB-family): https://www.doubango.org/SDKs/anpr/docs/cpp-api.html#_CPPv4N15ultimateAlprSdk16UltAlprSdkEngine7processEK22ULTALPR_SDK_IMAGE_TYPEPKvK6size_tK6size_tK6size_tKi
    # For YUV formats (data from camera): https://www.doubango.org/SDKs/anpr/docs/cpp-api.html#_CPPv4N15ultimateAlprSdk16UltAlprSdkEngine7processEK22ULTALPR_SDK_IMAGE_TYPEPKvPKvPKvK6size_tK6size_tK6size_tK6size_tK6size_tK6size_tKi
    # For compressed data (jpeg, png...): https://www.doubango.org/SDKs/anpr/docs/cpp-api.html#_CPPv4N15ultimateAlprSdk16UltAlprSdkEngine7processEPKvK6size_t
    with open(args.image, 'rb') as file:
        checkResult("Sync Process",
                    ultimateAlprSdk.UltAlprSdkEngine.process(
                        file.read(), 
                        os.fstat(file.fileno()).st_size
                    )
            )

    # Press any key to exit
    input("\nPress Enter to exit...\n") 

    # DeInit the engine
    checkResult("DeInit", 
                ultimateAlprSdk.UltAlprSdkEngine.deInit()
               )
    
    