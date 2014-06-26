package uap.ps.itm.pub.servlet;

import java.io.Serializable;

import uap.ps.itm.monitor.pub.Domain;
import uap.ps.itm.monitor.service.center.ServiceResource;

public class RequestInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	// 调用的服务
	private String service = null;
	// 方法
	private String methodName = null;
	// 参数
	private Object[] parameter = null;
	// 参数类型
	private Class[] parametertypes = null;
	private Domain domain = null;
	private ServiceResource resource = null;
	private boolean isServer2Server = false;

	public ServiceResource getResource() {
		return resource;
	}

	public void setServer2Server(boolean b) {
		isServer2Server = b;
	}

	public boolean isServer2Server() {
		return isServer2Server;
	}

	public void setResource(ServiceResource resource) {
		this.resource = resource;
	}

	public String getUsername() {
		return domain == null ? null : domain.getUsername();
	}

	public String getPassword() {
		return domain == null ? null : domain.getPassword();
	}

	public String getServiceName() {
		return service;
	}

	public void setServiceName(String servicetype) {
		this.service = servicetype;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Object[] getParameter() {
		return parameter;
	}

	public void setParameter(Object[] parameter) {
		this.parameter = parameter;
	}

	public Class[] getParametertypes() {
		return parametertypes;
	}

	public void setParametertypes(Class[] parametertypes) {
		this.parametertypes = parametertypes;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;

	}

	public Domain getDomain() {
		return domain;
	}

}
