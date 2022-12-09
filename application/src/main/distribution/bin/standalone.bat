@REM
@REM Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
@REM and other contributors as indicated by the @author tags.
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

@echo off
rem -------------------------------------------------------------------------
rem Ublhub Bootstrap Script for Windows
rem -------------------------------------------------------------------------

@if not "%ECHO%" == ""  echo %ECHO%
setlocal

if "%OS%" == "Windows_NT" (
  set "DIRNAME=%~dp0%"
) else (
  set DIRNAME=.\
)

:MAIN
rem $Id$
)

pushd "%DIRNAME%.."
set "RESOLVED_UBLHUB_HOME=%CD%"
popd

if "x%UBLHUB_HOME%" == "x" (
  set "UBLHUB_HOME=%RESOLVED_UBLHUB_HOME%"
)

pushd "%UBLHUB_HOME%"
set "SANITIZED_UBLHUB_HOME=%CD%"
popd

if /i "%RESOLVED_UBLHUB_HOME%" NEQ "%SANITIZED_UBLHUB_HOME%" (
   echo.
   echo   WARNING:  UBLHUB_HOME may be pointing to a different installation - unpredictable results may occur.
   echo.
   echo       UBLHUB_HOME: "%UBLHUB_HOME%"
   echo.
)

if "x%JAVA_HOME%" == "x" (
  set  JAVA=java
  echo JAVA_HOME is not set. Unexpected results may occur.
  echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
) else (
  if not exist "%JAVA_HOME%" (
    echo JAVA_HOME "%JAVA_HOME%" path doesn't exist
    goto END
   ) else (
     if not exist "%JAVA_HOME%\bin\java.exe" (
       echo "%JAVA_HOME%\bin\java.exe" does not exist
       goto END_NO_PAUSE
     )
      echo Setting JAVA property to "%JAVA_HOME%\bin\java"
    set "JAVA=%JAVA_HOME%\bin\java"
  )
)

"%JAVA%" --add-modules=java.se -version >nul 2>&1 && (set MODULAR_JDK=true) || (set MODULAR_JDK=false)

if not "%PRESERVE_JAVA_OPTS%" == "true" (
  rem Add -client to the JVM options, if supported (32 bit VM), and not overriden
  echo "%JAVA_OPTS%" | findstr /I \-server > nul
  if errorlevel == 1 (
    "%JAVA%" -client -version 2>&1 | findstr /I /C:"Client VM" > nul
    if not errorlevel == 1 (
      set "JAVA_OPTS=-client %JAVA_OPTS%"
    )
  )
)

rem Find quarkus-run.jar, or we can't continue
if exist "%UBLHUB_HOME%\quarkus-run.jar" (
    set "RUNJAR=%UBLHUB_HOME%\quarkus-run.jar"
) else (
  echo Could not locate "%UBLHUB_HOME%\quarkus-run.jar".
  echo Please check that you are in the bin directory when running this script.
  goto END
)

rem Setup JBoss specific properties

rem Setup directories, note directories with spaces do not work
setlocal EnableDelayedExpansion
set "CONSOLIDATED_OPTS=%JAVA_OPTS% %SERVER_OPTS%"
set baseDirFound=false
set configDirFound=false
set logDirFound=false
for %%a in (!CONSOLIDATED_OPTS!) do (
   if !baseDirFound! == true (
      set "UBLHUB_BASE_DIR=%%~a"
      set baseDirFound=false
   )
)
setlocal DisableDelayedExpansion

rem Set the standalone base dir
if "x%UBLHUB_BASE_DIR%" == "x" (
  set  "UBLHUB_BASE_DIR=%UBLHUB_HOME%\standalone"
)

echo ===============================================================================
echo.
echo   Ublhub Bootstrap Environment
echo.
echo   UBLHUB_HOME: "%UBLHUB_HOME%"
echo.
echo   JAVA: "%JAVA%"
echo.
echo ===============================================================================
echo.

cd "%UBLHUB_HOME%"

:RESTART
  "%JAVA%" %JAVA_OPTS% ^
      -jar "%UBLHUB_HOME%\quarkus-run.jar"

if %errorlevel% equ 10 (
	goto RESTART
)

:END
if "x%NOPAUSE%" == "x" pause

:END_NO_PAUSE
