cd client/dist/

@echo off
set /p "user=Enter Username: "

java -jar client.jar --create -u %user% -h 127.0.0.1 -p 5000

pause