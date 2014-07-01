package uap.ps.itm.npr.service.log.resolve;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import nc.monitor.Exception.RemoteException;
import nc.monitor.pub.HashVO;
import nc.monitor.pub.LogTool;
import nc.monitor.pub.Toolkit;
import nc.monitor.pub.Xml2HashVO;
import nc.monitor.service.center.ServerEnv;
import uap.ps.itm.npr.service.INPRConst;
import uap.ps.itm.npr.service.NPRGlobalControl;
import uap.ps.itm.npr.service.plugin.BusyBusiActionTop10;
import uap.ps.itm.npr.service.plugin.CallDbNumsTop10;
import uap.ps.itm.npr.service.plugin.CallMasterNumsTop10;
import uap.ps.itm.npr.service.plugin.DBSummaryPluginImpl;
import uap.ps.itm.npr.service.plugin.DatabaseWorkLoadChartImpl;
import uap.ps.itm.npr.service.plugin.INPRPluginConst;
import uap.ps.itm.npr.service.plugin.INPRPluginItf;
import uap.ps.itm.npr.service.plugin.JvmMemoryChartImpl;
import uap.ps.itm.npr.service.plugin.JvmRespCostChartImpl;
import uap.ps.itm.npr.service.plugin.JvmThroughputChartImpl;
import uap.ps.itm.npr.service.plugin.JvmWebThreadChartImpl;
import uap.ps.itm.npr.service.plugin.MWCpuTimePluginImpl;
import uap.ps.itm.npr.service.plugin.MWSummaryPluginImpl;
import uap.ps.itm.npr.service.plugin.MsgSnapshotPageImpl;
import uap.ps.itm.npr.service.plugin.MsgSnapshotPlugImpl;
import uap.ps.itm.npr.service.plugin.NetworkTrafficPluginImpl;
import uap.ps.itm.npr.service.plugin.OnlineUserChartImpl;
import uap.ps.itm.npr.service.plugin.OnlineUserTableImpl;
import uap.ps.itm.npr.service.plugin.ReadOrWriteOver1MImpl;
import uap.ps.itm.npr.service.plugin.RemoteCallConnNotClosePluginImpl;
import uap.ps.itm.npr.service.plugin.RemoteCallCostTimeTop10;
import uap.ps.itm.npr.service.plugin.RemoteCallReadTrafficTop10;
import uap.ps.itm.npr.service.plugin.RemoteCallResultTop10;
import uap.ps.itm.npr.service.plugin.RemoteCallSQLNumsTop10;
import uap.ps.itm.npr.service.plugin.RemoteCallSQLTimesOverLimit;
import uap.ps.itm.npr.service.plugin.RemoteCallTimesOverLimit;
import uap.ps.itm.npr.service.plugin.RemoteCallWriteTrafficTop10;
import uap.ps.itm.npr.service.plugin.RequestMasterTop10;
import uap.ps.itm.npr.service.plugin.SQLCostTimeTop10;
import uap.ps.itm.npr.service.plugin.SQLExecuteSummaryImpl;
import uap.ps.itm.npr.service.plugin.SQLResultTop10;
import uap.ps.itm.npr.service.plugin.ServersThroughputPluginImpl;
import uap.ps.itm.npr.service.plugin.SystemMonitorPluginImpl;
import uap.ps.itm.npr.service.plugin.ThroughputPluginImpl;
import uap.ps.itm.npr.service.util.NPRUtils;
import uap.ps.itm.npr.vo.XPRPluginResult;
import nc.vo.pub.lang.UFDateTime;

/**
 * 日志解析
 *
 * @author liuzy
 *
 */
public class LogResolve {

	private static Logger logger1 = null;

	public Logger getLogger() {
		synchronized (this) {
			if (logger1 == null) {
				logger1 = LogTool.createLogger("npr", "npr", "pluginlog.idx");
			}
		}
		return logger1;
	}

	/**
	 * 解析日志并返回所有插件结果
	 *
	 * @param shotPath
	 * @return
	 */
	public XPRPluginResult[] resolveByFiles(String shotPath,
			UFDateTime beginTime, UFDateTime endTime) {
		System.out.println("开始生成报告：路径=" + shotPath);
		System.out.println("开始生成报告：起始时间=" + beginTime);
		System.out.println("开始生成报告：结束时间=" + endTime);
		HashVO[] pluginInfos = this.pluginSort(this.getNPRPlugins());
		List<XPRPluginResult> resultList = new ArrayList<XPRPluginResult>();
		String currDataSrc = null;
		String tempDataSrc = null;
		File[] currFlies = null;
		XPRPluginResult result = null;
		String implClass = null;
		for (HashVO vo : pluginInfos) {
			int showLevel = vo.getIntegerValue(INPRPluginConst.SHOW_LEVEL);
			if (showLevel == 0) {
				result = new XPRPluginResult(vo);
				resultList.add(result);
				continue;
			}
			implClass = vo.getStringValue(INPRPluginConst.IMPL_CLASS);
			// if (!"uap.ps.itm.npr.service.plugin.SQLCostTimeTop10".equals(implClass))
			// continue;
			// if (implClass.indexOf("ChartImpl") < 0)
			// continue;
			try {
				// Class<?> c = Class.forName(implClass);
				INPRPluginItf itf = this.getPluginImpl(implClass);// (INPRPluginItf)
																	// c.newInstance();
																	// if (null
																	// == itf)
				// continue;
				System.out.println("NPR plugin begin : " + implClass);
				itf.setPluginInfo(vo);
				itf.setTimeRange(beginTime, endTime);
				itf.setSnapshotPaht(shotPath);
				tempDataSrc = vo.getStringValue(INPRPluginConst.DATA_SRC);
				if (Toolkit.isEmpty(tempDataSrc))
					tempDataSrc = INPRPluginConst.DATA_SRC_NULL;
				if (!tempDataSrc.equals(currDataSrc)) {
					currDataSrc = tempDataSrc;
					currFlies = this.getDataFiles(itf, tempDataSrc, shotPath,
							beginTime, endTime);
				}
				if (Toolkit.isEmpty(currFlies)) {
					result = itf.getResultVO(vo);
				} else {
					for (int i = 0; i < currFlies.length; i++) {
						if (currFlies[i].isDirectory())
							continue;
						HashVO[] fileDataVOs = this.getDataVOs(currFlies[i]);
						itf.process(currFlies[i], fileDataVOs);
						fileDataVOs = null;
					}
					result = itf.getResultVO();
				}
				if (Toolkit.isEmpty(result)) {
					result = new XPRPluginResult(vo);
				}
				resultList.add(result);
				itf = null;
				System.out.println("NPR plugin end : " + implClass);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this.resultSortByShowOrder(resultList);
	}

	/**
	 * 解析日志并返回所有插件结果
	 *
	 * @param shotPath
	 * @return
	 */
	public XPRPluginResult[] resolveByFilesOneTime(String shotPath,
			UFDateTime beginTime, UFDateTime endTime) throws RemoteException {
		System.out.println("开始生成报告：路径=" + shotPath);
		System.out.println("开始生成报告：起始时间=" + beginTime);
		System.out.println("开始生成报告：结束时间=" + endTime);
		boolean needGetBeginTime = false;
		boolean needGetEndTime = false;

		Map<String, Long[]> map_time = new HashMap<String, Long[]>();
		Map<String, List<HashVO>> map = this.getPluginSortMap(getNPRPlugins());
		List<XPRPluginResult> resultList = new ArrayList<XPRPluginResult>();
		File[] currFlies = null;
		XPRPluginResult result = null;
		String implClass = null;
		String fileName = null;
		try {
			NPRGlobalControl.getInstance().initServerTimes(shotPath, beginTime,
					endTime);
			NPRGlobalControl.getInstance().initNPRFilter();
			for (Map.Entry<String, List<HashVO>> entry : map.entrySet()) {
				if (Toolkit.isEmpty(beginTime)
						|| Toolkit.isEmpty(beginTime.toString())) {
					beginTime = new UFDateTime();
					needGetBeginTime = true;
				}

				if (Toolkit.isEmpty(endTime)
						|| Toolkit.isEmpty(endTime.toString())) {
					endTime = new UFDateTime(new Date());
					needGetEndTime = true;
				}
				String data_src = entry.getKey();
				List<HashVO> list = entry.getValue();
				currFlies = this.getDataFiles(data_src, shotPath, beginTime,
						endTime);
				if (Toolkit.isEmpty(currFlies)
						|| data_src.equals(INPRPluginConst.DATA_SRC_NULL)) {
					for (HashVO pluginInfo : list) {
						int showLevel = pluginInfo
								.getIntegerValue(INPRPluginConst.SHOW_LEVEL);
						if (showLevel == 0) {
							result = new XPRPluginResult(pluginInfo);
							resultList.add(result);
							continue;
						}
						implClass = pluginInfo
								.getStringValue(INPRPluginConst.IMPL_CLASS);
						INPRPluginItf itf = this.getPluginImpl(implClass);
						System.out.println("NPR plugin begin : " + implClass);
						itf.setPluginInfo(pluginInfo);
						itf.setTimeRange(beginTime, endTime);
						itf.setSnapshotPaht(shotPath);
						long begin = System.currentTimeMillis();
						result = itf.getResultVO(pluginInfo);
						if (Toolkit.isEmpty(result))
							result = new XPRPluginResult(pluginInfo);
						long end = System.currentTimeMillis();
						calcTime(map_time, implClass, begin, end, false);
						resultList.add(result);
						System.out.println("NPR plugin end : " + implClass);
					}
					continue;
				}

				boolean needTime = this.needCalcBeginEndTime(data_src);

				if (needGetBeginTime && needTime)
					beginTime = NPRUtils.getBeginTime(currFlies, null, null);

				if (needGetEndTime && needTime)
					endTime = NPRUtils.getEndTime(currFlies, null, null);

				Map<String, INPRPluginItf> map_itf = new HashMap<String, INPRPluginItf>();
				for (File file : currFlies) {
					if (file.isDirectory())
						continue;
					fileName = file.getName();
					System.out.println("process file : " + fileName);
					HashVO[] fileDataVOs = this.getDataVOs(file);
					if (Toolkit.isEmpty(fileDataVOs)
							&& file.getParentFile()
									.getName()
									.indexOf(
											INPRConst.NMC_LOG_SNAPSHOT_DIR_NAME) < 0)
						continue;
					for (HashVO pluginInfo : list) {
						implClass = pluginInfo
								.getStringValue(INPRPluginConst.IMPL_CLASS);
						String para1 = pluginInfo
								.getStringValue(INPRPluginConst.PARA1);
						String key = implClass;
						if (!Toolkit.isEmpty(para1))
							key += para1;
						System.out.println("NPR plugin process begin : "
								+ implClass);
						INPRPluginItf itf = null;
						if (map_itf.containsKey(key))
							itf = map_itf.get(key);
						else {
							itf = this.getPluginImpl(implClass.trim());
							itf.setPluginInfo(pluginInfo);
							itf.setTimeRange(beginTime, endTime);
							itf.setSnapshotPaht(shotPath);
							map_itf.put(key, itf);
						}
						long begin = System.currentTimeMillis();
						itf.process(file, fileDataVOs);
						long end = System.currentTimeMillis();
						calcTime(map_time, implClass, begin, end, true);
						System.out.println("NPR plugin process end : "
								+ implClass);
					}
				}
				for (Map.Entry<String, INPRPluginItf> e : map_itf.entrySet()) {
					System.out.println("NPR plugin get result begin : "
							+ e.getKey());
					long begin = System.currentTimeMillis();
					if (e.getValue() instanceof SystemMonitorPluginImpl) {
						XPRPluginResult[] specialresults = ((SystemMonitorPluginImpl) e
								.getValue()).getSpecialResultVO();
						long end = System.currentTimeMillis();
						calcTime(map_time, e.getKey(), begin, end, false);
						if (specialresults != null)
 {
							for (XPRPluginResult special : specialresults) {
								special.setPluginInfo(list.get(0));// 代码真的很难读
							}
						resultList.addAll(Arrays.asList(specialresults));
						}
					} else if (e.getValue() instanceof DatabaseWorkLoadChartImpl) {
						XPRPluginResult[] specialresults = ((DatabaseWorkLoadChartImpl) e
								.getValue()).getSpecialResultVO();
						long end = System.currentTimeMillis();
						calcTime(map_time, e.getKey(), begin, end, false);
						if (specialresults != null) {
							for (XPRPluginResult special : specialresults) {
								special.setPluginInfo(list.get(0));// 代码真的很难读
							}
							resultList.addAll(Arrays.asList(specialresults));
						}

					} else {
					result = e.getValue().getResultVO();
					if (Toolkit.isEmpty(result)) {
						result = new XPRPluginResult(e.getValue()
								.getPluginInfo());
					}
					long end = System.currentTimeMillis();
					calcTime(map_time, e.getKey(), begin, end, false);
					resultList.add(result);
					System.out.println("NPR plugin get result end : "
							+ e.getKey());
					}
				}
			}
			for (Map.Entry<String, Long[]> entry : map_time.entrySet()) {
				HashVO vo = new HashVO();
				vo.setAttributeValue("ts", Toolkit.getCurTime());
				vo.setAttributeValue(entry.getKey() + " process time ",
						entry.getValue()[0] + "ms");
				vo.setAttributeValue(entry.getKey() + " get result time ",
						entry.getValue()[1] + "ms");
				this.getLogger().info(vo.toString0());
			}
			return this.resultSortByShowOrder(resultList);
		} catch (Exception ex) {
			System.out.println("插件【" + implClass + "】 在处理文件【" + fileName
					+ "】时报错");
			ex.printStackTrace();
			throw new RemoteException(ex.getMessage());
		}
	}

	private void calcTime(Map<String, Long[]> map_time, String implClass,
			long begin, long end, boolean isProc) {
		Long[] l = null;
		if (!map_time.containsKey(implClass)) {
			l = new Long[2];
			l[0] = 0L;
			l[1] = 0L;
			map_time.put(implClass, l);
		}
		l = map_time.get(implClass);
		if (isProc) {
			long p = l[0];
			l[0] = end - begin + p;
		} else {
			long p = l[1];
			l[1] = end - begin + p;
		}
		map_time.put(implClass, l);
	}

	public XPRPluginResult getNMCSnapshotResult(String shotPath,
			UFDateTime beginTime, UFDateTime endTime, String destDirName) {
		// HashVO[] pluginInfos = this.pluginSort(this.getNPRPlugins());
		// HashVO plugin = null;
		// for (HashVO vo : pluginInfos) {
		// if (vo.getStringValue(INPRPluginConst.IMPL_CLASS).equals(
		// "uap.ps.itm.npr.service.plugin.MsgSnapshotPageImpl")) {
		// plugin = vo;
		// break;
		// }
		// }
		MsgSnapshotPageImpl impl = new MsgSnapshotPageImpl();
		// impl.setPluginInfo(plugin);
		impl.setTimeRange(beginTime, endTime);
		impl.setSnapshotPaht(shotPath);
		File[] files = this.getDataFiles(impl,
				INPRPluginConst.DATA_SRC_NMC_SNAPSHOT, shotPath, beginTime,
				endTime);
		if (Toolkit.isEmpty(files))
			return null;
		for (int i = 0; i < files.length; i++)
			impl.process(destDirName, files[i], this.getDataVOs(files[i]));
		return null;
	}

	private XPRPluginResult[] resultSortByShowOrder(
			List<XPRPluginResult> resultList) {
		XPRPluginResult[] rets = resultList
				.toArray(new XPRPluginResult[resultList.size()]);
		Toolkit.sort(rets, new PluginSortCompartor());
		return rets;
	}

	private class PluginSortCompartor implements Comparator<XPRPluginResult> {



		@Override
		public int compare(XPRPluginResult o1, XPRPluginResult o2) {
			int showOrder1 = o1.getPluginInfo().getIntegerValue(
					INPRPluginConst.SHOW_ORDER);
			int showOrder2 = o2.getPluginInfo().getIntegerValue(
					INPRPluginConst.SHOW_ORDER);
			if (showOrder1 == showOrder2)
				return 0;
			else if (showOrder1 > showOrder2)
				return 1;
			else if (showOrder1 < showOrder2)
				return -1;
			return 0;
		}

	}

	private File[] getDataFiles(INPRPluginItf itf, String dataSrc,
			String shotPath, UFDateTime beginTime, UFDateTime endTime) {
		if (dataSrc.equals(INPRPluginConst.DATA_SRC_NMC_MW))
			return new LogExtract().getFiles(itf.getNMCLogPath_MW(), beginTime,
					endTime);
		else if (dataSrc.equals(INPRPluginConst.DATA_SRC_NMC_DB))
			return new LogExtract().getFiles(itf.getNMCLogPath_DB(), beginTime,
					endTime);
		else if (dataSrc.equals(INPRPluginConst.DATA_SRC_NMC_ONLINEUSER))
			return new LogExtract().getFiles(itf.getNMCLogPath_OnlineUser(),
					beginTime, endTime);
		else if (dataSrc.equals(INPRPluginConst.DATA_SRC_NPR_MWINFO))
			return new LogExtract().getFiles(shotPath, beginTime, endTime);
		else if (dataSrc.equals(INPRPluginConst.DATA_SRC_NPR_DBINFO))
			return new LogExtract().getFiles(shotPath, beginTime, endTime);
		else if (dataSrc.equals(INPRPluginConst.DATA_SRC_NC_MW))
			return new LogExtract().getFiles(itf.getNCLogPath(),
					INPRConst.MWSMRY_FILE_NAME, beginTime, endTime);
		else if (dataSrc.equals(INPRPluginConst.DATA_SRC_NC_WARNINGSQL))
			return new LogExtract().getFiles(itf.getNCLogPath(),
					INPRConst.WNGSQL_FILE_NAME, beginTime, endTime);
		else if (dataSrc.equals(INPRPluginConst.DATA_SRC_NC_MASTER_MW))
			return new LogExtract().getFiles(itf.getNCLogPath(),
					INPRConst.MWSMRY_FILE_NAME, beginTime, endTime);
		else if (dataSrc.equals(INPRPluginConst.DATA_SRC_NMC_SNAPSHOT))
			return new LogExtract().getFiles(itf.getNMCLogPath_Snapshot(),
					null, beginTime, endTime);
		else if (dataSrc.equals(INPRPluginConst.DATA_SRC_NULL))
			return null;
		return null;
	}

	private File[] getDataFiles(String dataSrc, String shotPath,
			UFDateTime beginTime, UFDateTime endTime) {
		NPRUtils util = new NPRUtils(shotPath, beginTime, endTime);
		if (dataSrc.equals(INPRPluginConst.DATA_SRC_NMC_MW))
			return new LogExtract().getFiles(util.getNMCLogPath_MW(),
					beginTime, endTime);
		else if (dataSrc.equals(INPRPluginConst.DATA_SRC_NMC_DB))
			return new LogExtract().getFiles(util.getNMCLogPath_DB(),
					beginTime, endTime);
		else if (dataSrc.equals(INPRPluginConst.DATA_SRC_NMC_ONLINEUSER))
			return new LogExtract().getFiles(util.getNMCLogPath_OnlineUser(),
					beginTime, endTime);
		else if (dataSrc.equals(INPRPluginConst.DATA_SRC_NPR_MWINFO))
			return new LogExtract().getFiles(shotPath, beginTime, endTime);
		else if (dataSrc.equals(INPRPluginConst.DATA_SRC_NPR_DBINFO))
			return new LogExtract().getFiles(shotPath, beginTime, endTime);
		else if (dataSrc.equals(INPRPluginConst.DATA_SRC_NC_MW))
			return new LogExtract().getFiles(util.getNCLogPath(),
					INPRConst.MWSMRY_FILE_NAME, beginTime, endTime);
		else if (dataSrc.equals(INPRPluginConst.DATA_SRC_NC_WARNINGSQL))
			return new LogExtract().getFiles(util.getNCLogPath(),
					INPRConst.WNGSQL_FILE_NAME, beginTime, endTime);
		else if (dataSrc.equals(INPRPluginConst.DATA_SRC_NC_MASTER_MW))
			return new LogExtract().getFiles(util.getNCLogPath(),
					INPRConst.MWSMRY_FILE_NAME, beginTime, endTime);
		else if (dataSrc.equals(INPRPluginConst.DATA_SRC_NMC_SNAPSHOT))
			return new LogExtract().getFiles(util.getNMCLogPath_Snapshot(),
					null, beginTime, endTime);
		else if (dataSrc.equals(INPRPluginConst.DATA_SRC_NAME_SYSTEMMONITOR))
			return new LogExtract().getFiles(
					util.getNMCLogPath_Systemmonitor(), beginTime, endTime);
		else if (dataSrc.equals(INPRPluginConst.DATA_SRC_NULL))
			return null;
		return null;
	}

	private HashVO[] getDataVOs(File file) {
		return new LogExtract().getHashVOsByFile(file);
	}

	/**
	 * 获得所有NPR报告的插件
	 *
	 * @return
	 */
	private HashVO[] getNPRPlugins() {
		HashVO[] vos = null;
		String plugins_url = null;
		if (ServerEnv.getInstance().isDev())
			plugins_url = "D:/NC_UAP_MODULES/nmc/server/conf/nprplugin.xml";
		else
			plugins_url = "./conf/itmplugin.xml";
		try {
			vos = Xml2HashVO.convertToVO(plugins_url,
					INPRConst.NPR_PLUGIN_NODE_NAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return vos;
	}

	/**
	 * 插件排序，按照插件数据来源顺序重新排列
	 *
	 * @param plugins
	 * @return
	 */
	private HashVO[] pluginSort(HashVO[] plugins) {
		Map<String, List<HashVO>> map = new HashMap<String, List<HashVO>>();
		String data_src = null;
		List<HashVO> list = null;
		for (HashVO vo : plugins) {
			data_src = vo.getStringValue(INPRPluginConst.DATA_SRC);
			if (Toolkit.isEmpty(data_src))
				data_src = INPRPluginConst.DATA_SRC_NULL;
			list = map.get(data_src);
			if (null == list) {
				list = new ArrayList<HashVO>();
				map.put(data_src, list);
			}
			list.add(vo);
		}
		List<HashVO> retList = new ArrayList<HashVO>();
		for (List<HashVO> l : map.values())
			retList.addAll(l);
		return retList.toArray(new HashVO[retList.size()]);
	}

	/**
	 * 插件排序，按照插件数据来源顺序重新排列
	 *
	 * @param plugins
	 * @return
	 */
	private Map<String, List<HashVO>> getPluginSortMap(HashVO[] plugins) {
		Map<String, List<HashVO>> map = new HashMap<String, List<HashVO>>();
		String data_src = null;
		List<HashVO> list = null;
		for (HashVO vo : plugins) {
			data_src = vo.getStringValue(INPRPluginConst.DATA_SRC);
			if (Toolkit.isEmpty(data_src))
				data_src = INPRPluginConst.DATA_SRC_NULL;
			list = map.get(data_src);
			if (null == list) {
				list = new ArrayList<HashVO>();
				map.put(data_src, list);
			}
			list.add(vo);
		}
		return map;
	}

	private INPRPluginItf getPluginImpl(String implName) {
		implName = implName.trim();
		if ("uap.ps.itm.npr.service.plugin.MWSummaryPluginImpl".equals(implName))
			return new MWSummaryPluginImpl();
		else if ("uap.ps.itm.npr.service.plugin.SystemMonitorPluginImpl"
				.equals(implName))
			return new SystemMonitorPluginImpl();
		else if ("uap.ps.itm.npr.service.plugin.DBSummaryPluginImpl".equals(implName))
			return new DBSummaryPluginImpl();
		else if ("uap.ps.itm.npr.service.plugin.ServersThroughputPluginImpl"
				.equals(implName))
			return new ServersThroughputPluginImpl();
		else if ("uap.ps.itm.npr.service.plugin.ThroughputPluginImpl".equals(implName))
			return new ThroughputPluginImpl();
		else if ("uap.ps.itm.npr.service.plugin.ServersThroughputPluginImpl"
				.equals(implName))
			return new ServersThroughputPluginImpl();
		else if ("uap.ps.itm.npr.service.plugin.MWCpuTimePluginImpl".equals(implName))
			return new MWCpuTimePluginImpl();
		else if ("uap.ps.itm.npr.service.plugin.NetworkTrafficPluginImpl"
				.equals(implName))
			return new NetworkTrafficPluginImpl();
		else if ("uap.ps.itm.npr.service.plugin.DatabaseWorkLoadChartImpl"
				.equals(implName))
			return new DatabaseWorkLoadChartImpl();
		else if ("uap.ps.itm.npr.service.plugin.SQLExecuteSummaryImpl".equals(implName))
			return new SQLExecuteSummaryImpl();
		else if ("uap.ps.itm.npr.service.plugin.OnlineUserChartImpl".equals(implName))
			return new OnlineUserChartImpl();
		else if ("uap.ps.itm.npr.service.plugin.OnlineUserTableImpl".equals(implName))
			return new OnlineUserTableImpl();
		else if ("uap.ps.itm.npr.service.plugin.RemoteCallCostTimeTop10"
				.equals(implName))
			return new RemoteCallCostTimeTop10();
		else if ("uap.ps.itm.npr.service.plugin.RemoteCallWriteTrafficTop10"
				.equals(implName))
			return new RemoteCallWriteTrafficTop10();
		else if ("uap.ps.itm.npr.service.plugin.RemoteCallReadTrafficTop10"
				.equals(implName))
			return new RemoteCallReadTrafficTop10();
		else if ("uap.ps.itm.npr.service.plugin.RemoteCallResultTop10".equals(implName))
			return new RemoteCallResultTop10();
		else if ("uap.ps.itm.npr.service.plugin.BusyBusiActionTop10".equals(implName))
			return new BusyBusiActionTop10();
		else if ("uap.ps.itm.npr.service.plugin.SQLCostTimeTop10".equals(implName))
			return new SQLCostTimeTop10();
		else if ("uap.ps.itm.npr.service.plugin.SQLResultTop10".equals(implName))
			return new SQLResultTop10();
		else if ("uap.ps.itm.npr.service.plugin.RequestMasterTop10".equals(implName))
			return new RequestMasterTop10();
		else if ("uap.ps.itm.npr.service.plugin.CallMasterNumsTop10".equals(implName))
			return new CallMasterNumsTop10();
		else if ("uap.ps.itm.npr.service.plugin.CallDbNumsTop10".equals(implName))
			return new CallDbNumsTop10();
		else if ("uap.ps.itm.npr.service.plugin.RemoteCallSQLNumsTop10"
				.equals(implName))
			return new RemoteCallSQLNumsTop10();
		else if ("uap.ps.itm.npr.service.plugin.RemoteCallConnNotClosePluginImpl"
				.equals(implName))
			return new RemoteCallConnNotClosePluginImpl();
		else if ("uap.ps.itm.npr.service.plugin.RemoteCallSQLTimesOverLimit"
				.equals(implName))
			return new RemoteCallSQLTimesOverLimit();
		else if ("uap.ps.itm.npr.service.plugin.ReadOrWriteOver1MImpl".equals(implName))
			return new ReadOrWriteOver1MImpl();
		else if ("uap.ps.itm.npr.service.plugin.JvmMemoryChartImpl".equals(implName))
			return new JvmMemoryChartImpl();
		else if ("uap.ps.itm.npr.service.plugin.JvmRespCostChartImpl".equals(implName))
			return new JvmRespCostChartImpl();
		else if ("uap.ps.itm.npr.service.plugin.JvmThroughputChartImpl"
				.equals(implName))
			return new JvmThroughputChartImpl();
		else if ("uap.ps.itm.npr.service.plugin.MsgSnapshotPlugImpl".equals(implName))
			return new MsgSnapshotPlugImpl();
		else if ("uap.ps.itm.npr.service.plugin.RemoteCallTimesOverLimit"
				.equals(implName))
			return new RemoteCallTimesOverLimit();
		else if ("uap.ps.itm.npr.service.plugin.JvmWebThreadChartImpl".equals(implName))
			return new JvmWebThreadChartImpl();
		return null;
	}

	private boolean needCalcBeginEndTime(String data_src) {
		if (data_src.equals(INPRPluginConst.DATA_SRC_NC_MW)
				|| data_src.equals(INPRPluginConst.DATA_SRC_NC_WARNINGSQL)
				|| data_src.equals(INPRPluginConst.DATA_SRC_NMC_MW))
			return false;
		else
			return true;
	}

}
