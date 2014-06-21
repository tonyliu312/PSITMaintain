@echo off
net stop winmgmt
net stop wmi
java -cp ./lib/monitor.jar  nc.monitor.os.MonitorCPU 20 100