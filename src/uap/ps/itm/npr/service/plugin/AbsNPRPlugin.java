/**
 *
 */
package uap.ps.itm.npr.service.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nc.vo.pub.lang.UFDateTime;
import uap.ps.itm.monitor.RemoteException;
import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.npr.service.INPRConst;
import uap.ps.itm.npr.service.NPRGlobalControl;
import uap.ps.itm.npr.service.log.resolve.LogExtract;
import uap.ps.itm.npr.vo.XPRPluginResult;

/**
 * NPR插件的抽象实现
 *
 * @author liuzy
 *
 */
public abstract class AbsNPRPlugin implements INPRPluginItf {

	private String snapshotPath = null;

	private UFDateTime beginTime = null;

	private UFDateTime endTime = null;

	private HashVO pluginInfo = null;

	private final List<HashVO> contentVOList = new ArrayList<HashVO>();

	private XPRPluginResult result = null;

	protected boolean checkVO(HashVO vo) {
		String busiAction = vo.getStringValue(INPRPluginConst.NC_MW_BUSIACTION);
		if (Toolkit.isEmpty(busiAction))
			return true;

		if (!Toolkit.isEmpty(busiAction)
				&& INPRPluginConst.UNKOWN.equals(vo
						.getStringValue(INPRPluginConst.NC_MW_BUSIACTION)))
			return false;
		return true;
	}

	protected boolean checkVOByFilter(HashVO vo) {
		try {
		String busiAction = vo.getMsgStringValueByField(
				INPRPluginConst.NC_MW_BUSIACTION, null);
		return NPRGlobalControl.getInstance().isSkipThisBusiAction(
				this.getPluginInfo().getStringValue(
						INPRPluginConst.FILTER_FILE_NAME), busiAction);
		} catch (Exception e) {
			return false;
		}
	}

	protected boolean checkVOBeginTime(HashVO vo) {
		if (Toolkit.isEmpty(getBeginTime()))
			return true;
		try {
		String ts = vo.getStringValue(INPRPluginConst.TS);
		if (!Toolkit.isEmpty(ts)) {
			UFDateTime ufts = new UFDateTime(ts);
			if (ufts.compareTo(getBeginTime()) < 0)
				return false;
			else
				return true;
		}
		} catch (Exception e) {
			return false;
		}
		return false;

	}

	protected boolean checkVOEndTime(HashVO vo) throws Exception {
		if (Toolkit.isEmpty(getEndTime()))
			return true;
		String ts = vo.getStringValue(INPRPluginConst.TS);
		if (!Toolkit.isEmpty(ts)) {
			UFDateTime ufts = new UFDateTime(ts);
			if (ufts.compareTo(getEndTime()) > 0)
				return false;
			else
				return true;
		}
		return false;

	}

	protected boolean checkProc(File file, HashVO[] dataVOs) {
		if (Toolkit.isEmpty(file) || Toolkit.isEmpty(dataVOs))
			return false;
		return true;
	}

	protected boolean isMasterFile(File file) {
		return file.getParentFile().getName().equals(INPRPluginConst.MASTER) ? true
				: false;
	}

	/*
	 * 设置快照路径
	 *
	 * @see
	 * uap.ps.itm.npr.service.plugin.INPRPluginItf#setSnapshotPaht(java.lang.String)
	 */

	@Override
	public void setSnapshotPaht(String path) {
		this.snapshotPath = path;
	}

	/**
	 * @return the snapshotPath
	 */
	public String getSnapshotPath() {
		return snapshotPath;
	}

	@Override
	public String getNMCLogPath_DB() {
		return this.getSnapshotPath().concat("/")
				.concat(INPRConst.NMC_LOG_DIR_NAME).concat("/")
				.concat(INPRConst.NMC_LOG_DB_DIR_NAME);
	}

	@Override
	public String getNMCLogPath_MW() {
		return this.getSnapshotPath().concat("/")
				.concat(INPRConst.NMC_LOG_DIR_NAME).concat("/")
				.concat(INPRConst.NMC_LOG_MW_DIR_NAME);
	}

	@Override
	public String getNMCLogPath_OnlineUser() {
		return this.getSnapshotPath().concat("/")
				.concat(INPRConst.NMC_LOG_DIR_NAME).concat("/")
				.concat(INPRConst.NMC_LOG_ONLINEUSER_DIR_NAME);
	}

	@Override
	public String getNMCLogPath_Snapshot() {
		return this.getSnapshotPath().concat("/")
				.concat(INPRConst.NMC_LOG_DIR_NAME).concat("/")
				.concat(INPRConst.NMC_LOG_SNAPSHOT_DIR_NAME);
	}

	public String getNMCLogPath_Systemmonitor() {
		return this.getSnapshotPath().concat("/")
				.concat(INPRConst.NMC_LOG_DIR_NAME).concat("/")
				.concat(INPRConst.NMC_LOG_SYSTEM_MONITOR_NAME);
	}

	@Override
	public String getNCLogPath() {
		return this.getSnapshotPath().concat("/")
				.concat(INPRConst.NC_LOG_DIR_NAME);
	}

	@Override
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

	/*
	 * 设置时间区间
	 *
	 * @see
	 * uap.ps.itm.npr.service.plugin.INPRPluginItf#setTimeRange(nc.vo.pub.lang.UFDateTime
	 * , nc.vo.pub.lang.UFDateTime)
	 */
	@Override
	public void setTimeRange(UFDateTime beginTime, UFDateTime endTime) {
		this.beginTime = beginTime;
		this.endTime = endTime;
	}

	/**
	 * @return the beginTime
	 */
	protected UFDateTime getBeginTime() {
		return this.beginTime;
	}

	/**
	 * @return the endTime
	 */
	protected UFDateTime getEndTime() {
		return this.endTime;
	}

	protected HashVO[] getHashVOs(String path) {
		return new LogExtract().getHashVOs(path, this.beginTime, this.endTime);
	}

	protected HashVO[] getHashVOs(String path, String fileNamePre) {
		return new LogExtract().getHashVOs(path, fileNamePre, this.beginTime,
				this.endTime);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * uap.ps.itm.npr.service.plugin.INPRPluginItf#setPluginInfo(nc.monitor.pub.HashVO)
	 */

	@Override
	public void setPluginInfo(HashVO info) {
		this.pluginInfo = info;
		this.result = new XPRPluginResult(info);
	}

	@Override
	public HashVO getPluginInfo() {
		return this.pluginInfo;
	}

	protected void addContentVOs(HashVO[] vos) {
		this.contentVOList.addAll(Arrays.asList(vos));
	}

	protected void addContentVOsWithTimeCheck(HashVO[] vos) {
		for (HashVO vo : vos) {
			try {
				if (!checkVOEndTime(vo))
					return;
			} catch (Exception e) {
				continue;
			}
			if (!checkVOBeginTime(vo))
				return;
			this.addContentVO(vo);
		}
	}

	protected void addContentVOsWithCheck(HashVO[] vos) {
		for (HashVO vo : vos) {
			if (!checkVO(vo))
				continue;
			try {
				if (!checkVOEndTime(vo))
					return;
			} catch (Exception e) {
				continue;
			}
			if (!checkVOBeginTime(vo))
				return;
			this.addContentVO(vo);
		}
	}

	protected void addContentVO(HashVO vo) {
		this.contentVOList.add(vo);
	}

	/**
	 * @return the contentVOList
	 */
	protected List<HashVO> getContentVOList() {
		return contentVOList;
	}

	@Override
	public XPRPluginResult getResultVO() throws RemoteException {
		if (Toolkit.isEmpty(this.getContentVOList())
				&& Toolkit.isEmpty(result.getDataMap()))
			return null;
		if (!Toolkit.isEmpty(this.getContentVOList())) {
			HashVO[] vos = this.getContentVOList().toArray(
					new HashVO[this.getContentVOList().size()]);
			result.addData2Map(result.getLine_name(), vos);
			result.setContentVos(vos);
		}
		return result;
	}

	protected void addData2Map(String name, HashVO[] vos) {
		this.result.addData2Map(name, vos);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * uap.ps.itm.npr.service.plugin.INPRPluginItf#getResultVO(nc.monitor.pub.HashVO)
	 */

	@Override
	public XPRPluginResult getResultVO(HashVO pluginInfo) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
