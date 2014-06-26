/**
 *
 */
package uap.ps.itm.npr.service.plugin;

import uap.ps.itm.monitor.RemoteException;
import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.npr.vo.XPRPluginResult;

/**
 * 下行网络流量Top10计算插件
 *
 * @author liuzy
 *
 */
public class RemoteCallReadTrafficTop10 extends DefaultTop10PluginImpl {

	protected String trafficColName = "流量(KB)";

	public RemoteCallReadTrafficTop10() {
		this.setGroupColName(INPRPluginConst.NC_MW_CALLID, false);
		this.setCountColName(trafficColName);
		this.setValueColName(INPRPluginConst.NC_MW_WRITETOCLIENTBYTES, true);
		this.setShowMethod(true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uap.ps.itm.npr.service.plugin.DefaultTop10PluginImpl#getResultVO()
	 */
	@Override
	public XPRPluginResult getResultVO() throws RemoteException {
		XPRPluginResult resultVO = super.getResultVO();
		HashVO[] vos = resultVO.getContentVos();
		for (HashVO vo : vos) {
			try {
			long l = Long.valueOf(vo.getStringValue(trafficColName));
			l = l / 1024;
			vo.setAttributeValue(trafficColName, l);
			} catch (Exception e) {
				continue;
			}
		}
		return resultVO;
	}

}
