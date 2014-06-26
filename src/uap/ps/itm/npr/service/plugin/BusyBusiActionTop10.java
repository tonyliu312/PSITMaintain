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
import nc.vo.pub.lang.UFDouble;
import uap.ps.itm.monitor.RemoteException;
import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.npr.vo.XPRPluginResult;

/**
 * 执行最多的业务操作Top10
 *
 * @author liuzy
 *
 */
public class BusyBusiActionTop10 extends AbsNPRPlugin {

	private final Map<String, HashVO> map = new HashMap<String, HashVO>();

	private final String procTimesColName = "执行次数";


	@Override
	public void process(File file, HashVO[] dataVOs) throws RemoteException {
		if (!checkProc(file, dataVOs))
			return;
		if (isMasterFile(file))
			return;
		String currBusiAction = null;
		HashVO currVO = null;
		UFDateTime begin = null;
		UFDateTime end = null;
		for (HashVO vo : dataVOs) {
			if (!checkVO(vo))
				continue;
			if (!checkVOBeginTime(vo))
				continue;
			try {
			if (!checkVOEndTime(vo))
					break;
			} catch (Exception e) {
				continue;
			}
			if (checkVOByFilter(vo))
				continue;
			String busi = vo.getMsgStringValueByField(
					INPRPluginConst.NC_MW_BUSIACTION, null);
			String addr = vo.getMsgStringValueByField(
					INPRPluginConst.NC_MW_REMOTEADDR, null);
			if (Toolkit.isEmpty(busi) || busi.equals(INPRPluginConst.UNKOWN))
				continue;
			if ((busi + addr).equals(currBusiAction)) {
				continue;
			} else {
				currVO = map.get(busi);
				if (null == currVO) {
					currVO = new HashVO();
					currVO.setAttributeValue("排名", "");
					currVO.setAttributeValue(
							INPRPluginConst.PLUGIN_COLUMN_BUSIACTION, busi);
					currVO.setAttributeValue(procTimesColName, 0);
					map.put(busi, currVO);
				}
				int count = currVO.getIntegerValue(procTimesColName);
				count += 1;
				currVO.setAttributeValue(procTimesColName, count);
				currBusiAction = busi + addr;

				String ts = vo.getStringValue(INPRPluginConst.TS);
				if (Toolkit.isEmpty(ts))
					continue;
				begin = (UFDateTime) currVO
						.getAttributeValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME);
				end = (UFDateTime) currVO
						.getAttributeValue(INPRPluginConst.PLUGIN_COLUMN_ENDTIME);
				try {
				UFDateTime ufdts = new UFDateTime(ts);
				if (null == begin || begin.compareTo(ufdts) > 0) {
					begin = ufdts;
					currVO.setAttributeValue(
							INPRPluginConst.PLUGIN_COLUMN_BEGINTIME, ufdts);
				}

				if (null == end || end.compareTo(ufdts) < 0) {
					end = ufdts;
					currVO.setAttributeValue(
							INPRPluginConst.PLUGIN_COLUMN_ENDTIME, ufdts);
				}
				} catch (Exception e) {
					continue;
				}

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
		HashVO[] vos = map.values().toArray(new HashVO[map.size()]);
		Toolkit.sort(vos, new Top10DefComparetor(this.procTimesColName, false));
		List<HashVO> contentVOs = new ArrayList<HashVO>();
		int i = 1;
		int j = vos.length;
		while (true) {
			j--;
			if (j == -1)
				break;
			vos[j].setAttributeValue("排名", String.valueOf(i));

			long mins = UFDateTime
					.getMinutesBetween(
							(UFDateTime) vos[j]
									.getAttributeValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME),
							(UFDateTime) vos[j]
									.getAttributeValue(INPRPluginConst.PLUGIN_COLUMN_ENDTIME));
			if (mins <= 0)
				continue;
			long value = Long.valueOf(vos[j].getStringValue(procTimesColName));
			UFDouble avg = new UFDouble(value).div(mins);
			avg = avg.setScale(2, UFDouble.ROUND_HALF_UP);
			vos[j].setAttributeValue("平均(次/分)", avg);
			vos[j].setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME,
					null);
			vos[j].setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_ENDTIME,
					null);
			contentVOs.add(vos[j]);
			i++;
			if (i > 10)
				break;
		}
		XPRPluginResult result = new XPRPluginResult(this.getPluginInfo());
		result.setContentVos(contentVOs.toArray(new HashVO[contentVOs.size()]));
		return result;
	}

}
