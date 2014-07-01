/**
 * 
 */
package uap.ps.itm.npr.service.plugin;

import java.io.File;

import nc.monitor.Exception.RemoteException;
import nc.monitor.pub.HashVO;
import uap.ps.itm.npr.service.INPRConst;

/**
 * ���ݿ�������Ϣ
 * 
 * @author liuzy
 * 
 */
public class DBSummaryPluginImpl extends AbsNPRPlugin {

	/*
	 * (non-Javadoc)
	 * 
	 * @see uap.ps.itm.npr.service.plugin.INPRPluginItf#process(java.io.File,
	 * nc.monitor.pub.HashVO[])
	 */

	public void process(File file, HashVO[] dataVOs) throws RemoteException {
		if (!checkProc(file, dataVOs))
			return;
		if (file.getName().indexOf(INPRConst.NPR_DBINFO_FILE_NAME) < 0)
			return;
		this.addContentVOs(dataVOs);
	}

}
