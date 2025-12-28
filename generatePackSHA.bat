@echo off
powershell -NoProfile -Command "Get-FileHash 'BrainsPack.zip' -Algorithm SHA1"
pause