/**
 * 
 */
package uap.ps.itm.pub.adapter;

import uap.ps.itm.monitor.RemoteException;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.npr.service.NPRCentral;
import net.sf.json.JSONObject;

/**
 * @author liuzy
 * 
 */
public class NPRAdapter {
	
	public final String NPR_PATH = "nprcode";

	public JSONObject buildNPR(String shotcode, String beginDateTime,
			String endDateTime) throws RemoteException {

		NPRCentral nprCentral = new NPRCentral();
		if (Toolkit.isEmpty(shotcode)) {
			shotcode = nprCentral.doSnapshot();
		}

		String nprPath = nprCentral.creatReport(shotcode, null, null);
		
		JSONObject jobj = new JSONObject();
		jobj.accumulate(NPR_PATH, nprPath);

		return null;

	}
}
