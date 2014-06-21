@echo off
if "%JAVA_HOME%" == "" (
	echo Please set JAVA_HOME environment variable first.
	pause
	goto QUIT
)


"%JAVA_HOME%/bin/java" -Xmx100m -cp ./lib/landf.jar;./lib/lookfeel.jar;./lib/mx.jar;./lib/monitor.jar;./lib/ncdepend.jar;./lib/jgraph.jar nc.monitor.ui.pub.MainFrame >>log.txt


:QUIT
if "%OS%"=="Windows_NT" endlocal 