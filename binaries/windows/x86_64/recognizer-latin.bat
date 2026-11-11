setlocal
set PATH=%PATH%;%~dp0;%CUDA_PATH%\bin;%CUDNN_PATH%\bin;%TENSORRT_PATH%\bin;
recognizer.exe ^
    --image ../../../assets/images/lic_us_1280x720.jpg ^
    --assets ../../../assets ^
    --charset latin ^
    --platform auto ^
    --backend auto ^
    --nvision_enabled false ^
    --klass_lpci_enabled true ^
    --klass_vcr_enabled true ^
    --klass_vmmr_enabled true ^
    --klass_vbsr_enabled true ^
    --parallel false
endlocal