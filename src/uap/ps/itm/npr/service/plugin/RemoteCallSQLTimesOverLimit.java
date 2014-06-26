/**
 *
 */
package uap.ps.itm.npr.service.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uap.ps.itm.monitor.RemoteException;
import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.service.center.ServerEnv;
import uap.ps.itm.npr.service.INPRConst;
import uap.ps.itm.npr.service.NPRGlobalControl;

/**
 * SQL超标的插件
 *
 * @author liuzy
 *
 */
public class RemoteCallSQLTimesOverLimit extends AbsNPRPlugin {

	private final int sqlnum_limit = Integer.valueOf(ServerEnv.getInstance()
			.getProperty(INPRConst.NPR_REMOTECALL_SQLNUM_LIMIT));

	private final List<String> list = new ArrayList<String>();

	/*
	 * (non-Javadoc)
	 *
	 * @see uap.ps.itm.npr.service.plugin.INPRPluginItf#process(java.io.File,
	 * nc.monitor.pub.HashVO[])
	 */

	@Override
	public void process(File file, HashVO[] dataVOs) throws RemoteException {
		if (!checkProc(file, dataVOs))
			return;
		for (HashVO vo : dataVOs) {
			if (!checkVO(vo))
				continue;
			if (!checkVOBeginTime(vo))
				continue;
			try {
			if (!checkVOEndTime(vo))
					break;
			} catch (Exception e) {
				continue;
			}
			if (checkVOByFilter(vo))
				continue;
			int i;
			try {
				i = vo.getMsgIntValueByField(INPRPluginConst.NC_MW_SQLNUM, null);
			} catch (Exception e) {
				continue;
			}
			String methodname = vo.getMsgStringValueByField(
					INPRPluginConst.NC_MW_REMOTECALLMETHOD, null);
			if (i <= sqlnum_limit)
				continue;
			if (list.contains(methodname))
				continue;
			list.add(methodname);
			HashVO hsvo = new HashVO();
			hsvo.setAttributeValue(INPRPluginConst.NC_MW_CALLID,
					vo.getStringValue(INPRPluginConst.NC_MW_CALLID));
			try {
				hsvo.setAttributeValue(
						INPRPluginConst.PLUGIN_COLUMN_USERID,
						NPRGlobalControl.getInstance().getUserCodeByPK(
								vo.getStringValue(INPRPluginConst.NC_MW_USERID)));
			} catch (Exception e) {
				e.printStackTrace();
			}
			hsvo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_TS,
					vo.getStringValue(INPRPluginConst.TS));
			hsvo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_REMOTEADDR, vo
					.getMsgStringValueByField(INPRPluginConst.NC_MW_REMOTEADDR,
							null));
			hsvo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_BUSIACTION, vo
					.getMsgStringValueByField(INPRPluginConst.NC_MW_BUSIACTION,
							null));
			hsvo.setAttributeValue(
					INPRPluginConst.PLUGIN_COLUMN_REMOTECALLMETHOD, methodname);
			hsvo.setAttributeValue("SQL数量", i);
			this.addContentVOs(new HashVO[] { hsvo });
		}
	}

}
