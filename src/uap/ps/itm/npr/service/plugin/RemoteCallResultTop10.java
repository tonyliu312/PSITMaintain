/**
 * 
 */
package uap.ps.itm.npr.service.plugin;

/**
 * ��ȡ���������Զ�̵���ͳ�Ʋ��
 * 
 * @author liuzy
 * 
 */
public class RemoteCallResultTop10 extends DefaultTop10PluginImpl {

	public RemoteCallResultTop10() {
		this.setGroupColName(INPRPluginConst.NC_MW_CALLID, false);
		this.setShowMethod(true);
		this.setCountColName("�������");
		this.setValueColName(INPRPluginConst.NC_MW_READROWNUM, true);
	}

}
