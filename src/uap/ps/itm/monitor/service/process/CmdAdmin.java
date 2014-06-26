package uap.ps.itm.monitor.service.process;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.center.ServerEnv;
public class CmdAdmin implements IMwAdmin {

	public void dumpThread(String serverName) {
		// TODO Auto-generated method stub

	}

	public Object generateHeadDump(String serverName) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getThreadPoolRange(String serverName) {
		// TODO Auto-generated method stub
		return "100-100";
	}

	public void restart(String serverName) {
		// TODO Auto-generated method stub
		shutdownImmediate(serverName);
		startup(serverName);
	}

	public void shutdownImmediate(String serverName) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		if (Toolkit.isEmpty(ServerEnv.getInstance().getProperty("shutdowncmd")))
			return;
		ProcessMonitor.warn("now begin shutdown " + serverName, null, serverName);
		String cmd = ServerEnv.getInstance().getProperty("shutdowncmd");
		runprocess(cmd);
		checkShutdown(serverName);
		ProcessMonitor.warn("shutdown " + serverName + " over!", null, serverName);
	}

	public void startup(String serverName) {
		// TODO Auto-generated method stub
		if (Toolkit.isEmpty(ServerEnv.getInstance().getProperty("startupcmd")))
			return;
		ProcessMonitor.warn("now begin to startup " + serverName, null, serverName);
		String cmd = ServerEnv.getInstance().getProperty("startupcmd");
		runprocess(cmd);
		checkStartup(serverName);
		MonitorEnv.getInstance().getServerInfo(serverName).addRestartTimes();
		MonitorEnv.getInstance().getServerInfo(serverName).setLastRestartDate(Toolkit.getCurTime());
		ProcessMonitor.warn("startup " + serverName + " over!", null, serverName);
	}

	public void checkShutdown(String servername) {
		while (true) {
			if (!MonitorEnv.getInstance().getServerInfo(servername).getMonitorThread().checkPort()) {
				break;
			}
			try {
				Thread.currentThread().sleep(3000l);
			} catch (Exception e) {
			}
		}
	}
	public void checkStartup(String servername) {
		while (true) {
			if (MonitorEnv.getInstance().getServerInfo(servername).getMonitorThread().checkPort()) {
				break;
			}
			try {
				Thread.currentThread().sleep(3000);
			} catch (Exception e) {
			}
		}
	}

	public void stop() {
	}

	public void runprocess(final String cmd) {
		new Thread() {
			public void run() {
				ProcessMonitor.warn("###exec command:" + cmd, null, null);
				boolean stdout = true;
				BufferedReader reader = null;
				try {
					Process p = Runtime.getRuntime().exec(cmd);
					reader = stdout ? new BufferedReader(new InputStreamReader(p.getInputStream())) : new BufferedReader(new InputStreamReader(p.getErrorStream()));
					String outLine = reader.readLine();
					System.out.println(outLine);
					while (outLine != null) {
						try {
							outLine = reader.readLine();
						} catch (Exception e) {
							break;
						}
						ProcessMonitor.warn(outLine, null, null);
					}
				} catch (Exception exp) {
					exp.printStackTrace();
					ProcessMonitor.warn(exp.getMessage(), null, null);
				} finally {
					try {
						reader.close();
					} catch (Exception e) {

					}
				}
				ProcessMonitor.warn("###end command:" + cmd, null, null);
			}
		}.start();
	}
	
	public String getJDBCPool(String serverName){
		return null;
	}
	
}
