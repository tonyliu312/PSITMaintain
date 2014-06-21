#根据mpstat的字段修改. 
export JAVA_HOME=/IBM/WebSphere/AppServer/java
$JAVA_HOME/bin/java -cp ./lib/monitor.jar -DUserStr=us -DSysStr=sy -DWaitStr=wa -DIdleStr=id -DCpuIdStr=cpu nc.monitor.os.MonitorCPU 30 50
  