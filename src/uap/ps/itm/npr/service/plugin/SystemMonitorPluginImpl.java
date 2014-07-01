package uap.ps.itm.npr.service.plugin;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import nc.monitor.Exception.RemoteException;
import nc.monitor.pub.HashVO;
import nc.monitor.pub.Toolkit;
import nc.monitor.ui.system.SystemMonitorConst;
import uap.ps.itm.npr.service.NPRGlobalControl;
import uap.ps.itm.npr.vo.XPRPluginResult;
import nc.vo.pub.lang.UFDateTime;

/**
 *
 * <code>SystemMonitorPluginImpl<code>
 * <strong></strong>
 * <p>说明：
 * <li></li>
 * </p>
 * @since NC6.3
 * @version 2013-3-13 下午04:04:38
 * @author tangxx
 */

public class SystemMonitorPluginImpl extends AbsNPRPlugin {

	private final static String[] NAMES = new String[] {
			SystemMonitorConst.CPUAVA, SystemMonitorConst.MENAVA,
			SystemMonitorConst.IFNETAVA };
	private final Map<String, XPRPluginResult> map = new HashMap<String, XPRPluginResult>();
	private final String defBeginTime = "2010-01-01 00:00:00";

	@Override
	public void process(File file, HashVO[] dataVOs) throws RemoteException {
		UFDateTime begin = this.getBeginTime();
		if (Toolkit.isEmpty(begin) || new UFDateTime().compareTo(begin) == 0) {
			begin = new UFDateTime(defBeginTime);
			String name = file.getName();
			if (name.indexOf("-") < 0)
				return;
			String server = name.substring(0, name.indexOf("-"));
			begin = NPRGlobalControl.getInstance().getServerNMCMWBeginTime(
					server);
		}
		UFDateTime end = this.getEndTime();
		if (Toolkit.isEmpty(end)) {
			end = new UFDateTime(new Date());
			String name = file.getName();
			if (name.indexOf("-") < 0)
				return;
			String server = name.substring(0, name.indexOf("-"));
			end = NPRGlobalControl.getInstance().getServerNMCMWEndTime(server);
		}
		UFDateTime before_end_oneday = null;
		try {
		// 取一天的数
			before_end_oneday = end.getDateBefore(1);
		} catch (Exception e) {
			before_end_oneday = new UFDateTime(new Date()).getDateBefore(1);
		}
		if (begin.before(before_end_oneday)) {
			begin = before_end_oneday;
		}
		for (HashVO vo : dataVOs) {
			try {
			UFDateTime time = new UFDateTime(vo.getStringValue("ts"));
			if (time.before(begin) || time.after(end)) {
				continue;
			}
			} catch (Exception e) {
				continue;
			}
			String hostname = vo.getStringValue(SystemMonitorConst.HOSTNAME);
			if (this.map.get(hostname) == null) {
				XPRPluginResult pluginvo = new XPRPluginResult("系统监控"
						+ hostname);
				pluginvo.setxFiledName("ts");
				pluginvo.setFormat(INPRPluginConst.FORMAT_CHART);
				pluginvo.setChart_type(INPRPluginConst.LINE_CHART);
				pluginvo.setChart_high(418);
				pluginvo.setChart_width(2048);
				this.map.put(hostname, pluginvo);
			}
			XPRPluginResult plugionvo = map.get(hostname);
			for (String str : NAMES)
				plugionvo.addData2Map(str, vo);

		}


	}

	public XPRPluginResult[] getSpecialResultVO() throws RemoteException {
		return map.size() == 0 ? null : map.values().toArray(
				new XPRPluginResult[0]);
	}

	@Override
	protected boolean checkProc(File file, HashVO[] dataVOs) {
		if (Toolkit.isEmpty(file) || Toolkit.isEmpty(dataVOs))
			return false;
		return true;
	}

}
