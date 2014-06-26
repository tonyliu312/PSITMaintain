package uap.ps.itm.monitor.service.process;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import nc.bs.framework.mx.thread.RequestEntry;
import uap.ps.itm.monitor.RemoteException;
import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.IConfigConst;
import uap.ps.itm.monitor.pub.LogTool;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.center.IServer;
import uap.ps.itm.monitor.service.center.ServerEnv;
import uap.ps.itm.monitor.service.center.ServiceDesc;
import uap.ps.itm.monitor.service.center.ServiceHolder;
import uap.ps.itm.monitor.service.center.ServiceResource;
import uap.ps.itm.monitor.service.center.Socket;
import uap.ps.itm.monitor.service.db.DBExecutor;
import uap.ps.itm.monitor.service.db.DBSourceInfoHolder;
import uap.ps.itm.monitor.service.db.DbLogRunner;
import uap.ps.itm.monitor.service.db.DbSourceInfo;
import uap.ps.itm.monitor.service.db.GenerateIndexFactory;
import uap.ps.itm.monitor.service.db.IGenerateIndex;
import uap.ps.itm.monitor.service.message.MessageEntry;
import uap.ps.itm.monitor.service.message.MsgSender;
import uap.ps.itm.pub.servlet.URLCommuniatcor;

public class ProcessMonitor extends Thread implements IServer {
	public String javahome;

	private static Logger logger = null;


	private boolean checkWasOver = false;

	private static Map<String, HashVO[]> datasourceMapHashvo = new HashMap<String, HashVO[]>();// 记录该数据源的这几个指标值

	private static Map<String, DBExecutor> datasourceMapExcutor = new HashMap<String, DBExecutor>();

	private final static String addindex = "'redo size','user commits','physical reads','physical writes','user calls','db block gets','sorts (disk)'";

	public ProcessMonitor() {
		super();
	}

	@Override
	public void run() {
		setName("MainThread");
		Properties prop = ServerEnv.getInstance().getProp();
		String nodeagentserver = (prop.getProperty("nodeagenthost") == null ? null
				: prop.getProperty("nodeagenthost").toString());
		String nodeagentport = (prop.getProperty("nodeagentport") == null ? "8879"
				: prop.getProperty("nodeagentport").toString());
		String adminUsername = (prop.getProperty("adminusername") == null ? null
				: prop.getProperty("adminusername").toString());
		String adminPassword = (prop.getProperty("adminpassword") == null ? null
				: prop.getProperty("adminpassword").toString());
		String servertype = prop.getProperty("servertype", "was");
		MonitorEnv.getInstance().setServertype(servertype);
		if (Toolkit.isEmpty(nodeagentserver)) {
			MonitorEnv.getInstance().setWasServer(false);
		} else {
			warn(servertype + " node agent:" + nodeagentserver + ":"
					+ nodeagentport, this, null);
			try {
				MonitorEnv.getInstance().initNodeAgent(nodeagentserver,
						nodeagentport, adminUsername, adminPassword);
				MonitorEnv.getInstance().setWasServer(true);
			} catch (Exception e) {
				// warn(e.getMessage(),this,null);
				warn("################################", this, null);
				warn("nodeagent is not started!so it can not manager websphere!try to startup nodeagent or dm server\n if you want to manager was.",
						this, null);
				warn("################################", this, null);
				MonitorEnv.getInstance().setWasServer(false);
				// return;
			}
		}
		checkWasOver = true;
		loadCfg();
		// init profile.
		String propFile = prop.getProperty("propfile");
		if (propFile == null) {
			warn("property profile can't be null.set propfile local", this,
					null);
			propFile = "../../ierp/bin/prop.xml";
		}
		warn("propfile=" + propFile, this, null);
		try {
			MonitorEnv.getInstance().initServerInfoByfile(propFile);
		} catch (Exception e) {
			e.printStackTrace();
			warn("init propfile error.check the propfile:" + propFile, this,
					null);
			return;
		}
		startMonitor();
		if (MonitorEnv.getInstance().needAdd()) {
			PluginAdmin.addServer();
			warn("add standby server over!", this, null);
		}
		if (MonitorEnv.getInstance().needRemove()) {
			PluginAdmin.removeServer();
			warn("remove standby server over!", this, null);
		}
		// env.registerMBean(this, "owind:Name=NCMonitor");
		String logDuration = prop.getProperty("logDuration");
		warn("log duratioin:" + logDuration + "s", this, null);
		long sleeptime = (logDuration == null ? 1000 * 30 : Long
				.parseLong(logDuration) * 1000);
		while (!MonitorEnv.getInstance().isShutdown()) {
			String[] servers = MonitorEnv.getInstance().getServerNames();
			for (int i = 0; i < servers.length; i++) {
				MwServerInfo serverinfo = MonitorEnv.getInstance()
						.getServerInfo(servers[i]);
				if (serverinfo.getStatus() != MwServerInfo.SERVERRUNING) {
					continue;
				}
				HashVO vo = new HashVO();
				vo.setAttributeValue("ts", Toolkit.getCurTime());
				vo.setAttributeValue("servername", servers[i]);
				vo.setAttributeValue("max", serverinfo.getJvmmax());
				vo.setAttributeValue("total", serverinfo.getJvmtotal());
				vo.setAttributeValue("free", serverinfo.getJvmfree());
				Double ratio = Toolkit.getdouble(serverinfo.getJvmfree())
						/ Toolkit.getdouble(serverinfo.getJvmtotal());
				vo.setAttributeValue("ratio", ratio);
				vo.setAttributeValue("wcthreadnum",
						serverinfo.getThreadPoolUsed());
				vo.setAttributeValue("servicethreadnum",
						serverinfo.getServicenum());
				vo.setAttributeValue("wcrange", serverinfo.getTreadPoolRange());
				vo.setAttributeValue("respcost", serverinfo.getLastcheckcost());
				vo.setAttributeValue("throughput", serverinfo.getThroughput());
				log(vo.toString0(), servers[i]);
			}
			sleepBytime(sleeptime);
		}
	}

	public void loadCfg() {
		Properties prop = ServerEnv.getInstance().getProp();
		MonitorEnv.getInstance().setNcAdminPassword(
				prop.getProperty("ncadminpassword"));
		MonitorEnv.getInstance().setServlet(prop.getProperty("servlet"));
		String strChecktimes = prop.getProperty("fail.trytimes");
		MonitorEnv.getInstance().setChecktimes(
				strChecktimes == null ? 3 : Integer.parseInt(strChecktimes));
		warn("fail.trytimes=" + strChecktimes, this, null);
		String notResponselim = prop.getProperty("notResponseThreshold");
		MonitorEnv.getInstance().setNotResponseTimeLim(
				notResponselim == null ? 4000 : Integer
						.parseInt(notResponselim));
		warn("notResponseThreshold=" + notResponselim + "ms.", this, null);
		String memthreshold = prop.getProperty("MemoryThreshold");
		double memthre = Double.parseDouble(memthreshold == null ? "0.01"
				: memthreshold);
		warn("memory threshold=" + memthre, this, null);
		MonitorEnv.getInstance().setMemThreshold(memthre);
		String sMemTrytimes = prop.getProperty("MemoryTrytimes");
		int memTrytimes = Integer.parseInt(sMemTrytimes == null ? "10"
				: sMemTrytimes);
		warn("memory trytimes=" + memTrytimes, this, null);
		MonitorEnv.getInstance().setMemTryTimes(memTrytimes);
		// check failure stragy.
		String restartflag = prop
				.getProperty("restartWhenCheckResponsefailure");
		restartflag = (restartflag == null ? "true" : restartflag);
		warn("restartWhenCheckResponsefailure=" + restartflag, this, null);
		MonitorEnv.getInstance().setRestartWhenResponseCheckFail(
				Boolean.parseBoolean(restartflag));
		restartflag = prop.getProperty("restartWhenCheckMemoryfailure");
		restartflag = (restartflag == null ? "true" : restartflag);
		warn("restartWhenCheckMemoryfailure=" + restartflag, this, null);
		MonitorEnv.getInstance().setRestartWhenMemoryCheckFail(
				Boolean.parseBoolean(restartflag));
		restartflag = prop.getProperty("dumpHeapWhenRestart");
		restartflag = (restartflag == null ? "false" : restartflag);
		warn("dumpHeapWhenRestart=" + restartflag, this, null);
		MonitorEnv.getInstance().setGenHeapDumpBeforeRestart(
				Boolean.parseBoolean(restartflag));
		restartflag = prop.getProperty("dumpThreadWhenRestart");
		restartflag = (restartflag == null ? "false" : restartflag);
		warn("dumpThreadWhenRestart=" + restartflag, this, null);
		MonitorEnv.getInstance().setGenThreadDumpBeforeRestart(
				Boolean.parseBoolean(restartflag));
		String connectPoolusedlim = ServerEnv.getInstance().getProperty(
				"MW_THREAD_CHECK_CONNECTIONPOOLUSED");
		if (connectPoolusedlim != null) {
			MonitorEnv.getInstance().setConnectionusedlim(
					Double.parseDouble(connectPoolusedlim));
			warn("connection pool used limited=" + connectPoolusedlim + "%",
					this, null);
		}
		String n1_pluginfile = prop.getProperty("N1_PLUGIN_FILE");
		warn("N1_PLUGIN_FILE=" + n1_pluginfile, this, null);
		PluginAdmin.pluginCfgfile = n1_pluginfile;
		String n1_standbyserver_key = prop.getProperty("N1_STANDBYSERVER_KEY");
		warn("N1_STANDBYSERVER_KEY=" + n1_standbyserver_key, this, null);
		PluginAdmin.key = n1_standbyserver_key;
	}

	public void startMonitor() {
		String[] serverNames = MonitorEnv.getInstance().getServerNames();
		for (int i = 0; i < serverNames.length; i++) {
			MonitorThread monitorThread = new MonitorThread(serverNames[i]);
			MonitorEnv.getInstance().getServerInfo(serverNames[i])
					.setMonitorThread(monitorThread);
			monitorThread.start();
		}
		PurgeSnapshotThread.getInstance().setRun(true);
		PurgeSnapshotThread.getInstance().start();

	}

	public static void log(String msg, String serverName) {
		getLogger(serverName).info(msg);
	}


	public static Logger getLogger(String serverName) {
		if (MonitorEnv.getInstance().getServerInfo(serverName) == null) {
			if (logger == null) {
				logger = LogTool.createLogger("mw", "mw", "main");
			}
			return logger;
		} else {
			return MonitorEnv.getInstance().getServerInfo(serverName)
					.getLogger();
		}
	}

	public static void warn(String msg, Thread t, String serverName) {
		if (t != null && t instanceof ProcessMonitor) {
			String Msg = (t == null ? "[WASAgent]" : "[" + t.getName() + " "
					+ Toolkit.getCurTime() + "]");
			System.out.println(Msg + msg);
		}
		HashVO vo = new HashVO();
		vo.setAttributeValue("ts", Toolkit.getCurTime());
		vo.setAttributeValue("server", serverName);
		vo.setAttributeValue("msg", msg);
		msg = vo.toString0();
		log(msg, serverName);
	}

	public void restart(String serverName) {
		warn("restart server :" + serverName, this, serverName);
		if (serverName == null)
			return;
		if (!isManagServer(serverName) && !serverName.equals("all"))
			return;
		String[] serverNames = new String[] { serverName };
		if (serverName.equals("all"))
			serverNames = MonitorEnv.getInstance().getServerNames();
		for (int i = 0; i < serverNames.length; i++) {
			if (MonitorEnv.getInstance().getServerInfo(serverNames[i])
					.getStatus() != MwServerInfo.SERVERRUNING) {
				warn(serverNames[i] + " is not running!cannot restart.", this,
						serverName);
				continue;
			}
			shutdown(serverNames[i]);
			startup(serverNames[i]);
		}
	}

	public void shutdown(String serverName) {
		LockGC.lockGC(serverName);
		warn("shutdown server :" + serverName, this, serverName);
		if (serverName == null)
			return;
		if (!isManagServer(serverName))
			return;
		if (MonitorEnv.getInstance().getServerInfo(serverName).getStatus() != MwServerInfo.SERVERRUNING) {
			warn(serverName + " is not running!no need to shutdown.", this,
					serverName);
			return;
		}

		MonitorEnv.getInstance().getServerInfo(serverName).setOffline(true);
		MonitorEnv.getInstance().getNodeAgent(serverName)
				.shutdownImmediate(serverName);
		MonitorEnv.getInstance().getServerInfo(serverName)
				.setStatus(MwServerInfo.SERVERDOWN);
		if (MonitorEnv.getInstance().needAdd()) {
			PluginAdmin.addServer();
		}
	}

	public void startup(String serverName) {
		warn("startup server" + serverName, this, null);
		if (serverName == null)
			return;
		if (!isManagServer(serverName))
			return;
		if (MonitorEnv.getInstance().getServerInfo(serverName).getStatus() != MwServerInfo.SERVERDOWN) {
			warn(serverName + " is not shutdown!no need to startup.", this,
					serverName);
			return;
		}
		MonitorEnv.getInstance().getServerInfo(serverName)
				.setStatus(MwServerInfo.SERVERSTARTING);
		MonitorEnv.getInstance().getNodeAgent(serverName).startup(serverName);
		MonitorEnv.getInstance().getServerInfo(serverName).setOffline(false);
		MonitorEnv.getInstance().initThreadPool(serverName);
		if (MonitorEnv.getInstance().needRemove()) {
			PluginAdmin.removeServer();
		}
		// LockGC.lockGC(serverName);
	}

	public boolean isManagServer(String serverName) {
		if (MonitorEnv.getInstance().getNodeAgent(serverName) == null)
			return false;
		if (serverName == null)
			return false;
		String[] names = MonitorEnv.getInstance().getServerNames();
		for (int i = 0; i < names.length; i++) {
			if (names[i].equals(serverName))
				return true;
		}
		warn(serverName + " is not a manage server!", this, serverName);
		return false;
	}

	public void dumpThreads(String serverName) {
		warn("dumpThreads for " + serverName, this, serverName);
		if (serverName == null)
			return;
		if (!isManagServer(serverName))
			return;
		MonitorEnv.getInstance().getNodeAgent(serverName)
				.dumpThread(serverName);
	}

	public Object generateHeapDump(String serverName) {
		warn("generate HeapDump.", this, null);
		warn("generateHeapDump for " + serverName, this, serverName);
		if (serverName == null)
			return null;
		if (!isManagServer(serverName))
			return null;
		return MonitorEnv.getInstance().getNodeAgent(serverName)
				.generateHeadDump(serverName);
	}

	public String showServerDetail(String serverName) {
		warn("begin exec showserverdetail time.", this, null);
		if (serverName == null)
			return "";
		StringBuffer sb = new StringBuffer("\n==================\n");
		sb.append("SName\tStatus\tSocket\t\tschedule\tLChkCost(ms)\tRTimes\tLRDate\n");
		if (!serverName.equals("all"))
			sb.append(MonitorEnv.getInstance().getServerInfo(serverName)
					.getServerDetail());
		else {
			String[] serverNames = MonitorEnv.getInstance().getServerNames();
			for (int i = 0; i < serverNames.length; i++) {
				sb.append((MonitorEnv.getInstance().getServerInfo(
						serverNames[i]).getServerDetail()));
				sb.append("\n");

			}
		}
		sb.append("\n==================");
		return sb.toString();
	}

	private void sleepBytime(long mills) {
		try {
			Thread.sleep(mills);
		} catch (Exception e) {
		}
	}

	public String getJDBCPool(String serverName){
		return MonitorEnv.getInstance().getServerInfo(serverName).getWasJmxClient().getJDBCPool(serverName);
	}

	public void scheduleRestart(String serverName, String time) {
		System.out.println("begin exec schedule time.serverName=" + serverName);
		if (serverName == null || time == null)
			return;
		if (!isManagServer(serverName))
			return;
		warn("schedule time:" + serverName + "\t" + time, this, serverName);
		StringTokenizer st = new StringTokenizer(time, "$");
		String clock = st.nextToken();
		String days = "0";
		if (st.hasMoreTokens()) {
			days = st.nextToken();
		}
		MonitorEnv.getInstance().getServerInfo(serverName)
				.setScheduleTime(clock.trim());
		MonitorEnv.getInstance().getServerInfo(serverName)
				.setScheduleDays(Integer.parseInt(days));
		MonitorEnv.getInstance().getServerInfo(serverName)
				.setLastScheduleWorkDate(null);
		HashVO scheduleVO = new HashVO();
		scheduleVO.setAttributeValue("servername", serverName);
		scheduleVO.setAttributeValue("scheduletime", clock.trim());
		scheduleVO.setAttributeValue("scheduledays", days);
		ServerSchedule.getInstance().addSchedule(scheduleVO);
		ServerSchedule.getInstance().saveServerSchedule();

	}

	public MwServerInfo[] queryServerInfo() {
		return MonitorEnv.getInstance().getAllServerInfo();
	}

	@Override
	public void startServer() {
		this.start();
		new DbLogRunner().start();
	}

	@Override
	public void stopServer() {
		System.out.println("begin to stop ProcessMonitor...");
		MonitorEnv.getInstance().setShutdown(true);
		String[] names = MonitorEnv.getInstance().getServerNames();
		for (int i = 0; i < names.length; i++) {
			MonitorEnv.getInstance().getNodeAgent(names[i]).stop();
		}
		PurgeSnapshotThread.getInstance().setRun(false);
	}

	@Override
	public ServiceResource[] getServerResouce(ServiceDesc serviceDesc) {
		// 等待直到确认是否是was server.
		while (!checkWasOver) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
		}
		ServiceResource resource = new ServiceResource();
		resource.setDisplayName("MW-" + ServerEnv.getHostName());
		resource.setHost(ServerEnv.getHostName());
		resource.setPort(ServerEnv.getInstance().getLocalSocket().getPort());
		resource.setRequestUrl(ServerEnv.getInstance().getLocalUrl());
		resource.setServiceName(IConfigConst.SERVICEPROCESSMONITOR);
		resource.setServiceDesc(serviceDesc);
		resource.setInstanceName(MonitorEnv.getInstance().isWasServer() ? "was"
				: "nc");
		MonitorEnv.getInstance().setResource(resource);
		if (Toolkit.isEmpty(queryDBInfo())) {
			return new ServiceResource[] { resource };
		}
		ServiceResource resourcedb = new ServiceResource();
		resourcedb.setDisplayName("DB-" + ServerEnv.getHostName());
		resourcedb.setHost(ServerEnv.getHostName());
		resourcedb.setPort(ServerEnv.getInstance().getLocalSocket().getPort());
		resourcedb.setRequestUrl(ServerEnv.getInstance().getLocalUrl());
		resourcedb.setServiceName(IConfigConst.SERVICEPROCESSMONITOR);
		resourcedb.setServiceDesc(serviceDesc);
		return new ServiceResource[] { resource, resourcedb };
	}

	// 为了oracle新增几个指标所做的改动
	public HashVO[] queryDB() throws RemoteException {
		DbSourceInfo[] dbs = DBSourceInfoHolder.getInstance()
				.getAllServerInfo();
		HashVO[] vos = new HashVO[dbs.length];
		for (int i = 0; i < dbs.length; i++) {
			DbSourceInfo db = dbs[i];
			vos[i] = new HashVO();
			vos[i].setAttributeValue(
					"数据源", db.getDatasourcename());
			vos[i].setAttributeValue(
					"数据库类型", db.getDbtype());
			vos[i].setAttributeValue(
					"连接字符串", db.getUrl());
			vos[i].setAttributeValue(
					"连接池大小", db.getMinCon() + "~" + db.getMaxCon());
			vos[i].setAttributeValue("DBActiveSessions", db.getActiveSession());
			vos[i].setAttributeValue(
					"最近检查时间",
					db.getLastcheckts() == null ? "n/a" : db.getLastcheckts());
			vos[i].setAttributeValue(
					"最新警告",
					db.getLastcheckMsg() == null ? "n/a" : db.getLastcheckMsg());
			vos[i].setAttributeValue("redo size", "n/a");
			vos[i].setAttributeValue("user commits", "n/a");
			vos[i].setAttributeValue("physical reads", "n/a");
			vos[i].setAttributeValue("physical writes", "n/a");
			vos[i].setAttributeValue("user calls", "n/a");
			vos[i].setAttributeValue("db block gets", "n/a");
			vos[i].setAttributeValue("sorts (disk)", "n/a");

			String datasourcename = db.getDatasourcename();
			String dbty = db.getDbtype();
			if (dbty.toLowerCase().indexOf("orac") == -1) {
				continue;
			} else {
				computeIndex(db, vos[i]);
			}
		}
		return vos;
	}

	public DbSourceInfo[] queryDBInfo() {
		DbSourceInfo[] dbs = DBSourceInfoHolder.getInstance()
				.getAllServerInfo();

		return dbs;
	}

	private void initialAddIndex(DbSourceInfo[] dbs) {
		for (int i = 0; i < dbs.length; i++) {
			DbSourceInfo db = dbs[i];
			String dbtype = db.getDbtype();
			if (dbtype.indexOf("oracl".toUpperCase()) == -1) {
				continue;
			}
			String datasourcename = dbs[i].getDatasourcename();
			DBExecutor excutor = datasourceMapExcutor.get(db
					.getDatasourcename());
			if (excutor == null) {
				excutor = new DBExecutor(db.getDriverClassName(), db.getUrl(),
						db.getUsername(), db.getPassword());
				datasourceMapExcutor.put(db.getDatasourcename(), excutor);
			}
			try {
				HashVO[] addindexvos = excutor
						.queryVOBySql("select name,value from v$sysstat where name in ("
								+ addindex + ")order by name");
				datasourceMapHashvo.put(datasourcename, addindexvos);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	private void computeIndex(DbSourceInfo db, HashVO vo)
			throws RemoteException {
		DBExecutor excutor = datasourceMapExcutor.get(db.getDatasourcename());
		if (excutor == null) {
			excutor = new DBExecutor(db.getDriverClassName(), db.getUrl(),
					db.getUsername(), db.getPassword());
			datasourceMapExcutor.put(db.getDatasourcename(), excutor);
		}
		try {
			HashVO[] addindexvos = excutor
					.queryVOBySql("select name,value from v$sysstat where name in ("
							+ addindex + ")order by name");
			HashVO[] oldindexs = datasourceMapHashvo
					.get(db.getDatasourcename());
			boolean isFirstflag = false;
			if (Toolkit.isEmpty(oldindexs)) {
				isFirstflag = true;
			}
			for (int i = 0; i < addindexvos.length; i++) {
				String name = addindexvos[i].getStringValue("name"
						.toUpperCase());
				String value = addindexvos[i].getStringValue("value"
						.toUpperCase());
				Long varvalue = new Long(0);
				if (!isFirstflag)
					varvalue = (Long.parseLong(value) - Long
							.parseLong(oldindexs[i].getStringValue("value"
									.toUpperCase()))) / 5;
				vo.setAttributeValue(name, varvalue);
			}
			datasourceMapHashvo.put(db.getDatasourcename(), addindexvos);

		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		}

	}

	public void gc(String serverName) {
		if (serverName == null)
			return;
		MwServerInfo serverinfo = MonitorEnv.getInstance().getServerInfo(
				serverName);
		if (serverinfo == null)
			return;
		if (serverinfo.getStatus() != MwServerInfo.SERVERRUNING)
			return;

		String url = "http://" + serverinfo.getHost() + ":"
				+ serverinfo.getPort() + "/service/monitorservlet";
		RequestEntry entry = new RequestEntry();
		entry.setCmd("gc");
		URLCommuniatcor.remoteCall(entry, url);
	}
	public void setSqlBufferSize(String serverName,String size) {
		if (serverName == null)
			return;
		MwServerInfo serverinfo = MonitorEnv.getInstance().getServerInfo(
				serverName);
		if (serverinfo == null)
			return;
		if (serverinfo.getStatus() != MwServerInfo.SERVERRUNING)
			return;

		String url = "http://" + serverinfo.getHost() + ":"
				+ serverinfo.getPort() + "/service/monitorservlet";
		RequestEntry entry = new RequestEntry();
		entry.setCmd("sqlbuffersize");
		//zjs
//		entry.setParameter(size);
		URLCommuniatcor.remoteCall(entry, url);
	}

	public Object getSqlBufferSize(String serverName) {
		if (serverName == null)
			return null;
		MwServerInfo serverinfo = MonitorEnv.getInstance().getServerInfo(
				serverName);
		if (serverinfo == null)
			return null;
		if (serverinfo.getStatus() != MwServerInfo.SERVERRUNING)
			return null;
		String url = "http://" + serverinfo.getHost() + ":"
				+ serverinfo.getPort() + "/service/monitorservlet";
		RequestEntry entry = new RequestEntry();
		entry.setCmd("getsqlbuffersize");
		return URLCommuniatcor.remoteCall(entry, url);
	}
	public void 	snapshot(String serverName) {
		if (serverName == null)
			return;
		MwServerInfo serverinfo = MonitorEnv.getInstance().getServerInfo(
				serverName);
		if (serverinfo == null)
			return;
		MessageEntry entry=new MessageEntry();
		entry.setMsg("主动生成快照:"+serverName);
		entry.setNeedDump(true);
		ServiceDesc serviceDesc=new ServiceDesc();
		serviceDesc.setServiceName(serverName);
		ServiceResource[] resources=ServiceHolder.getInstance().getServer("mwmonitor").getServerResouce(serviceDesc);
		if(Toolkit.isEmpty(resources)){
			return;
		}
		entry.setMsgResource(resources[0]);
		MsgSender.send(entry);
	}


	public Socket getWebServer() {
		return MonitorEnv.getInstance().getWebServer();
	}

	public HashVO[] generateIndex(String datasourcename) throws RemoteException {
		DbSourceInfo[] infos = DBSourceInfoHolder.getInstance()
				.getAllServerInfo();
		DbSourceInfo selecteddb = null;
		for (DbSourceInfo info : infos) {
			String name = info.getDatasourcename();
			if (datasourcename.equals(name)) {
				selecteddb = info;
				break;
			}
		}
		DBExecutor excutor = new DBExecutor(selecteddb.getDriverClassName(),
				selecteddb.getUrl(), selecteddb.getUsername(),
				selecteddb.getPassword());
		String url = selecteddb.getUrl();
		int dbtype = url.indexOf("oracle") > -1 ? IConfigConst.ORACLE : (url
				.indexOf("db2") > -1 ? IConfigConst.DB2
				: IConfigConst.SQLSERVER);
		IGenerateIndex indexgenerate = GenerateIndexFactory
				.createGenerateIndex(dbtype);
		return indexgenerate.generateIndex(excutor);

	}

	@Override
	public void refreshEnv() {
		loadCfg();

		try {
			long hour = Long.parseLong(ServerEnv.getInstance().getProp()
					.getProperty("PURGE_SNAPSHOT_HOUR", "0"));
			PurgeSnapshotThread.purgeSnapshotHour = hour;
		} catch (Exception e) {
		}
	}
}
