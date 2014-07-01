/**
 * 
 */
package uap.ps.itm.npr.service.plugin;

/**
 * 读取结果集最多的远程调用统计插件
 * 
 * @author liuzy
 * 
 */
public class RemoteCallResultTop10 extends DefaultTop10PluginImpl {

	public RemoteCallResultTop10() {
		this.setGroupColName(INPRPluginConst.NC_MW_CALLID, false);
		this.setShowMethod(true);
		this.setCountColName("结果集数");
		this.setValueColName(INPRPluginConst.NC_MW_READROWNUM, true);
	}

}
