package uap.ps.itm.monitor.service.privilege;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.vo.framework.rsa.Encode;
import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.IConfigConst;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.pub.Xml2HashVO;
import uap.ps.itm.monitor.service.center.IServer;
import uap.ps.itm.monitor.service.center.ServerEnv;
import uap.ps.itm.monitor.service.center.ServiceDesc;
import uap.ps.itm.monitor.service.center.ServiceHolder;
import uap.ps.itm.monitor.service.center.ServiceRegister;
import uap.ps.itm.monitor.service.center.ServiceResource;
import uap.ps.itm.monitor.service.message.MessageEntry;
import uap.ps.itm.monitor.service.message.MsgSender;
import uap.ps.itm.pub.HashVO2XML;

public class PrivilegeManager implements IServer {

	ServiceResource resource = null;

	private Map<String, UserVO> users = new HashMap<String, UserVO>();

	public ServiceResource[] getServerResouce(ServiceDesc serviceDesc) {
		if (resource == null) {
			resource = new ServiceResource();
			resource = new ServiceResource();
			resource.setDisplayName(ServerEnv.getHostName());
			resource.setHost(ServerEnv.getLocalAddr());
			resource.setPort(ServerEnv.getInstance().getLocalSocket().getPort());
			resource.setRequestUrl(ServerEnv.getInstance().getLocalUrl());
			resource.setServiceName(serviceDesc.getServiceName());
			resource.setServiceDesc(serviceDesc);
		}
		return new ServiceResource[]{resource};
	}

	public void startServer() {
		String fileName = IConfigConst.PRIVILEGE_CONF_FILE;
		if (!new File(fileName).exists())
			return;
		try {
			final HashVO[] vos = Xml2HashVO.convertToVO(fileName, "user");
			if (Toolkit.isEmpty(vos))
				return;
			for (int i = 0; i < vos.length; i++) {
				String password = new Encode().decode(vos[i].getStringValue("password"));
				String name = vos[i].getStringValue("name");
				UserVO user = new UserVO(name, password);
				String resource = vos[i].getStringValue("resource");
				if (!Toolkit.isEmpty(resource)) {
					String[] resources = resource.split(";");
					for (String re : resources) {
						user.addResource(re);
					}
					users.put(name, user);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	public boolean checkPrivilege(String user, ServiceResource resource) {
		if (users.containsKey(user)) {
			Set<String> res = users.get(user).getResources();
			String sn = resource.getServiceName();
			if (res.contains(sn)) {
				return true;
			}
		}

		return false;
	}

	public void addUser(UserVO vo) {
		users.put(vo.getUser(), vo);
		saveUserToFile();
	}

	public void removeUser(String user) {
		if (users.containsKey(user)) {
			users.remove(user);
		}
		saveUserToFile();
	}

	private void saveUserToFile() {
		List<HashVO> vos = new ArrayList<HashVO>();
		for (UserVO uvo : users.values()) {
			HashVO hvo = new HashVO();
			hvo.setAttributeValue("name", uvo.getUser());
			hvo.setAttributeValue("password", new Encode().encode(uvo.getPassword()));
			StringBuffer sb = new StringBuffer();
			for (String re : uvo.getResources()) {
				sb.append(re + ";");
			}
			hvo.setAttributeValue("resource", sb.toString());
			vos.add(hvo);
		}

		HashVO2XML.writeToXmlFile(IConfigConst.PRIVILEGE_CONF_FILE, vos.toArray(new HashVO[0]), "user");
	}

	public Map<String, UserVO> getAllUser() {
		return users;
	}

	public String[] getAllService() {
		ServiceRegister sg = (ServiceRegister) ServiceHolder.getInstance().getServer(IConfigConst.SERVICERGISTERSERVICE);
		Set<String> services = new HashSet<String>();
		for (ServiceDesc sd : sg.getAllServices()) {
			services.add(sd.getServiceName());
		}
		return services.toArray(new String[0]);
	}

	public boolean checkPassword(String user, String password) {
		if (users.containsKey(user)) {
			if (users.get(user).getPassword().equals(password)) {
				return true;
			}
		} else if ("admin".equals(user) && "admin".equals(password)) {
			UserVO vo = new UserVO(user, password);
			for (String service : getAllService()) {
				vo.addResource(service);
			}
			this.addUser(vo);
			return true;
		}
		return false;
	}

	public boolean checkPrivilege(String service, String user, String password) {
		Object obj = ServiceHolder.getInstance().getServer(IConfigConst.SERVICERGISTERSERVICE);
		ServiceRegister register = (ServiceRegister) obj;
		ServiceDesc serviceDesc = register.getServiceByname(service);
		if (serviceDesc == null)
			return false;
		if (!serviceDesc.isNeedSecurityCheck())
			return true;
		if (checkPassword(user, password)) {
			if (users.get(user).getResources().contains(service)) {
				return true;
			}
		}
		try {
			MessageEntry entry = new MessageEntry();
			entry.setMsg("[WARNING]security check error.username=" + user);
			entry.setMsgResource(resource);
			entry.setTs(Toolkit.getCurTime());
			MsgSender.send(entry);
		} catch (Exception e) {

		}
		return false;
	}

	public void stopServer() {

	}

	public void refreshEnv() {

	}
	public boolean checkLicense() {
		return ServerEnv.checkLicense;
	}

}
