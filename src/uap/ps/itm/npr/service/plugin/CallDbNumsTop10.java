package uap.ps.itm.npr.service.plugin;

/**
 *
 * <code>CallDbNumsTop10<code>
 * <strong></strong>
 * <p>˵����
 * <li></li>
 * </p>
 * @since NC6.3
 * @version 2013-1-29 ����04:29:55
 * @author tangxx
 */

public class CallDbNumsTop10 extends DefaultTop10PluginImpl {

	public CallDbNumsTop10() {
		this.setGroupColName(INPRPluginConst.NC_MW_CALLID, false);
		this.setShowMethod(true);
		this.setShowCallid(true);
		this.setCountColName("�������ݿ�Ĵ���");
		this.setValueColName(INPRPluginConst.NC_MW_CC, true);
	}

}
