package uap.ps.itm.npr.service.plugin;

/**
 *
 * <code>CallMasterNumsTop10<code>
 * <strong></strong>
 * <p>说明：
 * <li></li>
 * </p>
 * @since NC6.3
 * @version 2013-1-29 下午02:53:59
 * @author tangxx
 */

public class CallMasterNumsTop10 extends DefaultTop10PluginImpl {

	public CallMasterNumsTop10() {
		this.setGroupColName(INPRPluginConst.NC_MW_CALLID, false);
		this.setShowMethod(true);
		this.setShowCallid(true);
		this.setCountColName("调用master次数");
		this.setValueColName(INPRPluginConst.NC_MW_CMC, true);
	}

}
