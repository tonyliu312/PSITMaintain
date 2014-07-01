/**
 *
 */
package uap.ps.itm.npr.service.plugin;

import java.io.File;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.monitor.Exception.RemoteException;
import nc.monitor.pub.HashVO;
import nc.monitor.pub.Toolkit;
import nc.monitor.service.center.ServerEnv;
import uap.ps.itm.npr.service.INPRConst;
import uap.ps.itm.npr.service.NPRGlobalControl;
import uap.ps.itm.npr.vo.XPRPluginResult;
import nc.vo.pub.lang.UFDateTime;

/**
 * Chart类型插件的默认实现
 *
 * @author liuzy
 *
 */
public abstract class ChartPluginDefultImpl extends AbsNPRPlugin {

	private final Map<String, HashVO> map_max = new HashMap<String, HashVO>();
	private final Map<String, HashVO> map_min = new HashMap<String, HashVO>();

	protected int logInterval = -1;
	protected String groupField = null;

	private XPRPluginResult result = null;

	private int logCalc = 0;

	private final String prevFileName = null;

	protected int sys_interval = -1;

	protected String fileNameSplit = ".";

	protected String xField = null;

	protected String yField = null;

	protected String valueFiled = null;

	private final int pointCount = Integer.valueOf(ServerEnv.getInstance()
			.getProperty(INPRConst.NPR_CHART_POINT_COUNT));

	private final String defBeginTime = "2010-01-01 00:00:00";


	@Override
	public void process(File file, HashVO[] dataVOs) throws RemoteException {
		if (!checkProc(file, dataVOs))
			return;

		if (null == result)
			result = new XPRPluginResult(this.getPluginInfo());

		xField = this.getPluginInfo().getStringValue(INPRPluginConst.X_FIELD);

		yField = this.getPluginInfo().getStringValue(INPRPluginConst.Y_FIELD);
		if (Toolkit.isEmpty(valueFiled))
			valueFiled = yField;
		if (this.sys_interval == -1)
			sys_interval = this.getPluginInfo().getIntegerValue(
					INPRPluginConst.SYS_INTERVAL);
		long timeBucket = -1L;
		UFDateTime begin = this.getBeginTime();
		if (Toolkit.isEmpty(begin) || new UFDateTime().compareTo(begin) == 0) {
			begin = new UFDateTime(defBeginTime);
			if (!Toolkit.isEmpty(this.groupField)
					&& INPRPluginConst.NMC_MW_SERVERNAME
							.equals(this.groupField)) {
				String name = file.getName();
				if (name.indexOf("-") < 0)
					return;
				String server = name.substring(0, name.indexOf("-"));
				begin = NPRGlobalControl.getInstance().getServerNMCMWBeginTime(
						server);
			}
		}
		UFDateTime end = this.getEndTime();
		if (Toolkit.isEmpty(end)) {
			end = new UFDateTime(new Date());
			if (!Toolkit.isEmpty(this.groupField)
					&& INPRPluginConst.NMC_MW_SERVERNAME
							.equals(this.groupField)) {
				String name = file.getName();
				if (name.indexOf("-") < 0)
					return;
				String server = name.substring(0, name.indexOf("-"));
				end = NPRGlobalControl.getInstance().getServerNMCMWEndTime(
						server);
			}
		}
		long secds = UFDateTime.getSecondsBetween(begin, end);
		// if (secds >= 86400)
		// timeBucket = 3600;
		// else {
		long pointdist = secds / pointCount;
		if (pointdist > sys_interval && pointCount * 1.5 < dataVOs.length)
			timeBucket = pointdist % sys_interval + pointdist;
		else
			timeBucket = sys_interval;
		// }
		long interval = timeBucket / sys_interval;

		String fileName = file.getName();
		if (fileName.indexOf(this.fileNameSplit) == -1)
			return;
		String currFileName = fileName.substring(0,
				fileName.indexOf(this.fileNameSplit));
		if (!currFileName.equals(this.prevFileName))
			logCalc = 0;

		Integer maxValue = null;
		Integer minValue = null;

		for (int i = 0; i < dataVOs.length; i++) {
			if (Toolkit.isEmpty(dataVOs[i].getAttributeValue(xField)))
				continue;
			logCalc++;
			if (logInterval > 0) {
				if (logCalc > 1 && logCalc < logInterval)
					continue;
				if (logCalc != 1 && logCalc % logInterval != 0)
					continue;
			}
			if (Toolkit.isEmpty(dataVOs[i]))
				continue;
			if (!checkVOBeginTime(dataVOs[i]))
				continue;
			try {
			if (!checkVOEndTime(dataVOs[i]))
					break;
			} catch (Exception e) {
				continue;
			}

			Integer value = null;
			try {
				value = dataVOs[i].getIntegerValue(valueFiled);
			} catch (Exception e) {
				continue;
			}
			if (Toolkit.isEmpty(value))
				continue;

			String name = null;
			if (!Toolkit.isEmpty(groupField)) {
				name = dataVOs[i].getStringValue(groupField);
			} else {
				name = result.getLine_name();
			}
			if (Toolkit.isEmpty(name))
				continue;
			if (logCalc == 1
					|| (logCalc >= interval && logCalc % interval == 0)) {
				HashVO hsvo = this.processVO(dataVOs[i]);
				if (Toolkit.isEmpty(hsvo))
					continue;
				maxValue = map_max.get(name) == null ? null : map_max.get(name)
						.getIntegerValue(yField);
				minValue = map_min.get(name) == null ? null : map_min.get(name)
						.getIntegerValue(yField);
				value = hsvo.getIntegerValue(yField);
				if (null == maxValue || value.compareTo(maxValue) > 0) {
					map_max.put(name, hsvo);
				}
				if (null == minValue || value.compareTo(minValue) < 0) {
					map_min.put(name, hsvo);
				}
				result.addData2Map(name, hsvo);
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
		if (Toolkit.isEmpty(this.result.getDataMap()))
			return null;

		HashVO maxVO = null;
		HashVO minVO = null;

		for (Map.Entry<String, List<HashVO>> entry : this.result.getDataMap()
				.entrySet()) {
			String name = entry.getKey();
			List<HashVO> contentList = entry.getValue();
			if (Toolkit.isEmpty(contentList)) {
				this.result.getDataMap().remove(name);
				continue;
			}
			Toolkit.sort(contentList, new ChartPointSort());
			maxVO = map_max.get(name);
			minVO = map_min.get(name);
			UFDateTime maxTime = new UFDateTime(maxVO.getStringValue(xField));
			UFDateTime minTime = new UFDateTime(minVO.getStringValue(xField));
			UFDateTime time1 = null;
			UFDateTime time2 = null;
			for (int i = 0; i < contentList.size() - 1; i++) {
				HashVO vo1 = contentList.get(i);
				HashVO vo2 = contentList.get(i + 1);
				String ts1 = vo1.getStringValue(xField);
				String ts2 = vo2.getStringValue(xField);
				try {
				time1 = new UFDateTime(ts1);
				time2 = new UFDateTime(ts2);
				// if (time2.compareTo(time1) < 0)
				// System.out.println();

				if (maxTime.compareTo(time1) > 0
						&& maxTime.compareTo(time2) < 0) {
					contentList.add(i + 1, maxVO);
				}
				if (minTime.compareTo(time1) > 0
						&& minTime.compareTo(time2) < 0) {
					contentList.add(i + 1, minVO);
				}
				} catch (Exception e) {
					continue;
				}
			}
		}
		return result;
	}

	protected HashVO processVO(HashVO vo) {
		HashVO retvo = new HashVO();
		retvo.setAttributeValue(xField, vo.getAttributeValue(xField));
		retvo.setAttributeValue(yField, vo.getAttributeValue(yField));
		return retvo;
	}

	private class ChartPointSort implements Comparator<HashVO> {


		@Override
		public int compare(HashVO o1, HashVO o2) {
			String ts1 = o1.getStringValue(xField);
			String ts2 = o2.getStringValue(xField);
			UFDateTime ufts1 = new UFDateTime(ts1);
			UFDateTime ufts2 = new UFDateTime(ts2);
			if (ufts1.compareTo(ufts2) > 0)
				return 1;
			else if (ufts1.compareTo(ufts2) < 0)
				return -1;
			return 0;
		}

	}

}
