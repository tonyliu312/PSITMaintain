package uap.ps.itm.npr.service.plugin;

/**
 *
 * <code>CallMasterNumsTop10<code>
 * <strong></strong>
 * <p>˵����
 * <li></li>
 * </p>
 * @since NC6.3
 * @version 2013-1-29 ����02:53:59
 * @author tangxx
 */

public class CallMasterNumsTop10 extends DefaultTop10PluginImpl {

	public CallMasterNumsTop10() {
		this.setGroupColName(INPRPluginConst.NC_MW_CALLID, false);
		this.setShowMethod(true);
		this.setShowCallid(true);
		this.setCountColName("����master����");
		this.setValueColName(INPRPluginConst.NC_MW_CMC, true);
	}

}
