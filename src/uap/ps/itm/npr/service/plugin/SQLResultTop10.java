/**
 *
 */
package uap.ps.itm.npr.service.plugin;


/**
 * 读取结果集最多的SQL Top10统计插件
 *
 * @author liuzy
 *
 */
public class SQLResultTop10 extends DefaultTop10PluginImpl {

	public SQLResultTop10() {
		this.setMsgToken(INPRPluginConst.MSG_TOKEN_STICK);
		this.setGroupColName(INPRPluginConst.NC_MSG, true);
		this.setValueColName(INPRPluginConst.NC_WARNINGSQL_ROWNUM, true);
		this.setCountColName("结果集行数");
		this.setShowColName(INPRPluginConst.NC_MSG, true);
		this.setBusiActionColName(INPRPluginConst.PLUGIN_COLUMN_BUSIACTION,
				INPRPluginConst.NC_WARNINGSQL_DEFCOL1, true);
		this.setDealMaster(true);
	}

}
