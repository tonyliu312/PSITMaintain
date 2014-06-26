package uap.ps.itm.monitor.pub;

import java.io.Serializable;

public class EmailDomain implements Serializable {

	private static final long serialVersionUID = 8464528536860911391L;
	
	/**
	 * 消息中心邮件地址
	 */
	private String centerMail = null;
	
	/**
	 * 域用户名
	 */
	private String userName = null;
	
	/**
	 * 域密码
	 */
	private String userPwd = null;
	
	/**
	 * 邮箱账户
	 */
	private String email = null;
	
	/**
	 * 邮箱密码
	 */
	private String emailPwd = null;
	
	/**
	 * 发送服务器
	 */
	private String smtpHost = null;
	
	/**
	 * 接收服务器
	 */
	private String popHost = null;
	
	public EmailDomain(String centerMail) {
		this.centerMail = centerMail;
	}

	public String getCenterMail() {
		return centerMail;
	}

	public void setCenterMail(String centerMail) {
		this.centerMail = centerMail;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserPwd() {
		return userPwd;
	}

	public void setUserPwd(String userPwd) {
		this.userPwd = userPwd;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmailPwd() {
		return emailPwd;
	}

	public void setEmailPwd(String emailPwd) {
		this.emailPwd = emailPwd;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public String getPopHost() {
		return popHost;
	}

	public void setPopHost(String popHost) {
		this.popHost = popHost;
	}
	
	

}
