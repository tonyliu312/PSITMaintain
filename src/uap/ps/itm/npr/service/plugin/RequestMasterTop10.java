/**
 * 
 */
package uap.ps.itm.npr.service.plugin;

/**
 * 
 * ��master��������Զ�̵���top10ͳ�Ʋ��
 * 
 * @author liuzy
 * 
 */
public class RequestMasterTop10 extends DefaultTop10PluginImpl {

	public RequestMasterTop10() {
		this.setGroupColName(INPRPluginConst.NC_MW_REMOTECALLMETHOD, true);
		this.setCountByGroup(true);
		this.setBusiActionColName("��������",
				INPRPluginConst.NC_MW_REMOTECALLMETHOD, true);
		this.setCountColName("Զ�̵��ô���");
		this.setDealMaster(true);
		this.setSum(true);
		this.setShowCallid(false);
		this.setAvgColName("ƽ��(��/��)");
		this.setDealServers(false);
		this.setShowTimeRange(true);
		this.setShowUserid(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uap.ps.itm.npr.service.plugin.AbsNPRPlugin#getNCLogPath()
	 */
	// @Override
	// public String getNCLogPath() {
	// String path = super.getNCLogPath();
	// path += "/" + INPRPluginConst.MASTER;
	// return path;
	// }

}
