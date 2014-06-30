package nc.monitor.service.center;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import nc.monitor.licence.CheckLicense63;
import nc.monitor.pub.HashVO;
import nc.monitor.pub.IConfigConst;
import nc.monitor.pub.Toolkit;
import nc.monitor.pub.Xml2HashVO;
import nc.monitor.servlet.RequestInfo;
import nc.monitor.servlet.URLCommuniatcor;
import nc.monitor.ui.pub.DummyPrintStream;

public class WebStartupHook implements ServletContextListener {
	public WebStartupHook() {
		super();
	}

	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("stop the monitor.");
		ServiceHolder.getInstance().stopServer();
		// 从service center 取消注册。
		unRegistService();
	}

	public void contextInitialized(ServletContextEvent arg0) {
		if (ServerEnv.isRunning)
			return;
		ServerEnv.isRunning = true;
		System.out.println("the OS  is " + System.getProperty("os.name"));
		IConfigConst.init();
		// 加载参数配置.
		ServerEnv.getInstance().loadProp();
		// check license;
		PrintStream ps = System.out;
		PrintStream err = System.err;
		try {

			System.setOut(new NullPrintStream(new OutputStream() {
				@Override
				public void write(int b) throws IOException {
				}
			}));
			System.setErr(new NullPrintStream(new OutputStream() {
				@Override
				public void write(int b) throws IOException {
				}
			}));
			// 检查license
			ServerEnv.checkLicense = new CheckLicense63().check(); //new CheckLicense().check();

		} catch (Exception e) {

		} finally {
			System.setOut(ps);
			System.setErr(err);
		}
		// 加载服务。
		loadService();
//		if (!ServerEnv.getInstance().isDev())
//			resetStream();
	}

	/**
	 * 重定向流
	 */
	private void resetStream() {
		System.setOut(new DummyPrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
			}
		}, true));

		System.setErr(new DummyPrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
			}
		}, false));
	}

	private void loadService() {

		// 加载服务信息
		ServiceDesc[] services = loadServices();
		for (int i = 0; i < services.length; i++) {
			if (!services[i].isStart())
				continue;
			// 判断service center 检查服务是否允许启动。
			String serviceName = services[i].getServiceName();
			boolean isCanStart = isCanBeStart(services[i]);
			// 如果允许，则启动这个service,
			boolean isStarted = false;
			if (isCanStart)
				isStarted = startService(services[i]);
			if (!isStarted)
				continue;
			// 如果启动成功，则向service center 注册。
			registerToServiceCenter(services[i]);
			// 等待2s以确保service 已经将资源加载完毕。
			sleep(1000 * 2);
			// 向serviceCenter注册资源。
			ServiceResource[] resouces = ServiceHolder.getInstance()
					.getServer(serviceName).getServerResouce(services[i]);
			registerResourceToServiceCenter(resouces);
		}
	}

	private void registerToServiceCenter(ServiceDesc service) {
		System.out.println("begin to register service ["
				+ service.getServiceName() + "] to service center.");
		if (isServiceCenter()) {
			ServiceRegister register = (ServiceRegister) ServiceHolder
					.getInstance()
					.getServer(IConfigConst.SERVICERGISTERSERVICE);
			register.registerService(ServerEnv.getInstance().getLocalSocket(),
					service);
		} else {
			// 构造一个RequestEntry,
			RequestInfo request = new RequestInfo();
			request.setServer2Server(true);
			request.setMethodName("registerService");
			request.setParameter(new Object[] {
					ServerEnv.getInstance().getLocalSocket(), service });
			request.setParametertypes(new Class[] { Socket.class,
					ServiceDesc.class });
			request.setServiceName(IConfigConst.SERVICERGISTERSERVICE);
			URLCommuniatcor.remoteCall(request, ServerEnv.getInstance()
					.getServiceCenterUrl());
		}
		System.out.println("register service[" + service.getServiceName()
				+ "] over!");
	}

	private void unRegistService() {
		System.out.println("begin to unregister service from service center.");
		if (isServiceCenter()) {
			ServiceRegister register = (ServiceRegister) ServiceHolder
					.getInstance()
					.getServer(IConfigConst.SERVICERGISTERSERVICE);
			register.unRegisterService(ServerEnv.getInstance().getLocalSocket());
		} else {
			// 构造一个RequestEntry,
			RequestInfo request = new RequestInfo();
			request.setServer2Server(true);
			request.setMethodName("unRegisterService");
			request.setParameter(new Object[] { ServerEnv.getInstance()
					.getLocalSocket() });
			request.setParametertypes(new Class[] { Socket.class });
			request.setServiceName(IConfigConst.SERVICERGISTERSERVICE);
			URLCommuniatcor.remoteCall(request, ServerEnv.getInstance()
					.getServiceCenterUrl());
		}
		System.out.println("unregister service  over!");
	}

	private void registerResourceToServiceCenter(ServiceResource[] resource) {
		if (Toolkit.isEmpty(resource))
			return;
		System.out
				.println("begin to register service resource  to service center.");
		System.out.println("resource number is " + resource.length);
		if (isServiceCenter()) {
			ServiceRegister register = (ServiceRegister) ServiceHolder
					.getInstance()
					.getServer(IConfigConst.SERVICERGISTERSERVICE);
			register.registerResource(ServerEnv.getInstance().getLocalSocket(),
					resource);
		} else {
			// 构造一个RequestEntry,
			RequestInfo request = new RequestInfo();
			request.setServer2Server(true);
			request.setMethodName("registerResource");
			request.setParameter(new Object[] {
					ServerEnv.getInstance().getLocalSocket(), resource });
			request.setParametertypes(new Class[] { Socket.class,
					ServiceResource[].class });
			request.setServiceName(IConfigConst.SERVICERGISTERSERVICE);
			URLCommuniatcor.remoteCall(request, ServerEnv.getInstance()
					.getServiceCenterUrl());
		}
		System.out.println("register service resource over!");
	}

	private boolean startService(ServiceDesc service) {
		String className = service.getImplementClass();
		if (className == null) {
			System.out.println("service " + service.getServiceName()
					+ " has no implement Class!start service failed!");
			return false;
		}
		try {
			Object obj = Thread.currentThread().getContextClassLoader()
					.loadClass(className).newInstance();
			if (obj instanceof IServer) {
				IServer server = (IServer) obj;
				server.startServer();
				ServiceHolder.getInstance().registServer(
						service.getServiceName(), server);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private ServiceDesc[] loadServices() {
		try {
			HashVO[] vos = Xml2HashVO.convertToVO(IConfigConst.SERVICECFGFILE,
					"service");
			ServiceDesc[] services = new ServiceDesc[vos.length];
			for (int i = 0; i < vos.length; i++) {
				services[i] = new ServiceDesc();
				services[i].setImplementClass(vos[i]
						.getStringValue("implementClass"));
				services[i].setNeedSecurityCheck(vos[i]
						.getBooleanValue("isNeedSecurityCheck"));
				services[i]
						.setServiceName(vos[i].getStringValue("serviceName"));
				services[i].setSingle(vos[i].getBooleanValue("isSingle"));
				services[i].setStart(vos[i].getBooleanValue("isStart"));
				services[i].setMsgHandler(vos[i].getStringValue("msghandle"));
				services[i].setDumpItems(getDumpItems(vos[i]
						.getStringValue("snapshotitem")));
			}
			return services;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private SnapShotItem[] getDumpItems(String itemStr) {
		if (Toolkit.isEmpty(itemStr))
			return null;
		StringTokenizer st = new StringTokenizer(itemStr, ",");
		ArrayList<SnapShotItem> al = new ArrayList<SnapShotItem>();
		SnapShotItem temp = null;
		while (st.hasMoreTokens()) {
			String item = st.nextToken();
			StringTokenizer st1 = new StringTokenizer(item, "|");
			if (st1.hasMoreTokens()) {
				temp = new SnapShotItem();
				temp.setServices(st1.nextToken());
				temp.setScopes(st1.nextToken());
				al.add(temp);
			}
		}
		return (SnapShotItem[]) al.toArray(new SnapShotItem[al.size()]);
	}

	private boolean isServiceCenter() {
		return ServerEnv.getInstance().isServiceCenter();
	}

	private boolean isCanBeStart(ServiceDesc service) {
		if (isServiceCenter()) {
			if (service.getServiceName().equalsIgnoreCase(
					IConfigConst.SERVICERGISTERSERVICE))
				return true;
			ServiceRegister register = (ServiceRegister) ServiceHolder
					.getInstance()
					.getServer(IConfigConst.SERVICERGISTERSERVICE);
			return register.isCanbeStart(service);
		} else {
			// 构造一个RequestEntry,
			RequestInfo request = new RequestInfo();
			request.setServer2Server(true);
			request.setMethodName("isCanbeStart");
			request.setParameter(new Object[] { service });
			request.setParametertypes(new Class[] { ServiceDesc.class });
			request.setServiceName(IConfigConst.SERVICERGISTERSERVICE);
			Object oRes = URLCommuniatcor.remoteCall(request, ServerEnv
					.getInstance().getServiceCenterUrl());
			if (oRes != null && oRes instanceof Boolean) {
				return (Boolean) oRes;
			}
		}
		return false;
	}

	private void sleep(long duration) {
		try {
			Thread.sleep(duration);
		} catch (Exception e) {

		}
	}

	public static final void main(String[] args) {
		WebStartupHook hook = new WebStartupHook();
		ServerEnv.isRunning = true;
		hook.loadService();
	}

}
