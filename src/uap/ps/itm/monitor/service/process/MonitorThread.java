package uap.ps.itm.monitor.service.process;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.db.DbSourceInfo;
import uap.ps.itm.monitor.service.message.MessageEntry;
import uap.ps.itm.monitor.service.message.MsgSender;

public class MonitorThread extends Thread {
	private String serverName = null;

	private String strURL = null;

	private CheckThread checkT = null;

	public MonitorThread(String serverName) {
		MwServerInfo server = MonitorEnv.getInstance().getServerInfo(serverName);
		setName("monitorThread-" + serverName);
		strURL = "http://" + server.getHost() + ":" + server.getPort() + "/" + MonitorEnv.getInstance().getServlet();
		this.serverName = serverName;
	}

	public MonitorThread() {
		setName("monitorThread");
	}

	public void run() {
		ProcessMonitor.warn("now begin to monitor " + serverName + " ......", this, serverName);
		while (!MonitorEnv.getInstance().isShutdown()) {
			boolean checkOK = true;
			checkOK = check();
			if (!checkOK) {
				LockGC.lockGC(serverName);
			}
			if (!checkOK && !MonitorEnv.getInstance().getServerInfo(serverName).isOffline() && MonitorEnv.getInstance().isRestartWhenResponseCheckFail()) {
				try {
					MonitorEnv.getInstance().getServerInfo(serverName).setStatus(MwServerInfo.SERVERDOWN);
					ProcessMonitor.warn("check failed!now begin to stop the server:" + serverName, this, serverName);
					ProcessMonitor.warn("shutdown the server:" + serverName + " immediately with nodeagent", this, serverName);
					// check if generate dump before restart the server.
					if ((MonitorEnv.getInstance().genDumpBeforeRestart() || MonitorEnv.getInstance().genThreadDumpBeforeRestart()) && MonitorEnv.getInstance().isWasServer()
							&& MonitorEnv.getInstance().getServerInfo(serverName).getStatus() != MwServerInfo.SERVERRUNING) {
						ProcessMonitor.warn("begin dump server " + serverName, this, serverName);
						long dumpbegin = System.currentTimeMillis();
						dump(serverName);
						long dumpend = System.currentTimeMillis();
						long dumpcost = dumpend - dumpbegin;
						ProcessMonitor.warn("end dump server " + serverName + " costtime=" + dumpcost, this, serverName);
						sendMsg("[WARNING][mw]" + serverName + " generator thread and heap dump over! costtime=" + dumpcost, false);
					}
					// restart the server.
					long begin = System.currentTimeMillis();
					shutdownByNode(serverName);
					if (MonitorEnv.getInstance().needAdd()) {
						ProcessMonitor.warn("add standby server to cluster!", this, serverName);
						sendMsg("[WARNING]add standby server to cluster!", false);
						PluginAdmin.addServer();
					}
					startByNode(serverName);
					if (MonitorEnv.getInstance().needRemove()) {
						ProcessMonitor.warn("remove standby server to cluster!", this, serverName);
						sendMsg("[WARNING]remove standby server to cluster!", false);
						PluginAdmin.removeServer();
					}
					long end = System.currentTimeMillis();
					long costtime = end - begin;
					ProcessMonitor.warn("restart server " + serverName + " costtime=" + costtime, this, serverName);
					sendMsg("[WARNING][mw]" + serverName + " restart over ,costtime=" + costtime, false);
					// update status.
					updateServerStatus(checkPort());
					sleepBytime(1000 * 60);// for schedule.
				} catch (Exception e) {
					ProcessMonitor.warn(e.getMessage(), this, serverName);
				}
			}
			if (MonitorEnv.getInstance().getServerInfo(serverName).getStatus() == MwServerInfo.SERVERRUNING) {
				MonitorEnv.getInstance().getServerInfo(serverName).setCheckMessage(null);
				MonitorEnv.getInstance().getServerInfo(serverName).setLastcheckts(Toolkit.getCurTime());
			}
		}
	}

	public boolean check() {
		// check schedule.
		if (isUpTime()) {
			return false;
		}
		boolean isHealth = true;
		// check port.
		for (int i = 0; i < MonitorEnv.getInstance().getTryTimes(); i++) {
			isHealth = checkPort();
			if (isHealth)
				break;
			if (!MonitorEnv.getInstance().getServerInfo(serverName).isOffline())
				ProcessMonitor.warn(serverName + ": Response time is more than threshold! check again!", this, serverName);
			sleepBytime(MonitorEnv.getInstance().getIcheckDuration());
		}
		if (!isHealth) {
			sendMsg("[ERROR][mw]" + serverName + " response check failed.", false);
		}
		// check memory.
		if (isHealth) {
			for (int i = 0; i < MonitorEnv.getInstance().getMemTryTimes(); i++) {
				isHealth = checkMem();
				if (isHealth)
					break;
				ProcessMonitor.warn(serverName + ": Memory is less than threshold! check again!", this, serverName);
				sleepBytime(MonitorEnv.getInstance().getIcheckDuration());
			}
			if (!isHealth) {
				sendMsg("[ERROR][mw]" + serverName + " memory check failed.", true);
				if (!MonitorEnv.getInstance().isRestartWhenMemoryCheckFail()) {
					isHealth = true;
				}
			}
		}
		if (isHealth) {
			checkConnectionPool();
		}
		// update server status.
		updateServerStatus(isHealth);
		// sleep when health.
		if (isHealth) {
			sleepBytime(MonitorEnv.getInstance().getIcheckDuration());
		}
		return isHealth;
	}

	private void checkConnectionPool() {
		if (MonitorEnv.getInstance().getConnectionusedlim() < 0)
			return;
		String[] dsnames = MonitorEnv.getInstance().getServerInfo(serverName).getDsNames();
		if (!Toolkit.isEmpty(dsnames)) {
			for (int i = 0; i < dsnames.length; i++) {
				DbSourceInfo dbinfo = MonitorEnv.getInstance().getServerInfo(serverName).getDBSourceinfo(dsnames[i]);
				if (dbinfo == null)
					continue;
				int maxcon = Integer.parseInt(dbinfo.getMaxCon());
				int usedcon = Integer.parseInt(MonitorEnv.getInstance().getServerInfo(serverName).getUsedConnection(dsnames[i]));
				double usedpercent = (double) usedcon / (double) maxcon;
				if (usedpercent * 100 > MonitorEnv.getInstance().getConnectionusedlim()) {
					sendMsg("server[" + serverName + "]数据源" + "[" + dsnames[i] + "]连接池使用超过了" + MonitorEnv.getInstance().getConnectionusedlim() + "%", true);
				}
			}
		}
	}

	private void sendMsg(String msg, boolean needDump) {
		MessageEntry entry = new MessageEntry();
		entry.setMsg(msg);
		entry.setMsgResource(MonitorEnv.getInstance().getResource());
		HashVO vo = new HashVO();
		vo.setAttributeValue("server", new String[]{serverName});
		entry.setTs(Toolkit.getCurTime());
		entry.setVoKeyInfo(vo);
		entry.setNeedDump(needDump);
		MsgSender.send(entry);
		MonitorEnv.getInstance().getServerInfo(serverName).setCheckMessage(msg);
		MonitorEnv.getInstance().getServerInfo(serverName).setLastcheckts(Toolkit.getCurTime());
	}

	private void updateServerStatus(boolean isHealth) {
		int status = MonitorEnv.getInstance().getServerInfo(serverName).getStatus();
		MonitorEnv.getInstance().getServerInfo(serverName).setStatus(isHealth ? MwServerInfo.SERVERRUNING : (status == MwServerInfo.SERVERSTARTING ? status : MwServerInfo.SERVERDOWN));
	}

	private void sleepBytime(long mills) {
		try {
			Thread.sleep(mills);
		} catch (Exception exp) {
		}
	}

	private boolean isUpTime() {
		String clock = MonitorEnv.getInstance().getServerInfo(serverName).getScheduleTime();
		if (clock.equals("25:00")) {
			return false;
		}
		//check clock.
		SimpleDateFormat df = new SimpleDateFormat("HH:mm");
		String curtime = df.format(new Date());
		boolean upclock = curtime.equals(clock);
		if (!upclock)
			return false;
		//check date
		boolean upday = false;
		// get now date.
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date dnow = Calendar.getInstance().getTime();
		String strNow = null;
		try {
			strNow = sdf.format(dnow);
		} catch (Exception e) {
		}
		// compare to old schedule time.
		int days = compareDate(MonitorEnv.getInstance().getServerInfo(serverName).getLastScheduleWorkDate(), strNow);
		// check days interval condition.
		upday = (days == MonitorEnv.getInstance().getServerInfo(serverName).getScheduleDays() || days == -1);
		if (upday) {
			ProcessMonitor.warn("SCHEDULE WORK:it's time to restart server [" + serverName + "] CLOCK:" + curtime, this, serverName);
			// record now.
			MonitorEnv.getInstance().getServerInfo(serverName).setLastScheduleWorkDate(strNow);
		}
		return upday;
	}
	private int compareDate(String old, String now) {
		try {
			if (old == null || old.length() == 0)
				return -1;
			// date now.
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			java.util.Date dnow = Calendar.getInstance().getTime();
			dnow = sdf.parse(sdf.format(dnow));
			// date old.
			Date dold = sdf.parse(old);
			return Math.round(((dnow.getTime() - dold.getTime()) / 1000 / 60 / 60 / 24));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public boolean checkPort() {
		synchronized (this) {
			if (checkT != null && checkT.isAlive()) {
				return checkT.isHealth();
			}
			boolean isHealth = true;
			checkT = new CheckThread(strURL, serverName);
			long begin = System.currentTimeMillis();
			checkT.start();
			try {
				checkT.join(MonitorEnv.getInstance().getNotResponseTimeLim());
			} catch (Exception e) {
				isHealth = false;
			}
			long end = System.currentTimeMillis();
			isHealth = checkT.isHealth() && !checkT.isAlive();
			MonitorEnv.getInstance().getServerInfo(serverName).setLastcheckcost(end - begin);
			return isHealth;
		}
	}

	public boolean checkMem() {
		if (MonitorEnv.getInstance().getNodeAgent(serverName) == null) {
			return true;
		}
		if (checkPort()) {
			double ratio = MonitorEnv.getInstance().getServerInfo(serverName).getJvmRatio();
			return MonitorEnv.getInstance().getMemThreshold() < ratio;
		}
		return false;
	}

	public void shutdownByNode(String serverName) {
		LockGC.lockGC(serverName);
		if (MonitorEnv.getInstance().getNodeAgent(serverName) == null)
			return;
		MonitorEnv.getInstance().getNodeAgent(serverName).shutdownImmediate(serverName);
	}

	public void dump(String serverName) {
		try {
			if (MonitorEnv.getInstance().getNodeAgent(serverName) == null)
				return;
			if (MonitorEnv.getInstance().genDumpBeforeRestart()) {
				MonitorEnv.getInstance().getNodeAgent(serverName).generateHeadDump(serverName);
			}
			if (MonitorEnv.getInstance().genThreadDumpBeforeRestart()) {
				MonitorEnv.getInstance().getNodeAgent(serverName).dumpThread(serverName);
			}
		} catch (Exception e) {
		}
	}

	public void startByNode(String serverName) {
		if (MonitorEnv.getInstance().getNodeAgent(serverName) == null)
			return;
		// check master is started.
		if (!MonitorEnv.getInstance().getServerInfo(serverName).isMaster()) {
			boolean isMasterStarted = false;
			while (!isMasterStarted) {
				ProcessMonitor.warn("master is not started,wait until it started!", this, serverName);
				isMasterStarted = checkMasterStart();
				sleepBytime(5 * 1000l);
			}
		}
		MonitorEnv.getInstance().getServerInfo(serverName).setStatus(MwServerInfo.SERVERSTARTING);
		MonitorEnv.getInstance().getNodeAgent(serverName).startup(serverName);
		MonitorEnv.getInstance().initThreadPool(serverName);
		// LockGC.lockGC(serverName);
	}

	private boolean checkMasterStart() {
		if (MonitorEnv.getInstance().getMaster() == null || MonitorEnv.getInstance().getMaster().getMonitorThread() == null)
			return true;
		return MonitorEnv.getInstance().getMaster().getMonitorThread().checkPort();
	}
}
