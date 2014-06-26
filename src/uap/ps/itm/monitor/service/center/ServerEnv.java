package uap.ps.itm.monitor.service.center;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.Properties;

import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.IConfigConst;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.pub.Xml2HashVO;
import uap.ps.itm.pub.servlet.RequestInfo;
import uap.ps.itm.pub.servlet.URLCommuniatcor;

public class ServerEnv {
	private static ServerEnv m_inst = null;

	private Socket serviceCenter = null;

	private Socket localServer = null;

	private Properties prop = null;

	private boolean isServiceCenter = false;

	private String serviceCenterUrl = null;

	private String localurl = null;

	private boolean isDev;

	public static boolean isRunning = false;

	public static String hostName = null;

	public static String hostCanonicalHostName = null;

	public static String localAddr = null;
	
	private ThreadLocal<RequestInfo> req=new ThreadLocal<RequestInfo>();

	public static Hashtable<String, Boolean> hLocalHost = new Hashtable<String, Boolean>();
	public static boolean checkLicense = true;
	public boolean checkLicense() {
		return checkLicense;
	}
	public RequestInfo getRequestInfo(){
		return req.get();
	}
	public void setRequestInfo(RequestInfo req){
		this.req.set(req);
		
	}
	public String getUserName(){
		return this.req.get().getDomain()==null?null:this.req.get().getDomain().getUsername();
	}
	public Socket getServiceCenter() {

		return serviceCenter;
	}

	public Socket getLocalSocket() {
		return localServer;
	}

	public static ServerEnv getInstance() {
		if (m_inst == null) {
			m_inst = new ServerEnv();
		}
		return m_inst;
	}

	private ServerEnv() {
		System.setProperty("user.timezone", "Asia/Shanghai");
		setHostName();
		loadServerCfg();
		setServiceCenter();
		setUrl();
		String dev = System.getProperty("dev");
		isDev = (dev == null ? false : Boolean.parseBoolean(dev));
	}

	public Properties getProp() {
		return prop;
	}

	public boolean isServiceCenter() {
		// return false;
		return isServiceCenter;
	}

	private void loadServerCfg() {
		// load Server configure and properties.
		HashVO[] vos = null;
		try {
			vos = Xml2HashVO.convertToVO(IConfigConst.SERVICECFGFILE, "servicecenter");
			if (!Toolkit.isEmpty(vos)) {
				String host = vos[0].getStringValue("host");
				String port = vos[0].getStringValue("port");
				serviceCenter = new Socket(host, port);
				System.out.println("service center is " + host + ":" + port);
			}
			vos = Xml2HashVO.convertToVO(IConfigConst.SERVICECFGFILE, "server");

			if (!Toolkit.isEmpty(vos)) {
				vos = Xml2HashVO.convertToVO(IConfigConst.SERVICECFGFILE, "server");
				// 直接用本机的主机名
				String host = getLocalAddr();
				String port = vos[0].getStringValue("port");
				localServer = new Socket(host, port);
				System.out.println("local web server is " + host + ":" + port);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setServiceCenter() {
		isServiceCenter = isLocalHost(serviceCenter.getHost());
	}

	public void loadProp() {
		if (!isServiceCenter()) {
			System.out.println("read parameters from service center.");
			RequestInfo request = new RequestInfo();
			request.setServer2Server(true);
			request.setMethodName("getconfigure");
			request.setServiceName(IConfigConst.SERVICERGISTERSERVICE);
			Object obj = URLCommuniatcor.remoteCall(request, getServiceCenterUrl());
			if (obj instanceof Properties) {
				prop = (Properties) obj;
				System.out.println("read parameter over!");
				return;
			}
			System.out.println("can't read parameters from service center.then read parameters from local.");
		}
		prop = new Properties();
		try {
			prop.load(new FileInputStream(IConfigConst.PROPCONFIGFILE));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setUrl() {
		localurl = localServer.tourl() + IConfigConst.REMOTECALLSERVLET;
		serviceCenterUrl = serviceCenter.tourl() + IConfigConst.REMOTECALLSERVLET;
	}

	public String getLocalUrl() {
		return localurl;
	}

	public String getServiceCenterUrl() {
		return serviceCenterUrl;
	}

	public String getProperty(String name) {
		String value = getProp().getProperty(name);
		if (Toolkit.isEmpty(value))
			return System.getProperty(name);
		return value;
	}

	public static String getOStype() {
		String osName = System.getProperty("os.name");
		if (osName.toUpperCase().indexOf(IConfigConst.WINDOWS) > -1) {
			return IConfigConst.WINDOWS;
		} else if (osName.toUpperCase().indexOf(IConfigConst.AIX) > -1) {
			return IConfigConst.AIX;
		} else if (osName.toUpperCase().indexOf(IConfigConst.HP) > -1) {
			return IConfigConst.HP;
		} else if (osName.toUpperCase().indexOf(IConfigConst.LINUX) > -1) {
			return IConfigConst.LINUX;
		} else if (osName.toUpperCase().indexOf(IConfigConst.SALORIS) > -1) {
			return IConfigConst.SALORIS;
		} else {
			return IConfigConst.LINUX;
		}
	}

	private static void setHostName() {
		try {
			hostName = InetAddress.getLocalHost().getHostName();
			hostCanonicalHostName = InetAddress.getLocalHost().getCanonicalHostName();
			localAddr = InetAddress.getLocalHost().getHostAddress();
			if (localAddr.equals("127.0.0.1")) {
				System.out.println("多网卡建议将机器名绑定到一个IP地址");
				// localAddr = hostName;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getHostName() {
		return hostName;
	}

	public static String getLocalAddr() {
		return localAddr;
	}

	public static boolean isLocalResource(ServiceResource resource) {
		String host = resource.getHost();
		return isLocalHost(host);

	}

	public static boolean isLocalHost(String host) {
		Boolean res = hLocalHost.get(host);
		if (res != null) {
			return res.booleanValue();
		}
		if (Toolkit.isEqual(host, "localhost") || Toolkit.isEqual(host, "127.0.0.1") || Toolkit.isEqual(host, hostName) || Toolkit.isEqual(host, hostCanonicalHostName) || Toolkit.isEqual(host, localAddr)) {
			hLocalHost.put(host, new Boolean(true));
			return true;
		}
		boolean isIP = Toolkit.isNum(host);
		if (isIP) {
			try {
				InetAddress[] nets = InetAddress.getAllByName(host);
				for (int i = 0; i < nets.length; i++) {
					String name = nets[i].getHostName();
					if (Toolkit.isEqual(name, "localhost") || Toolkit.isEqual(name, hostName) || Toolkit.isEqual(name, hostCanonicalHostName)) {
						hLocalHost.put(host, new Boolean(true));
						return true;
					}
				}
				hLocalHost.put(host, new Boolean(false));
				return false;
			} catch (Exception e) {
			}
		}
		hLocalHost.put(host, new Boolean(false));
		return false;
	}
	/**
	 * @return the isDev
	 */
	public boolean isDev() {
		return isDev;
	}
	/**
	 * @param isDev the isDev to set
	 */
	public void setDev(boolean isDev) {
		this.isDev = isDev;
	}
}
