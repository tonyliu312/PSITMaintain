package uap.ps.itm.monitor.pub;

import java.io.Serializable;

public class EmailDomain implements Serializable {

	private static final long serialVersionUID = 8464528536860911391L;
	
	/**
	 * ��Ϣ�����ʼ���ַ
	 */
	private String centerMail = null;
	
	/**
	 * ���û���
	 */
	private String userName = null;
	
	/**
	 * ������
	 */
	private String userPwd = null;
	
	/**
	 * �����˻�
	 */
	private String email = null;
	
	/**
	 * ��������
	 */
	private String emailPwd = null;
	
	/**
	 * ���ͷ�����
	 */
	private String smtpHost = null;
	
	/**
	 * ���շ�����
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
