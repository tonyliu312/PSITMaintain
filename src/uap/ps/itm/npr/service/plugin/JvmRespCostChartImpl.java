/**
 * 
 */
package uap.ps.itm.npr.service.plugin;

import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Toolkit;

/**
 * JVM相应时间
 * 
 * @author liuzy
 * 
 */
public class JvmRespCostChartImpl extends ChartPluginDefultImpl {
	public JvmRespCostChartImpl() {
		this.groupField = INPRPluginConst.NMC_MW_SERVERNAME;
		this.fileNameSplit = "-";
		this.valueFiled = INPRPluginConst.NMC_MW_RESPCOST;
	}

	@Override
	protected HashVO processVO(HashVO vo) {
		Object obj = vo.getAttributeValue(INPRPluginConst.NMC_MW_RESPCOST);
		if (Toolkit.isEmpty(obj))
			return null;
		int cost = Integer.valueOf(obj.toString());
		HashVO retvo = new HashVO();
		retvo.setAttributeValue(xField, vo.getAttributeValue(xField));
		retvo.setAttributeValue("cost", cost);
		return retvo;
	}
}
