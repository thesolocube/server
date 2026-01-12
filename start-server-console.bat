@echo off
echo ========================================
echo   Serveur Chat LAN - Console
echo ========================================
echo.
cd /d %~dp0
mvn exec:java -Dexec.mainClass="org.example.socketproject.server.ServerMain"
pause

