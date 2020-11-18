### Curent Folder: binaries/jetson_tftrt/aarch64 ###

# Download Tensorflow v2.4.0-rc1 from Doubango server (requires CUDA 10.2 which comes with JetPack 4.4+) #
if [ ! -f "libtensorflow_2.4.0-rc1_jetson_gpu.tar.xz" ]; then
    sudo wget https://doubango.org/deep_learning/libtensorflow_2.4.0-rc1_jetson_gpu.tar.xz
fi
if [ ! -f "libtensorflow_cc.so" ]; then
    sudo tar -xf libtensorflow_2.4.0-rc1_jetson_gpu.tar.xz
fi

# Run the optimizer to generate the plans (needs write permissions) #
sudo chmod +x ./trt_optimizer
sudo ./trt_optimizer --assets ../../../assets
