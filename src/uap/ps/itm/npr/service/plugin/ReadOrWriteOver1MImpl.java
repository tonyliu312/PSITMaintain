/**
 *
 */
package uap.ps.itm.npr.service.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nc.monitor.Exception.RemoteException;
import nc.monitor.pub.HashVO;

/**
 * 网络流量Read或Write超过1M的统计插件
 *
 * @author liuzy
 *
 */
public class ReadOrWriteOver1MImpl extends AbsNPRPlugin {

	private final List<String> list = new ArrayList<String>();

	private final long oneM = 1024 * 1024;

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
			try{
			if (!checkVOEndTime(vo))
				break;
			}
			catch(Exception e){
				continue;
			}
			if (checkVOByFilter(vo))
				continue;
			long read;
			long write;
			try {
				read = vo.getMsgLongValueByField(
						INPRPluginConst.NC_MW_WRITETOCLIENTBYTES, null);
				write = vo.getMsgLongValueByField(
						INPRPluginConst.NC_MW_READFROMCLIENTBYTES, null);
			} catch (Exception e) {
				continue;
			}
			String methodname = vo.getMsgStringValueByField(
					INPRPluginConst.NC_MW_REMOTECALLMETHOD, null);
			if (read < oneM && write < oneM)
				continue;
			if (list.contains(methodname))
				continue;
			list.add(methodname);
			HashVO hsvo = new HashVO();
			hsvo.setAttributeValue(INPRPluginConst.NC_MW_CALLID,
					vo.getStringValue(INPRPluginConst.NC_MW_CALLID));
			hsvo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_USERID,
					vo.getStringValue(INPRPluginConst.NC_MW_USERID));
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
			hsvo.setAttributeValue("上行流量(KB)", "");
			hsvo.setAttributeValue("下行流量(KB)", "");
			if (write > oneM)
				hsvo.setAttributeValue("上行流量(KB)", write / 1024);
			else
				hsvo.setAttributeValue("下行流量(KB)", read / 1024);
			this.addContentVOs(new HashVO[] { hsvo });
		}
	}
}

