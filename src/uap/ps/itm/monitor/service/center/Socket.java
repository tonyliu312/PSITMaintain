package uap.ps.itm.monitor.service.center;

import java.io.Serializable;

public class Socket implements Serializable {
	private static final long serialVersionUID = 1L;
	private String host = null;
	private String port = null;

	public Socket(String host, String port) {
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String tourl() {
		return "http://" + host + ":" + port + "/";
	}

	public String toString() {
		if (host == null)
			return null;
		return host + ":" + port;
	}

	public String getToolTipText() {
		return "主机地址:" + getHost() + "\n端口:" + port;
	}
}
