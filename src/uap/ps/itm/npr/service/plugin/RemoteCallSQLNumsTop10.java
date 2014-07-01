/**
 * 
 */
package uap.ps.itm.npr.service.plugin;

/**
 * SQL��������Զ�̵���Top10ͳ�Ʋ��
 * 
 * @author liuzy
 * 
 */
public class RemoteCallSQLNumsTop10 extends DefaultTop10PluginImpl {

	public RemoteCallSQLNumsTop10() {
		this.setGroupColName(INPRPluginConst.NC_MW_CALLID, false);
		this.setValueColName(INPRPluginConst.NC_MW_SQLNUM, true);
		this.setBusiActionColName(INPRPluginConst.PLUGIN_COLUMN_BUSIACTION,
				INPRPluginConst.NC_MW_BUSIACTION, true);
		this.setCountColName("SQL����");
		this.setShowMethod(true);
	}

}
