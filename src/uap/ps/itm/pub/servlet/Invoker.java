package uap.ps.itm.pub.servlet;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import uap.ps.itm.monitor.RemoteException;
import uap.ps.itm.monitor.pub.IConfigConst;
import uap.ps.itm.monitor.pub.SecurityNotPermissionException;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.center.ServerEnv;
import uap.ps.itm.monitor.service.center.ServiceHolder;
import uap.ps.itm.monitor.service.center.ServiceResource;
import uap.ps.itm.monitor.service.privilege.PrivilegeManager;

public class Invoker {

	public void invoke(RequestInfo request, OutputStream out) {
		ServiceResource resource = request.getResource();
		// 防止死循环.
		request.setResource(null);
		Object res = null;
		// 如果Resource不再本机,则重定向.
		if (resource != null && !ServerEnv.isLocalResource(resource)) {
			res = URLCommuniatcor.remoteCall(request, resource.getRequestUrl());
		} else {
			// 调用方法
			res = invokeMethod(request, out);
		}
		// 将流压缩后写出
		if (res != null && res instanceof EmptyObject) {
			return;
		}
		writeOut(res, out);
	}

	private void writeOut(Object o, OutputStream outStream) {
		ObjectOutputStream output = null;
		// 将流压缩
		// DeflaterOutputStream zipStream = null;
		try {
			// 将流压缩
			// zipStream = new DeflaterOutputStream(outStream);
//			if (outStream instanceof MailOutputStream) {
//				output = (MailOutputStream) outStream;
//			} else {
				output = new ObjectOutputStream(outStream);
//			}
			output.writeObject(o);
			output.flush();
			// zipStream.flush();
			// zipStream.finish();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
					// zipStream.close();
				} catch (Exception e) {
				}
			}

		}
	}

	private Object invokeMethod(RequestInfo request, OutputStream out) {
		try {
			// 安全检查
			if (!checkSecurity(request)) {
				return new SecurityNotPermissionException("security not permit!");
			}
			String serviceName = request.getServiceName();
			if (IConfigConst.SERVICESECURITYCHECK.equals(serviceName)) {
				serviceName = IConfigConst.SERVICEPRIVILEGE;
			}
			Object service = ServiceHolder.getInstance().getServer(serviceName);
			if (service == null) {
				System.out.println("service " + serviceName + " is  null");
				service = Thread.currentThread().getContextClassLoader().loadClass(serviceName).newInstance();
			}
			if (service instanceof IHttpServletAdapt) {
				return ((IHttpServletAdapt) service).invoke(request, out);
			}
			Method method = service.getClass().getMethod(request.getMethodName(), request.getParametertypes());
			return method.invoke(service, request.getParameter());
		} catch (Exception e) {
			e.printStackTrace();
			return new RemoteException("远程调用异常\n" + Toolkit.getExceptionStack(e));
		}
	}

	private boolean checkSecurity(RequestInfo request) {
		if (request.getServiceName().equals(IConfigConst.SERVICESECURITYCHECK))
			return true;
		if (request.getServiceName().equals(IConfigConst.SERVICEPRIVILEGE))
			return true;
		if (request.isServer2Server())
			return true;
		if (ServerEnv.getInstance().isServiceCenter()) {
			Object obj = ServiceHolder.getInstance().getServer(IConfigConst.SERVICEPRIVILEGE);
			if (obj == null)
				return true;
			PrivilegeManager securityCheck = (PrivilegeManager) obj;
			return securityCheck.checkPrivilege(request.getServiceName(), request.getUsername(), request.getPassword());
		} else {
			RequestInfo req = new RequestInfo();
			req.setMethodName("checkPrivilege");
			req.setParameter(new Object[] { request.getServiceName(), request.getUsername(), request.getPassword() });
			req.setParametertypes(new Class[] { String.class, String.class, String.class });
			req.setServiceName(IConfigConst.SERVICEPRIVILEGE);
			req.setDomain(request.getDomain());
			Object objRes = URLCommuniatcor.remoteCall(req, ServerEnv.getInstance().getServiceCenterUrl());
			if (objRes == null)
				return false;
			return (Boolean) objRes;
		}
	}

	public void debug(RequestInfo request) {
		StringBuffer sb = new StringBuffer();
		sb.append(request.getServiceName());
		sb.append("#").append(request.getMethodName());
		sb.append("#").append(request.getUsername());
		System.out.println(sb.toString());
	}

}
