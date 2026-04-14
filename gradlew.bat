@echo off
setlocal

set "GRADLE_VERSION=8.13"
set "BASE_DIR=%~dp0"
set "DIST_DIR=%BASE_DIR%\.gradle-dist"
set "ZIP_NAME=gradle-%GRADLE_VERSION%-bin.zip"
set "ZIP_PATH=%DIST_DIR%\%ZIP_NAME%"
set "GRADLE_HOME=%DIST_DIR%\gradle-%GRADLE_VERSION%"

if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"

if not exist "%GRADLE_HOME%" (
  if not exist "%ZIP_PATH%" (
    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
      "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/%ZIP_NAME%' -OutFile '%ZIP_PATH%'"
  )
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "Expand-Archive -Path '%ZIP_PATH%' -DestinationPath '%DIST_DIR%' -Force"
)

call "%GRADLE_HOME%\bin\gradle.bat" %*
