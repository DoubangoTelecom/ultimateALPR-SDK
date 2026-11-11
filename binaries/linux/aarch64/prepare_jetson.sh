#!/bin/bash 2> nul

# get Jetpack version
jetpack_version=`apt-cache show nvidia-jetpack | grep "Version:"`
echo "Your Jetpack version: $jetpack_version"

# Create symlink on the plugin
if $(echo $jetpack_version | grep -q "Version: 4."); then 
   echo "Preparing for Jetpack 4.6.6..."
   ln -nsf libplugin_dl_trt466.so libplugin_dl_trt.so
elif $(echo $jetpack_version | grep -q "Version: 5."); then 
   echo "Preparing for Jetpack 5.1.6..."
   ln -nsf libplugin_dl_trt516.so libplugin_dl_trt.so
elif $(echo $jetpack_version | grep -q "Version: 6."); then 
   echo "Preparing for Jetpack 6.2.0..."
   ln -nsf libplugin_dl_trt620.so libplugin_dl_trt.so
elif $(echo $jetpack_version | grep -q "Version: 7."); then 
   echo "Preparing for Jetpack 7.2.0..."
   ln -nsf libplugin_dl_trt720.so libplugin_dl_trt.so
else
   echo "Jetson version not supported: $jetpack_version"
   exit 0
fi

# Gnerate the plans
sudo chmod +x ./trt_optimizer && LD_LIBRARY_PATH=.:$LD_LIBRARY_PATH ./trt_optimizer --assets ../../../assets

