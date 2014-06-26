package uap.ps.itm.monitor.service.message;

import java.io.Serializable;

import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.service.center.ServiceResource;

public class MessageEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private String msg = null;

	private HashVO voKeyInfo = null;

	private String ts = null;

	private ServiceResource msgResource = null;
	private boolean needDump=true;
	public boolean isNeedDump() {
		return needDump;
	}

	public void setNeedDump(boolean needDump) {
		this.needDump = needDump;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public ServiceResource getMsgResource() {
		return msgResource;
	}

	public void setMsgResource(ServiceResource msgResource) {
		this.msgResource = msgResource;
	}

	public HashVO getVosKeyInfo() {
		return voKeyInfo;
	}

	public void setVoKeyInfo(HashVO voKeyInfo) {
		this.voKeyInfo = voKeyInfo;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("WARNING;");
		if (msgResource != null) {
			sb.append("[source]=");
			sb.append(msgResource.getServiceName()).append("-").append(msgResource.getDisplayName());
			sb.append(";ts="+getTs());
		}
		sb.append(";[content]=").append(msg);
		return sb.toString();
	}

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}
}
