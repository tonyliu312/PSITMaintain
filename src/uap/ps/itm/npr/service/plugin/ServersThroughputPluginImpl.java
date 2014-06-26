/**
 *
 */
package uap.ps.itm.npr.service.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.vo.pub.lang.UFDateTime;
import uap.ps.itm.monitor.RemoteException;
import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.npr.vo.XPRPluginResult;

/**
 * @author liuzy
 *
 */
public class ServersThroughputPluginImpl extends AbsNPRPlugin {

	private final Map<String, ThroughputPluginImpl> map = new HashMap<String, ThroughputPluginImpl>();

	/*
	 * (non-Javadoc)
	 *
	 * @see uap.ps.itm.npr.service.plugin.INPRPluginItf#process(nc.monitor.pub.HashVO[])
	 */

	@Override
	public void process(File file, HashVO[] dataVOs) throws RemoteException {
		if (!checkProc(file, dataVOs))
			return;
		ThroughputPluginImpl impl = null;
		for (HashVO vo : dataVOs) {
			try {
			if (!checkVOBeginTime(vo))
				continue;
			if (!checkVOEndTime(vo))
					break;
			String serverName = vo
					.getStringValue(INPRPluginConst.NMC_MW_SERVERNAME);
			if (Toolkit.isEmpty(serverName))
				continue;
			if (serverName.equals(INPRPluginConst.MASTER))
				return;
			if (!map.containsKey(serverName)) {
				impl = new ThroughputPluginImpl();
				HashVO newPluginInfo = (HashVO) this.getPluginInfo().clone();
				newPluginInfo.setAttributeValue(INPRPluginConst.PARA1,
						serverName);
				impl.setPluginInfo(newPluginInfo);
				impl.setSnapshotPaht(this.getSnapshotPath());
				impl.setTimeRange(this.getBeginTime(), this.getEndTime());
				map.put(serverName, impl);
			}
			impl = map.get(serverName);
			impl.process(file, dataVOs);
			break;
			} catch (Exception e) {
				continue;
			}
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uap.ps.itm.npr.service.plugin.AbsNPRPlugin#getResultVO()
	 */
	@Override
	public XPRPluginResult getResultVO() throws RemoteException {
		String calcType = this.getPluginInfo().getStringValue(
				INPRPluginConst.PARA1);
		if (INPRPluginConst.SERVERS.equals(calcType))
			return this.getServersResultVO();
		else if (INPRPluginConst.CLUSTER.equals(calcType))
			return this.getClusterResultVO();
		return null;
	}

	private XPRPluginResult getServersResultVO() throws RemoteException {
		List<HashVO> contentVOs = new ArrayList<HashVO>(map.size());
		for (ThroughputPluginImpl impl : map.values())
			contentVOs.add(impl.getResultVO().getContentVos()[0]);
		XPRPluginResult result = new XPRPluginResult(this.getPluginInfo());
		result.setContentVos(contentVOs.toArray(new HashVO[contentVOs.size()]));
		return result;
	}

	private XPRPluginResult getClusterResultVO() throws RemoteException {
		if (null == map || map.size() == 0)
			return null;
		int min = 0;
		int max = 0;
		int sum = 0;
		// int serverNums = map.size();
		UFDateTime begin = null;
		UFDateTime end = null;
		for (ThroughputPluginImpl impl : map.values()) {
			int tempMin = impl.getResultVO().getContentVos()[0]
					.getIntegerValue("最小");
			int tempMax = impl.getResultVO().getContentVos()[0]
					.getIntegerValue("最大");
			int tempSum = impl.getResultVO().getContentVos()[0]
					.getIntegerValue("合计");
			UFDateTime tempBegin = new UFDateTime(
					impl.getResultVO().getContentVos()[0]
							.getStringValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME));
			UFDateTime tempEnd = new UFDateTime(
					impl.getResultVO().getContentVos()[0]
							.getStringValue(INPRPluginConst.PLUGIN_COLUMN_ENDTIME));
			if (tempMin < min)
				min = tempMin;
			if (tempMax > max)
				max = tempMax;
			if (null == begin || tempBegin.compareTo(begin) < 0)
				begin = tempBegin;
			if (null == end || tempEnd.compareTo(end) > 0)
				end = tempEnd;
			sum += tempSum;
		}

		int mins = UFDateTime.getMinutesBetween(begin, end);
		if(mins == 0)
			mins = 1;

		HashVO contentvo = new HashVO();
		contentvo.setAttributeValue("名称",
				this.getPluginInfo().getStringValue(INPRPluginConst.PARA1));
		contentvo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME,
				begin.toString());
		contentvo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_ENDTIME,
				end.toString());
		contentvo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_TIMES,
				UFDateTime.getMinutesBetween(begin, end));
		contentvo.setAttributeValue("最大", String.valueOf(max));
		contentvo.setAttributeValue("最小", String.valueOf(min));
		contentvo.setAttributeValue("平均(次/分)",
				String.valueOf(new Double(sum / mins).intValue()));
		contentvo.setAttributeValue("合计", String.valueOf(sum));
		XPRPluginResult result = new XPRPluginResult(this.getPluginInfo());
		result.setContentVos(new HashVO[] { contentvo });
		return result;
	}
}
