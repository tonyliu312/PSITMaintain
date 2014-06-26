package uap.ps.itm.monitor.service.process;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;

import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.message.MessageEntry;
import uap.ps.itm.monitor.service.message.MsgSender;
import weblogic.management.MBeanHome;
import weblogic.management.configuration.ServerMBean;

public class WlsJmxAdmin implements IMwAdmin {
	private String serverHost = "127.0.0.1";

	private String serverPort = "7001";

	private String userName = null;

	private String passWord = null;

	public WlsJmxAdmin(String host, String port, String username, String password) throws Exception {
		// TODO Auto-generated constructor stub
		serverHost = host;
		serverPort = port;
		this.userName = username;
		this.passWord = password;
		getBHome();
	}

	public void dumpThread(String serverName) {
	}

	public Object generateHeadDump(String serverName) {
		return "not support";
	}

	public String getThreadPoolRange(String serverName) {
		return "50";
	}

	public void restart(String serverName) {
		synchronized (this) {
			shutdownImmediate(serverName);
			startup(serverName);
		}
	}

	public synchronized void shutdownImmediate(String serverName) {
		try {
			getServerMBean(serverName).synchronousKill();
		} catch (Exception e) {
			handleException(e);
		}
	}

	public synchronized void startup(String serverName) {
		try {
			getServerMBean(serverName).synchronousStart();
		} catch (Exception e) {
			handleException(e);
		}
		MonitorEnv.getInstance().getServerInfo(serverName).addRestartTimes();
		MonitorEnv.getInstance().getServerInfo(serverName).setLastRestartDate(Toolkit.getCurTime());
		ProcessMonitor.warn("startup " + serverName + " over!", null, serverName);
	}
	
	
	private MBeanHome getBHome() throws Exception {
		Hashtable env = new Hashtable();
		env.put(Context.PROVIDER_URL, "t3://" + serverHost + ":" + serverPort);
		env.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
		env.put(Context.SECURITY_CREDENTIALS, passWord);
		env.put(Context.SECURITY_PRINCIPAL, userName);
		Context ctx = new InitialContext(env);
		MBeanHome home = (MBeanHome) ctx.lookup(MBeanHome.ADMIN_JNDI_NAME);
		return home;
	}
	private ServerMBean getServerMBean(String servername) {
		try {
			MBeanHome home = getBHome();
			Set set = null;
			set = home.getMBeansByType("ServerConfig");
			Iterator itr = set.iterator();
			while (itr.hasNext()) {
				ServerMBean mbean = (ServerMBean) itr.next();
				if (mbean.getName().toString().equalsIgnoreCase(servername)) {
					return mbean;
				}
			}
			return null;
		} catch (Exception e) {
			handleException(e);
		}
		return null;
	}

	private void handleException(Exception e) {
		String time = "[" + Toolkit.getCurTime() + "]";
		ProcessMonitor.log(time + e.getMessage(), null);
		sendMsg(e.getMessage());
		// e.printStackTrace();
		// TODO
		if (e instanceof javax.naming.CommunicationException || e instanceof java.rmi.ConnectException || e instanceof java.net.ConnectException) {
			try {
				ProcessMonitor.log(time + "wls admin server is restarted.so reinit the connection!", null);
			} catch (Exception ex) {
				ProcessMonitor.warn(ex.getMessage(), null, null);
			}
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

	}
	public String getJDBCPool(String serverName){
		return "this function do not support for weblogic!";
	}
}
