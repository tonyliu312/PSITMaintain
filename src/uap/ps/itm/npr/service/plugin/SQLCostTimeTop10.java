/**
 *
 */
package uap.ps.itm.npr.service.plugin;

import java.io.File;

import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Toolkit;

/**
 *
 * ���ʱ��SQLTop10ͳ�Ʋ��
 *
 * @author liuzy
 *
 */
public class SQLCostTimeTop10 extends DefaultTop10PluginImpl {

	public SQLCostTimeTop10() {
		this.setMsgToken(INPRPluginConst.MSG_TOKEN_STICK);
		this.setGroupColName(INPRPluginConst.NC_MSG, true);
		this.setValueColName(INPRPluginConst.NC_COSTTIME, true);
		this.setCountColName("��ʱ(����)");
		this.setShowColName(INPRPluginConst.NC_MSG, true);
		this.setBusiActionColName(INPRPluginConst.PLUGIN_COLUMN_BUSIACTION,
				INPRPluginConst.NC_WARNINGSQL_DEFCOL1, true);
		this.setDealMaster(true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * uap.ps.itm.npr.service.plugin.DefaultTop10PluginImpl#processVO(nc.monitor.pub
	 * .HashVO)
	 */
	@Override
	protected HashVO processVO(File file, HashVO vo) {
		HashVO hsvo = super.processVO(file, vo);
		if (Toolkit.isEmpty(hsvo))
			return null;
		Long rownum = vo.getMsgLongValueByField(
				INPRPluginConst.NC_WARNINGSQL_ROWNUM,
				INPRPluginConst.MSG_TOKEN_STICK);
		if(Toolkit.isEmpty(rownum))
			rownum = 0L;
		hsvo.setAttributeValue("���������", rownum);
		return hsvo;
	}
}
