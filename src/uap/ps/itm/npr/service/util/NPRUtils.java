/**
 *
 */
package uap.ps.itm.npr.service.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.monitor.pub.HashVO;
import nc.monitor.pub.Toolkit;
import uap.ps.itm.npr.service.INPRConst;
import uap.ps.itm.npr.service.log.resolve.FileSortComparetor;
import uap.ps.itm.npr.service.log.resolve.LogExtract;
import uap.ps.itm.npr.service.plugin.INPRPluginConst;
import nc.vo.pub.lang.UFDateTime;

/**
 * @author liuzy
 *
 */
public class NPRUtils {

	private String snapshotPath = null;

	private UFDateTime beginTime = null;

	private UFDateTime endTime = null;

	public NPRUtils(String shotPath, UFDateTime beginTime, UFDateTime endTime) {
		this.snapshotPath = shotPath;
		this.beginTime = beginTime;
		this.endTime = endTime;
	}

	public String getNMCLogPath_DB() {
		return this.snapshotPath.concat("/").concat(INPRConst.NMC_LOG_DIR_NAME)
				.concat("/").concat(INPRConst.NMC_LOG_DB_DIR_NAME);
	}

	public String getNMCLogPath_MW() {
		return this.snapshotPath.concat("/").concat(INPRConst.NMC_LOG_DIR_NAME)
				.concat("/").concat(INPRConst.NMC_LOG_MW_DIR_NAME);
	}

	public String getNMCLogPath_OnlineUser() {
		return this.snapshotPath.concat("/").concat(INPRConst.NMC_LOG_DIR_NAME)
				.concat("/").concat(INPRConst.NMC_LOG_ONLINEUSER_DIR_NAME);
	}

	public String getNMCLogPath_Snapshot() {
		return this.snapshotPath.concat("/").concat(INPRConst.NMC_LOG_DIR_NAME)
				.concat("/").concat(INPRConst.NMC_LOG_SNAPSHOT_DIR_NAME);
	}

	public String getNMCLogPath_Systemmonitor() {
		return this.snapshotPath.concat("/")
.concat(INPRConst.NMC_LOG_DIR_NAME)
				.concat("/")
				.concat(INPRConst.NMC_LOG_SYSTEM_MONITOR_NAME);
	}

	public String getNCLogPath() {
		return this.snapshotPath.concat("/").concat(INPRConst.NC_LOG_DIR_NAME);
	}

	public String getNCLogPathByServerName(String serverName) {
		return this.getNCLogPath().concat("/").concat(serverName);
	}

	public HashVO[] getNC_MW_ByServer(String serverName) {
		return this.getHashVOs(this.getNCLogPathByServerName(serverName),
				INPRConst.MWSMRY_FILE_NAME);
	}

	public HashVO[] getNC_MW_ALL() {
		return this.getHashVOs(this.getNCLogPath(), INPRConst.MWSMRY_FILE_NAME);
	}

	public HashVO[] getNC_Warningsql_ByServer(String serverName) {
		return this.getHashVOs(this.getNCLogPathByServerName(serverName),
				INPRConst.WNGSQL_FILE_NAME);
	}

	public HashVO[] getNC_Warningsql_ALL() {
		return this.getHashVOs(this.getNCLogPath(), INPRConst.WNGSQL_FILE_NAME);
	}

	public HashVO[] getNMC_DB_Info() {
		return this.getHashVOs(this.getNMCLogPath_DB());
	}

	public HashVO[] getNMC_MW_Info() {
		return this.getHashVOs(this.getNMCLogPath_MW());
	}

	public HashVO[] getNMC_OnlineUser_Info() {
		return this.getHashVOs(this.getNMCLogPath_OnlineUser());
	}

	public HashVO[] getNMC_Snapshot_Info() {
		return this.getHashVOs(this.getNMCLogPath_Snapshot());
	}

	public HashVO[] getNMC_Systemmonitor_Info() {
		return this.getHashVOs(this.getNMCLogPath_Systemmonitor());
	}
	protected HashVO[] getHashVOs(String path) {
		return new LogExtract().getHashVOs(path, this.beginTime, this.endTime);
	}

	protected HashVO[] getHashVOs(String path, String fileNamePre) {
		return new LogExtract().getHashVOs(path, fileNamePre, this.beginTime,
				this.endTime);
	}

	public Map<String, HashVO> getServersBeginEndTime(String type) {
		if (Toolkit.isEmpty(type))
			return null;
		String dir = null;
		if (INPRPluginConst.DATA_SRC_NMC_MW.equals(type))
			dir = snapshotPath + "/nmclogs/mw/";
		else if (INPRPluginConst.DATA_SRC_NC_MW.equals(type)
				|| INPRPluginConst.DATA_SRC_NC_WARNINGSQL.equals(type))
			dir = snapshotPath + "/nclogs/";
		File[] files = null;
		if (INPRPluginConst.DATA_SRC_NMC_MW.equals(type))
			files = new LogExtract().getFiles(dir, null, beginTime, endTime);
		else if (INPRPluginConst.DATA_SRC_NC_MW.equals(type))
			files = new LogExtract().getFiles(dir, INPRConst.MWSMRY_FILE_NAME,
					beginTime, endTime);
		else if (INPRPluginConst.DATA_SRC_NC_WARNINGSQL.equals(type))
			files = new LogExtract().getFiles(dir, INPRConst.WNGSQL_FILE_NAME,
					beginTime, endTime);
		if (Toolkit.isEmpty(files))
			return null;
		Map<String, List<File>> map = groupFilesByName(files, type);
		Map<String, HashVO> retMap = new HashMap<String, HashVO>();
		for (Map.Entry<String, List<File>> entry : map.entrySet()) {
			List<File> list = entry.getValue();
			if (list.size() > 1)
				Toolkit.sort(list, new FileSortComparetor());
			HashVO vo = new HashVO();
			vo.setAttributeValue(
					"begin",
					getBeginTime(list.toArray(new File[list.size()]),
							beginTime, endTime));
			vo.setAttributeValue(
					"end",
					getEndTime(list.toArray(new File[list.size()]), beginTime,
							endTime));
			retMap.put(entry.getKey(), vo);
		}
		return retMap;
	}

	private Map<String, List<File>> groupFilesByName(File[] files, String type) {
		Map<String, List<File>> map = new HashMap<String, List<File>>();
		String server = null;
		for (File file : files) {
			if (INPRPluginConst.DATA_SRC_NMC_MW.equals(type)) {
				String name = file.getName();
				if (name.indexOf("-") < 0)
					continue;
				server = name.substring(0, name.indexOf("-"));
			} else if (INPRPluginConst.DATA_SRC_NC_MW.equals(type)
					|| INPRPluginConst.DATA_SRC_NC_WARNINGSQL.equals(type))
				server = file.getParentFile().getName();
			List<File> list = map.get(server);
			if (Toolkit.isEmpty(list)) {
				list = new ArrayList<File>();
				map.put(server, list);
			}
			list.add(file);
		}
		return map;
	}

	public static UFDateTime getBeginTime(File[] files, UFDateTime begin,
			UFDateTime end) {
		UFDateTime uftBeginTime = null;
		LogExtract logex = new LogExtract();
		for (File f : files) {
			HashVO[] vos = logex.getHashVOsByFile(f);
			if (Toolkit.isEmpty(vos))
				continue;
			for (HashVO vo : vos) {
				try {
				String ts = vo.getStringValue(INPRPluginConst.TS);
				if (Toolkit.isEmpty(ts))
					continue;
				UFDateTime ufts = new UFDateTime(ts);
				if (Toolkit.isEmpty(begin) || Toolkit.isEmpty(begin.toString())) {
					uftBeginTime = ufts;
					break;
				} else {
					if (begin.compareTo(ufts) > 0)
						continue;
					if (ufts.compareTo(end) < 0)
						uftBeginTime = ufts;
					else
						uftBeginTime = begin;
					break;
				}
				} catch (Exception e) {
					continue;
				}
			}
			break;
			}
		return Toolkit.isEmpty(uftBeginTime) ? begin : uftBeginTime;
	}

	public static UFDateTime getEndTime(File[] files, UFDateTime begin,
			UFDateTime end) {
		UFDateTime uftEndTime = null;
		LogExtract logex = new LogExtract();
		for (int i = files.length - 1; i > -1; i--) {
			HashVO[] vos = logex.getHashVOsByFile(files[i]);
			if (Toolkit.isEmpty(vos))
				continue;
			for (int j = vos.length - 1; j > -1; j--) {
				try {
				String ts = vos[j].getStringValue(INPRPluginConst.TS);
				if (Toolkit.isEmpty(ts))
					continue;
				UFDateTime ufts = new UFDateTime(ts);
				if (Toolkit.isEmpty(end) || Toolkit.isEmpty(end.toString())) {
					uftEndTime = ufts;
					break;
				} else {
					if (end.compareTo(ufts) < 0)
						continue;
					if (ufts.compareTo(begin) > 0)
						uftEndTime = ufts;
					else
						uftEndTime = end;
					break;
				}
				} catch (Exception e) {
					continue;
				}
			}
			break;
			}
		return Toolkit.isEmpty(uftEndTime) ? begin : uftEndTime;
	}

}
