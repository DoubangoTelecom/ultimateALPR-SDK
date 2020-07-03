
These binaries were cross-compiled on Windows 8 using Toolchain at [http://sysprogs.com/getfile/566/raspberry-gcc8.3.0.exe](http://sysprogs.com/getfile/566/raspberry-gcc8.3.0.exe) for [Raspbian Buster](https://en.wikipedia.org/wiki/Raspbian) but should work on both Raspberry Pi 3 and 4.

Please note that even if Raspberry Pi 3 and 4 has a 64-bit CPU Raspbian uses a 32-bit kernel and this is why we provide **armv7l** binaries instead of **arm64-a**.

More toolchains at [https://gnutoolchains.com/raspberry/](https://gnutoolchains.com/raspberry/)

Result for `C:/SysGCC/raspberry/bin/arm-linux-gnueabihf-g++.exe -v`
```
Using built-in specs.
COLLECT_GCC=C:/SysGCC/raspberry/bin/arm-linux-gnueabihf-g++.exe
COLLECT_LTO_WRAPPER=c:/sysgcc/raspberry/bin/../libexec/gcc/arm-linux-gnueabihf/8/lto-wrapper.exe
Target: arm-linux-gnueabihf
Configured with: ../../src/gcc-8-8.3.0/src/configure --with-pkgversion='Raspbian 8.3.0-6+rpi1' --enable-languages=c,c++,lto --with-gcc-major-version-only --enable-shared --enable linker-build-id --without-included-gettext --enable-threads=posix --enable-nls --enable-bootstrap --enable-clocale=gnu --enable-libstdcxx-debug --enable-libstdcxx-time=yes --with-default-libstdcxx-abi=new --enable-gnu-unique-object --disable-libitm --disable-libquadmath --disable-libquadmath-support --enable-plugin --with-system-zlib --with-target-system-zlib --enable-objc-gc=auto --enable-multiarch --disable-sjlj-exceptions --with-arch=armv6 --with-fpu=vfp --with-float=hard --disable-werror --enable-checking=release --target=arm-linux-gnueabihf --with-sysroot=/mnt/f/gnu/raspberry-buster/out/arm-linux-gnueabihf/sysroot --host=i686-w64-mingw32 --prefix /mnt/f/gnu/raspberry-buster/out/ --disable-nls
Thread model: posix
gcc version 8.3.0 (Raspbian 8.3.0-6+rpi1)
```
