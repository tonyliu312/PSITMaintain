@echo off
set java_home=../../ufjdk

"%JAVA_HOME%/bin/java" -cp ./lib/monitor.jar;./lib/ncdepend.jar;./lib/wasdepend.jar nc.monitor.ui.serviceconfig.ServiceConfigFrame

:QUIT
if "%OS%"=="Windows_NT" endlocal 