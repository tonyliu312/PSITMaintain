export JAVA_HOME=/IBM/WebSphere/AppServer/java
$JAVA_HOME/bin/java -cp ./lib/monitor.jar -DFreeStr=fre -DPiStr=pi -DPoStr=po nc.monitor.os.MonitorMem 30
