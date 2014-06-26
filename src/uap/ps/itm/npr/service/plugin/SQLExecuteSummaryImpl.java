/**
 *
 */
package uap.ps.itm.npr.service.plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import nc.vo.pub.lang.UFDateTime;
import uap.ps.itm.monitor.RemoteException;
import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.npr.vo.XPRPluginResult;

/**
 * SQL执行信息统计
 *
 * @author Tony
 *
 */
public class SQLExecuteSummaryImpl extends AbsNPRPlugin {

	private final Map<String, HashVO> map = new HashMap<String, HashVO>();
	private final String SQL_EXEC_NUM = "SQL执行数量总计";
	private final String AVG_MIN_NUM = "平均(条/分)";
	private final String SQL_EXEC_TIME_SUM = "sql执行时间总计(秒)";
	private final String SQL_RESULT_SUM = "sql读取结果集总计";
	private final String SQL_RESULT_TIME_SUM = "sql读取结果集耗时总计(秒)";
	private final String AVG_SEC_NUM = "平均(条/秒)";

	private final String server = "Server Name";

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
		HashVO hsvo = map.get(serverName);
		if (Toolkit.isEmpty(hsvo)) {
			hsvo = new HashVO();
			hsvo.setAttributeValue(server, serverName);
			hsvo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME, "");
			hsvo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_ENDTIME, "");
			hsvo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_TIMES, "");
			hsvo.setAttributeValue(SQL_EXEC_NUM, 0L);
			hsvo.setAttributeValue(AVG_MIN_NUM, 0L);
			hsvo.setAttributeValue(SQL_EXEC_TIME_SUM, 0L);
			hsvo.setAttributeValue(SQL_RESULT_SUM, 0L);
			hsvo.setAttributeValue(SQL_RESULT_TIME_SUM, 0L);
			hsvo.setAttributeValue(AVG_SEC_NUM, 0L);
			map.put(serverName, hsvo);
		}

		String begin = hsvo
				.getStringValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME);
		String end = hsvo.getStringValue(INPRPluginConst.PLUGIN_COLUMN_ENDTIME);
		UFDateTime uftBegin = Toolkit.isEmpty(begin) ? null : new UFDateTime(
				begin);
		UFDateTime uftEnd = Toolkit.isEmpty(end) ? null : new UFDateTime(end);

		Long sqlnum = (Long) hsvo.getAttributeValue(SQL_EXEC_NUM);
		Long sqlcosttime = (Long) hsvo.getAttributeValue(SQL_EXEC_TIME_SUM);
		Long readresulttime = (Long) hsvo
				.getAttributeValue(SQL_RESULT_TIME_SUM);
		Long readrownum = (Long) hsvo.getAttributeValue(SQL_RESULT_SUM);

		for (HashVO vo : dataVOs) {
			if (!checkVOBeginTime(vo))
				continue;
			try {
			if (!checkVOEndTime(vo))
					break;
			} catch (Exception e) {
				continue;
			}
			String ts = vo.getStringValue(INPRPluginConst.TS);
			if (Toolkit.isEmpty(ts))
				continue;

			try {
				Long costtime = vo.getMsgLongValueByField(
						INPRPluginConst.NC_COSTTIME, null);
				if(null == costtime)
					costtime = 0L;
				Long readtime = vo.getMsgLongValueByField(
						INPRPluginConst.NC_MW_READRESULTTIME, null);
				if(null == readtime)
					readtime = 0L;
				if (costtime < readtime)
					continue;
				UFDateTime dt = new UFDateTime(ts);
				if (null == uftBegin || dt.compareTo(uftBegin) < 0)
					uftBegin = dt;
				if (null == uftEnd || dt.compareTo(uftEnd) > 0)
					uftEnd = dt;
				Long temp = vo.getMsgLongValueByField(
						INPRPluginConst.NC_MW_SQLNUM, null);
				temp = temp == null ?0L:temp;
				sqlnum += temp;
				temp = vo.getMsgLongValueByField(
						INPRPluginConst.NC_MW_SQLCOSTTIME, null);
				temp = temp == null ?0L:temp;
				sqlcosttime += temp;
				temp = vo.getMsgLongValueByField(
						INPRPluginConst.NC_MW_READRESULTTIME, null);
				temp = temp == null ?0L:temp;
				readresulttime += temp;
				temp = vo.getMsgLongValueByField(
						INPRPluginConst.NC_MW_READROWNUM, null);
				temp = temp == null ?0L:temp;
				readrownum += temp;
			} catch (Exception e) {
				continue;
			}
		}
		try {
		hsvo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME,
				uftBegin.toString());
		hsvo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_ENDTIME,
				uftEnd.toString());
		hsvo.setAttributeValue(SQL_EXEC_NUM, sqlnum);
		hsvo.setAttributeValue(SQL_EXEC_TIME_SUM, sqlcosttime);
		hsvo.setAttributeValue(SQL_RESULT_SUM, readrownum);
		hsvo.setAttributeValue(SQL_RESULT_TIME_SUM, readresulttime);
		} catch (Exception e) {

		}

	}

	@Override
	public XPRPluginResult getResultVO() throws RemoteException {
		HashVO[] vos = new HashVO[map.size() + 1];
		UFDateTime uftBeginAll = null;
		UFDateTime uftEndAll = null;
		String begin;
		String end;
		long sqlnumSum = 0L;
		long sqlexectimeSum = 0L;
		long sqlreadSum = 0L;
		long sqlreadtimeSum = 0L;
		int i = 0;
		for (Map.Entry<String, HashVO> entry : map.entrySet()) {
			HashVO vo = entry.getValue();
			begin = vo.getStringValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME);
			if (Toolkit.isEmpty(begin))
				continue;
			end = vo.getStringValue(INPRPluginConst.PLUGIN_COLUMN_ENDTIME);
			if (Toolkit.isEmpty(end))
				continue;
			UFDateTime uftBegin = new UFDateTime(begin);
			if (null == uftBeginAll || uftBeginAll.compareTo(uftBegin) > 0)
				uftBeginAll = uftBegin;
			UFDateTime uftEnd = new UFDateTime(end);
			if (null == uftEndAll || uftEndAll.compareTo(uftEnd) < 0)
				uftEndAll = uftEnd;
			long min = UFDateTime.getMinutesBetween(uftBegin, uftEnd);
			if (min == 0)
				min = 1;
			vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_TIMES, min);
			long sqlnum = (Long) vo.getAttributeValue(SQL_EXEC_NUM);
			sqlnumSum += sqlnum;
			long avg = sqlnum / min;
			vo.setAttributeValue(AVG_MIN_NUM, avg);
			long sqlcosttime = (Long) vo.getAttributeValue(SQL_EXEC_TIME_SUM);
			long sqlcosttimeBySec = sqlcosttime / 1000;
			sqlexectimeSum += sqlcosttimeBySec;
			vo.setAttributeValue(SQL_EXEC_TIME_SUM, sqlcosttimeBySec);
			long readresulttime = (Long) vo
					.getAttributeValue(SQL_RESULT_TIME_SUM);
			long readresulttimeBySec = readresulttime / 1000;
			sqlreadtimeSum += readresulttimeBySec;
			vo.setAttributeValue(SQL_RESULT_TIME_SUM, readresulttimeBySec);
			if (readresulttimeBySec == 0)
				readresulttimeBySec = 1;
			long readrownum = (Long) vo.getAttributeValue(SQL_RESULT_SUM);
			sqlreadSum += readrownum;
			long resAvg = readrownum / readresulttimeBySec;
			vo.setAttributeValue(AVG_SEC_NUM, resAvg);
			vos[i++] = vo;
		}
		try {
		HashVO vo = new HashVO();
		vo.setAttributeValue(server, "合计");
		vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME,
				uftBeginAll.toString());
		vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_ENDTIME,
				uftEndAll.toString());
		long mins = UFDateTime.getMinutesBetween(uftBeginAll, uftEndAll);
		if (mins == 0)
			mins = 1;
		vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_TIMES, mins);
		vo.setAttributeValue(SQL_EXEC_NUM, sqlnumSum);
		vo.setAttributeValue(AVG_MIN_NUM, sqlnumSum / mins);
		vo.setAttributeValue(SQL_EXEC_TIME_SUM, sqlexectimeSum);
		vo.setAttributeValue(SQL_RESULT_SUM, sqlreadSum);
		vo.setAttributeValue(SQL_RESULT_TIME_SUM, sqlreadtimeSum);
		if (sqlreadtimeSum == 0)
			sqlreadtimeSum = 1;
		vo.setAttributeValue(AVG_SEC_NUM, sqlreadSum / sqlreadtimeSum);
		vos[i] = vo;
		} catch (Exception e) {

		}

		XPRPluginResult result = new XPRPluginResult(getPluginInfo());
		result.setContentVos(vos);
		return result;
	}
}
