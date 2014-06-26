package uap.ps.itm.monitor.service.process;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import nc.bs.framework.server.cluster.ClusterInfo;
import nc.bs.framework.server.cluster.ClusterMemberInfo;
import nc.bs.framework.server.cluster.WebServerInfo;
import nc.vo.jcom.xml.XMLToObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.pub.Xml2HashVO;
import uap.ps.itm.monitor.service.center.ServiceResource;
import uap.ps.itm.monitor.service.center.Socket;

public class MonitorEnv extends Thread {
	private int icheckDuration = 3000;

	private int iNotResponseTime = 10000;

	private int iTryTimes = 10;

	private int OSTYPE = WINDOWS;

	public final static int WINDOWS = 0;

	public final static int UNIX = 1;

	private static MonitorEnv env = null;

	private static boolean bShutdown = false;

	private String javahome = null;

	private double dMemoryThreshold = 0.95;

	private int iMemTryTimes = 10;

	private Hashtable<String, Object> hServerInfo = new Hashtable<String, Object>();

	private String agentHost = null;

	private String agentPort = null;

	private String agentUser = null;

	private String agentPassword = null;

	private String ncAdminPassword = null;

	private String servlet = null;

	private boolean isWasServer = true;

	private boolean restartWhenResponseCheckFail = true;

	private boolean restartWhenMemoryCheckFail = false;

	private ServiceResource resource;

	private boolean genDumpBeforeRestart = false;

	private double connectionusedlim = 50d;

	private boolean isInited = false;

	private Socket httpServer = null;

	private boolean genThreadDumpBeforeRestart = false;

	private String servertype = "nc";// nc,was,wls

	public Socket getWebServer() {
		return httpServer;
	}

	public boolean isRestartWhenMemoryCheckFail() {
		return restartWhenMemoryCheckFail;
	}

	public void setRestartWhenMemoryCheckFail(boolean restartWhenMemoryCheckFail) {
		this.restartWhenMemoryCheckFail = restartWhenMemoryCheckFail;
	}

	public boolean isRestartWhenResponseCheckFail() {
		return restartWhenResponseCheckFail;
	}

	public void setRestartWhenResponseCheckFail(
			boolean restartWhenResponseCheckFail) {
		this.restartWhenResponseCheckFail = restartWhenResponseCheckFail;
	}

	public String getNcAdminPassword() {
		return ncAdminPassword;
	}

	public void setNcAdminPassword(String ncAdminPassword) {
		this.ncAdminPassword = ncAdminPassword;
	}

	public String getJavahome() {
		return javahome;
	}

	private MonitorEnv() {
		setName("monitorEnv");
		init();
	}

	public void setJavahome(String javahome) {
		this.javahome = javahome;
	}

	public static MonitorEnv getInstance() {
		if (env == null) {
			env = new MonitorEnv();
		}
		return env;
	}

	public int getIcheckDuration() {
		return icheckDuration;
	}

	public int getNotResponseTimeLim() {
		return iNotResponseTime;
	}

	public void setNotResponseTimeLim(int lim) {
		iNotResponseTime = lim;
	}

	public int getTryTimes() {
		return iTryTimes;
	}

	public MwServerInfo getServerInfo(String serverName) {
		if (serverName == null)
			return null;
		Object o = hServerInfo.get(serverName);
		return o == null ? null : (MwServerInfo) o;
	}

	private void init() {
		setOsType();

	}

	private void setOsType() {
		String osName = System.getProperty("os.name");
		if (osName.toLowerCase().indexOf("win") > -1)
			OSTYPE = WINDOWS;
		else
			OSTYPE = UNIX;

	}

	public int getOStype() {
		return OSTYPE;
	}

	public void setChecktimes(int checktimes) {
		this.iTryTimes = checktimes;

	}

	public String[] getServerNames() {
		Enumeration<String> enu = hServerInfo.keys();
		ArrayList<String> al = new ArrayList<String>();
		while (enu.hasMoreElements()) {
			al.add(enu.nextElement());
		}
		String[] Names = new String[al.size()];
		for (int i = 0; i < Names.length; i++)
			Names[i] = al.get(i).toString();
		return Names;
	}

	public IMwAdmin initNodeAgent(String host, String port, String username,
			String password) throws Exception {
		agentHost = host;
		agentPort = port;
		agentUser = username;
		agentPassword = password;
		if (servertype.equals("was")) {
			return new WasJmxAdmin(host, port, username, password);
		} else if (servertype.equals("wls")) {
			return new WlsJmxAdmin(host, port, username, password);
		}else{
			return new CmdAdmin();
		}
	}

	public IMwAdmin createNodeAgent() throws Exception {
		try {
			if (servertype.equals("was")) {
				return new WasJmxAdmin(agentHost, agentPort, agentUser,
						agentPassword);
			} else if (servertype.equals("wls")) {
				return new WlsJmxAdmin(agentHost, agentPort, agentUser,
						agentPassword);
			} else {
				return new CmdAdmin();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public IMwAdmin getNodeAgent(String serverName) {
		try {
			return getServerInfo(serverName).getWasJmxClient();
		} catch (Exception e) {
			ProcessMonitor.warn("fail to connect to nodeagent.", null,
					serverName);
		}
		return null;
	}

	public void initServerInfoByfile(String propFile) {
		ServerSchedule schedule = ServerSchedule.getInstance();
		try {
			ClusterInfo cluinfo = getClusterInfoForServer(propFile);
			if (cluinfo == null) {
				WebServerInfo server = getServerInfoByFile(propFile);
				MwServerInfo singserver = new MwServerInfo();
				singserver.setServerName(server.getName());
				singserver.setHost(server.getAddress());
				singserver.setPort(Integer.valueOf(server.getPort()));
				singserver.setNodeagent(null);
				HashVO svo = schedule.getSchedule(server.getName());
				singserver.setScheduleTime(svo.getStringValue("scheduletime"));
				singserver.setScheduleDays(Integer.parseInt(svo
						.getStringValue("scheduledays")));
				singserver.setLastScheduleWorkDate(svo
						.getStringValue("lastscheduleworkdate"));
				singserver.setWasJmxClient(createNodeAgent());
				singserver.setMaster(true);
				initThreadPoolOfServer(singserver);
				hServerInfo.put(singserver.getServerName(), singserver);
				return;
			}
			ClusterMemberInfo[] mem = cluinfo.getPeers();
			MwServerInfo server = null;
			for (int i = 0; i < mem.length; i++) {
				server = new MwServerInfo();
				server.setHost(mem[i].getHttpEndpoints()[0].getAddress());
				server.setPort((mem[i].getHttpEndpoints()[0].getPort()));
				server.setServerName(mem[i].getName());
				HashVO svo = schedule.getSchedule(mem[i].getName());
				server.setScheduleTime(svo.getStringValue("scheduletime"));
				server.setScheduleDays(Integer.parseInt(svo
						.getStringValue("scheduledays")));
				server.setLastScheduleWorkDate(svo
						.getStringValue("lastscheduleworkdate"));
				server.setNodeagent(null);
				server.setWasJmxClient(createNodeAgent());
				initThreadPoolOfServer(server);
				hServerInfo.put(server.getServerName(), server);
			}
			server = new MwServerInfo();
			server.setHost(cluinfo.getMgr().getHttpEndpoints()[0].getAddress());
			server.setPort(cluinfo.getMgr().getHttpEndpoints()[0].getPort());
			server.setNodeagent(null);
			HashVO svo = schedule.getSchedule(cluinfo.getMgr().getName());
			server.setScheduleTime(svo.getStringValue("scheduletime"));
			server.setScheduleDays(Integer.parseInt(svo
					.getStringValue("scheduledays")));
			server.setLastScheduleWorkDate(svo
					.getStringValue("lastscheduleworkdate"));
			server.setServerName(cluinfo.getMgr().getName());
			server.setWasJmxClient(createNodeAgent());
			server.setMaster(true);
			initThreadPoolOfServer(server);
			hServerInfo.put(server.getServerName(), server);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			isInited = true;
		}
		initWebServer(propFile);
	}

	public void initWebServer(String propfile) {
		HashVO[] vos = null;
		try {
			vos = Xml2HashVO.convertToVO(propfile, "webServer");
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		if (Toolkit.isEmpty(vos))
			return;
		httpServer = new Socket(vos[0].getStringValue("address"),
				vos[0].getStringValue("port"));
	}

	public boolean isInited() {
		return isInited;
	}

	private void initThreadPoolOfServer(MwServerInfo server) {
		if (!Toolkit.isEmpty(server.getTreadPoolRange())
				&& !server.getTreadPoolRange().equals("100"))
			return;
		if (server.getWasJmxClient() == null) {
			server.setTreadPoolRange("100");
			server.setWAS(false);
			return;
		}
		server.setWAS(true);
		String threadPoolRange = server.getWasJmxClient().getThreadPoolRange(
				server.getServerName());
		if (Toolkit.isEmpty(threadPoolRange)) {
			try {
				Thread.sleep(3 * 1000l);
			} catch (Exception e) {
			}
			threadPoolRange = server.getWasJmxClient().getThreadPoolRange(
					server.getServerName());
		}
		if (Toolkit.isEmpty(threadPoolRange)) {
			server.setWAS(false);
			threadPoolRange = "100";
		}
		server.setTreadPoolRange(threadPoolRange);
	}

	public void initThreadPool(String serverName) {
		MwServerInfo serverinfo = getServerInfo(serverName);
		if (serverinfo == null)
			return;
		initThreadPoolOfServer(serverinfo);
	}

	public final static void main(String[] args) {
		MonitorEnv env = getInstance();
		try {
			env.initWebServer("c:/ufsoft/nchome/ierp/bin/prope.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ClusterInfo getClusterInfoForServer(String propFile)
			throws Exception {
		DocumentBuilder parser = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = parser.parse(propFile);
		NodeList nodeList = doc.getElementsByTagName("domain");
		if (nodeList != null && nodeList.getLength() > 0) {
			Element node = (Element) nodeList.item(0);
			NodeList clusterNodeList = node.getElementsByTagName("cluster");
			Node clusterNode = null;
			if (clusterNodeList != null && clusterNodeList.getLength() > 0) {
				for (int i = 0; i < clusterNodeList.getLength(); i++) {
					clusterNode = clusterNodeList.item(i);
					ClusterInfo clusterConf;
					try {
						clusterConf = (ClusterInfo) XMLToObject
								.getJavaObjectFromNode(clusterNode,
										ClusterInfo.class, true);
						return clusterConf;
					} catch (Exception e) {
					}
				}
			}
		}
		return null;
	}

	public static WebServerInfo getServerInfoByFile(String propFile)
			throws Exception {
		DocumentBuilder parser = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = parser.parse(propFile);
		NodeList nodeList = doc.getElementsByTagName("domain");
		WebServerInfo info = new WebServerInfo();
		info.setAddress("127.0.0.1");
		if (nodeList != null && nodeList.getLength() > 0) {
			Element node = (Element) nodeList.item(0);
			NodeList serverNodeList = node.getElementsByTagName("server");
			NodeList list = serverNodeList.item(0).getChildNodes();
			Node cNode = null;
			if (list != null && list.getLength() > 0) {
				for (int i = 0; i < list.getLength(); i++) {
					cNode = list.item(i);
					if (cNode.getNodeName().equals("name")) {
						info.setName(cNode.getFirstChild().getNodeValue());
						continue;
					}
					if (cNode.getNodeName().equalsIgnoreCase("http")) {
						NodeList httpNodeList = node
								.getElementsByTagName("port");
						info.setPort(httpNodeList.item(0).getFirstChild()
								.getNodeValue());
						httpNodeList = node.getElementsByTagName("address");
						if (Toolkit.isEmpty(httpNodeList)
								|| httpNodeList.getLength() == 0) {
							info.setAddress("127.0.0.1");
						} else {
							String host = null;
							try {
								host = httpNodeList.item(0).getFirstChild()
										.getNodeValue();
							} catch (Exception e) {
							}
							host = Toolkit.isEmpty(host) ? "127.0.0.1" : host;
							info.setAddress(host);
						}
						continue;
					}
				}
			}
		}
		return info;
	}

	public int getMemTryTimes() {
		return iMemTryTimes;
	}

	public double getMemThreshold() {
		return dMemoryThreshold;
	}

	public void setMemThreshold(double memthre) {
		dMemoryThreshold = memthre;

	}

	public void setMemTryTimes(int memTrytimes) {
		this.iMemTryTimes = memTrytimes;
	}

	public MwServerInfo[] getAllServerInfo() {
		Enumeration<Object> en = hServerInfo.elements();
		ArrayList<MwServerInfo> al = new ArrayList<MwServerInfo>();
		while (en.hasMoreElements()) {
			al.add((MwServerInfo) en.nextElement());
		}
		return (MwServerInfo[]) al.toArray(new MwServerInfo[al.size()]);

	}

	public String getServlet() {
		return servlet == null ? "" : servlet;
	}

	public void setServlet(String servlet) {
		this.servlet = servlet;
	}

	public void setShutdown(boolean b) {
		bShutdown = b;
	}

	public boolean isShutdown() {
		return bShutdown;
	}

	public boolean isWasServer() {
		return isWasServer;
	}

	public void setWasServer(boolean isWasServer) {
		this.isWasServer = isWasServer;
	}

	public MwServerInfo getMaster() {
		Enumeration<Object> en = hServerInfo.elements();
		while (en.hasMoreElements()) {
			MwServerInfo server = (MwServerInfo) en.nextElement();
			if (server.isMaster())
				return server;
		}
		return null;
	}

	public void setResource(ServiceResource resource) {
		this.resource = resource;
	}

	public ServiceResource getResource() {
		return resource;
	}

	public boolean genDumpBeforeRestart() {
		return genDumpBeforeRestart;
	}

	public void setGenHeapDumpBeforeRestart(boolean dumpBeforeRestart) {
		this.genDumpBeforeRestart = dumpBeforeRestart;
	}

	public double getConnectionusedlim() {
		return connectionusedlim;
	}

	public void setConnectionusedlim(double connectionusedlim) {
		this.connectionusedlim = connectionusedlim;
	}

	public void setGenThreadDumpBeforeRestart(boolean dumpthreadwhenrestart) {
		this.genThreadDumpBeforeRestart = dumpthreadwhenrestart;
	}

	public boolean genThreadDumpBeforeRestart() {
		return genThreadDumpBeforeRestart;
	}

	public boolean isN1() {
		return !Toolkit.isEmpty(PluginAdmin.key);
	}

	public boolean needRemove() {
		if (!isN1())
			return false;
		// 所有server处于running状态，摘除standby server.
		MwServerInfo[] servers = getAllServerInfo();
		for (int i = 0; i < servers.length; i++) {
			if (servers[i].getStatus() != MwServerInfo.SERVERRUNING)
				return false;
		}
		return true;
	}

	public boolean needAdd() {
		if (!isN1())
			return false;
		// 有server处于down状态，启动standby server.
		MwServerInfo[] servers = getAllServerInfo();
		for (int i = 0; i < servers.length; i++) {
			if (servers[i].getStatus() != MwServerInfo.SERVERRUNING)
				return true;
		}
		return false;
	}

	public void setServertype(String string) {
		servertype = string;
	}

	public String getServertype() {
		return servertype;
	}
}
