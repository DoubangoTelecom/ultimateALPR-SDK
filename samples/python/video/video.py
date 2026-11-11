'''
    * Copyright (C) 2011-2026 Doubango Telecom <https://www.doubango.org>
    * File author: Mamadou DIOP (Doubango Telecom, France).
    * License: For non commercial use only.
    * Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
    * WebSite: https://www.doubango.org/webapps/alpr/


    https://github.com/DoubangoTelecom/ultimateALPR/blob/master/SDK_dist/samples/python/video/README.md
	Usage: 
		video.py \
			--assets <path-to-assets-folder> \
            [--source <video-source>] \
            [--charset <recognition-charset:latin/korean/chinese>] \
			[--tokenfile <path-to-license-token-file>] \
			[--tokendata <base64-license-token-data>]
	Example:
		video.py \
			--source "0" \
            --charset "latin" \
			--assets C:/Projects/GitHub/ultimate/ultimateALPR/SDK_dist/assets \
			--tokenfile C:/Projects/GitHub/ultimate/ultimateALPR/SDK_dev/tokens/windows-iMac.lic
'''

import ultimateAlprSdk
import argparse
import json
import os
import cv2
import numpy as np

TAG = "[PythonVideo] "

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
    "pyramidal_search_minscore": 0.3,
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
    "recogn_minscore": 0.2,
}

# Check result
def checkResult(operation :str, result :ultimateAlprSdk.UltAlprSdkResult):
    if not result.isOK():
        print(f'{TAG} {operation}: failed -> {result.phrase()}')
        raise Exception(f'Error: {result.phrase()}')
    else:
        print(f'{TAG} {operation}: ok -> {result.json()}')
    
    return result

# Compute max latency
def maxLatency(max_latency: int):
    if max_latency > 0:
        return max_latency
    else:
        # Based on C++ code at
        #   https://github.com/DoubangoTelecom/ultimateALPR/blob/239ffa11f9847d784b9b3a0890df81ddf0173945/SDK_dev/lib/source/ultimate_alpr_sdk_private_engine.cxx#L146
        # Use a value other than "<0" to enforce max latency (bad perfs). Otherwise, it's adaptative.
        abc = os.cpu_count()
        kUltAlprSdkEnginePrivateMaxLatency = 8
        min_latency = kUltAlprSdkEnginePrivateMaxLatency if ("hailo" in JSON_CONFIG["backend"]) else min(kUltAlprSdkEnginePrivateMaxLatency, os.cpu_count())
        numProcessingThreads = min_latency
        return (min_latency * numProcessingThreads)

# Manages frames from parallel processing
class FrameQueue(ultimateAlprSdk.UltAlprSdkParallelDeliveryCallback):
    def __init__(self, max_latency :int, parallel: bool):
        super().__init__()
        self.max_latency = max(max_latency, 1) if parallel else 1
        self.parallel = parallel
        self.queue = [None,] * max_latency
        self.counter = 0
        self.running = True

    # Async result callback for // processing: https://www.doubango.org/SDKs/anpr/docs/Parallel_processing.html
    def onNewResult(self, result :ultimateAlprSdk.UltAlprSdkResult):
        checkResult("Async Process", result)
        self.draw_result(result)
        return 0

    def push_frame(self, frame :np.ndarray):
        self.queue[self.counter % self.max_latency] = { "id": self.counter, "frame": frame }
        self.counter += 1

    @staticmethod
    def DrawText(frame, text, topleft, fontFace, fontScale, color, thickness, align_bottom=True):
        ((fw, fh), baseline) = cv2.getTextSize(text, fontFace=fontFace, fontScale=fontScale, thickness=thickness)
        if not align_bottom:
            topleft = (topleft[0], topleft[1] + fh)
        cv2.rectangle(frame, (topleft[0], topleft[1]-fh), (topleft[0]+fw, topleft[1]), color, -1)
        cv2.putText(frame, text, topleft, fontFace, fontScale, (0, 0, 0), thickness)

    def draw_result(self, result :ultimateAlprSdk.UltAlprSdkResult):
        if not result.isOK():
            raise Exception(f'Error: {result.phrase()}')

        # Parse JSON result
        rjson = json.loads(result.json())
        for field in ['frame_id', 'blocks']:
            if not field in rjson:
                raise Exception(f'{field} field missing: {result.json()}')

        frame_id = rjson['frame_id']
        entry = self.queue[frame_id % self.max_latency]
        if entry is None:
            raise Exception(f'No frame at index: {frame_id}')
        if entry['id'] != frame_id:
            print(f'{TAG} Error: Video frame({entry['id']}) and json result({frame_id}) not synchronized')
        frame = entry['frame']

        # Loop through the blocks (plate, car...)
        for block in rjson['blocks']:
            # Draw plate
            if 'plate' in block:
                plate = block['plate']
                pwBox = plate['warpedBox']
                cv2.rectangle(frame, (int(pwBox[0]), int(pwBox[1])), (int(pwBox[4]), int(pwBox[5])), (0, 255, 255), 2)
                if 'text' in plate:
                    FrameQueue.DrawText(frame, plate['text'], (int(pwBox[0]), int(pwBox[1])), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 255), 2, align_bottom=True)
                if 'lpci' in plate:
                    lpci_best = plate['lpci'][0]
                    FrameQueue.DrawText(frame, lpci_best['code'], (int(pwBox[6]), int(pwBox[7])), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 0, 0), 2, align_bottom=False)

            # Draw car
            if 'car' in block:
                car = block['car']
                cwBox = car['warpedBox']
                cv2.rectangle(frame, (int(cwBox[0]), int(cwBox[1])), (int(cwBox[4]), int(cwBox[5])), (0, 0, 255), 2)
                cText = ''
                if 'vmmr' in car:
                    cText += f'{car['vmmr'][0]['make']}({car['vmmr'][0]['model']})'
                if 'vbsr' in car:
                    cText += f' - [{car['vbsr'][0]['name']}]'
                if 'vcr' in car:
                    cText += f' - [{car['vcr'][0]['name']}]'
                if cText:
                    FrameQueue.DrawText(frame, cText, (int(cwBox[0]), int(cwBox[1])), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 255), 2, align_bottom=True)

        cv2.imshow('[ALPR] Press "q" to exit.', frame)
        self.running = (cv2.waitKey(1) != ord('q'))

# Entry point
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="""
    This is the video sample using python language
    """)

    parser.add_argument("--source", required=False, type=str, default="0", help="Camera index or path to a file (e.g. mp4)")
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
    parser.add_argument("--detect_minscore", required=False, type=float, default=0.5, help="Detection threshold. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#detect-minscore")
    parser.add_argument("--max_latency", required=False, type=int, default=-1, help="Maximum latency for parallel delivery. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#max-latency")
    parser.add_argument("--tokenfile", required=False, default="", help="Path to license token file")
    parser.add_argument("--tokendata", required=False, default="", help="Base64 license token data")

    args = parser.parse_args()

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
    JSON_CONFIG["max_latency"] = args.max_latency
    JSON_CONFIG["detect_minscore"] = args.detect_minscore
    JSON_CONFIG["klass_lpci_enabled"] = (str(args.klass_lpci_enabled).lower() == "true")
    JSON_CONFIG["klass_vcr_enabled"] = (str(args.klass_vcr_enabled).lower() == "true")
    JSON_CONFIG["klass_vmmr_enabled"] = (str(args.klass_vmmr_enabled).lower() == "true")
    JSON_CONFIG["klass_vbsr_enabled"] = (str(args.klass_vbsr_enabled).lower() == "true")
    JSON_CONFIG["license_token_file"] = args.tokenfile
    JSON_CONFIG["license_token_data"] = args.tokendata

    # Open camera or video file (e.g. mp4)
    print(f'Starting video source at {args.source}...')
    cap = cv2.VideoCapture(int(args.source) if args.source.isdigit() else args.source)
    if not cap.isOpened():
        raise Exception(f'Cannot open source: {args.source}')
    print(f'Started video source at {args.source}.')

    # Initialize the engine
    frameQueue = FrameQueue(maxLatency(args.max_latency), str(args.parallel).lower() == "true")
    checkResult("Init", 
                ultimateAlprSdk.UltAlprSdkEngine.init(
                    json.dumps(JSON_CONFIG), 
                    frameQueue if frameQueue.parallel else None
                ))

    # Read frames from camera
    while frameQueue.running:
        ret, frame = cap.read()
        # Check read status
        if not ret:
            print("Camera stream cut. Exiting...")
            break
        # Register frame for deferred drawing
        frameQueue.push_frame(frame)
        ''' Processing:
            For packed formats (RGB-family): https://www.doubango.org/SDKs/anpr/docs/cpp-api.html#_CPPv4N15ultimateAlprSdk16UltAlprSdkEngine7processEK22ULTALPR_SDK_IMAGE_TYPEPKvK6size_tK6size_tK6size_tKi
            For YUV formats (data from camera): https://www.doubango.org/SDKs/anpr/docs/cpp-api.html#_CPPv4N15ultimateAlprSdk16UltAlprSdkEngine7processEK22ULTALPR_SDK_IMAGE_TYPEPKvPKvPKvK6size_tK6size_tK6size_tK6size_tK6size_tK6size_tKi
            For compressed data (jpeg, png...): https://www.doubango.org/SDKs/anpr/docs/cpp-api.html#_CPPv4N15ultimateAlprSdk16UltAlprSdkEngine7processEPKvK6size_t
        '''
        result = checkResult("Procees", 
                            ultimateAlprSdk.UltAlprSdkEngine.process(
                                ultimateAlprSdk.ULTALPR_SDK_IMAGE_TYPE_BGR24, # OpenCV chroma is BGR
                                frame.tobytes(),
                                frame.shape[1], # width
                                frame.shape[0], # height
                                0, # stride
                                1 # exifOrientation (already rotated in load_image -> use default value: 1)
                            ))
        # Wait for the 'onResult' callback if parallel. Otherwise, trigger it.
        if not frameQueue.parallel:
            frameQueue.onNewResult(result)

    # Cleanup OpenCV
    cap.release()
    cv2.destroyAllWindows()

    # DeInit the engine
    checkResult("DeInit", 
                ultimateAlprSdk.UltAlprSdkEngine.deInit()
               )
    
    