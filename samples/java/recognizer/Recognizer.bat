REM using Anaconda with 'tensorflow2' env

REM building
javac @sources.txt -d .

REM Update PATH
setlocal
set PATH=%PATH%;../../../binaries/windows/x86_64

REM running
java -Djava.library.path=../../../binaries/windows/x86_64 Recognizer --image ../../../assets/images/lic_us_1280x720.jpg --assets ../../../assets

endlocal