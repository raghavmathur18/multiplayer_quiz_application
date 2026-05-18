@echo off
REM ============================================================
REM QuizArena Build Script for Windows
REM ============================================================
echo === QuizArena Build Script ===

set MYSQL_JAR=lib\mysql-connector-java-8.0.33.jar
set JFREE_JAR=lib\jfreechart-1.5.4.jar
set CLASSPATH=%MYSQL_JAR%;%JFREE_JAR%
set SRC_DIR=src\main\java
set OUT_DIR=target\classes
set JAR_OUT=target\QuizArena.jar

if not exist "%MYSQL_JAR%" (
    echo ERROR: %MYSQL_JAR% not found.
    echo Download: https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar
    pause & exit /b 1
)
if not exist "%JFREE_JAR%" (
    echo ERROR: %JFREE_JAR% not found.
    echo Download: https://repo1.maven.org/maven2/org/jfree/jfreechart/1.5.4/jfreechart-1.5.4.jar
    pause & exit /b 1
)

echo [1/3] Compiling...
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"
dir /s /b %SRC_DIR%\*.java > sources.txt
javac -cp "%CLASSPATH%" -d "%OUT_DIR%" @sources.txt
if errorlevel 1 ( echo COMPILE FAILED & pause & exit /b 1 )
echo       Compiled successfully!

echo [2/3] Creating JAR...
echo Main-Class: com.quizapp.Main > MANIFEST.MF
echo Class-Path: lib/mysql-connector-java-8.0.33.jar lib/jfreechart-1.5.4.jar >> MANIFEST.MF
jar cfm "%JAR_OUT%" MANIFEST.MF -C "%OUT_DIR%" .
echo       JAR created: %JAR_OUT%

echo [3/3] Done!
echo.
echo To run SERVER:  java -cp target\QuizArena.jar;lib\* com.quizapp.Main server
echo To run CLIENT:  java -cp target\QuizArena.jar;lib\* com.quizapp.Main
pause
