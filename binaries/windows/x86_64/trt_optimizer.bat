setlocal
set PATH=%PATH%;%~dp0;%CUDA_PATH%\bin;%CUDNN_PATH%\bin;%TENSORRT_PATH%\bin;
.\trt_optimizer.exe --assets ../../../assets
endlocal