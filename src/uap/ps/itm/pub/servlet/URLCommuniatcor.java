package uap.ps.itm.pub.servlet;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import uap.ps.itm.monitor.RemoteException;
import uap.ps.itm.monitor.pub.SecurityNotPermissionException;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.center.ServerEnv;

public class URLCommuniatcor {
//	public static boolean MAILCHANEL = Toolkit.isEqual(System.getProperty("protocal"), "email");

	public static Object remoteCall(Object request, String strUrl) {
//		if (MAILCHANEL) {
//			return email(request);
//		}
//		if(request instanceof RequestInfo){
//			RequestInfo req=(RequestInfo)request;
//			if(req.getDomain()==null&&ClientEnv.isClient){
//				try{						
//					req.setDomain(ResourceManager.getInstance().getDomain(StatContext.getInstance().getCurStat().getDomain()));
//				}catch(Exception e){
//				}
//			}
//		}
		ObjectOutputStream outPut = null;
		ObjectInputStream input = null;
		// InflaterInputStream zipStream = null;
		Object oIn = null;
		try {
			URL url = new URL(strUrl);
			URLConnection con = url.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			outPut = new ObjectOutputStream(con.getOutputStream());
			outPut.writeObject(request);
			// zipStream = new InflaterInputStream(con.getInputStream());
			input = new ObjectInputStream(con.getInputStream());
			oIn = input.readObject();
			outPut.close();
		} catch (UnknownHostException e) {
//			if (!ServerEnv.isRunning) {
//				JOptionPane.showMessageDialog(StatContext.getInstance().getMf(), LangResUtil.getInstance().getRes("输入的服务中心地址或者端口错误"));
//			}
			return oIn;
		} catch (ConnectException ex) {
//			if (!ServerEnv.isRunning) {
//				JOptionPane.showMessageDialog(StatContext.getInstance().getMf(), LangResUtil.getInstance().getRes("输入的服务中心地址或者端口错误"));
//			}
			return oIn;
		} catch (Exception e) {
			// e.printStackTrace();
			if (ServerEnv.isRunning) {
				return oIn;
			} else {
				// e.printStackTrace();
//				JOptionPane.showMessageDialog(StatContext.getInstance().getMf(), e.getMessage());
				return oIn;
			}
		} finally {
			try {
				if (outPut != null)
					outPut.close();
			} catch (Exception e) {
			}
			try {
				if (input != null)
					input.close();
			} catch (Exception e) {
			}
			if (oIn == null || ServerEnv.isRunning) {
				return oIn;
			}
			if (oIn instanceof SecurityNotPermissionException) {
				return oIn;
			} else if (oIn instanceof RemoteException) {
				RemoteException remoteEx = (RemoteException) oIn;
				if (!Toolkit.isEmpty(remoteEx.getMessage()) && remoteEx.getMessage().startsWith("远程调用异常")) {
					remoteEx.printStackTrace();
					throw new RuntimeException();
				}
//				JOptionPane.showMessageDialog(StatContext.getInstance().getMf(), remoteEx.getMessage());
				return null;
			}
		}
		return oIn;
	}

//	public static Object email(Object request) {
//		Object oIn = null;
//		try {
//			oIn = EmailCommunicator.remoteCall(request);
//		} catch (Exception e) {
//			// e.printStackTrace();
//		}
//		if (oIn == null) {
//			return null;
//		}
//		if (oIn instanceof SecurityNotPermissionException) {
//			return oIn;
//		} else if (oIn instanceof RemoteException) {
//			RemoteException remoteEx = (RemoteException) oIn;
//			JOptionPane.showMessageDialog(StatContext.getInstance().getMf(), remoteEx.getMessage());
//			return null;
//		}
//		return oIn;
//	}

//	public static final void main(String[] args) {
//		new ClientMailReceiver().start();
//		MAILCHANEL = true;
//		System.setProperty("protocal", "email");
//		System.setProperty("POPSERVER", "mail1.ufida.com.cn");
//		System.setProperty("POPTO", "liujb@ufida.com.cn");
//		System.setProperty("MAILUSER", "liujb");
//		System.setProperty("MAILPASSWORD", "d.");
//		System.setProperty("SMTPHOST", "mail1.ufida.com.cn");
//		System.setProperty("SMTPFROM", "liujb@ufida.com.cn");
//		System.setProperty("SMTPTO", "liujb@ufida.com.cn");
//		RequestInfo req = new RequestInfo();
//		req.setServiceName("processmonitor");
//		req.setMethodName("queryServerInfo");
//		req.setParameter(null);
//		Object o = URLCommuniatcor.remoteCall(req, null);
//		System.out.println(o);
//	}
}
