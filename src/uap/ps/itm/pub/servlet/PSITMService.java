/**
 * 
 */
package uap.ps.itm.pub.servlet;

import nc.monitor.Exception.RemoteException;
import nc.monitor.pub.HashVO;
import nc.monitor.pub.Text2HashVO;
import nc.monitor.service.center.IServer;
import nc.monitor.service.center.ServiceDesc;
import nc.monitor.service.center.ServiceResource;
import nc.vo.pub.lang.UFDateTime;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import uap.ps.itm.npr.service.INPRConst;
import uap.ps.itm.npr.service.ReportCentral;

/**
 * @author liuzy
 * 
 */
public class PSITMService implements IServer {

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
		// TODO Auto-generated method stub
		return null;
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

	public void buildNPR() throws RemoteException {
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
	}

	public JSONObject loadhistory() {
		return new ReportCentral().loadhistory();
	}

}
