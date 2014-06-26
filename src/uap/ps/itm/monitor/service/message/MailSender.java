package uap.ps.itm.monitor.service.message;

import java.io.FileInputStream;
import java.util.Properties;

import nc.vo.framework.rsa.Encode;
import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.IConfigConst;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.center.ServerEnv;

public class MailSender extends Thread {
	private String content = null;

	private String address = null;

	private static MailSender m_inst = null;

	private static String smtphost = null;

	private static String smtpport = null;

	private static String username = null;

	private static String password = null;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public static MailSender getInstance() {
		if (m_inst == null) {
			m_inst = new MailSender();
			Properties prop = new Properties();
			try {
				prop.load(new FileInputStream(IConfigConst.MAILCONFIGFILE));
			} catch (Exception e) {

			}
			smtphost = prop.getProperty("smtphost");
			smtpport = prop.getProperty("smtpport");
			username = prop.getProperty("username");
			password = new Encode().decode(prop.getProperty("password"));
		}
		return m_inst;
	}

	private MailSender() {

	}

	public void sendMail(String msg, String file) {
		if (Toolkit.isEmpty(msg))
			return;
		if (Toolkit.isEmpty(smtphost) || Toolkit.isEmpty(smtpport))
			return;
		// ªÒµ√µÿ÷∑.
		String[] addresses = new String[4];
		boolean has = false;
		for (int i = 0; i < addresses.length; i++) {
			addresses[i] = ServerEnv.getInstance().getProperty("MAIL_SENDTO_" + i);
			if (!Toolkit.isEmpty(addresses[i])) {
				has = true;
			}
		}
		if (!has)
			return;
		StringBuffer content = new StringBuffer();
		content.append(msg);
		if (!Toolkit.isEmpty(file)) {
			MessageQuery query = new MessageQuery();
			HashVO[] vos = query.querySnapShot(file);
			if (!Toolkit.isEmpty(vos)) {
				for (int i = 0; i < vos.length; i++) {
					content.append("\n\n======snapshot=====\n");
					content.append(vos[i].toXml0());
				}
			}
		}
		for (int i = 0; i < addresses.length; i++) {
			if (!Toolkit.isEmpty(addresses[i])) {
				MailSender sender = new MailSender();
				sender.setContent(content.toString());
				sender.setAddress(addresses[i]);
				sender.start();
			}
		}
	}

	public void run() {

		MailSenderInfo mailinfo = getMailInfo();
		if (mailinfo == null)
			return;
		sendMail(mailinfo);
	}

	public void sendMail(MailSenderInfo mailinfo) {
		final MailSenderInfo mailinfo0 = mailinfo;
		Thread t = new Thread() {
			public void run() {
				new SimpleMailSender().sendTextMail(mailinfo0);
			}
		};
		t.start();
		try {
			t.join(10 * 1000);
		} catch (Exception e) {
		}
		if (t.isAlive())
			t.interrupt();
	}

	public MailSenderInfo getMailInfo() {
		MailSenderInfo mailInfo = new MailSenderInfo();
		mailInfo.setMailServerHost(smtphost);
		if (mailInfo.getMailServerHost() == null)
			return null;
		mailInfo.setMailServerPort(smtpport);
		mailInfo.setValidate(true);
		mailInfo.setUserName(username);
		mailInfo.setPassword(password);
		String sender = ServerEnv.getInstance().getProperty("MAIL_SENDER");
		sender = (Toolkit.isEmpty(sender) ? "monitor@ufida.com.cn" : sender);
		mailInfo.setFromAddress(sender);
		mailInfo.setToAddress(address);
		mailInfo.setSubject("msgtype=SNAPSHOT;" + ServerEnv.getInstance().getProperty("SNAPSHOTTITLE") + ";nc monitor server message!");
		mailInfo.setContent(content);
		return mailInfo;

	}
}
