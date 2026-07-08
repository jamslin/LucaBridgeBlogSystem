@REM Maven Wrapper startup script (Windows). See mvnw for the POSIX equivalent.
@echo off
setlocal enabledelayedexpansion

set "BASEDIR=%~dp0"
set "WRAPPER_JAR=%BASEDIR%.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_PROPERTIES=%BASEDIR%.mvn\wrapper\maven-wrapper.properties"

REM %~dp0 always ends in a backslash. A trailing backslash immediately before a
REM closing double-quote escapes that quote in Windows argument parsing, which
REM corrupts everything after it on the command line, so strip it before this
REM path is used inside a quoted -D argument below.
set "PROJECT_DIR=%BASEDIR%"
if "%PROJECT_DIR:~-1%"=="\" set "PROJECT_DIR=%PROJECT_DIR:~0,-1%"

if "%JAVA_HOME%"=="" (
  set "JAVA_CMD=java"
) else (
  set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
)

if not exist "%WRAPPER_JAR%" (
  for /f "tokens=2 delims==" %%A in ('findstr /b "wrapperUrl=" "%WRAPPER_PROPERTIES%"') do set "WRAPPER_URL=%%A"
  echo Downloading Maven Wrapper from: !WRAPPER_URL!
  if not exist "%BASEDIR%.mvn\wrapper" mkdir "%BASEDIR%.mvn\wrapper"
  powershell -NoProfile -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '!WRAPPER_URL!' -OutFile '!WRAPPER_JAR!'"
)

"%JAVA_CMD%" -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%PROJECT_DIR%" org.apache.maven.wrapper.MavenWrapperMain %*
