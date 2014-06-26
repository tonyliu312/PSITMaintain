package uap.ps.itm.monitor.service.process;

import java.net.ConnectException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.management.ObjectName;

import uap.ps.itm.monitor.pub.IConfigConst;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.center.ServerEnv;
import uap.ps.itm.monitor.service.message.MessageEntry;
import uap.ps.itm.monitor.service.message.MsgSender;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.AdminClientFactory;
import com.ibm.websphere.management.exception.AdminException;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.management.exception.ConnectorNotAvailableException;

public class WasJmxAdmin implements IMwAdmin {
	private String serverHost = "127.0.0.1";

	private String serverPort = "8879";

	private String userName = null;

	private String passWord = null;

	private AdminClient adminClient;

	public WasJmxAdmin(String host, String port, String userName,
			String password) throws Exception {
		serverHost = host;
		serverPort = port;
		this.userName = userName;
		this.passWord = password;
		init();
	}

	public void init() throws Exception {
		synchronized (this) {
			Properties adminProps = new Properties();
			if (serverHost == null)
				return;
			adminProps.setProperty("type", "SOAP");
			adminProps.setProperty("host", serverHost);
			adminProps.setProperty("port", serverPort);
			if (!Toolkit.isEmpty(userName)) {
				adminProps.setProperty("securityEnabled", "true");
				adminProps.setProperty("username", userName);
				adminProps.setProperty("password", passWord);
				if (!Toolkit.isEmpty(ServerEnv.getInstance().getProperty(
						"javax.net.ssl.trustStore"))) {
					adminProps.setProperty("javax.net.ssl.trustStore",
							ServerEnv.getInstance().getProperty(
									"javax.net.ssl.trustStore"));
					adminProps.setProperty("javax.net.ssl.trustStorePassword",
							ServerEnv.getInstance().getProperty(
									"javax.net.ssl.trustStorePassword"));
				}
				if (!Toolkit.isEmpty(ServerEnv.getInstance().getProperty(
						"javax.net.ssl.keyStore"))) {
					adminProps.setProperty("javax.net.ssl.keyStore", ServerEnv
							.getInstance()
							.getProperty("javax.net.ssl.keyStore"));
					adminProps.setProperty("javax.net.ssl.keyStorePassword",
							ServerEnv.getInstance().getProperty(
									"javax.net.ssl.keyStorePassword"));
				}
			}
			adminClient = AdminClientFactory.createAdminClient(adminProps);
		}
	}

	public void shutdownImmediate(String serverName) {
		synchronized (this) {
			String opName = "terminate";
			String signature[] = { "java.lang.String" };
			String params[] = { serverName };
			ObjectName serverAgent = MonitorEnv.getInstance().getServerInfo(
					serverName).getNodeagent();
			try {
				ProcessMonitor.warn("now begin shutdown " + serverName, null,
						serverName);
				if (serverAgent == null)
					serverAgent = getServerAgent(serverName);
				if (serverAgent == null)
					throw new Exception(
							"server "
									+ serverName
									+ " is shutdown now.or it's nodeagent is not startup.");
				adminClient.invoke(serverAgent, opName, params, signature);
				ProcessMonitor.warn("shutdown " + serverName + " over!", null,
						serverName);
			} catch (Exception e) {
				handleException(e);
				return;
			}
			MonitorEnv.getInstance().getServerInfo(serverName).setNodeagent(
					serverAgent);
		}
	}

	public ObjectName getServerAgent(String serverName) {
		synchronized (this) {
			ObjectName serverBean = getServerMbean(serverName);
			if (serverBean == null)
				return null;
			StringTokenizer st = new StringTokenizer(serverBean
					.getCanonicalName(), ",");
			String prop = null;
			String nodeagentName = null;
			while ((prop = st.nextToken()) != null) {
				if (prop.substring(0, 5).equals("node=")) {
					nodeagentName = prop.substring(5, prop.length());
					ProcessMonitor.warn("server :" + serverName + " nodeagent="
							+ nodeagentName, null, serverName);
					break;
				}
			}
			ObjectName[] agents = getAllNodeAgent();
			for (int i = 0; i < agents.length; i++) {
				if (agents[i].getCanonicalName().contains(nodeagentName))
					return agents[i];
			}
			return null;
		}
	}

	public void startup(String serverName) {
		synchronized (this) {
			ProcessMonitor.warn("now begin to startup " + serverName, null,
					serverName);
			String opName = "launchProcess";
			String signature[] = { "java.lang.String" };
			String params[] = { serverName };
			ObjectName serverAgent = MonitorEnv.getInstance().getServerInfo(
					serverName).getNodeagent();
			try {
				if (serverAgent == null) {
					ObjectName[] agents = getAllNodeAgent();
					for (int i = 0; i < agents.length; i++) {
						try {
							serverAgent = agents[i];
							adminClient.invoke(serverAgent, opName, params,
									signature);
							MonitorEnv.getInstance().getServerInfo(serverName)
									.setNodeagent(serverAgent);
							break;
						} catch (Exception e) {
							handleException(e);
						}
					}
				} else {
					adminClient.invoke(serverAgent, opName, params, signature);
				}
				if (MonitorEnv.getInstance().getServerInfo(serverName)
						.getNodeagent() == null) {
					throw new Exception("agent for server:" + serverName
							+ " is not starting.");
				}
			} catch (Exception e) {
				handleException(e);
			}
			MonitorEnv.getInstance().getServerInfo(serverName)
					.addRestartTimes();
			MonitorEnv.getInstance().getServerInfo(serverName)
					.setLastRestartDate(Toolkit.getCurTime());
			ProcessMonitor.warn("startup " + serverName + " over!", null,
					serverName);
		}
	}

	public ObjectName[] getAllNodeAgent() {
		synchronized (this) {
			Iterator iter;
			Vector<ObjectName> v = new Vector<ObjectName>();
			try {
				String query = "WebSphere:type=NodeAgent,*";
				ObjectName queryName = new ObjectName(query);
				Set s = adminClient.queryNames(queryName, null);
				if (!s.isEmpty()) {
					iter = s.iterator();
					while (iter.hasNext()) {
						v.add((ObjectName) iter.next());
					}
				} else {
					throw new AdminException("NodeAgent MBean was not found");
				}
			} catch (Exception e) {
				handleException(e);
			}
			ObjectName[] agents = new ObjectName[v.size()];
			for (int i = 0; i < agents.length; i++) {
				agents[i] = (ObjectName) v.elementAt(i);
			}
			return agents;
		}
	}

	public void restart(String serverName) {
		synchronized (this) {
			shutdownImmediate(serverName);
			startup(serverName);
		}
	}

	private ObjectName getServerMbean(String serverName) {
		synchronized (this) {
			Iterator iter;
			try {
				String query = "WebSphere:*,type=Server,j2eeType=J2EEServer";
				ObjectName queryName = new ObjectName(query);
				Set s = adminClient.queryNames(queryName, null);
				if (!s.isEmpty()) {
					iter = s.iterator();
					while (iter.hasNext()) {
						ObjectName oTemp = (ObjectName) iter.next();
						if (oTemp.getCanonicalName().contains(serverName))
							return oTemp;
					}
				} else {
					return null;
				}
			} catch (Exception e) {
				handleException(e);
			}
			return null;
		}
	}

	private ObjectName getJvmMBean(String serverName) throws AdminException {
		Iterator iter;
		try {
			String query = "WebSphere:*,type=JVM,j2eeType=JVM";
			ObjectName queryName = new ObjectName(query);
			Set s = adminClient.queryNames(queryName, null);
			if (!s.isEmpty()) {
				iter = s.iterator();
				while (iter.hasNext()) {
					ObjectName oTemp = (ObjectName) iter.next();
					if (oTemp.getCanonicalName().contains(serverName))
						return oTemp;
				}
			} else {
				throw new AdminException("JVM MBean was not found");
			}
		} catch (Exception e) {
			handleException(e);
		}
		return null;
	}

	public String getServerHost() {
		return serverHost;
	}

	public void dumpThread(String serverName) {
		synchronized (this) {
			String opName = "dumpThreads";
			String signature[] = null;
			String params[] = null;
			try {
				ProcessMonitor.warn(
						"now begin to dumpthread for " + serverName, null,
						serverName);
				adminClient.invoke(getJvmMBean(serverName), opName, params,
						signature);
				ProcessMonitor.warn("dumpthreads for " + serverName + " over!",
						null, serverName);
			} catch (Exception e) {
				handleException(e);
			}
		}
	}

	public Object generateHeadDump(String serverName) {
		synchronized (this) {
			String opName = "generateHeapDump";
			String signature[] = null;
			String params[] = null;
			try {
				ProcessMonitor.warn("now begin to generate headump for "
						+ serverName, null, serverName);
				Object o = adminClient.invoke(getJvmMBean(serverName), opName,
						params, signature);
				ProcessMonitor.warn("generate headdump for " + serverName
						+ " over!", null, serverName);
				return o;
			} catch (Exception e) {
				handleException(e);
				return e.getMessage();
			}
		}
	}

	private void handleException(Exception e) {
		String time = "[" + Toolkit.getCurTime() + "]";
		ProcessMonitor.log(time + e.getMessage(), null);
		sendMsg(e.getMessage());
		// e.printStackTrace();
		if (e instanceof ConnectorNotAvailableException
				|| e instanceof ConnectorException
				|| e instanceof ConnectException) {
			try {

				System.out
						.println(time
								+ "was admin jmx server is restarted.so reinit the connection!wait 60s to the adminserver. ");
				ProcessMonitor
						.log(
								time
										+ "was admin jmx server is restarted.so reinit the connection!",
								null);
				ProcessMonitor.log(time
						+ "now wait 60s to the admin server startup!", null);
				sendMsg(time
						+ " was admin jxm server is restarted.wait 1min to reinit");
				Thread.sleep(1000 * 60);
				init();
			} catch (Exception ex) {
				ProcessMonitor.warn(ex.getMessage(), null, null);
				ex.printStackTrace();
			}
		} else if (e instanceof javax.management.InstanceNotFoundException) {
			// nodeagent is shutdown .
			ProcessMonitor.log(time
					+ "nodeagent is shutdown!wait until the agent startup.",
					null);
			sendMsg(time
					+ "nodeagent is shutdown!wait until the agent startup.");
		} else {
			// e.printStackTrace(System.out);
		}
	}

	private void sendMsg(String message) {
		if (MonitorEnv.getInstance().getResource() == null)
			return;
		MessageEntry entry = new MessageEntry();
		entry.setMsg("[ERROR]" + message);
		entry.setNeedDump(false);
		entry.setTs(Toolkit.getCurTime());
		entry.setMsgResource(MonitorEnv.getInstance().getResource());
		MsgSender.send(entry);

	}

	public void stop() {
		adminClient = null;
	}

	public String getThreadPoolRange(String serverName) {
		synchronized (this) {
			try {
				ObjectName threadPoolBean = getThreadPoolBean(serverName);
				if (threadPoolBean == null)
					return "";
				String max = adminClient.getAttribute(threadPoolBean,
						"maximumSize").toString();
				String min = adminClient.getAttribute(threadPoolBean,
						"minimumSize").toString();
				return min + "-" + max;
			} catch (Exception e) {
				handleException(e);
			}
			return "";
		}
	}

	private ObjectName getThreadPoolBean(String serverName)
			throws AdminException {
		Iterator iter;
		try {
			String query = "WebSphere:*,type=ThreadPool";
			ObjectName queryName = new ObjectName(query);
			Set s = adminClient.queryNames(queryName, null);
			if (!s.isEmpty()) {
				iter = s.iterator();
				while (iter.hasNext()) {
					ObjectName oTemp = (ObjectName) iter.next();
					if (oTemp.getCanonicalName().contains(serverName)
							&& oTemp.getCanonicalName()
									.contains("WebContainer"))
						return oTemp;
				}
			} else {
				throw new AdminException("ThreadPool MBean was not found");
			}
		} catch (Exception e) {
			handleException(e);
		}
		return null;
	}

	public static final void main(String[] args) {
		try {
			IConfigConst.init();
			ServerEnv.getInstance().loadProp();
			// System.setProperty("javax.net.ssl.trustStore", "d:/cacerts");
			// System.setProperty("javax.net.ssl.trustStorePassword",
			// "owindowind");
			WasJmxAdmin admin = new WasJmxAdmin("20.10.130.138", "8879", null,
					null);
			// admin.generateHeadDump("ncMem01");
			System.out.println(admin.getJDBCPool("ncMem01"));
			// System.out.print(admin.getAllNodeAgent());
			// "java.lang.String", "java.lang.String", "java.lang.String",
			// "java.lang.Boolean" };
			// Object[] params = new Object[] { "d:/was/profiles",
			// "localhostCell01", "localhostNode01", "mast", true };
			//
			// System.out.println(o);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getJDBCPool(String serverName) {
		try {
			ObjectName serverBean = getServerMbean(serverName);
			StringTokenizer st = new StringTokenizer(serverBean
					.getCanonicalName(), ",");
			String prop = null;
			String nodeagentName = null;
			while ((prop = st.nextToken()) != null) {
				if (prop.substring(0, 5).equals("node=")) {
					nodeagentName = prop.substring(5, prop.length());
					ProcessMonitor.warn("server :" + serverName + " nodeagent="
							+ nodeagentName, null, serverName);
					break;
				}
			}
			System.out.println("agentname=" + nodeagentName);
			Set set = adminClient.queryNames(new ObjectName(
					"*:type=DataSource,process=" + serverName + ",node="
							+ nodeagentName + ",*"), null);
			Object[] o = set.toArray();
			ObjectName[] on = new ObjectName[o.length];
			for (int i = 0; i < o.length; i++) {
				on[i] = (ObjectName) o[i];
			}

			StringBuffer sb = new StringBuffer();

			for (int i = 0; i < on.length; i++) {
				// String name = (String) adminClient.getAttribute(on[i],
				// "name");
				String content = (String) adminClient.invoke(on[i],
						"showPoolContents", new Object[] {}, new String[] {});
				sb.append(content);
				sb.append("\n");
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public void testCluster() {

	}
}
