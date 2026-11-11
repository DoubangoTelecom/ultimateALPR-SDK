setlocal
set PATH=%PATH%;%~dp0;%CUDA_PATH%\bin;%CUDNN_PATH%\bin;%TENSORRT_PATH%\bin;
recognizer.exe ^
    --image ../../../assets/images/china.jpg ^
    --assets ../../../assets ^
    --charset chinese ^
    --platform auto ^
    --backend auto ^
    --nvision_enabled false ^
    --klass_lpci_enabled true ^
    --klass_vcr_enabled true ^
    --klass_vmmr_enabled true ^
    --klass_vbsr_enabled true ^
    --parallel false
endlocal