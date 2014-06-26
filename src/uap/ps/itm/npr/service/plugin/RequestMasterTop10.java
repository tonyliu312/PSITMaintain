/**
 * 
 */
package uap.ps.itm.npr.service.plugin;

/**
 * 
 * 对master请求最多的远程调用top10统计插件
 * 
 * @author liuzy
 * 
 */
public class RequestMasterTop10 extends DefaultTop10PluginImpl {

	public RequestMasterTop10() {
		this.setGroupColName(INPRPluginConst.NC_MW_REMOTECALLMETHOD, true);
		this.setCountByGroup(true);
		this.setBusiActionColName("方法名称",
				INPRPluginConst.NC_MW_REMOTECALLMETHOD, true);
		this.setCountColName("远程调用次数");
		this.setDealMaster(true);
		this.setSum(true);
		this.setShowCallid(false);
		this.setAvgColName("平均(次/分)");
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
