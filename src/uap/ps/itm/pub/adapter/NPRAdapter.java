/**
 * 
 */
package uap.ps.itm.pub.adapter;

import nc.monitor.Exception.RemoteException;
import nc.monitor.pub.Toolkit;
import net.sf.json.JSONObject;
import uap.ps.itm.npr.service.ReportCentral;

/**
 * @author liuzy
 * 
 */
public class NPRAdapter {
	
	public final String NPR_PATH = "nprcode";

	public JSONObject buildNPR(String shotcode, String beginDateTime,
			String endDateTime) throws RemoteException {

		ReportCentral nprCentral = new ReportCentral();
		if (Toolkit.isEmpty(shotcode)) {
			shotcode = nprCentral.doSnapshot();
		}

		String nprPath = nprCentral.creatReport(shotcode, null, null);
		
		JSONObject jobj = new JSONObject();
		jobj.accumulate(NPR_PATH, nprPath);

		return null;

	}
}
