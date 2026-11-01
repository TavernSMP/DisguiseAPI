@echo off
setlocal
cd /d "%~dp0"
set "JAVA_HOME=D:\Java JDK\jdk-25.0.4.7-hotspot"
set "TEMP=%~dp0verification\tmp"
set "TMP=%TEMP%"
if not exist "%TEMP%" mkdir "%TEMP%"
set "DISGUISE_MAVEN=D:\SpigotMC\FreshBuildTools\apache-maven-3.9.6\bin\mvn.cmd"
if not exist "%DISGUISE_MAVEN%" (
    echo Maven was not found at "%DISGUISE_MAVEN%". See README.md.
    exit /b 1
)
if "%~1"=="" (
    call "%DISGUISE_MAVEN%" "-Dmaven.repo.local=D:\cache\.maven\repository" -B -ntp clean verify
) else (
    call "%DISGUISE_MAVEN%" "-Dmaven.repo.local=D:\cache\.maven\repository" %*
)
exit /b %ERRORLEVEL%