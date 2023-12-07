PYTHONPATH=../../../binaries/jetson/aarch64:../../../python \
LD_LIBRARY_PATH=../../../binaries/jetson/aarch64:$LD_LIBRARY_PATH \
python3 recognizer.py --image ../../../assets/images/lic_us_1280x720.jpg --assets ../../../assets 