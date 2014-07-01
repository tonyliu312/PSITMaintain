/**
 *
 */
package uap.ps.itm.npr.service;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nc.monitor.pub.HashVO;
import nc.monitor.pub.Text2HashVO;
import nc.monitor.pub.Toolkit;
import nc.monitor.pub.Xml2HashVO;
import nc.monitor.service.center.ServerEnv;
import nc.monitor.service.center.ServiceHolder;
import nc.monitor.service.db.DBMonitor;
import uap.ps.itm.npr.service.plugin.INPRPluginConst;
import uap.ps.itm.npr.service.util.NPRUtils;
import nc.vo.pub.lang.UFDateTime;

/**
 * @author liuzy
 *
 */
public class NPRGlobalControl {

	private static final String END = "end";

	private static final String BEGIN = "begin";

	public static String NPR_SNAPSHOT_LOCK = "NPR_SNAPSHOT_LOCK";

	public static String NPR_CREATE_REPORT_LOCK = "NPR_CREATE_REPORT_LOCK";

	private static NPRGlobalControl instance = null;

	private boolean bSnapShotOnOpera = false;
	private boolean bCreateReportOnOpera = false;

	private Map<String, HashVO> map_nmc_mw_time = null;
	private Map<String, HashVO> map_nc_mw_time = null;
	private Map<String, HashVO> map_nc_wnsq_time = null;

	private final Map<String, String> map_nc_user = new HashMap<String, String>();

	private final Map<String, Set<String>> map_filter = new HashMap<String, Set<String>>();

	private NPRGlobalControl() {

	}

	public static NPRGlobalControl getInstance() {
		if (null == instance)
			instance = new NPRGlobalControl();
		return instance;
	}

	public synchronized boolean getOperatLock(String lockFlag) {
		if (lockFlag.equals(NPR_SNAPSHOT_LOCK)) {
			if (!bSnapShotOnOpera) {
				bSnapShotOnOpera = true;
				return false;
			} else
				return bSnapShotOnOpera;
		} else if (lockFlag.equals(NPR_CREATE_REPORT_LOCK)) {
			if (!bCreateReportOnOpera) {
				bCreateReportOnOpera = true;
				return false;
			} else
				return bCreateReportOnOpera;
		}
		return false;
	}

	public synchronized void operatUnLock(String lockFlag) {
		if (lockFlag.equals(NPR_SNAPSHOT_LOCK))
			bSnapShotOnOpera = false;
		else if (lockFlag.equals(NPR_CREATE_REPORT_LOCK))
			bCreateReportOnOpera = false;
	}

	public void initServerTimes(String shotPath, UFDateTime beginTime,
			UFDateTime endTime) {
		this.clearServerNMCMWTimes();
		map_nmc_mw_time = new NPRUtils(shotPath, beginTime, endTime)
				.getServersBeginEndTime(INPRPluginConst.DATA_SRC_NMC_MW);
		map_nc_mw_time = new NPRUtils(shotPath, beginTime, endTime)
				.getServersBeginEndTime(INPRPluginConst.DATA_SRC_NC_MW);
		map_nc_wnsq_time = new NPRUtils(shotPath, beginTime, endTime)
				.getServersBeginEndTime(INPRPluginConst.DATA_SRC_NC_WARNINGSQL);
	}

	public UFDateTime getServerNMCMWBeginTime(String serverName) {
		return this.getServerTime(serverName, BEGIN,
				INPRPluginConst.DATA_SRC_NMC_MW);
	}

	public UFDateTime getServerNMCMWEndTime(String serverName) {
		return this.getServerTime(serverName, END,
				INPRPluginConst.DATA_SRC_NMC_MW);
	}

	public UFDateTime getServerNCMWBeginTime(String serverName) {
		return this.getServerTime(serverName, BEGIN,
				INPRPluginConst.DATA_SRC_NC_MW);
	}

	public UFDateTime getServerNCMWEndTime(String serverName) {
		return this.getServerTime(serverName, END,
				INPRPluginConst.DATA_SRC_NC_MW);
	}

	public UFDateTime getServerNCWNSQLBeginTime(String serverName) {
		return this.getServerTime(serverName, BEGIN,
				INPRPluginConst.DATA_SRC_NC_WARNINGSQL);
	}

	public UFDateTime getServerNCWNSQLEndTime(String serverName) {
		return this.getServerTime(serverName, END,
				INPRPluginConst.DATA_SRC_NC_WARNINGSQL);
	}

	private UFDateTime getServerTime(String serverName, String beginOrEnd,
			String data_src) {
		if (Toolkit.isEmpty(serverName) || Toolkit.isEmpty(beginOrEnd)
				|| Toolkit.isEmpty(data_src))
			return null;
		HashVO vo = null;
		if (INPRPluginConst.DATA_SRC_NMC_MW.equals(data_src))
			vo = map_nmc_mw_time.get(serverName);
		else if (INPRPluginConst.DATA_SRC_NC_MW.equals(data_src))
			vo = map_nc_mw_time.get(serverName);
		else if (INPRPluginConst.DATA_SRC_NC_WARNINGSQL.equals(data_src))
			vo = map_nc_wnsq_time.get(serverName);
		if (Toolkit.isEmpty(vo))
			return null;
		if (BEGIN.equals(beginOrEnd))
			return (UFDateTime) vo.getAttributeValue(BEGIN);
		else if (END.equals(beginOrEnd))
			return (UFDateTime) vo.getAttributeValue(END);
		return null;
	}

	private void clearServerNMCMWTimes() {
		if (!Toolkit.isEmpty(map_nmc_mw_time))
			this.map_nmc_mw_time.clear();
		else
			map_nmc_mw_time = new HashMap<String, HashVO>();
		if (!Toolkit.isEmpty(map_nc_mw_time))
			this.map_nc_mw_time.clear();
		else
			map_nc_mw_time = new HashMap<String, HashVO>();
		if (!Toolkit.isEmpty(map_nc_wnsq_time))
			this.map_nc_wnsq_time.clear();
		else
			map_nc_wnsq_time = new HashMap<String, HashVO>();
	}

	public String getUserCodeByPK(String cuserid) throws Exception {
		if (Toolkit.isEmpty(map_nc_user))
			initUserMap();
		if (Toolkit.isEmpty(map_nc_user))
			return cuserid;
		String user_code = map_nc_user.get(cuserid);
		if (Toolkit.isEmpty(user_code))
			return cuserid;
		return user_code;
	}

	public void initUserMap() throws Exception {
		map_nc_user.clear();
		HashVO[] vosDataSource = Xml2HashVO.convertToVO(ServerEnv.getInstance()
				.getProperty("propfile"), "dataSource");
		if (Toolkit.isEmpty(vosDataSource))
			return;
		String sql = "select cuserid,user_code,user_name from sm_user";
		DBMonitor dbmonitor = (DBMonitor) ServiceHolder.getInstance()
				.getServer("dbmonitor");
		for (HashVO hsvo : vosDataSource) {
			String dataSrcName = hsvo.getStringValue("dataSourceName");
			HashVO[] vos;
			try {
				vos = (HashVO[]) dbmonitor.exec(dataSrcName, sql);
			} catch (Exception e) {
//				e.printStackTrace();
				continue;
			}
			if (Toolkit.isEmpty(vos))
				return;
			for (HashVO vo : vos) {
				map_nc_user.put(
						vo.getStringValue("cuserid"),
						vo.getStringValue("user_code") + "("
								+ vo.getStringValue("user_name") + ")");
			}
		}

	}

	public void initNPRFilter() {
		String filterdir = null;
		if (ServerEnv.getInstance().isDev())
			filterdir = "D:/NC_UAP_MODULES/nmc/server/conf/nprfilter";
		else
			filterdir = INPRConst.NPR_PLUGIN_FILTER_DIR;
		File dir = new File(filterdir);
		if (Toolkit.isEmpty(dir))
			return;
		File[] files = dir.listFiles();
		for (File f : files) {
			if (f.isDirectory())
				continue;
			String fileName = f.getName();
			if (!fileName.endsWith(INPRConst.NPR_PLUGIN_FILTER_EXTEND_NAME))
				continue;
			String path = f.getPath();
			HashVO[] nprFilterVOs = new Text2HashVO().convertToVO(path, "$$");
			if (Toolkit.isEmpty(nprFilterVOs))
				continue;
			for (HashVO vo : nprFilterVOs) {
				String busiAction = vo
						.getStringValue(INPRPluginConst.PLUGIN_COLUMN_BUSIACTION);
				if (Toolkit.isEmpty(busiAction))
					continue;
				String key = fileName + "-" + busiAction;
				Set<String> set = map_filter.get(key);
				if (Toolkit.isEmpty(set)) {
					set = new HashSet<String>();
					map_filter.put(key, set);
				}
				String method = vo
						.getStringValue(INPRPluginConst.PLUGIN_COLUMN_REMOTECALLMETHOD);
				if (!Toolkit.isEmpty(method))
					set.add(method);
			}
		}
	}

	public boolean isSkipThisBusiAction(String filterFileName, String busiAction) {
		String selfkey = filterFileName + "-" + busiAction;
		String allkey = INPRConst.NPR_PLUGIN_ALL_FILTER_FILE_NAME + "-"
				+ busiAction;
		if (Toolkit.isEmpty(map_filter))
			return false;
		if (this.map_filter.containsKey(selfkey)
				|| this.map_filter.containsKey(allkey))
			return true;
		// if (this.map_filter.get(selfkey).size() == 0
		// || this.map_filter.get(allkey).size() == 0)
		// return true;
		return false;
	}

	public boolean isSkipThisRemoteCall(String filterFileName,
			String busiAction, String method) {
		String selfkey = filterFileName + "-" + busiAction;
		String allkey = INPRConst.NPR_PLUGIN_ALL_FILTER_FILE_NAME + "-"
				+ busiAction;
		if (Toolkit.isEmpty(method))
			return this.isSkipThisBusiAction(filterFileName, busiAction);
		return this.map_filter.get(selfkey).contains(method)
				|| this.map_filter.get(allkey).contains(method);
	}

	public int getTopN() {
		String topn = ServerEnv.getInstance().getProperty(INPRConst.NPR_TOP_N);
		if (Toolkit.isEmpty(topn))
			return 10;
		return Integer.valueOf(topn);

		// return 30;
	}
}
