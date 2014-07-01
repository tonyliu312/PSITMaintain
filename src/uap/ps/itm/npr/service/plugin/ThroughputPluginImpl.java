/**
 *
 */
package uap.ps.itm.npr.service.plugin;

import java.io.File;

import nc.monitor.Exception.RemoteException;
import nc.monitor.pub.HashVO;
import nc.monitor.pub.Toolkit;
import uap.ps.itm.npr.service.NPRGlobalControl;
import uap.ps.itm.npr.vo.XPRPluginResult;
import nc.vo.pub.lang.UFDateTime;

/**
 * 某个Server的吞吐量统计插件
 *
 * @author liuzy
 *
 */
public class ThroughputPluginImpl extends AbsNPRPlugin {

	private int max = 0;
	private int min = 0;
	private int sum = 0;
	private UFDateTime prev = null;
	private String server = null;

	/**
	 * 按给定server计算吞吐量
	 *
	 * @param calcType
	 * @param pluginInfo
	 * @param datavos
	 * @return
	 */
	private void calcThroughput(String calcType, HashVO[] datavos) {

		server = this.getPluginInfo().getStringValue(INPRPluginConst.PARA1);
		if (Toolkit.isEmpty(server))
			return;
		for (HashVO vo : datavos) {
			try {
			if (!checkVOBeginTime(vo))
				continue;
			if (!checkVOEndTime(vo))
					break;
			String servername = vo
					.getStringValue(INPRPluginConst.NMC_MW_SERVERNAME);
			;
			if (Toolkit.isEmpty(servername))
				continue;
			if (!servername.equals(server))
				continue;
			UFDateTime ts = new UFDateTime(vo.getStringValue("ts"));
			if (Toolkit.isEmpty(ts))
				continue;
			UFDateTime curr = ts;
			if (Toolkit.isEmpty(prev)
					|| UFDateTime.getSecondsBetween(prev, curr) > 60) {
				int throughput = vo
						.getIntegerValue(INPRPluginConst.NMC_MW_THROUGHPUT);
				if (throughput < min)
					min = throughput;
				if (throughput > max)
					max = throughput;
				sum += throughput;
				prev = curr;
			}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}

	}


	@Override
	public void process(File file, HashVO[] dataVOs) throws RemoteException {
		if (!checkProc(file, dataVOs))
			return;
		String calcType = this.getPluginInfo().getStringValue(
				INPRPluginConst.PARA1);
		calcThroughput(calcType, dataVOs);
	}

	@Override
	public XPRPluginResult getResultVO() throws RemoteException {
		UFDateTime begin = NPRGlobalControl.getInstance()
				.getServerNMCMWBeginTime(server);
		UFDateTime end = NPRGlobalControl.getInstance().getServerNMCMWEndTime(
				server);
		if (Toolkit.isEmpty(begin) || Toolkit.isEmpty(end))
			return null;
		Double dsum = new Double(sum);
		int minutes = UFDateTime.getMinutesBetween(begin, end);
		if(minutes == 0)
			minutes = 1;
		Double davg = new Double(dsum / minutes);

		HashVO contentvo = new HashVO();
		contentvo.setAttributeValue("名称",
				this.getPluginInfo().getStringValue(INPRPluginConst.PARA1));
		contentvo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME,
				begin.toString());
		contentvo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_ENDTIME,
				end.toString());
		contentvo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_TIMES,
				minutes);
		contentvo.setAttributeValue("最大", String.valueOf(max));
		contentvo.setAttributeValue("最小", String.valueOf(min));
		contentvo.setAttributeValue("平均(次/分)", String.valueOf(davg.intValue()));
		contentvo.setAttributeValue("合计", String.valueOf(dsum.intValue()));
		XPRPluginResult result = new XPRPluginResult(this.getPluginInfo());
		result.setContentVos(new HashVO[] { contentvo });
		return result;
	}
}
