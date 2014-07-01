/**
 * 
 */
package uap.ps.itm.npr.service.report;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import nc.monitor.Exception.RemoteException;
import nc.monitor.pub.HashVO;
import nc.monitor.pub.IMonitorConst;
import nc.monitor.pub.Toolkit;
import nc.monitor.service.center.ServerEnv;
import uap.ps.itm.npr.service.log.resolve.LogResolve;
import uap.ps.itm.npr.vo.XPRPluginResult;
import uap.ps.itm.npr.vo.BPRReport;
import nc.vo.pub.lang.UFDateTime;

/**
 * NC Performance Report
 * 
 * @author liuzy
 * 
 */
public class NPRReportBuilder {

	public String buildReport(String shotPath, UFDateTime beginTime,
			UFDateTime endTime) throws RemoteException {
		try {
			LogResolve logre = new LogResolve();
			// NPRPluginResult[] pluginResults = logre.resolveByFiles(shotPath,
			// beginTime, endTime);
			XPRPluginResult[] pluginResults = logre.resolveByFilesOneTime(
					shotPath, beginTime, endTime);
			if (Toolkit.isEmpty(pluginResults))
				return null;
			String reportDir = new SimpleDateFormat("yyyyMMddHHmmss")
					.format(new Date());
			reportDir = "NPR" + reportDir;
			String destDirName = "./webapps/npr/" + reportDir;
			File destDir = new File(destDirName);
			if (!destDir.exists())
				destDir.mkdirs();
			String destFileName = destDirName + "/npr.html";
			BPRReport report = new BPRReport();
			report.setReportTitle("NC Performance Report");
			report.setDestFileName(destFileName);
			report.setBeginTime(beginTime);
			report.setEndTime(endTime);
			report.setPluginResult(pluginResults);
			String sHtml = new HTMLFrame(report, IMonitorConst.REPORT_TYPE_NPR)
					.getHtml();
			Toolkit.writeFile(destFileName, sHtml.getBytes());
			logre.getNMCSnapshotResult(shotPath, beginTime, endTime,
					destDirName);
			return reportDir;
		} catch (RemoteException e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void buildReportIndex(HashVO[] reportLogVOs) {
		String destFileName = null;
		if (ServerEnv.getInstance().isDev())
			destFileName = "d:/dev_env/20120911/nmc/server/webapps/npr/index.html";
		else
			destFileName = "./webapps/npr/index.html";
		File file = new File(destFileName);
		if (file.exists())
			file.delete();
		String sHtml = new HTMLFrame().getIndexHtml(reportLogVOs);
		Toolkit.writeFile(destFileName, sHtml.getBytes());
	}

}
