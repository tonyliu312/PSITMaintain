package uap.ps.itm.npr.service.plugin;

import java.io.File;

import nc.monitor.Exception.RemoteException;
import nc.monitor.pub.HashVO;
import uap.ps.itm.npr.vo.XPRPluginResult;
import nc.vo.pub.lang.UFDateTime;

public interface INPRPluginItf {

	void setSnapshotPaht(String path);

	void setTimeRange(UFDateTime beginTime, UFDateTime endTime);

	void setPluginInfo(HashVO info);

	HashVO getPluginInfo();

	String getNMCLogPath_DB();

	String getNMCLogPath_MW();

	String getNMCLogPath_OnlineUser();

	String getNMCLogPath_Snapshot();

	String getNCLogPath();

	String getNCLogPathByServerName(String serverName);

	void process(File file, HashVO[] dataVOs) throws RemoteException;

	XPRPluginResult getResultVO() throws RemoteException;

	XPRPluginResult getResultVO(HashVO pluginInfo) throws RemoteException;

}
