@echo off
setlocal
set PYTHONPATH=;.;../../../python
set PATH=%PATH%;%~dp0

python ../../../samples/python/recognizer/recognizer.py --image ../../../assets/images/AP43_attacked.png --assets ../../../assets --ienv_enabled False --klass_lpci_enabled True --klass_vcr_enabled True --klass_vmmr_enabled True --klass_vbsr_enabled True --output_image ../../../output/result.jpg


