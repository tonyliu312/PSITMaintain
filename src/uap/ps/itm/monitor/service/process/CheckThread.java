package uap.ps.itm.monitor.service.process;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

import nc.bs.framework.mx.thread.RequestEntry;
import uap.ps.itm.monitor.pub.Toolkit;

public class CheckThread extends Thread {
	private boolean isHealth = false;
	private String url = null;
	private MwServerInfo serverinfo = null;

	public CheckThread(String strURL, String serverName) {
		this.url = strURL;
		setName("checkThread");
		serverinfo = MonitorEnv.getInstance().getServerInfo(serverName);
	}

	public void run() {
		ObjectInputStream in = null;
		ObjectOutputStream output = null;
		URLConnection urlc = null;
		try {
			URL checkURL = null;
			checkURL = new URL(url);
			RequestEntry requestentry = new RequestEntry();
			requestentry.setCmd(RequestEntry.CMDTYPE_DEBUG_PROCESSINFO);
			if (checkURL == null)
				return;
			urlc = checkURL.openConnection();
			urlc.setDoOutput(true);
			urlc.setDoInput(true);
			output = new ObjectOutputStream(urlc.getOutputStream());
			output.writeObject(requestentry);
			output.flush();
			output.close();
			in = new ObjectInputStream(urlc.getInputStream());
			Object o = in.readObject();
			in.close();
			if (o instanceof String) {
				fillJvmInfo((String) o);
			}
			isHealth = true;
		} catch (Exception e) {
			// e.printStackTrace();
			ProcessMonitor.warn(
					"url=" + url + "->error:" + Toolkit.getExceptionStack(e),
					this, null);
			isHealth = false;
		} finally {
			try {
				if (output != null)
					output.close();
				output = null;
			} catch (Exception e) {
			}
			try {
				if (in != null)
					in.close();
				in = null;
			} catch (Exception e) {
			}
		}

	}

	// max,total,free,used.
	private void fillJvmInfo(String jvmInfo) {
		try {
			StringTokenizer st = new StringTokenizer(jvmInfo);
			serverinfo.setJvmmax(st.nextToken());
			serverinfo.setJvmtotal(st.nextToken());
			serverinfo.setJvmfree(st.nextToken());
			serverinfo.setJvmused(st.nextToken());
			if (st.hasMoreTokens()) {
				if (st.hasMoreTokens())
					serverinfo.setThreadPoolUsed(st.nextToken());
				if (st.hasMoreTokens())
					serverinfo.setServicenum(st.nextToken());
				if (st.hasMoreTokens()) {
					serverinfo.setThroughput(Integer.parseInt(st.nextToken()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isHealth() {
		return isHealth;
	}

	public static final void main(String[] args) {
		new CheckThread("http://duy:33/service/mxservlet", "master").run();
	}
}
