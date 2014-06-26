/**
 *
 */
package uap.ps.itm.npr.service.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import uap.ps.itm.monitor.RemoteException;
import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.npr.service.INPRConst;
import uap.ps.itm.npr.service.NPRGlobalControl;
import uap.ps.itm.npr.vo.XPRPluginResult;

/**
 * Top10插件默认实现
 *
 * @author liuzy
 *
 */
public class DefaultTop10PluginImpl extends AbsNPRPlugin {

	private String avgColName = null;

	private boolean busiActionColIsMsg = false;

	private String busiActionColName = INPRPluginConst.NC_MW_BUSIACTION;

	private String busiActionShowName = INPRPluginConst.PLUGIN_COLUMN_BUSIACTION;

	private boolean countByGroup = false;

	private String countColName = null;

	private boolean groupColIsMsg = false;

	private String groupColName = INPRPluginConst.NC_MW_REMOTECALLID;

	private boolean isDealMaster = false;

	private boolean isDealServers = true;

	private boolean isSum = false;

	private final Map<String, HashVO> map = new HashMap<String, HashVO>();

	private String msgToken = INPRPluginConst.MSG_TOKEN_SEMICOLON;

	private boolean showAvgCol = false;

	private boolean showCallid = true;

	private boolean showColIsMsg = false;

	private String showColName = null;

	private boolean showMethodCol = false;

	private boolean showTimeRange = false;

	private boolean showUserid = true;

	private final List<HashVO> sortedList = new ArrayList<HashVO>();

	private boolean valueColIsMsg = false;

	private String valueColName = null;

	/*
	 * (non-Javadoc)
	 *
	 * @see uap.ps.itm.npr.service.plugin.AbsNPRPlugin#getResultVO()
	 */
	@Override
	public XPRPluginResult getResultVO() throws RemoteException {
		HashVO[] newVos = null;
		if (isSum)
			newVos = map.values().toArray(new HashVO[map.size()]);
		else
			newVos = this.sortedList
					.toArray(new HashVO[this.sortedList.size()]);
		Toolkit.sort(newVos, new Top10DefComparetor(this.countColName, false));
		List<HashVO> contentVOs = new ArrayList<HashVO>();
		// List<String> methodList = new ArrayList<String>();
		int topn = NPRGlobalControl.getInstance().getTopN();
		for (int i = 0; i < topn && i < newVos.length; i++) {
			newVos[i].setAttributeValue("排名", String.valueOf(i + 1));
			// HashVO vo = new HashVO();
			// vo.setAttributeValue("排名", String.valueOf(i + 1));
			// if (this.showCallid)
			// vo.setAttributeValue("callID",
			// newVos[i].getStringValue("callID"));
			// if (this.showUserid) {
			// vo.setAttributeValue("操作员ID", newVos[i].getStringValue("操作员ID"));
			// if (this.getPluginInfo()
			// .getStringValue(INPRPluginConst.DATA_SRC)
			// .indexOf(INPRPluginConst.DATA_SRC_NC_WARNINGSQL) < 0)
			// vo.setAttributeValue(
			// INPRPluginConst.PLUGIN_COLUMN_REMOTEADDR,
			// newVos[i]
			// .getStringValue(INPRPluginConst.PLUGIN_COLUMN_REMOTEADDR));
			// }
			// if (this.showMethodCol) {
			// String method = newVos[i]
			// .getStringValue(INPRPluginConst.PLUGIN_COLUMN_REMOTECALLMETHOD);
			// vo.setAttributeValue(
			// INPRPluginConst.PLUGIN_COLUMN_REMOTECALLMETHOD, method);
			// methodList.add(method);
			// }
			// String busiAction = newVos[i].getStringValue(busiActionShowName);
			// vo.setAttributeValue(this.busiActionShowName, busiAction);
			// vo.setAttributeValue(this.countColName,
			// newVos[i].getStringValue(this.countColName));

			if (showAvgCol) {
				long mins = UFDateTime
						.getMinutesBetween(
								(UFDateTime) newVos[i]
										.getAttributeValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME),
								(UFDateTime) newVos[i]
										.getAttributeValue(INPRPluginConst.PLUGIN_COLUMN_ENDTIME));
				if (mins <= 0)
					continue;
				long value = Long.valueOf(newVos[i]
						.getStringValue(this.countColName));
				UFDouble avg = new UFDouble(value).div(mins);
				avg = avg.setScale(2, UFDouble.ROUND_HALF_UP);
				newVos[i].setAttributeValue(avgColName, avg);
			}
			if (!Toolkit.isEmpty(showColName))
				newVos[i].setAttributeValue("详细信息",
						newVos[i].getStringValue("详细信息"));
			contentVOs.add(newVos[i]);
		}

		XPRPluginResult result = new XPRPluginResult(this.getPluginInfo());
		result.setContentVos(contentVOs.toArray(new HashVO[contentVOs.size()]));
		return result;
	}

	public boolean isBusiActionColIsMsg() {
		return busiActionColIsMsg;
	}

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

		String server = file.getParentFile().getName();
		if (server.equals(INPRPluginConst.MASTER) && !this.isDealMaster)
			return;

		if (!server.equals(INPRPluginConst.MASTER) && !this.isDealServers)
			return;

		if (Toolkit.isEmpty(this.valueColName) && !this.countByGroup)
			return;

		UFDateTime begin = null;
		UFDateTime end = null;

		String key = null;
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
			if (this.groupColIsMsg)
				key = vo.getMsgStringValueByField(this.groupColName,
						this.msgToken);
			else
				key = vo.getStringValue(this.groupColName);
			;
			if (Toolkit.isEmpty(key))
				continue;
			if (!map.containsKey(key)) {
				if (isSum)
					vo.setAttributeValue(this.countColName, 0);
				else {
					if (this.valueColIsMsg) {
						Long val = null;
						try {
							val = vo.getMsgLongValueByField(this.valueColName,
									this.msgToken);
						} catch (Exception e) {
							continue;
						}
						vo.setAttributeValue(this.countColName, val);
					} else
						vo.setAttributeValue(this.countColName,
								vo.getAttributeValue(this.valueColName));
				}
				HashVO putVO = processVO(file, vo);
				if (Toolkit.isEmpty(putVO)
						|| Toolkit.isEmpty(putVO
								.getAttributeValue(this.countColName)))
					continue;
				map.put(key, putVO);
			}

			HashVO hsvo = map.get(key);
			String s = hsvo.getStringValue(this.countColName);
			if (Toolkit.isEmpty(s))
				continue;
			long l = Long.valueOf(hsvo.getStringValue(this.countColName));
			long value = 0L;
			if (this.countByGroup) {
				l += 1;
			} else {
				if (this.valueColIsMsg)
					try {
						value = vo.getMsgLongValueByField(valueColName,
								this.msgToken);
					} catch (NumberFormatException e) {
						continue;
					}
				else
					value = Long.valueOf(vo.getStringValue(valueColName));
				if (isSum) {
					l += value;
				} else {
					if (value > l)
						l = value;
				}
			}
			hsvo.setAttributeValue(this.countColName, l);

			if (showAvgCol) {
				String ts = vo.getStringValue(INPRPluginConst.TS);
				if (Toolkit.isEmpty(ts))
					continue;
				begin = (UFDateTime) hsvo
						.getAttributeValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME);
				end = (UFDateTime) hsvo
						.getAttributeValue(INPRPluginConst.PLUGIN_COLUMN_ENDTIME);
				UFDateTime ufdts = new UFDateTime(ts);
				if (null == begin || begin.compareTo(ufdts) > 0) {
					begin = ufdts;
					hsvo.setAttributeValue(
							INPRPluginConst.PLUGIN_COLUMN_BEGINTIME, ufdts);
				}

				if (null == end || end.compareTo(ufdts) < 0) {
					end = ufdts;
					hsvo.setAttributeValue(
							INPRPluginConst.PLUGIN_COLUMN_ENDTIME, ufdts);
				}
			}
		}
		if (!isSum) {
			if (Toolkit.isEmpty(map))
				return;
			HashVO[] newVos = map.values().toArray(new HashVO[map.size()]);
			List<HashVO> list = new ArrayList<HashVO>();
			list.addAll(Arrays.asList(newVos));
			list.addAll(this.sortedList);
			Toolkit.sort(list, new Top10DefComparetor(this.countColName, false));
			this.sortedList.clear();
			int listSize = list.size();
			int topn = NPRGlobalControl.getInstance().getTopN();
			for (int i = 0; i < topn && i < listSize; i++)
				sortedList.add(list.get(i));
			map.clear();
		}
	}

	protected HashVO processVO(File file, HashVO vo) {
		HashVO hsvo = new HashVO();
		hsvo.setAttributeValue("排名", "");
		if (this.showCallid) {
			hsvo.setAttributeValue("CallID",
					vo.getStringValue(INPRPluginConst.NC_MW_CALLID));
			hsvo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_TS,
					vo.getStringValue(INPRPluginConst.TS));
		}
		dealTimeRange(file, hsvo);
		dealUserID(vo, hsvo);
		if (this.showMethodCol) {
			String method = vo.getMsgStringValueByField(
					INPRPluginConst.NC_MW_REMOTECALLMETHOD, this.msgToken);
			if (Toolkit.isEmpty(method))
				return null;
			hsvo.setAttributeValue(
					INPRPluginConst.PLUGIN_COLUMN_REMOTECALLMETHOD, method);
		}
		String busiAction = vo.getMsgStringValueByField(this.busiActionColName,
				this.msgToken);
		if (INPRPluginConst.UNKOWN.equals(busiAction))
			return null;
		hsvo.setAttributeValue(this.busiActionShowName, busiAction);
		hsvo.setAttributeValue(this.countColName,
				vo.getStringValue(this.countColName));

		if (!Toolkit.isEmpty(showColName)) {
			if (this.showColIsMsg)
				hsvo.setAttributeValue("详细信息", vo.getMsgStringValueByField(
						this.showColName, this.msgToken));
			else
				hsvo.setAttributeValue("详细信息",
						vo.getStringValue(this.showColName));
		}

		return hsvo;
	}

	private void dealUserID(HashVO vo, HashVO hsvo) {
		if (this.showUserid) {
			String cuserid = vo.getStringValue(INPRPluginConst.NC_MW_USERID);
			String user_code = "";
			try {
				user_code = NPRGlobalControl.getInstance().getUserCodeByPK(
						cuserid);
			} catch (Exception e) {
				e.printStackTrace();
			}
			hsvo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_USERID,
					user_code);
			if (this.getPluginInfo().getStringValue(INPRPluginConst.DATA_SRC)
					.indexOf(INPRPluginConst.DATA_SRC_NC_WARNINGSQL) < 0)
				hsvo.setAttributeValue(
						INPRPluginConst.PLUGIN_COLUMN_REMOTEADDR, vo
								.getMsgStringValueByField(
										INPRPluginConst.NC_MW_REMOTEADDR, null));
		}
	}

	private void dealTimeRange(File file, HashVO hsvo) {
		if (this.showTimeRange) {
			String path = file.getPath();
			boolean isNC = false;
			boolean isWnsql = false;
			if (path.indexOf(INPRConst.NC_LOG_DIR_NAME) > -1)
				isNC = true;
			String server = null;
			UFDateTime begin = null;
			UFDateTime end = null;
			if (isNC) {
				server = file.getParentFile().getName();
				isWnsql = path.indexOf(INPRConst.WNGSQL_FILE_NAME) > -1;
				if (isWnsql) {
					begin = NPRGlobalControl.getInstance()
							.getServerNCWNSQLBeginTime(server);
					end = NPRGlobalControl.getInstance()
							.getServerNCWNSQLEndTime(server);
				} else {
					begin = NPRGlobalControl.getInstance()
							.getServerNCMWBeginTime(server);
					end = NPRGlobalControl.getInstance().getServerNCMWEndTime(
							server);
				}
			} else {
				String name = file.getName();
				if (name.indexOf("-") > -1) {
					server = name.substring(0, name.indexOf("-"));
					begin = NPRGlobalControl.getInstance()
							.getServerNMCMWBeginTime(server);
					end = NPRGlobalControl.getInstance().getServerNMCMWEndTime(
							server);
				}
			}
			if (!Toolkit.isEmpty(begin) && !Toolkit.isEmpty(end)) {
				hsvo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_BEGINTIME,
						begin);
				hsvo.setAttributeValue(INPRPluginConst.PLUGIN_COLUMN_ENDTIME,
						end);
			}
		}
	}

	/**
	 * @param avgColName
	 *            the avgColName to set
	 */
	public void setAvgColName(String avgColName) {
		this.avgColName = avgColName;
		this.showAvgCol = true;
	}

	public void setBusiActionColIsMsg(boolean busiActionColIsMsg) {
		this.busiActionColIsMsg = busiActionColIsMsg;
	}

	/**
	 * @param busiActionColName
	 *            the busiActionColName to set
	 */
	protected void setBusiActionColName(String busiActionShowName,
			String busiActionColName, boolean busiActionColIsMsg) {
		this.busiActionShowName = busiActionShowName;
		this.busiActionColName = busiActionColName;
		this.setBusiActionColIsMsg(busiActionColIsMsg);
	}

	/**
	 * @param countByGroup
	 *            the countByGroup to set
	 */
	protected void setCountByGroup(boolean countByGroup) {
		this.countByGroup = countByGroup;
	}

	/**
	 * @param countColName
	 *            the countColName to set
	 */
	protected void setCountColName(String countColName) {
		this.countColName = countColName;
	}

	/**
	 * @param isDealMaster
	 *            the isDealMaster to set
	 */
	protected void setDealMaster(boolean isDealMaster) {
		this.isDealMaster = isDealMaster;
	}

	/**
	 * @param isDealServers
	 *            the isDealServers to set
	 */
	public void setDealServers(boolean isDealServers) {
		this.isDealServers = isDealServers;
	}

	/**
	 * @param groupColName
	 *            the groupColName to set
	 */
	protected void setGroupColName(String groupColName, boolean groupColIsMsg) {
		this.groupColName = groupColName;
		this.groupColIsMsg = groupColIsMsg;
	}

	/**
	 * @param msgToken
	 *            the msgToken to set
	 */
	protected void setMsgToken(String msgToken) {
		this.msgToken = msgToken;
	}

	/**
	 * @param showCallid
	 *            the showCallid to set
	 */
	public void setShowCallid(boolean showCallid) {
		this.showCallid = showCallid;
	}

	/**
	 * @param showColName
	 *            the showColName to set
	 */
	protected void setShowColName(String showColName, boolean showColIsMsg) {
		this.showColName = showColName;
		this.showColIsMsg = showColIsMsg;
	}

	/**
	 * @param showMethod
	 *            the showMethod to set
	 */
	protected void setShowMethod(boolean showMethod) {
		this.showMethodCol = showMethod;
	}

	/**
	 * @param showTimeRange
	 *            the showTimeRange to set
	 */
	public void setShowTimeRange(boolean showTimeRange) {
		this.showTimeRange = showTimeRange;
	}

	/**
	 * @param showUserid
	 *            the showUserid to set
	 */
	public void setShowUserid(boolean showUserid) {
		this.showUserid = showUserid;
	}

	/**
	 * @param isSum
	 *            the isSum to set
	 */
	protected void setSum(boolean isSum) {
		this.isSum = isSum;
	}

	/**
	 * @param valueColName
	 *            the valueColName to set
	 */
	protected void setValueColName(String valueColName, boolean valueColIsMsg) {
		this.valueColName = valueColName;
		this.valueColIsMsg = valueColIsMsg;
	}

}
