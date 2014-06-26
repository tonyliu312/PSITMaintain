package uap.ps.itm.monitor.service.process;

public interface IMwAdmin {
	public void shutdownImmediate(String serverName);
	public void startup(String serverName) ;
	public void restart(String serverName) ;
	public void dumpThread(String serverName) ;
	public Object generateHeadDump(String serverName) ;
	public String getThreadPoolRange(String serverName) ;
	public String getJDBCPool(String serverName);
	public void stop();
}
