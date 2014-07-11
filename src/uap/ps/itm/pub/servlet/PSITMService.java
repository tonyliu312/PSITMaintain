/**
 * 
 */
package uap.ps.itm.pub.servlet;

import nc.monitor.Exception.RemoteException;
import nc.monitor.service.center.IServer;
import nc.monitor.service.center.ServerEnv;
import nc.monitor.service.center.ServiceDesc;
import nc.monitor.service.center.ServiceResource;
import net.sf.json.JSONObject;
import uap.ps.itm.npr.service.ReportCentral;
import uap.ps.itm.pub.consts.IServletConst;

/**
 * @author liuzy
 * 
 */
public class PSITMService implements IServer {

	public static ServiceResource resource = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see nc.monitor.service.center.IServer#startServer()
	 */
	@Override
	public void startServer() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nc.monitor.service.center.IServer#stopServer()
	 */
	@Override
	public void stopServer() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * nc.monitor.service.center.IServer#getServerResouce(nc.monitor.service
	 * .center.ServiceDesc)
	 */
	@Override
	public ServiceResource[] getServerResouce(ServiceDesc serviceDesc) {
		if (!ServerEnv.getInstance().isServiceCenter())
			return null;
		resource = new ServiceResource();
		resource.setDisplayName(ServerEnv.getHostName());
		resource.setHost(ServerEnv.getLocalAddr());
		resource.setPort(ServerEnv.getInstance().getLocalSocket().getPort());
		resource.setRequestUrl(ServerEnv.getInstance().getLocalUrl());
		resource.setServiceName(serviceDesc.getServiceName());
		resource.setServiceDesc(serviceDesc);
		return new ServiceResource[] { resource };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nc.monitor.service.center.IServer#refreshEnv()
	 */
	@Override
	public void refreshEnv() {
		// TODO Auto-generated method stub

	}

	public String buildNPR() throws RemoteException {
		// File f = new File(".");
		// System.out.println(f.getAbsolutePath());
		// try {
		// System.out.println(f.getCanonicalPath());
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		ReportCentral rc = new ReportCentral();
		rc.buildNPR();
		JSONObject resultJSON = new JSONObject();
		resultJSON.accumulate(IServletConst.RESULT_SUCCESS,
				IServletConst.RESULT_SUCCESS_VAL);
		resultJSON.accumulate(IServletConst.RESULT_TYPE,
				IServletConst.RESULT_STRING);
		resultJSON.accumulate(IServletConst.RESULT_VALUE, "OK");
		return resultJSON.toString();
	}

	public String loadhistory() {
		 String jsonStr = new ReportCentral().loadhistory().toString();
//		String jsonStr = "{\"draw\": 1,\"recordsTotal\": 57,\"recordsFiltered\": 57,\"data\":[[\"2014-06-30 12:28:47\",\"2014-05-21 00:00:00\",\"2014-06-30 12:28:06\",\"20140630122805\",\"41s\",\"http://192.168.1.103:9999/npr/NPR20140630122847/npr.html\"],[\"2014-06-30 14:32:05\",\"2014-05-21 00:00:00\",\"2014-06-30 14:32:01\",\"20140630143201\",\"3s\",\"http://192.168.1.103:9999/npr/NPR20140630143205/npr.html\"],[\"2014-06-30 14:39:54\",\"2014-05-21 00:00:00\",\"2014-06-30 14:39:50\",\"20140630143950\",\"3s\",\"http://192.168.1.103:9999/npr/NPR20140630143954/npr.html\"],[\"2014-06-30 14:41:15\",\"2014-05-21 00:00:00\",\"2014-06-30 14:41:13\",\"20140630144112\",\"2s\",\"http://192.168.1.103:9999/npr/NPR20140630144115/npr.html\"],[\"2014-07-01 13:46:18\",\"2014-05-22 00:00:00\",\"2014-07-01 13:46:14\",\"20140701134614\",\"3s\",\"http://192.168.1.104:9999/npr/NPR20140701134618/npr.html\"],[\"2014-07-01 14:02:48\",\"2014-05-22 00:00:00\",\"2014-07-01 14:02:45\",\"20140701140245\",\"3s\",\"http://192.168.1.104:9999/npr/NPR20140701140248/npr.html\"],[\"2014-07-01 14:03:50\",\"2014-05-22 00:00:00\",\"2014-07-01 14:03:47\",\"20140701140347\",\"2s\",\"http://192.168.1.104:9999/npr/NPR20140701140350/npr.html\"]]}";
		return jsonStr;
	}

}
