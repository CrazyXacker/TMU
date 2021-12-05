@echo off
echo UPXing...
upx.exe -9 -q -o "..\build\client\x86_64-windows\TMU.exe" "..\build\client\x86_64-windows\TelegraphMangaUploader.exe"
echo Executable packed!