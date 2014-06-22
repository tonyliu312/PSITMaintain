@echo off
if "%JAVA_HOME%" == "" (
	echo Please set JAVA_HOME environment variable first.
	pause
	goto QUIT
)


start %JAVA_HOME%/bin/javaw -Xmx512m -Dbasedir=./ -cp ./lib/landf.jar;./lib/mx.jar;./lib/monitor.jar;./lib/ncdepend.jar;./lib/ChartDirector.jar;./lib/j2ee.jar nc.monitor.ui.pub.MainFrame >>log.txt


:QUIT
if "%OS%"=="Windows_NT" endlocal 