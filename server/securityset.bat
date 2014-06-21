@echo off
if "%JAVA_HOME%" == "" (
	echo Please set JAVA_HOME environment variable first.
	pause
	goto QUIT
)

"%JAVA_HOME%/bin/java" -cp ./lib/monitor.jar;./lib/ncdepend.jar nc.monitor.service.security.SecurityConfig

:QUIT
if "%OS%"=="Windows_NT" endlocal 