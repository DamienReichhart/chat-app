@echo off
echo Chat Application Runner
echo ======================

echo Building project...
call mvn clean compile package install

echo.
echo Starting server...
start cmd /k "cd chat-server && mvn spring-boot:run"

echo.
echo Waiting for server to start...
timeout /t 5 /nobreak

echo.
echo Starting client 1...
start cmd /k "cd chat-client && mvn javafx:run"

echo.
echo Starting client 2...
start cmd /k "cd chat-client && mvn javafx:run"

echo.
echo Done! 