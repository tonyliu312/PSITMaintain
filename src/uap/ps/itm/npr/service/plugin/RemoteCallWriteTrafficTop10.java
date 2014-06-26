/**
 * 
 */
package uap.ps.itm.npr.service.plugin;

/**
 * 上行流量top10统计插件
 * 
 * @author liuzy
 * 
 */
public class RemoteCallWriteTrafficTop10 extends RemoteCallReadTrafficTop10 {

	public RemoteCallWriteTrafficTop10() {
		this.setGroupColName(INPRPluginConst.NC_MW_CALLID, false);
		this.setCountColName(trafficColName);
		this.setValueColName(INPRPluginConst.NC_MW_READFROMCLIENTBYTES, true);
		this.setShowMethod(true);
	}

}
