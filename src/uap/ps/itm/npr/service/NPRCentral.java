package uap.ps.itm.npr.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Logger;

import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.lang.UFDateTime;
import uap.ps.itm.monitor.RemoteException;
import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.LogTool;
import uap.ps.itm.monitor.pub.Text2HashVO;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.center.IServer;
import uap.ps.itm.monitor.service.center.ServerEnv;
import uap.ps.itm.monitor.service.center.ServiceDesc;
import uap.ps.itm.monitor.service.center.ServiceResource;
import uap.ps.itm.monitor.service.filetransfer.FileInfo;
import uap.ps.itm.monitor.service.loganalyze.Filter;
import uap.ps.itm.monitor.service.message.MailSender;
import uap.ps.itm.monitor.service.message.MailSenderInfo;
import uap.ps.itm.monitor.service.message.SimpleMailSender;
import uap.ps.itm.npr.service.log.collect.FileCollector;
import uap.ps.itm.npr.service.log.collect.FileInfoService;
import uap.ps.itm.npr.service.log.resolve.LogExtract;
import uap.ps.itm.npr.service.log.resolve.LogResolve;
import uap.ps.itm.npr.service.report.NPRReportBuilder;
import uap.ps.itm.npr.service.schedule.NPRScheduleConfig;
import uap.ps.itm.npr.service.schedule.NPRScheduleThread;
import uap.ps.itm.npr.vo.FileVO;
import uap.ps.itm.npr.vo.XPRFileVO;

public class NPRCentral implements IServer {

	public static ServiceResource resource = null;
	private static Logger logger1 = null;
	private static Logger logger2 = null;

	private NPRScheduleThread thread1;
	private NPRScheduleThread thread2;

	public void clearNPR() {
		String snapshotPath = ServerEnv.getInstance().getProperty(
				INPRConst.NPR_DEF_LOG);
		String nprPath = null;
		if (ServerEnv.getInstance().isDev())
			nprPath = "d:/dev_env/20120911/nmc/server/webapps/npr/";
		else
			nprPath = "./webapps/npr/";
		String nprLogPath = INPRConst.NMC_LOG_DIR + "/npr";// "E:/view/NC_UAP_FW6.1_dev/NC6_UAP_VOB/NC_UAP_FW/monitor/logs/npr";
		Toolkit.delDir(snapshotPath);
		File[] files = new File(nprPath).listFiles();
		for (File f : files)
			Toolkit.delDir(f.getPath());
		this.closeLoggerHandler(getLogLogger());
		this.closeLoggerHandler(getReportLogger());
		this.closeLoggerHandler(new LogResolve().getLogger());
		Toolkit.delDir(nprLogPath);
		NPRCentral.logger1 = null;
		NPRCentral.logger2 = null;
	}

	public void clearNPRByDays() {
		int days = Integer.valueOf(this.getCurrNprCfg().getStringValue(
				INPRConst.NPR_CONFIG_CLEAR_SAVE_DAYS));
		delDirByDays(days, INPRConst.NPR_CLEAR_DEL_SNAPSHOT);
		delDirByDays(days, INPRConst.NPR_CLEAR_DEL_REPORT);
	}

	private void delDirByDays(int days, String delType) {
		String path = null;
		if (delType.equals(INPRConst.NPR_CLEAR_DEL_SNAPSHOT))
			path = ServerEnv.getInstance().getProperty(INPRConst.NPR_DEF_LOG);
		else if (delType.equals(INPRConst.NPR_CLEAR_DEL_REPORT))
			path = "./webapps/npr/";
		File file = new File(path);
		File[] files = file.listFiles();
		if (Toolkit.isEmpty(files))
			return;
		UFDateTime currTime = new UFDateTime(new Date());
		long clearTime = currTime.getDateBefore(days - 1).getMillis();
		for (File f : files) {
			long lastModify = f.lastModified();
			if (lastModify < clearTime)
				Toolkit.delDir(f.getPath());
		}

	}

	private void closeLoggerHandler(Logger logger) {
		Handler[] handlers = logger.getHandlers();
		for (int i = 0; i < handlers.length; i++) {
			handlers[i].close();
		}
	}

	public void createReportIndexPage() {
		HashVO[][] logvos = getNprlog();
		if (Toolkit.isEmpty(logvos))
			return;
		HashVO[] snapshotvos = logvos[0];
		if (Toolkit.isEmpty(snapshotvos))
			return;
		HashVO[] reportvos = logvos[1];
		if (Toolkit.isEmpty(reportvos))
			return;
		// Map<String, List<HashVO>> map = new HashMap<String, List<HashVO>>();
		// for (HashVO vo : snapshotvos)
		// map.put(vo.getStringValue(INPRConst.NPR_COL_SNAPSHOT_ADDR), null);
		// for (HashVO vo : reportvos) {
		// String snapshotid = vo
		// .getStringValue(INPRConst.NPR_COL_SNAPSHOT_ADDR);
		// List<HashVO> list = map.get(snapshotid);
		// if (Toolkit.isEmpty(list)) {
		// list = new ArrayList<HashVO>();
		// map.put(snapshotid, list);
		// }
		// list.add(vo);
		// }
		new NPRReportBuilder().buildReportIndex(reportvos);
	}

	public String creatReport(String shotPath, UFDateTime beginTime,
			UFDateTime endTime) throws RemoteException {
		boolean bOnOperat = NPRGlobalControl.getInstance().getOperatLock(
				NPRGlobalControl.NPR_CREATE_REPORT_LOCK);
		if (bOnOperat)
			throw new RemoteException("另一个【生成报告】任务正在执行,请稍后再试！");
		shotPath = ServerEnv.getInstance().getProperty(INPRConst.NPR_DEF_LOG)
				+ "/" + shotPath;
		try {
			long t1 = System.currentTimeMillis();
			String reportPath = new NPRReportBuilder().buildReport(shotPath,
					beginTime, endTime);
			long t2 = System.currentTimeMillis();
			if (Toolkit.isEmpty(beginTime)
					|| Toolkit.isEmpty(beginTime.toString()))
				beginTime = new UFDateTime();
			if (Toolkit.isEmpty(endTime) || Toolkit.isEmpty(endTime.toString()))
				endTime = new UFDateTime(new Date());
			String logPath = shotPath.substring(shotPath.lastIndexOf("/") + 1,
					shotPath.length());
			String ipAddr = ServerEnv.getLocalAddr();
			String port = ServerEnv.getInstance().getLocalSocket().getPort();
			reportPath = "http://" + ipAddr + ":" + port + "/npr/" + reportPath
					+ "/npr.html";
			// this.sendMail(reportPath);
			HashVO vo = new HashVO();
			vo.setAttributeValue(INPRConst.NPR_COL_CREATE_TIME,
					Toolkit.getCurTime());
			String begin = null;
			if (beginTime.compareTo(new UFDateTime()) == 0)
				begin = "日志创建时间";
			else
				begin = beginTime.toString();
			vo.setAttributeValue(INPRConst.NPR_COL_BODY_BEGINTIME, begin);
			vo.setAttributeValue(INPRConst.NPR_COL_BODY_ENDTIME,
					endTime.toString());
			vo.setAttributeValue(INPRConst.NPR_COL_SNAPSHOT_ADDR, logPath);
			vo.setAttributeValue(INPRConst.NPR_COL_BODY_REPORT_URL, reportPath);
			vo.setAttributeValue(INPRConst.NPR_COL_CREATE_COST,
					((t2 - t1) / 1000) + "s");
			getReportLogger().info(vo.toString0());
			this.createReportIndexPage();
			return reportPath;
		} catch (RemoteException ex) {
			ex.printStackTrace();
			throw new RemoteException("生成报告时发生错误,请稍后重试或请联系支持人员");
		} finally {
			NPRGlobalControl.getInstance().operatUnLock(
					NPRGlobalControl.NPR_CREATE_REPORT_LOCK);
		}
	}

	public void deleteSnapshot(Map<String, String[]> delMap) {
		if (Toolkit.isEmpty(delMap))
			return;
		String snapshotPath = ServerEnv.getInstance().getProperty(
				INPRConst.NPR_DEF_LOG)
				+ "/";
		String nmcPath = null;
		if (ServerEnv.getInstance().isDev())
			nmcPath = "D:/NC_UAP_MODULES/nmc/server/webapps/npr/";
		else
			nmcPath = "./webapps/npr/";
		String[] snapshotDirs = delMap.keySet().toArray(
				new String[delMap.size()]);
		String[] reportDirs = null;
		for (String snapshot : snapshotDirs) {
			String delPath = snapshotPath + snapshot;
			Toolkit.delDir(delPath);
			reportDirs = delMap.get(snapshot);
			if (Toolkit.isEmpty(reportDirs))
				continue;
			for (String dir : reportDirs) {
				delPath = nmcPath + dir;
				Toolkit.delDir(delPath);
			}
		}
	}

	public String doSnapshot() throws RemoteException {
		boolean bOnOperat = NPRGlobalControl.getInstance().getOperatLock(
				NPRGlobalControl.NPR_SNAPSHOT_LOCK);
		if (bOnOperat)
			throw new RemoteException("另一个【生成快照】任务正在执行,请稍后再试！");
		try {
			long t1 = System.currentTimeMillis();
			String path = new FileCollector().collectLogFilesForSnpsht();
			long t2 = System.currentTimeMillis();
			path = path.substring(path.lastIndexOf("/") + 1, path.length());
			HashVO vo = new HashVO();
			vo.setAttributeValue(INPRConst.NPR_COL_CREATE_TIME,
					Toolkit.getCurTime());
			vo.setAttributeValue(INPRConst.NPR_COL_SNAPSHOT_ADDR, path);
			vo.setAttributeValue(INPRConst.NPR_COL_CREATE_COST,
					((t2 - t1) / 1000) + "s");
			getLogLogger().info(vo.toString0());
			return path;
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RemoteException("生成日志快照时发生错误，请稍后重试或请联系支持人员");
		} finally {
			NPRGlobalControl.getInstance().operatUnLock(
					NPRGlobalControl.NPR_SNAPSHOT_LOCK);
		}
	}

	public XPRFileVO[] downloadReport(String[] nprNames) {
		if (Toolkit.isEmpty(nprNames))
			return null;
		List<XPRFileVO> list_fileVOs = new ArrayList<XPRFileVO>();
		String nprPath = null;
		if (ServerEnv.getInstance().isDev())
			nprPath = "D:/NC_UAP_MODULES/nmc/server/webapps/npr/";
		else
			nprPath = "./webapps/npr/";
		LogExtract logExtract = new LogExtract();
		for (int i = 0; i < nprNames.length; i++) {
			String filePath = nprPath + nprNames[i];
			File[] files = logExtract.getFiles(filePath, null, null, null);
			if (Toolkit.isEmpty(files))
				continue;
			for (File file : files) {
				String path = file.getPath();
				String name = file.getName();
				byte[] bytes = Toolkit.getFileBytes(path);
				FileVO filevo = new FileVO();
				filevo.setName(name);
				filevo.setContent(bytes);
				filevo.setFileSize(bytes.length);
				String relativePath = path.substring(path.indexOf(nprNames[i])
						+ nprNames[i].length(), path.indexOf(name));
				XPRFileVO nprFileVO = new XPRFileVO();
				nprFileVO.setFilevo(filevo);
				nprFileVO.setNprName(nprNames[i]);
				nprFileVO.setRelativePath(relativePath);
				list_fileVOs.add(nprFileVO);
			}
		}
		return list_fileVOs.toArray(new XPRFileVO[list_fileVOs.size()]);
	}

	public HashVO getCurrNprCfg() {
		return NPRScheduleConfig.getInstance().getCurrNprCfg();
	}

	public FileInfo[] getFileInfosFromServer(Filter[] filters) throws Exception {
		return new FileInfoService().getFileInfos(filters);
	}

	private Logger getLogLogger() {
		synchronized (this) {
			if (logger1 == null) {
				logger1 = LogTool.createLogger("npr", "npr", "nprlog.idx");
			}
		}
		return logger1;
	}

	public HashVO[][] getNprlog() {
		HashVO[] logVOs = null;
		HashVO[] reportLogVOs = null;
		try {

				logVOs = new Text2HashVO().convertToVO(INPRConst.NMC_LOG_DIR
						+ "/npr/nprlog.idx.log", "$$");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
				reportLogVOs = new Text2HashVO().convertToVO(
						INPRConst.NMC_LOG_DIR + "/npr/nprreport.idx.log", "$$");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new HashVO[][] { logVOs, reportLogVOs };
	}

	private Logger getReportLogger() {
		synchronized (this) {
			if (logger2 == null) {
				logger2 = LogTool.createLogger("npr", "npr", "nprreport.idx");
			}
		}
		return logger2;
	}


	public ServiceResource[] getServerResouce(ServiceDesc serviceDesc) {

		if (!ServerEnv.getInstance().isServiceCenter())
			return null;
		resource = new ServiceResource();
		resource.setDisplayName(ServerEnv.getHostName());
		resource.setHost(ServerEnv.getLocalAddr());
		resource.setPort(ServerEnv.getInstance().getLocalSocket().getPort());
		resource.setRequestUrl(ServerEnv.getInstance().getLocalUrl());
		resource.setServiceName(serviceDesc.getServiceName());
		resource.setServiceDesc(serviceDesc);
		return new ServiceResource[] { resource };

	}


	public void refreshEnv() {
		// TODO Auto-generated method stub

	}

	public void saveConfig(HashVO configVO) {
		if (Toolkit.isEmpty(configVO))
			return;
		NPRScheduleConfig.getInstance().saveConfig(configVO);
	}

	public void sendMail(String reportAddr) {
		if (Toolkit.isEmpty(reportAddr))
			return;
		HashVO configVO = NPRScheduleConfig.getInstance().getCurrNprCfg();
		if (Toolkit.isEmpty(configVO))
			return;
		String emailAddrs = configVO
				.getStringValue(INPRConst.NPR_CONFIG_EMAIL_ADDR);
		if (Toolkit.isEmpty(emailAddrs))
			return;
//		String liuzyMail = "liuzy@ufida.com;";
//		if (!emailAddrs.endsWith(";"))
//			liuzyMail = ";" + liuzyMail;
//		emailAddrs += liuzyMail;
		String[] addrs = null;
		if (emailAddrs.indexOf(";") > -1)
			addrs = StringUtil.split(emailAddrs, ";");
		else
			addrs = new String[] { emailAddrs };
		String reportDir = reportAddr.substring(reportAddr.indexOf("NPR"),
				reportAddr.indexOf("/npr.html"));
		String fileName = "./webapps/npr/" + reportDir + "/npr.html";
		MailSenderInfo mailInfo = MailSender.getInstance().getMailInfo();
		mailInfo.setSubject("NC Performance Report(" + Toolkit.getCurTime()
				+ ")");
		mailInfo.setAttachFileNames(new String[] { fileName });
		StringBuilder sb = new StringBuilder();
		sb.append("<p>了解系统性能状态请查看附件性能报告(为保证图标正常显示，推荐使用浏览器：Chrome、FireFox、IE8及以上版本)。</p>");
		sb.append("<p>若要了解更详细的信息，请访问:<a href=\"");
		sb.append(reportAddr);
		sb.append("\">");
		sb.append(reportAddr);
		sb.append("</a></p><p>NPR(NC   performance   report)是高度聚合、全面的性能分析报告。");
		sb.append("通过NPR可快速了解系统性能状态，及时发现系统问题。</p>");
		sb.append("<p><br />此报告基于NPR2.0.20120810，由系统自动生成并发布，请勿回复！</p>");
		for (String addr : addrs) {
			mailInfo.setToAddress(addr);
			mailInfo.setContent(sb.toString());
			new SimpleMailSender().sendTextMail(mailInfo);
		}
	}


	public void startServer() {
		if (!ServerEnv.getInstance().isServiceCenter())
			return;
		thread1 = new NPRScheduleThread("NPRScheduleThread",
				NPRScheduleThread.PROC_REPORT);
		thread1.setRun(true);
		thread1.start();
		thread2 = new NPRScheduleThread("NPRScheduleThread",
				NPRScheduleThread.PROC_CLEAR);
		thread2.setRun(true);
		thread2.start();
		String clearOnStart = this.getCurrNprCfg().getStringValue(
				INPRConst.NPR_CONFIG_CLEAR_RESTART_ENABLE);
		if (!Toolkit.isEmpty(clearOnStart) && "Y".equals(clearOnStart))
			new NPRCentral().clearNPR();
		initUserMap();
	}

	private void initUserMap() {
		Thread thread = new Thread() {
			/*
			 * (non-Javadoc)
			 *
			 * @see java.lang.Thread#run()
			 */
			@Override
			public void run() {
				try {
					NPRGlobalControl.getInstance().initUserMap();
				} catch (Exception e) {
//					e.printStackTrace();
				}
			}
		};
		thread.start();
	}


	public void stopServer() {
		thread1.setRun(false);
		thread2.setRun(false);
	}

	public void onTest() {

		Thread t = new Thread() {

			/*
			 * (non-Javadoc)
			 *
			 * @see java.lang.Thread#run()
			 */
			@Override
			public void run() {
				String NC_HOME = ServerEnv.getInstance().getProperty("NC_HOME");
				String logPath = NC_HOME + "/nclogs/liuzy_test.txt";
				File file = new File(logPath);
				try {
					if (!file.exists())
						file.createNewFile();
					FileInputStream in = new FileInputStream(file);
					in.read();
					while (true) {
						System.out.println("#####liuzy测试文件已锁定");
						Thread.currentThread();
						Thread.sleep(1000 * 5);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};
		t.start();
	}

	public void onRenameTest() {
		String NC_HOME = ServerEnv.getInstance().getProperty("NC_HOME");
		String logPath = NC_HOME + "/nclogs/liuzy_test.txt";
		File file = new File(logPath);
		boolean success = file.renameTo(new File(NC_HOME
				+ "/nclogs/liuzy_test1.txt"));
		if (success)
			System.out.println("*****liuzy文件重命名成功");
		else
			System.out.println("*****liuzy文件重命名失败");
	}
}
