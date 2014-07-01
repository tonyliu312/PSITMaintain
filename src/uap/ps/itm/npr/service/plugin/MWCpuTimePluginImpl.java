/**
 *
 */
package uap.ps.itm.npr.service.plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import nc.monitor.Exception.RemoteException;
import nc.monitor.pub.HashVO;
import uap.ps.itm.npr.service.NPRGlobalControl;
import uap.ps.itm.npr.vo.XPRPluginResult;
import nc.vo.pub.lang.UFDateTime;

/**
 * 中间件CPU时间
 *
 * @author liuzy
 *
 */
public class MWCpuTimePluginImpl extends AbsNPRPlugin {

	private final Map<String, HashVO> map = new HashMap<String, HashVO>();

	private final String cpuTime = "CPU时间(分)";

	private final String server = "Server Name";

	/*
	 * (non-Javadoc)
	 *
	 * @see uap.ps.itm.npr.service.plugin.INPRPluginItf#process(nc.monitor.pub.HashVO[])
	 */

	@Override
	public void process(File file, HashVO[] dataVOs) throws RemoteException {
		if (!checkProc(file, dataVOs))
			return;
		String serverName = file.getParentFile().getName();
		if (!map.containsKey(serverName)) {
			HashVO vo = new HashVO();
			vo.setAttributeValue(server, serverName);
			vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME, "");
			vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_ENDTIME, "");
			vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_TIMES, "");
			vo.setAttributeValue(cpuTime, 0);
			map.put(serverName, vo);
		}
		HashVO resultVO = map.get(serverName);
		// String sBegin = resultVO
		// .getStringValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME);
		// String sEnd = resultVO
		// .getStringValue(INPRPluginCoRnst.PLUGIN_COLUMN_ENDTIME);
		// UFDateTime begin = Toolkit.isEmpty(sBegin) ? null : new UFDateTime(
		// sBegin);
		// UFDateTime end = Toolkit.isEmpty(sEnd) ? null : new UFDateTime(sEnd);
		// UFDateTime ts = null;
		for (HashVO vo : dataVOs) {
			try {
			if (!checkVOBeginTime(vo))
				continue;
			if (!checkVOEndTime(vo))
					break;
			if (checkVOByFilter(vo))
				continue;
			} catch (Exception e) {
				continue;
			}

			Long costtime;
			Long sqlcosttime;
			Long readresulttime;
			Long readfromclienttime;
			Long writetoclienttime;
			Long mwCpuTime;
			try {
				costtime = vo.getMsgLongValueByField(
						INPRPluginConst.NC_COSTTIME, null);
				sqlcosttime = vo.getMsgLongValueByField(
						INPRPluginConst.NC_MW_SQLCOSTTIME, null);
				readresulttime = vo.getMsgLongValueByField(
						INPRPluginConst.NC_MW_READRESULTTIME, null);
				readfromclienttime = vo.getMsgLongValueByField(
						INPRPluginConst.NC_MW_READFROMCLIENTTIME, null);
				writetoclienttime = vo.getMsgLongValueByField(
						INPRPluginConst.NC_MW_WRITETOCLIENTTIME, null);
				if(null == costtime)
					costtime = 0L;
				if(null == sqlcosttime)
					sqlcosttime = 0L;
				if(null == readresulttime)
					readresulttime = 0L;
				if(null == readfromclienttime)
					readfromclienttime = 0L;
				if(null == writetoclienttime)
					writetoclienttime = 0L;
				mwCpuTime = costtime - sqlcosttime - readresulttime
						- readfromclienttime - writetoclienttime;
				if (mwCpuTime < 0)
					continue;
				long calcedTime = Long
						.valueOf(resultVO.getStringValue(cpuTime));
				resultVO.setAttributeValue(cpuTime,
						String.valueOf(mwCpuTime + calcedTime));
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
		HashVO[] vos = new HashVO[map.size() + 1];
		long sumCpuTimes = 0;
		long sumRangeTimes = 0;
		int i = 0;
		for (HashVO vo : map.values()) {
			String serverName = vo.getStringValue(server);
			UFDateTime begin = NPRGlobalControl.getInstance()
					.getServerNCMWBeginTime(serverName);
			UFDateTime end = NPRGlobalControl.getInstance()
					.getServerNCMWEndTime(serverName);
			vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME,
					begin.toString());
			vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_ENDTIME,
					end.toString());
			vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_TIMES,
					UFDateTime.getMinutesBetween(begin, end));
			long voTime = vo.getIntegerValue(cpuTime);
			long voRangeTimes = vo
					.getIntegerValue(INPRPluginConst.PLUGIN_COLUMN_TIMES);
			sumRangeTimes += voRangeTimes;
			voTime = voTime / 1000 / 60;
			vo.setAttributeValue(cpuTime, voTime);
			sumCpuTimes += voTime;
			vos[i] = vo;
			i++;
		}
		HashVO vo = new HashVO();
		vo.setAttributeValue(server, "合计");
		vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_TIMES, sumRangeTimes);
		vo.setAttributeValue(cpuTime, String.valueOf(sumCpuTimes));
		vos[i] = vo;

		XPRPluginResult result = new XPRPluginResult(getPluginInfo());
		result.setContentVos(vos);
		return result;
	}

}
