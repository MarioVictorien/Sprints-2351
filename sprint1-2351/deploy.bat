@echo off

rem Variable fixe
set webapps="C:\xampp\tomcat\webapps"
set temp="temp"
set src="src"
set lib="lib"
set web="web"
set xml="xml"

rem Paramètres
set "appName=%1"

if "%appName%"=="" (
    echo Erreur: Veuillez entrer le nom du projet 
    goto :fin
)

if exist "%temp%" (
    rmdir /s /q "%temp%"
)

mkdir "%temp%/WEB-INF/lib"
mkdir "%temp%/WEB-INF/classes"

javac -d bin -source 1.8 -target 1.8 src/*.java
copy "%xml%" "%temp%/WEB-INF/"
copy "%lib%" "%temp%/WEB-INF/lib"
copy "%web%" "%temp%/"

jar cvf "%temp%/WEB-INF/lib/"%appName%.jar -C bin .

Xcopy %temp% %webapps%\%appName%\ /E /H /C /I /Y

@REM cd %temp%

@REM jar -cvf "%appName%.war" *

@REM move %appName%.war %webapps%

@REM echo %appName%

@REM timeout /t 15 > nul
@REM cd ../%web%

@REM Copy assets to server
Xcopy .\assets %webapps%\%appName%\assets\ /E /H /C /I /Y

Xcopy .\css %webapps%\%appName%\css\ /E /H /C /I /Y

Xcopy .\img %webapps%\%appName%\img\ /E /H /C /I /Y

Xcopy .\js %webapps%\%appName%\js\ /E /H /C /I /Y