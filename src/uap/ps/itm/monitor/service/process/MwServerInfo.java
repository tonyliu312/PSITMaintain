package uap.ps.itm.monitor.service.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.management.ObjectName;

import uap.ps.itm.monitor.pub.LogTool;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.center.ServerEnv;
import uap.ps.itm.monitor.service.db.DBSourceInfoHolder;
import uap.ps.itm.monitor.service.db.DbSourceInfo;

public class MwServerInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private String serverName = null;

	private int status = SERVERRUNING;

	private String scheduleTime = null;

	public static final int SERVERDOWN = 0;
	public static final int SERVERRUNING = 1;
	public static final int SERVERSTARTING = 2;
	public static final String[] SERVERSTATUSALL = new String[]{"down", "running", "starting"};

	private boolean isOffline = false;

	private ObjectName nodeagent = null;

	private String host = null;

	private int port;

	private long lastcheckcost = 0;
	private String lastScheduleWorkDate = null;
	private int scheduleDays = 0;
	private int restartTimes = 0;

	private String lastRestartDate = null;

	private transient Logger logger = null;

	private transient IMwAdmin wasClient = null;

	private String treadPoolRange = null;

	private String threadPoolUsed = null;
	private String servicenum = null;
	private boolean isWAS = false;

	private String jvmmax = null;

	private String jvmtotal = null;

	private String jvmfree = null;

	private String jvmused = null;

	private boolean isMaster = false;
	private transient MonitorThread monitorThread = null;
	private String hostname = null;
	private Hashtable<String, String> hDatasourceOpenconnection = new Hashtable<String, String>();
	private Hashtable<String, DbSourceInfo> hDBSourceinfo = new Hashtable<String, DbSourceInfo>();
	private String checkMessage = null;
	private String lastcheckts = null;
	
	/**
	 * 服务器的吞吐量
	 */
	private int throughput = 0;

	public int getThroughput() {
		return throughput;
	}

	public void setThroughput(int throughput) {
		this.throughput = throughput;
	}

	public String getCheckMessage() {
		return checkMessage;
	}

	public void setCheckMessage(String checkMessage) {
		this.checkMessage = checkMessage;
	}

	public String getLastcheckts() {
		return lastcheckts;
	}

	public void setLastcheckts(String lastcheckts) {
		this.lastcheckts = lastcheckts;
	}

	public String getJvmfree() {
		return jvmfree;
	}

	public String[] getDsNames() {
		Set<String> set = hDatasourceOpenconnection.keySet();
		Iterator<String> iter = set.iterator();
		ArrayList<String> al = new ArrayList<String>();
		while (iter.hasNext()) {
			al.add(iter.next());
		}
		return al.toArray(new String[al.size()]);
	}

	public DbSourceInfo getDBSourceinfo(String dbname) {
		if (dbname == null)
			return null;
		return hDBSourceinfo.get(dbname);
	}

	public String getUsedConnection(String dsName) {
		if (dsName == null)
			return "0";
		Object obj = hDatasourceOpenconnection.get(dsName);
		if (obj == null)
			return "0";
		return (String) obj;
	}

	public void setDsUsed(String dsName, String count) {
		if (Toolkit.isEmpty(dsName))
			return;
		hDatasourceOpenconnection.put(dsName, count);
		if (hDBSourceinfo.get(dsName) == null) {
			hDBSourceinfo.put(dsName, DBSourceInfoHolder.getInstance().getDBserverInfo(dsName));
		}
	}

	public void setJvmfree(String jvmfree) {
		this.jvmfree = jvmfree;
	}

	public String getJvmmax() {
		return jvmmax;
	}

	public void setJvmmax(String jvmmax) {
		this.jvmmax = jvmmax;
	}

	public String getJvmtotal() {
		return jvmtotal;
	}

	public void setJvmtotal(String jvmtotal) {
		this.jvmtotal = jvmtotal;
	}

	public String getJvmused() {
		return jvmused;
	}

	public void setJvmused(String jvmused) {
		this.jvmused = jvmused;
	}

	public void setWasJmxClient(IMwAdmin client) {
		wasClient = client;
	}

	public IMwAdmin getWasJmxClient() {
		return wasClient;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
		if (ServerEnv.isLocalHost(host)) {
			this.hostname = ServerEnv.getHostName();
		} else {
			this.hostname = host;
		}
	}

	public String getHostName() {
		return hostname;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getScheduleTime() {
		return scheduleTime;
	}

	public void setScheduleTime(String scheduleTime) {
		this.scheduleTime = scheduleTime;
	}

	public boolean isOffline() {
		return isOffline;
	}

	public void setOffline(boolean isOffline) {
		this.isOffline = isOffline;
	}

	public ObjectName getNodeagent() {
		return nodeagent;
	}

	public void setNodeagent(ObjectName nodeagent) {
		this.nodeagent = nodeagent;
	}

	public long getLastcheckcost() {
		return lastcheckcost;
	}

	public void setLastcheckcost(long lastcheckcost) {
		this.lastcheckcost = lastcheckcost;
	}

	public int getRestartTimes() {
		return restartTimes;
	}

	public void setRestartTimes(int restartTimes) {
		this.restartTimes = restartTimes;
	}

	public String getLastRestartDate() {
		return lastRestartDate;
	}

	public void setLastRestartDate(String lastRestartDate) {
		this.lastRestartDate = lastRestartDate;
	}

	public String getServerDetail() {
		StringBuffer sb = new StringBuffer();
		sb.append(getServerName());
		sb.append("\t" + SERVERSTATUSALL[getStatus()]);
		sb.append("\t" + getHost() + ":" + getPort());
		sb.append("\t" + getScheduleTime());
		sb.append("\t" + getLastcheckcost());
		sb.append("\t" + getRestartTimes());
		sb.append("\t" + getLastRestartDate());
		// 吞吐量
		sb.append("\t" + getThroughput());
		return sb.toString();
	}

	public String getNodeAgentMame() {
		if (getNodeagent() == null)
			return null;
		StringTokenizer st = new StringTokenizer(getNodeagent().getCanonicalName(), ",");
		String prop = null;
		while ((prop = st.nextToken()) != null) {
			if (prop.substring(0, 5).equals("node=")) {
				String nodeagentName = prop.substring(5, prop.length());
				return nodeagentName;
			}
		}
		return null;
	}

	public void addRestartTimes() {
		restartTimes++;
	}

	public synchronized Logger getLogger() {
		if (logger == null) {
			String filename = serverName + "-" + getHost() + "-" + getPort();
			logger = LogTool.createLogger("mw", "mw", filename);
		}
		return logger;
	}
	public String getTreadPoolRange() {
		return treadPoolRange;
	}

	public void setTreadPoolRange(String treadPoolRange) {
		this.treadPoolRange = treadPoolRange;
	}

	public String getThreadPoolUsed() {
		return threadPoolUsed;
	}

	public void setThreadPoolUsed(String threadPoolUsed) {
		this.threadPoolUsed = threadPoolUsed;
	}

	public boolean isWAS() {
		return isWAS;
	}

	public void setWAS(boolean isWAS) {
		this.isWAS = isWAS;
	}

	public boolean isMaster() {
		return isMaster;
	}

	public void setMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}

	public MonitorThread getMonitorThread() {
		return monitorThread;
	}

	public void setMonitorThread(MonitorThread monitorThread) {
		this.monitorThread = monitorThread;
	}

	public Double getJvmRatio() {
		return Toolkit.getdouble(getJvmfree()) / Toolkit.getdouble(getJvmtotal());
	}

	public String getServicenum() {
		return servicenum;
	}

	public void setServicenum(String servicenum) {
		this.servicenum = servicenum;
	}

	public String getToolTipText() {
		StringBuffer sb = new StringBuffer();
		sb.append("服务器地址").append(":").append(getHost());
		sb.append("\n");
		sb.append("端口").append(":").append(getPort());
		sb.append("\n");
		sb.append("是否主服务器").append(":").append(isMaster());
		sb.append("\njvmmax:").append(getJvmmax());
		sb.append("\njvmtotal:").append(getJvmtotal());
		sb.append("\njvmused:").append(getJvmused());
		sb.append("\njvmfree:").append(getJvmfree());
		sb.append("\n");
		sb.append("线程池大小").append(":").append(getTreadPoolRange());
		sb.append("\n");
		sb.append("线程池已用").append(":").append(getThreadPoolUsed());
		sb.append("\n");
		sb.append("上次检查时间").append(":").append(getLastcheckts());
		sb.append("\n");
		sb.append("上次检查结果").append(":").append(getCheckMessage());
		sb.append("\n");
		sb.append("吞吐量").append(":").append(getThroughput());
		return sb.toString();
	}

	public String getLastScheduleWorkDate() {
		return lastScheduleWorkDate;
	}

	public void setLastScheduleWorkDate(String lastScheduleWorkDate) {
		this.lastScheduleWorkDate = lastScheduleWorkDate;
	}

	public int getScheduleDays() {
		return scheduleDays;
	}

	public void setScheduleDays(int scheduleDays) {
		this.scheduleDays = scheduleDays;
	}

}
