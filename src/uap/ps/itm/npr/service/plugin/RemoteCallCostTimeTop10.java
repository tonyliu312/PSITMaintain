/**
 *
 */
package uap.ps.itm.npr.service.plugin;

import java.io.File;

import nc.monitor.pub.HashVO;
import nc.monitor.pub.Toolkit;

/**
 *
 * ���ʱ��Զ�̵���TOP10
 *
 * @author liuzy
 *
 */
public class RemoteCallCostTimeTop10 extends DefaultTop10PluginImpl {

	public RemoteCallCostTimeTop10() {
		this.setGroupColName(INPRPluginConst.NC_MW_CALLID, false);
		this.setValueColName(INPRPluginConst.NC_COSTTIME, true);
		this.setShowMethod(true);
		this.setCountColName("��ʱ(ms)");
//		this.setShowTimeRange(true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uap.ps.itm.npr.service.plugin.DefaultTop10PluginImpl#processVO(java.io.File,
	 * nc.monitor.pub.HashVO)
	 */
	@Override
	protected HashVO processVO(File file, HashVO vo) {
		HashVO hsvo = super.processVO(file, vo);
		if (Toolkit.isEmpty(hsvo))
			return null;
		try {
			long costtime = vo.getMsgLongValueByField(
					INPRPluginConst.NC_COSTTIME, null);
			long sqlcosttime = vo.getMsgLongValueByField(
					INPRPluginConst.NC_MW_SQLCOSTTIME, null);
			long readresulttime = vo.getMsgLongValueByField(
					INPRPluginConst.NC_MW_READRESULTTIME, null);
			long readfromclienttime = vo.getMsgLongValueByField(
					INPRPluginConst.NC_MW_READFROMCLIENTTIME, null);
			long writetoclienttime = vo.getMsgLongValueByField(
					INPRPluginConst.NC_MW_WRITETOCLIENTTIME, null);
			long mwCpuTime = costtime - sqlcosttime - readresulttime
					- readfromclienttime - writetoclienttime;

			String sqlnum = vo.getMsgStringValueByField(
					INPRPluginConst.NC_MW_SQLNUM, null);

			if (mwCpuTime < 0)
				return null;

			hsvo.setAttributeValue("SQL��ʱ(ms)", sqlcosttime);
			hsvo.setAttributeValue("��ȡ�������ʱ(ms)", readresulttime);
			hsvo.setAttributeValue("�����ʱ(ms)", readfromclienttime
					+ writetoclienttime);
			hsvo.setAttributeValue("�м��CPU��ʱ(ms)", mwCpuTime);
			hsvo.setAttributeValue("SQL����", sqlnum);
		} catch (Exception e) {
			return null;
		}

		return hsvo;
	}

}
