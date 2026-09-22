@echo off
title DC-Lab Worker Node
powershell -ExecutionPolicy Bypass -File "%~dp0start-worker.ps1"
pause
