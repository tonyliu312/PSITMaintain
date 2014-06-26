package uap.ps.itm.monitor.service.center;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import uap.ps.itm.monitor.pub.IConfigConst;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.privilege.PrivilegeManager;

public class ServiceRegister implements IServer {
	private Hashtable<String, ArrayList<ServiceDesc>> hServiceRegisted = new Hashtable<String, ArrayList<ServiceDesc>>();
	private Hashtable<String, ArrayList<ServiceResource[]>> hServiceResourceRegisted = new Hashtable<String, ArrayList<ServiceResource[]>>();

	public void startServer() {
		System.out.println("register server is started.");
	}

	public void stopServer() {
	}

	public boolean isCanbeStart(ServiceDesc service) {
		boolean isStart = service.isStart();
		if (!isStart)
			return false;
		boolean isSingle = service.isSingle();
		if (!isSingle)
			return true;
		String serviceName = service.getServiceName();
		ServiceDesc serviceExist = null;
		ArrayList<ServiceDesc> al = getAllServices();
		for (int i = 0; i < al.size(); i++) {
			serviceExist = (ServiceDesc) al.get(i);
			if (serviceName.equalsIgnoreCase(serviceExist.getServiceName()))
				return false;
		}
		return true;
	}

	public ArrayList<ServiceDesc> getAllServices() {
		Enumeration<ArrayList<ServiceDesc>> enu = hServiceRegisted.elements();
		ArrayList<ServiceDesc> al = new ArrayList<ServiceDesc>();
		ArrayList<ServiceDesc> alTmp = new ArrayList<ServiceDesc>();
		while (enu.hasMoreElements()) {
			alTmp = enu.nextElement();
			for (int i = 0; i < alTmp.size(); i++) {
				al.add(alTmp.get(i));
			}
		}
		return al;
	}

	public void registerService(Socket socket, ServiceDesc service) {
		if (socket == null || service == null || socket.toString() == null)
			return;
		ArrayList<ServiceDesc> al = hServiceRegisted.get(socket.toString());
		if (al == null) {
			al = new ArrayList<ServiceDesc>();
			hServiceRegisted.put(socket.toString(), al);
		}
		al.add(service);
	}

	public void unRegisterService(Socket socket) {
		if (socket == null || socket.toString() == null)
			return;
		hServiceResourceRegisted.remove(socket.toString());
		hServiceRegisted.remove(socket.toString());
	}

	public void registerResource(Socket socket, ServiceResource[] resources) {
		if (socket == null || socket.toString() == null)
			return;
		if (resources == null)
			hServiceResourceRegisted.remove(socket.toString());
		else {
			ArrayList<ServiceResource[]> al = hServiceResourceRegisted
					.get(socket.toString());
			if (al == null) {
				al = new ArrayList<ServiceResource[]>();
				hServiceResourceRegisted.put(socket.toString(), al);
			}
			al.add(resources);
		}
	}

	public ServiceDesc getServiceByname(String serviceName) {
		Enumeration<ArrayList<ServiceDesc>> enu = hServiceRegisted.elements();
		ArrayList<ServiceDesc> alTmp = new ArrayList<ServiceDesc>();
		while (enu.hasMoreElements()) {
			alTmp = enu.nextElement();
			for (int i = 0; i < alTmp.size(); i++) {
				if (Toolkit.isEqual(serviceName, alTmp.get(i).getServiceName()))
					return alTmp.get(i);
			}
		}
		return null;
	}

	public ServiceResource[] getServerResouce(ServiceDesc serviceDesc) {
		return null;
	}

	public ServiceResource[] getAllResources() {
		return null;
	}

	public ServiceResource[] getAllResources(String user, String password) {
		Enumeration<ArrayList<ServiceResource[]>> enu = hServiceResourceRegisted
				.elements();
		ArrayList<ServiceResource> al = new ArrayList<ServiceResource>();
		ArrayList<ServiceResource[]> alTmp = new ArrayList<ServiceResource[]>();
		while (enu.hasMoreElements()) {
			alTmp = enu.nextElement();
			if (Toolkit.isEmpty(alTmp))
				continue;
			for (int i = 0; i < alTmp.size(); i++) {
				ServiceResource[] resources = alTmp.get(i);
				if (Toolkit.isEmpty(resources))
					continue;

				for (int j = 0; j < resources.length; j++) {
					if (!checkPrivilege(user, resources[j])) {
						continue;
					}
					al.add(resources[j]);
				}
			}
		}
		return al.toArray(new ServiceResource[al.size()]);
	}

	private boolean checkPrivilege(String user, ServiceResource resource) {
		Object obj = ServiceHolder.getInstance().getServer(IConfigConst.SERVICEPRIVILEGE);
		if (obj == null)
			return true;
		PrivilegeManager pm = (PrivilegeManager) obj;
		return pm.checkPrivilege(user, resource);
	}

	public ServiceResource[] getResourcesByService(ServiceDesc serviceDesc) {

		Enumeration<ArrayList<ServiceResource[]>> enu = hServiceResourceRegisted
				.elements();
		ArrayList<ServiceResource> al = new ArrayList<ServiceResource>();
		ArrayList<ServiceResource[]> alTmp = new ArrayList<ServiceResource[]>();
		while (enu.hasMoreElements()) {
			alTmp = enu.nextElement();
			if (Toolkit.isEmpty(alTmp))
				continue;
			for (int i = 0; i < alTmp.size(); i++) {
				ServiceResource[] resources = alTmp.get(i);
				if (Toolkit.isEmpty(resources))
					continue;

				for (int j = 0; j < resources.length; j++) {
					if (Toolkit.isEqual(resources[j].getServiceName(),
							serviceDesc.getServiceName())) {
						al.add(resources[j]);
					}
				}
			}
		}
		return al.toArray(new ServiceResource[al.size()]);
	}

	public Properties getconfigure() {
		return ServerEnv.getInstance().getProp();
	}

	public void refreshEnv() {
	}

	public String[] getRemoteHost() {
		Enumeration<String> enu = hServiceRegisted.keys();
		ArrayList<String> al = new ArrayList<String>();
		while (enu.hasMoreElements()) {
			String socket = enu.nextElement();
			StringTokenizer st = new StringTokenizer(socket, ":");
			String host = st.nextToken();
			if (!ServerEnv.isLocalHost(host) && !al.contains(socket)) {
				al.add(socket);
			}
		}
		return al.toArray(new String[al.size()]);
	}
}
