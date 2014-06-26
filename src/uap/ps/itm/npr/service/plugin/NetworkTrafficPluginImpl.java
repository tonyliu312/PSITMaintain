/**
 *
 */
package uap.ps.itm.npr.service.plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import uap.ps.itm.monitor.RemoteException;
import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.npr.service.NPRGlobalControl;
import uap.ps.itm.npr.vo.XPRPluginResult;

/**
 * 网络流量计算插件
 *
 * @author liuzy
 *
 */
public class NetworkTrafficPluginImpl extends AbsNPRPlugin {

	private final Map<String, HashVO> map = new HashMap<String, HashVO>();

	private final String uploadName = "上行流量(MB)";

	private final String downloadName = "下行流量(MB)";

	private final String trafficSumName = "流量合计(MB)";

	private final String trafficAvgName = "流量平均(MB/min)";

	private final String server = "Server name";

	private final UFDouble unit = new UFDouble(1024 * 1024);

	/*
	 * (non-Javadoc)
	 *
	 * @see uap.ps.itm.npr.service.plugin.INPRPluginItf#process(java.io.File,
	 * nc.monitor.pub.HashVO[])
	 */

	@Override
	public void process(File file, HashVO[] dataVOs) throws RemoteException {
		if (!checkProc(file, dataVOs))
			return;
		String serverName = file.getParentFile().getName();
		HashVO vo = null;
		if (!map.containsKey(serverName)) {
			vo = new HashVO();
			vo.setAttributeValue(server, serverName);
			vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME, "");
			vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_ENDTIME, "");
			vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_TIMES, "");
			vo.setAttributeValue(this.uploadName, new UFDouble(0.0));
			vo.setAttributeValue(this.downloadName, new UFDouble(0.0));
			vo.setAttributeValue(this.trafficSumName, new UFDouble(0.0));
			vo.setAttributeValue(this.trafficAvgName, new UFDouble(0.0));
			map.put(serverName, vo);
		}
		vo = map.get(serverName);
		UFDouble upload = new UFDouble(vo.getStringValue(uploadName));
		UFDouble down = new UFDouble(vo.getStringValue(downloadName));
		upload.setScale(2, UFDouble.ROUND_UP);
		down.setScale(2, UFDouble.ROUND_UP);

		for (HashVO data : dataVOs) {
			if (!checkVOBeginTime(data))
				continue;
			try {
			if (!checkVOEndTime(data))
					break;
			} catch (Exception e) {
				continue;
			}
			if (checkVOByFilter(data))
				continue;
			try {
				Long temp = data.getMsgLongValueByField(
						INPRPluginConst.NC_MW_READFROMCLIENTBYTES, null);
				if(null == temp)
					temp = 0L;
				upload = upload.add(temp);
				temp = data.getMsgLongValueByField(
						INPRPluginConst.NC_MW_WRITETOCLIENTBYTES, null);
				if(null == temp)
					temp = 0L;
				down = down.add(temp);
			} catch (NumberFormatException e) {
				continue;
			}
		}

		vo.setAttributeValue(uploadName,
				upload.div(unit).setScale(2, UFDouble.ROUND_UP));
		vo.setAttributeValue(downloadName,
				down.div(unit).setScale(2, UFDouble.ROUND_UP));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uap.ps.itm.npr.service.plugin.AbsNPRPlugin#getResultVO()
	 */
	@Override
	public XPRPluginResult getResultVO() throws RemoteException {
		HashVO[] contentVOs = new HashVO[map.size() + 1];
		UFDouble sumUp = new UFDouble(0.0);
		UFDouble sumDown = new UFDouble(0.0);
		UFDouble sumTraffic = new UFDouble(0.0);
		UFDouble sumTrafficAvg = new UFDouble(0.0);
		UFDouble trafficSum = new UFDouble(0.0);
		UFDouble trafficAvg = new UFDouble(0.0);
		UFDateTime begin = null;
		UFDateTime end = null;
		long sumTimes = 0;
		int i = 0;
		for (HashVO vo : map.values()) {
			String serverName = vo.getStringValue(server);
			begin = NPRGlobalControl.getInstance().getServerNCMWBeginTime(
					serverName);
			end = NPRGlobalControl.getInstance().getServerNCMWEndTime(
					serverName);
			vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME,
					begin.toString());
			vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_ENDTIME,
					end.toString());
			trafficSum = ((UFDouble) vo.getAttributeValue(uploadName))
					.add(((UFDouble) vo.getAttributeValue(downloadName)));
			sumTraffic = sumTraffic.add(trafficSum);
			vo.setAttributeValue(trafficSumName,
					trafficSum.setScale(2, UFDouble.ROUND_UP));
			UFDouble upload = (UFDouble) vo.getAttributeValue(uploadName);
			UFDouble down = (UFDouble) vo.getAttributeValue(downloadName);
			sumUp = sumUp.add(upload);
			sumDown = sumDown.add(down);
			String b = vo
					.getStringValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME);
			if (Toolkit.isEmpty(b))
				continue;
			// begin = new UFDateTime(
			// vo.getStringValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME));
			// end = new UFDateTime(
			// vo.getStringValue(INPRPluginConst.PLUGIN_COLUMN_ENDTIME));
			long mins = UFDateTime.getMinutesBetween(begin, end);
			sumTimes += mins;
			vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_TIMES, mins);
			trafficAvg = trafficSum.div(mins);
			vo.setAttributeValue(trafficAvgName,
					trafficAvg.setScale(2, UFDouble.ROUND_UP));
			sumTrafficAvg = sumTrafficAvg.add(trafficAvg);
			contentVOs[i] = vo;
			i++;
		}
		HashVO vo = new HashVO();
		vo.setAttributeValue(server, "合计");
		vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME, "");
		vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_ENDTIME, "");
		vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_TIMES, sumTimes);
		vo.setAttributeValue(this.uploadName,
				sumUp.setScale(2, UFDouble.ROUND_UP));
		vo.setAttributeValue(this.downloadName,
				sumDown.setScale(2, UFDouble.ROUND_UP));
		vo.setAttributeValue(trafficSumName,
				sumTraffic.setScale(2, UFDouble.ROUND_UP));
		vo.setAttributeValue(trafficAvgName,
				sumTrafficAvg.setScale(2, UFDouble.ROUND_UP));
		contentVOs[i] = vo;
		XPRPluginResult result = new XPRPluginResult(getPluginInfo());
		result.setContentVos(contentVOs);
		return result;
	}
}
