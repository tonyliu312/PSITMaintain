package uap.ps.itm.monitor.connector.mail;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import uap.ps.itm.monitor.connector.pub.IMsg;
import uap.ps.itm.pub.servlet.RequestInfo;

public class MailSender {
	SMTPServerInfo smtp = null;

	public MailSender(SMTPServerInfo smtp) {
		this.smtp = smtp;
	}

	public void sendMail(IMsg msg) {
		String host = smtp.getHost();
		String from = smtp.getSrcMailAddr();
		String to = msg.getEmailAddr();
		String user = smtp.getUsername();
		String ps = smtp.getPassword();
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.auth", "true");
		Session session = Session.getInstance(props, new SmtpAuth());
		// session.setDebug(true);
		MimeMessage message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					to));
			message.setSubject(msg.getTitle());
			Multipart multipart = new MimeMultipart();
			BodyPart contentPart = new MimeBodyPart();
			contentPart.setText(msg.getBody() == null ? "n/a" : msg.getBody());
			multipart.addBodyPart(contentPart);
			// 添加附件
			BodyPart attachmentPart = new MimeBodyPart();
			attachmentPart.setDataHandler(new DataHandler(getByteDataSource(msg
					.getObj())));
			sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();
			attachmentPart.setFileName("=?GBK?B?"
					+ enc.encode("remotecall".getBytes()) + "?=");
			multipart.addBodyPart(attachmentPart);
			message.setContent(multipart);
			// 发送邮件
			message.saveChanges();
			Transport transport = session.getTransport("smtp");
			transport.connect(host, user, ps);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public DataSource getByteDataSource(Object obj) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(obj);
		oos.close();
		ByteArrayDataSource datasource = new ByteArrayDataSource(
				bos.toByteArray(), "application/octet-stream");
		return datasource;
	}

	class SmtpAuth extends javax.mail.Authenticator {
		private String user, password;

		public void getuserinfo(String getuser, String getpassword) {
			user = getuser;
			password = getpassword;
		}

		protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
			return new javax.mail.PasswordAuthentication(user, password);
		}
	}

}
