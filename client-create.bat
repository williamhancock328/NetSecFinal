cd client

@echo off
set /p "user=Enter Username: "

java -jar dist/client.jar --create -u %user% -h 127.0.0.1 -p 5000

pause