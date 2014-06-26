/**
 * 
 */
package uap.ps.itm.npr.service.plugin;

/**
 * SQL数量最多的远程调用Top10统计插件
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
		this.setCountColName("SQL数量");
		this.setShowMethod(true);
	}

}
