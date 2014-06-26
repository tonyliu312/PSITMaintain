/**
 * 
 */
package uap.ps.itm.npr.service.plugin;

import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Toolkit;

/**
 * �м��JVM Web�߳�ͳ�Ʋ��
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
