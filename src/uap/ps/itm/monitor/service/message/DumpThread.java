package uap.ps.itm.monitor.service.message;

import uap.ps.itm.monitor.service.center.ISnapShot;
import uap.ps.itm.monitor.service.center.ServerEnv;
import uap.ps.itm.monitor.service.center.ServiceHolder;
import uap.ps.itm.monitor.service.center.ServiceResource;
import uap.ps.itm.pub.servlet.RequestInfo;
import uap.ps.itm.pub.servlet.URLCommuniatcor;

public class DumpThread extends Thread {
	private Object dumpRes = null;
	private ServiceResource dumpResource = null;

	public void run() {
		dumpRes = dump();
	}

	public void setResource(ServiceResource resource) {
		dumpResource = resource;
	}

	public Object dump() {
		// �Ƿ��Ǳ�����Դ.
		if (ServerEnv.isLocalResource(dumpResource)) {
			// ����Ǳ�����Դ��ֱ�ӵ���.
			Object o = ServiceHolder.getInstance().getServer(dumpResource.getServiceDesc().getServiceName());
			if (!(o instanceof ISnapShot)) {
				return null;
			}
			return ((ISnapShot) o).dump(dumpResource);
		} else {
			// Զ����Դ,��Զ�̵���.
			RequestInfo request = new RequestInfo();
			request.setServiceName(dumpResource.getServiceDesc().getServiceName());
			request.setMethodName("dump");
			request.setParameter(new Object[] { dumpResource });
			request.setParametertypes(new Class[] { ServiceResource.class });
			String url = dumpResource.getRequestUrl();
			return URLCommuniatcor.remoteCall(request, url);
		}
	}

	public Object getDumpRes() {
		return dumpRes;
	}
}
