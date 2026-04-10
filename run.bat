@echo off
echo Compiling the Gesture Wave application...
javac -d bin -sourcepath src -cp "lib\opencv-4120.jar" src\auth\*.java src\hand\*.java src\camera\*.java

if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Starting the application...
java -cp "bin;lib\opencv-4120.jar" auth.MainApp
