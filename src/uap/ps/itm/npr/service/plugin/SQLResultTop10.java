/**
 *
 */
package uap.ps.itm.npr.service.plugin;


/**
 * ��ȡ���������SQL Top10ͳ�Ʋ��
 *
 * @author liuzy
 *
 */
public class SQLResultTop10 extends DefaultTop10PluginImpl {

	public SQLResultTop10() {
		this.setMsgToken(INPRPluginConst.MSG_TOKEN_STICK);
		this.setGroupColName(INPRPluginConst.NC_MSG, true);
		this.setValueColName(INPRPluginConst.NC_WARNINGSQL_ROWNUM, true);
		this.setCountColName("���������");
		this.setShowColName(INPRPluginConst.NC_MSG, true);
		this.setBusiActionColName(INPRPluginConst.PLUGIN_COLUMN_BUSIACTION,
				INPRPluginConst.NC_WARNINGSQL_DEFCOL1, true);
		this.setDealMaster(true);
	}

}
