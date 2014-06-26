package uap.ps.itm.monitor.service.center;

import java.io.Serializable;

public class ServiceDesc implements Serializable {
	private static final long serialVersionUID = 1L;

	private String serviceName = null;

	private String implementClass = null;

	private boolean isSingle = false;

	private boolean isNeedSecurityCheck = false;

	private boolean isStart = false;

	private Socket socket = null;

	private SnapShotItem[] dumpItems = null;

	private String msgHandler = null;

	public String getMsgHandler() {
		return msgHandler;
	}

	public void setMsgHandler(String msgHandler) {
		this.msgHandler = msgHandler;
	}

	public SnapShotItem[] getDumpItems() {
		return dumpItems;
	}

	public void setDumpItems(SnapShotItem[] dumpItems) {
		this.dumpItems = dumpItems;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getImplementClass() {
		return implementClass;
	}

	public void setImplementClass(String implementClass) {
		this.implementClass = implementClass;
	}

	public boolean isSingle() {
		return isSingle;
	}

	public void setSingle(boolean isSingle) {
		this.isSingle = isSingle;
	}

	public boolean isNeedSecurityCheck() {
		return isNeedSecurityCheck;
	}

	public void setNeedSecurityCheck(boolean isNeedSecurityCheck) {
		this.isNeedSecurityCheck = isNeedSecurityCheck;
	}

	public boolean isStart() {
		return isStart;
	}

	public void setStart(boolean isStart) {
		this.isStart = isStart;
	}

}
