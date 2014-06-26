package uap.ps.itm.monitor.connector.mail;

public class SMTPServerInfo {
	private String host = null;

	private String username = null;

	private String password = null;

	private String srcMailAddr = null;

	private String targetMailAddr = null;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getSrcMailAddr() {
		return srcMailAddr;
	}

	public void setSrcMailAddr(String srcMailAddr) {
		this.srcMailAddr = srcMailAddr;
	}

	public String getTargetMailAddr() {
		return targetMailAddr;
	}

	public void setTargetMailAddr(String targetMailAddr) {
		this.targetMailAddr = targetMailAddr;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
