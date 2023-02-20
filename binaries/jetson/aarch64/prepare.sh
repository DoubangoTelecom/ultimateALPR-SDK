### Curent Folder: binaries/jetson/aarch64 ###

# get Jetpack version
jetpack_version=`apt-cache show nvidia-jetpack | grep "Version:"`
echo "Your Jetpack version is $jetpack_version"

# Create symlink on the plugin
if $(echo $jetpack_version | grep -q "Version: 4.")
then 
   echo "Preparing for Jetpack 4.4.1..."
   ln -nsf libultimatePluginTensorRT.so.4.4.1 libultimatePluginTensorRT.so
else
   echo "Preparing for Jetpack 5.1.0..."
   ln -nsf libultimatePluginTensorRT.so.5.1.0 libultimatePluginTensorRT.so
fi

# Run the optimizer to generate the plans (needs write permissions) #
sudo chmod +x ./trt_optimizer
sudo ./trt_optimizer --assets ../../../assets




