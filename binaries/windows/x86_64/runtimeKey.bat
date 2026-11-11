setlocal
set PATH=%PATH%;%~dp0;%CUDA_PATH%\bin;%CUDNN_PATH%\bin;%TENSORRT_PATH%\bin;
.\runtimeKey.exe --json true --assets ../../../assets
endlocal