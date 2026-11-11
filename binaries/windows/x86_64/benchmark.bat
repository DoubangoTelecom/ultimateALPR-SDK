setlocal
set PATH=%PATH%;%~dp0;%CUDA_PATH%\bin;%CUDNN_PATH%\bin;%TENSORRT_PATH%\bin;
benchmark.exe ^
    --assets ../../../assets ^
    --image ../../../assets/images/lic_us_1280x720.jpg ^
    --charset latin ^
    --backend auto ^
    --batching_mode auto ^
    --num_threads -1 ^
    --max_latency -1 ^
    --pysearch_enabled false ^
    --recogn_enabled true ^
    --klass_lpci_enabled false ^
    --klass_vcr_enabled false ^
    --klass_vmmr_enabled false ^
    --klass_vbsr_enabled false ^
    --loops 500 ^
    --parallel true
endlocal