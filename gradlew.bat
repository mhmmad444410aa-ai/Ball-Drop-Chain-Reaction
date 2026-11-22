@echo off
where gradle >nul 2>nul
if %errorlevel% neq 0 (
 echo Please install Gradle 8.13 then run: gradle assembleDebug
 exit /b 1
)
gradle %*
