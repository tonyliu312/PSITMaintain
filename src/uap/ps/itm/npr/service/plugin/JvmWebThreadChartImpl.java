/**
 * 
 */
package uap.ps.itm.npr.service.plugin;

import nc.monitor.pub.HashVO;
import nc.monitor.pub.Toolkit;

/**
 * 中间件JVM Web线程统计插件
 * 
 * @author liuzy
 * 
 */
public class JvmWebThreadChartImpl extends ChartPluginDefultImpl {
	public JvmWebThreadChartImpl() {
		this.groupField = INPRPluginConst.NMC_MW_SERVERNAME;
		this.fileNameSplit = "-";
		this.valueFiled = INPRPluginConst.NMC_MW_WCTHREADNUM;
	}

	@Override
	protected HashVO processVO(HashVO vo) {
		Object obj = vo.getAttributeValue(INPRPluginConst.NMC_MW_WCTHREADNUM);
		if (Toolkit.isEmpty(obj))
			return null;
		int web = Integer.valueOf(obj.toString());
		HashVO retvo = new HashVO();
		retvo.setAttributeValue(xField, vo.getAttributeValue(xField));
		retvo.setAttributeValue("webthread", web);
		return retvo;
	}
}
