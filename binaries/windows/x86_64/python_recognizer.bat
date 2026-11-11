setlocal
set PYTHONPATH=%PYTHONPATH%;.;../../../python
set PATH=%~dp0;%PATH%
python ../../../samples/python/recognizer/recognizer.py --image ../../../assets/images/lic_us_1280x720.jpg --assets ../../../assets --platform "auto" --backend "auto" --parallel False --recogn_enabled True --charset latin --nvision_enabled False --klass_lpci_enabled True --klass_vcr_enabled True --klass_vmmr_enabled True --klass_vbsr_enabled True
endlocal