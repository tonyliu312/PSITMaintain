/**
 *
 */
package uap.ps.itm.npr.service.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nc.monitor.Exception.RemoteException;
import nc.monitor.pub.HashVO;
import uap.ps.itm.npr.service.NPRGlobalControl;

/**
 * 怀疑有连接泄露的远程调用插件
 *
 * @author liuzy
 *
 */
public class RemoteCallConnNotClosePluginImpl extends AbsNPRPlugin {

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
				i = vo.getMsgIntValueByField(
						INPRPluginConst.NC_MW_NOTCLOSEDCONNECTIONCOUNT, null);
			} catch (NumberFormatException e) {
				continue;
			}
			if (i <= 0)
				continue;
			String method = vo.getMsgStringValueByField(
					INPRPluginConst.NC_MW_REMOTECALLMETHOD, null);
			if (list.contains(method))
				continue;
			list.add(method);
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
					INPRPluginConst.PLUGIN_COLUMN_REMOTECALLMETHOD, vo
							.getMsgStringValueByField(
									INPRPluginConst.NC_MW_REMOTECALLMETHOD,
									null));
			hsvo.setAttributeValue("泄露连接数", vo.getMsgIntValueByField(
					INPRPluginConst.NC_MW_NOTCLOSEDCONNECTIONCOUNT, null));
			this.addContentVOs(new HashVO[] { hsvo });
		}
	}
}
