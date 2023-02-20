### Curent Folder: binaries/jetson_tftrt/aarch64 ###

# Get Jetpack version
jetpack_version=`apt-cache show nvidia-jetpack | grep "Version:"`
echo "Your Jetpack version is $jetpack_version"

# Create symlink on the library and get the Tensorflow version
if $(echo $jetpack_version | grep -q "Version: 4.")
then 
   echo "Preparing for Jetpack 4.4.1..."
   ln -nsf ../../jetson/aarch64/libultimatePluginTensorRT.so.4.4.1 libultimatePluginTensorRT.so
   tf_file="libtensorflow_2.4.0-rc1_jetpack4.4.1_gpu.tar.xz" 
else
   echo "Preparing for Jetpack 5.1.0..."
   ln -nsf ../../jetson/aarch64/libultimatePluginTensorRT.so.5.1.0 libultimatePluginTensorRT.so
   tf_file="libtensorflow_2.6.0_jetpack5.1.0_gpu.tar.xz" 
fi

# Download Tensorflow C++ library from the server #
if [ ! -f $tf_file ]; then
    sudo wget https://doubango.org/deep_learning/$tf_file
fi
if [ ! -f "libtensorflow_cc.so" ]; then
    sudo tar -xf $tf_file
fi

# Run the optimizer to generate the plans (needs write permissions) #
sudo chmod +x ./trt_optimizer
sudo ./trt_optimizer --assets ../../../assets
