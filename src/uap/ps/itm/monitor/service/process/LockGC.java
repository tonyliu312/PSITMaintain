package uap.ps.itm.monitor.service.process;

import nc.bs.framework.mx.thread.RequestEntry;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.center.ServerEnv;
import uap.ps.itm.pub.servlet.URLCommuniatcor;

public class LockGC {
	private static String masterurl = null;

	private static boolean isDebug = false;

	private static String masterName = "master";

	public static void lockGC(String servername) {
		try {
			if (!Toolkit.isEqual(ServerEnv.getInstance().getProperty("EnableLOCKGC"), true)) {
				debug("EnableLOCKGC=false");
				return;
			}
			if (MonitorEnv.getInstance().getServerInfo(servername).isMaster()) {
				debug(servername + " is master!");
				return;
			}
			if (MonitorEnv.getInstance().getServerInfo(masterName).getStatus() != MwServerInfo.SERVERRUNING) {
				debug("master is not Running!gc failed");
				return;
			}
			debug("begin release lock for server:" + servername);
			RequestEntry requestentry = new RequestEntry();
			requestentry.setCmd("lockgc-" + servername);
			URLCommuniatcor.remoteCall(requestentry, getmastUrl());
			debug("end release lock for server:" + servername);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getmastUrl() {
		if (masterurl == null) {
			MwServerInfo msi = MonitorEnv.getInstance().getServerInfo(masterName);
			masterurl = "http://" + msi.getHost() + ":" + msi.getPort() + "/" + MonitorEnv.getInstance().getServlet();
		}
		return masterurl;
	}

	private static void debug(String s) {
		if (isDebug) {
			System.out.println("[lock gc]"+s);
		}
	}
}
