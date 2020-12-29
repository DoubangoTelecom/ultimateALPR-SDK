setlocal
set PYTHONPATH=%PYTHONPATH%;.;../../../python
set PATH=%PATH%;%~dp0
python ../../../samples/python/recognizer/recognizer.py --image ../../../assets/images/lic_us_1280x720.jpg --assets ../../../assets --ienv_enabled True --klass_lpci_enabled True --klass_vcr_enabled True --klass_vmmr_enabled True --klass_vbsr_enabled True
endlocal