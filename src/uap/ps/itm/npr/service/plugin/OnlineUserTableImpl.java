/**
 *
 */
package uap.ps.itm.npr.service.plugin;

import java.io.File;

import nc.monitor.Exception.RemoteException;
import nc.monitor.pub.HashVO;
import nc.monitor.pub.Toolkit;
import uap.ps.itm.npr.vo.XPRPluginResult;
import nc.vo.pub.lang.UFDateTime;

/**
 * �����û���ͳ�Ʊ���
 *
 * @author liuzy
 *
 */
public class OnlineUserTableImpl extends AbsNPRPlugin {

	private UFDateTime begin = null;

	private UFDateTime end = null;

	private int max = 0;

	private int min = 0;

	private int sum = 0;

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
		for (HashVO vo : dataVOs) {
			if (!checkVOBeginTime(vo))
				continue;
			try {
			if (!checkVOEndTime(vo))
					break;
			} catch (Exception e) {
				continue;
			}
			try {
			UFDateTime ts = new UFDateTime(
					vo.getStringValue(INPRPluginConst.TS));
			if (Toolkit.isEmpty(ts))
				continue;
			if (null == begin || ts.compareTo(begin) < 0)
				begin = ts;

			if (null == end || ts.compareTo(end) > 0)
				end = ts;
			int userCount = vo
					.getIntegerValue(INPRPluginConst.NMC_ONLINEUSER_USERCOUNT);
			if (Toolkit.isEmpty(userCount))
				continue;
			if (userCount < min)
				min = userCount;
			if (userCount > max)
				max = userCount;
			sum += userCount;
			} catch (Exception e) {
				e.printStackTrace();
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
		if (Toolkit.isEmpty(begin))
			return null;
		int times = UFDateTime.getMinutesBetween(begin, end);
		if(times == 0)
			times = 1;
		HashVO vo = new HashVO();
		vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME, begin);
		vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_ENDTIME, end);
		vo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_TIMES, times);
		vo.setAttributeValue("�����������", max);
		vo.setAttributeValue("��С��������", min);
		vo.setAttributeValue("ƽ����������(��/����)", sum / times);
		XPRPluginResult result = new XPRPluginResult(getPluginInfo());
		result.setContentVos(new HashVO[] { vo });
		return result;
	}
}
