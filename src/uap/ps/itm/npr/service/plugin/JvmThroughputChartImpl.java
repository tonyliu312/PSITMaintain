/**
 * 
 */
package uap.ps.itm.npr.service.plugin;

import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Toolkit;

/**
 * �м��JVM������ͳ�Ʋ��
 * 
 * @author liuzy
 * 
 */
public class JvmThroughputChartImpl extends ChartPluginDefultImpl {
	public JvmThroughputChartImpl() {
		this.logInterval = 6;
		this.groupField = INPRPluginConst.NMC_MW_SERVERNAME;
		this.fileNameSplit = "-";
	}

	@Override
	protected HashVO processVO(HashVO vo) {
		Object obj = vo.getAttributeValue(INPRPluginConst.NMC_MW_THROUGHPUT);
		if (Toolkit.isEmpty(obj))
			return null;
		int throughput = Integer.valueOf(obj.toString());
		HashVO retvo = new HashVO();
		retvo.setAttributeValue(xField, vo.getAttributeValue(xField));
		retvo.setAttributeValue(INPRPluginConst.NMC_MW_THROUGHPUT, throughput);
		return retvo;
	}
}
