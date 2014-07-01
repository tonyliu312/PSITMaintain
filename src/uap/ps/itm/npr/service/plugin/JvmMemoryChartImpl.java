/**
 * 
 */
package uap.ps.itm.npr.service.plugin;

import nc.monitor.pub.HashVO;
import nc.monitor.pub.Toolkit;
import nc.monitor.service.center.ServerEnv;

/**
 * 中间件JVM内存使用情况统计图
 * 
 * @author liuzy
 * 
 */
public class JvmMemoryChartImpl extends ChartPluginDefultImpl {
	public JvmMemoryChartImpl() {
		// this.logInterval = 6;
		this.sys_interval = Integer.valueOf(ServerEnv.getInstance()
				.getProperty("logDuration"));
		this.groupField = INPRPluginConst.NMC_MW_SERVERNAME;
		this.fileNameSplit = "-";
		this.valueFiled = INPRPluginConst.NMC_MW_TOTAL;
	}

	@Override
	protected HashVO processVO(HashVO vo) {
		Object totalObj = vo.getAttributeValue(INPRPluginConst.NMC_MW_TOTAL);
		if (Toolkit.isEmpty(totalObj))
			return null;
		int total = Integer.valueOf(totalObj.toString());
		int free = vo.getIntegerValue(INPRPluginConst.NMC_MW_FREE);
		HashVO retvo = new HashVO();
		retvo.setAttributeValue(xField, vo.getAttributeValue(xField));
		retvo.setAttributeValue("used", total - free);
		return retvo;
	}
}
