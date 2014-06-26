package uap.ps.itm.monitor.service.center;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

public class ServiceHolder {
	private Hashtable<String, IServer> hServer = new Hashtable<String, IServer>();
	private static ServiceHolder m_inst = null;

	public static ServiceHolder getInstance() {
		if (m_inst == null) {
			m_inst = new ServiceHolder();
		}
		return m_inst;
	}

	public void registServer(String serverName, IServer server) {
		if (hServer.get(serverName) != null)
			return;
		hServer.put(serverName, server);
	}

	public IServer getServer(String serverName) {
		return hServer.get(serverName);
	}

	public void stopServer() {
		Enumeration enu = hServer.keys();
		while (enu.hasMoreElements()) {
			Object o = hServer.get(enu.nextElement());
			if (o == null)
				continue;
			IServer server = (IServer) o;
			server.stopServer();
		}
	}

	public IServer[] getServers() {
		Collection<IServer> collection = hServer.values();
		Iterator<IServer> iter = collection.iterator();
		ArrayList<IServer> al = new ArrayList<IServer>();
		while (iter.hasNext()) {
			al.add(iter.next());
		}
		return al.toArray(new IServer[al.size()]);
	}
}
