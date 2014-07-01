/**
 *
 */
package uap.ps.itm.npr.service.plugin;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import nc.monitor.Exception.RemoteException;
import nc.monitor.pub.HashVO;
import nc.monitor.pub.Toolkit;
import uap.ps.itm.npr.service.NPRGlobalControl;
import uap.ps.itm.npr.vo.XPRPluginResult;
import nc.vo.pub.lang.UFDateTime;


/**
 * 数据库负载波动图插件
 *
 * @author liuzy
 *
 */
public class DatabaseWorkLoadChartImpl extends AbsNPRPlugin {

	private final static String[] NAMES = new String[] { "activesessions",
			"redo_size", "user_commits", "physical_reads", "physical_writes",
			"user_calls", "db_block_gets", "sorts_(disk)" };
	private final Map<String, XPRPluginResult> map = new HashMap<String, XPRPluginResult>();
	private final String defBeginTime = "2010-01-01 00:00:00";
	private static Map<String, HashVO> maxMapdatasource = new HashMap<String, HashVO>();

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

		if (Toolkit.isEmpty(end) || end == null) {
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
		int namebeginindex = file.getName().indexOf(".log");
		if (namebeginindex == -1) {
			return;
		}
		String datasourcename = file.getName().substring(0, namebeginindex);
		long redo_size_max = 0;
		long user_commits_max = 0;
		long physical_reads_max = 0;
		long physical_writes_max = 0;
		long user_calls_max = 0;
		long db_block_gets_max = 0;
		long sorts_max = 0;
		HashVO maxvo = maxMapdatasource.get(datasourcename);
		if (maxvo == null) {
			maxvo = new HashVO();
			redo_size_max = 2000000;
			user_commits_max = 100;
			physical_reads_max = 5000;
			physical_writes_max = 20000;
			user_calls_max = 5000;
			db_block_gets_max = 200000;
			sorts_max = 300;
		} else {
			redo_size_max = Long.parseLong(maxvo.getStringValue("redo_size"));
			user_commits_max = Long.parseLong(maxvo
					.getStringValue("user_commits"));
			physical_reads_max = Long.parseLong(maxvo
					.getStringValue("physical_reads"));
			physical_writes_max = Long.parseLong(maxvo
					.getStringValue("physical_writes"));
			user_calls_max = Long.parseLong(maxvo.getStringValue("user_calls"));
			db_block_gets_max = Long.parseLong(maxvo
					.getStringValue("db_block_gets"));
			sorts_max = Long.parseLong(maxvo.getStringValue("sorts_(disk)"));
		}
		for (int i = 0; i < dataVOs.length; i++) {
			try {
			UFDateTime time = new UFDateTime(dataVOs[i].getStringValue("ts"));
			if (time.before(begin) || time.after(end)) {
				continue;
			}
			} catch (Exception e) {
				continue;
			}
			long redo_size = Toolkit.isEmpty(dataVOs[i]
					.getStringValue("redo_size")) ? 0 : Long
					.parseLong(dataVOs[i]
					.getStringValue("redo_size"));
			long user_commits = Toolkit.isEmpty(dataVOs[i]
					.getStringValue("user_commits")) ? 0 : Long
					.parseLong(dataVOs[i]
					.getStringValue("user_commits"));
			long physical_reads = Toolkit.isEmpty(dataVOs[i]
					.getStringValue("physical_reads")) ? 0 : Long
					.parseLong(dataVOs[i]
					.getStringValue("physical_reads"));
			long physical_writes = Toolkit.isEmpty(dataVOs[i]
					.getStringValue("physical_writes")) ? 0 : Long
					.parseLong(dataVOs[i]
					.getStringValue("physical_writes"));
			long user_calls = Toolkit.isEmpty(dataVOs[i]
					.getStringValue("user_calls")) ? 0 : Long
					.parseLong(dataVOs[i]
					.getStringValue("user_calls"));
			long db_block_gets = Toolkit.isEmpty(dataVOs[i]
					.getStringValue("db_block_gets")) ? 0 : Long
					.parseLong(dataVOs[i]
					.getStringValue("db_block_gets"));
			long sorts = Toolkit.isEmpty(dataVOs[i]
					.getStringValue("sorts_(disk)")) ? 0 : Long
					.parseLong(dataVOs[i]
					.getStringValue("sorts_(disk)"));
			if (redo_size > redo_size_max) {
				redo_size_max = redo_size;
			}
			if (user_commits > user_commits_max) {
				user_commits_max = user_commits;
			}
			if (physical_reads > physical_reads_max) {
				physical_reads_max = physical_reads;
			}
			if (physical_writes > physical_writes_max) {
				physical_writes_max = physical_writes;
			}
			if (user_calls > user_calls_max) {
				user_calls_max = user_calls;
			}
			if (db_block_gets > db_block_gets_max) {
				db_block_gets_max = db_block_gets;
			}
			if (sorts > sorts_max) {
				sorts_max = sorts;
			}

		}
		maxvo.setAttributeValue("redo_size", redo_size_max);
		maxvo.setAttributeValue("user_commits", user_commits_max);
		maxvo.setAttributeValue("physical_reads", physical_reads_max);
		maxvo.setAttributeValue("physical_writes", physical_writes_max);
		maxvo.setAttributeValue("user_calls", user_calls_max);
		maxvo.setAttributeValue("db_block_gets", db_block_gets_max);
		maxvo.setAttributeValue("sorts_(disk)", sorts_max);
		maxMapdatasource.put(datasourcename, maxvo);
		for (HashVO vo : dataVOs) {
			try {
			UFDateTime time = new UFDateTime(vo.getStringValue("ts"));
			if (time.before(begin) || time.after(end)) {
				continue;
			}
			} catch (Exception e) {
				continue;
			}
			if (this.map.get(datasourcename) == null) {
				XPRPluginResult pluginvo = new XPRPluginResult("数据库负载波动图"
						+ datasourcename);
				pluginvo.setxFiledName("ts");
				pluginvo.setFormat(INPRPluginConst.FORMAT_CHART);
				pluginvo.setChart_type(INPRPluginConst.LINE_CHART);
				pluginvo.setChart_high(418);
				pluginvo.setChart_width(2048);
				this.map.put(datasourcename, pluginvo);
			}
			XPRPluginResult plugionvo = map.get(datasourcename);
			long redo_size = Toolkit.isEmpty(vo.getStringValue("redo_size")) ? 0
					: Long.parseLong(vo
					.getStringValue("redo_size"));
			long user_commits = Toolkit.isEmpty(vo
					.getStringValue("user_commits")) ? 0 : Long.parseLong(vo
					.getStringValue("user_commits"));
			long physical_reads = Toolkit.isEmpty(vo
					.getStringValue("physical_reads")) ? 0 : Long.parseLong(vo
					.getStringValue("physical_reads"));
			long physical_writes = Toolkit.isEmpty(vo
					.getStringValue("physical_writes")) ? 0 : Long.parseLong(vo
					.getStringValue("physical_writes"));
			long user_calls = Toolkit.isEmpty(vo.getStringValue("user_calls")) ? 0
					: Long.parseLong(vo
					.getStringValue("user_calls"));
			long db_block_gets = Toolkit.isEmpty(vo
					.getStringValue("db_block_gets")) ? 0 : Long.parseLong(vo
					.getStringValue("db_block_gets"));
			long sorts = Toolkit.isEmpty(vo.getStringValue("sorts_(disk)")) ? 0
					: Long.parseLong(vo
					.getStringValue("sorts_(disk)"));
			HashVO newvo = new HashVO();
			newvo.setAttributeValue("ts", vo.getAttributeValue("ts"));
			newvo.setAttributeValue("activesessions",
					vo.getAttributeValue("activesessions"));
			if (redo_size_max == 0) {
				newvo.setAttributeValue("redo_size", 0);
			} else {
			newvo.setAttributeValue("redo_size", (double) 100 * redo_size
					/ redo_size_max);
			}
			if (user_commits_max == 0) {
				newvo.setAttributeValue("user_commits", 0);
			} else {
				newvo.setAttributeValue("user_commits", (double) 100
						* user_commits / user_commits_max);
			}
			if (physical_reads_max == 0) {
				newvo.setAttributeValue("physical_reads", 0);
			} else {
				newvo.setAttributeValue("physical_reads",
						(double) physical_reads * 100 / physical_reads_max);
			}
			if (physical_writes_max == 0) {
				newvo.setAttributeValue("physical_writes", 0);
			} else {
				newvo.setAttributeValue("physical_writes", (double) 100
						* physical_writes / physical_writes_max);
			}
			if (user_calls_max == 0) {
				newvo.setAttributeValue("user_calls", 0);
			} else {
				newvo.setAttributeValue("user_calls", (double) 100 * user_calls
						/ user_calls_max);
			}
			if (db_block_gets_max == 0) {
				newvo.setAttributeValue("db_block_gets", 0);
			} else {
				newvo.setAttributeValue("db_block_gets", (double) 100
						* db_block_gets / db_block_gets_max);
			}
			if (sorts_max == 0) {
				newvo.setAttributeValue("sorts_(disk)", 0);
			} else {
				newvo.setAttributeValue("sorts_(disk)", (double) 100 * sorts
						/ sorts_max);
			}

			for (String str : NAMES)
				plugionvo.addData2Map(str, newvo);

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
