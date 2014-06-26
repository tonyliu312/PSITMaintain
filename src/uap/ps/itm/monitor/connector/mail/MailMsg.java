package uap.ps.itm.monitor.connector.mail;

import java.util.StringTokenizer;

import uap.ps.itm.monitor.connector.pub.IMsg;

public class MailMsg implements IMsg {
	private String msgType = null;

	private String title = null;

	private String body = null;

	private Object obj = null;

	private String emailAddr = null;

	private String callid = null;

	public String getEmailAddr() {
		return emailAddr;
	}

	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		StringTokenizer st = new StringTokenizer(title, ";");
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			if (s.indexOf("=") < 0) {
				continue;
			}
			StringTokenizer st1 = new StringTokenizer(s, "=");
			String ele = st1.nextElement().toString();
			if (ele.equals("msgtype")) {
				msgType = st1.nextElement().toString();
				continue;
			}
			if (ele.equals("callid")) {
				callid = st1.nextElement().toString();
				continue;
			}
		}
	}

	public String getCallID() {
		return callid;
	}

	public boolean isValid() {
		return getTitle() == null || getTitle().startsWith("msgtype=");
	}

}
