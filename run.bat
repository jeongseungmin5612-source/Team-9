@echo off
rem CAMPLE 빌드 + 실행 (더블클릭하거나 터미널에서 run.bat)
chcp 65001 > nul
cd /d "%~dp0"

echo [1/3] 이전 빌드 결과(out) 지우는 중...
if exist out rmdir /s /q out

echo [2/3] 컴파일 중...
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
set ERR=%errorlevel%
del sources.txt
if not "%ERR%"=="0" (
    echo.
    echo 컴파일 실패. 위의 에러 메시지를 확인하세요.
    pause
    exit /b 1
)

echo [3/3] 실행 중...
java -cp out com.onioncalendar.Main
