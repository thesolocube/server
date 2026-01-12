@echo off
echo ========================================
echo   Serveur Chat LAN - Interface Graphique
echo ========================================
echo.
cd /d %~dp0
mvn javafx:run
pause

