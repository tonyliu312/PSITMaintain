package uap.ps.itm.npr.service.log.collect;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nc.monitor.Exception.RemoteException;
import nc.monitor.pub.HashVO;
import nc.monitor.pub.HashVO2Text;
import nc.monitor.pub.IConfigConst;
import nc.monitor.pub.Toolkit;
import nc.monitor.service.center.ServerEnv;
import nc.monitor.service.center.ServiceHolder;
import nc.monitor.service.center.ServiceRegister;
import nc.monitor.service.db.DBSourceInfoHolder;
import nc.monitor.service.db.DbSourceInfo;
import nc.monitor.service.filetransfer.FileInfo;
import nc.monitor.service.loganalyze.Filter;
import nc.monitor.service.process.MonitorEnv;
import nc.monitor.service.process.MwServerInfo;
import nc.monitor.servlet.RequestInfo;
import nc.monitor.servlet.URLCommuniatcor;
import nc.monitor.ui.pub.ExceptionHandler;
import uap.ps.itm.npr.service.INPRConst;
import nc.vo.pub.lang.UFDateTime;

/**
 * Ϊ�����Ѽ�������Դ�ļ� nclogs��nmclogs
 * 
 * @author liuzy
 * 
 */
public class FileCollector {

	/**
	 * Ϊ�����Ѽ���־�ļ�
	 */
	public String collectLogFilesForSnpsht() throws RemoteException {
		String destPath = this.localLogsCollect();
		String tsPath = destPath;
		this.remoteCollect(destPath);
		collectDeployInfo(tsPath);
		return tsPath;
	}

	private void collectDeployInfo(String tsPath) {
		List<HashVO> list = new ArrayList<HashVO>();
		MwServerInfo[] mwservers = MonitorEnv.getInstance().getAllServerInfo();
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		// String ts = sdf.format(new Date());
		// ts += ":00";
		for (MwServerInfo info : mwservers) {
			HashVO vo = new HashVO();
			vo.setAttributeValue("����", info.getServerName());
			vo.setAttributeValue("��ַ", info.getHost() + ":" + info.getPort());
			vo.setAttributeValue("WAS", info.isWAS() ? "��" : "��");
			vo.setAttributeValue("-Xmx", info.getJvmmax());
			// vo.setAttributeValue("�����ڴ�", info.getJvmused());
			// vo.setAttributeValue("ʣ���ڴ�", info.getJvmfree());
			vo.setAttributeValue("�̳߳ش�С", info.getTreadPoolRange());
			// vo.setAttributeValue("�����߳���", info.getThreadPoolUsed());
			// vo.setAttributeValue("������", info.getThroughput());
			vo.setAttributeValue("��������", info.getRestartTimes());
			vo.setAttributeValue("���һ������ʱ��", info.getLastRestartDate());
			// vo.setAttributeValue(INPRPluginConst.TS, ts);
			// vo.setAttributeValue("���һ�μ��ʱ��", info.getLastcheckts());
			// vo.setAttributeValue("���һ�μ���ʱ", info.getLastcheckcost());
			list.add(vo);
		}
		HashVO2Text.writeToFile(new File(tsPath + "/mwinfo.log"),
				list.toArray(new HashVO[list.size()]), "$$");

		list.clear();
		DbSourceInfo[] dataSrcList = DBSourceInfoHolder.getInstance()
				.getAllServerInfo();
		if (Toolkit.isEmpty(dataSrcList))
			return;
		for (DbSourceInfo dbinfo : dataSrcList) {
			HashVO vo = new HashVO();
			vo.setAttributeValue("����Դ����", dbinfo.getDatasourcename());
			vo.setAttributeValue("���ݿ�����", dbinfo.getDbtype());
			vo.setAttributeValue("���ݿ��ַ", dbinfo.getUrl());
			vo.setAttributeValue("�û���", dbinfo.getUsername());
			// vo.setAttributeValue("��Ự��", dbinfo.getActiveSession());
			vo.setAttributeValue("���������", dbinfo.getMaxCon());
			vo.setAttributeValue("��С������", dbinfo.getMinCon());
			// vo.setAttributeValue(INPRPluginConst.TS, ts);
			// vo.setAttributeValue("�����ʱ��", dbinfo.getLastcheckts());
			// vo.setAttributeValue("�������Ϣ", dbinfo.getLastcheckMsg());
			list.add(vo);
		}
		HashVO2Text.writeToFile(new File(tsPath + "/dbinfo.log"),
				list.toArray(new HashVO[list.size()]), "$$");
	}

	/**
	 * �ӱ����Ѽ�
	 */
	private String localLogsCollect() throws RemoteException {
		FileInfo[] fileInfos = new FileInfoService()
				.getFileInfos(new Filter[] { new Filter(null, null,
						INPRConst.COLLECT_TYPE_NMC) });
		String tsPath = this.copyFiles(fileInfos, null, true);

		fileInfos = new FileInfoService()
				.getFileInfos(new Filter[] { new Filter(null, null,
						INPRConst.COLLECT_TYPE_NC) });
		copyFiles(fileInfos, tsPath, false);

		return tsPath;
	}

	private String copyFiles(FileInfo[] fileInfos, String toPath,
			boolean isNMCLogs) throws RemoteException {
		String tsPath = null;
		if (Toolkit.isEmpty(toPath)) {
			String dir = new SimpleDateFormat("yyyyMMddHHmmss")
					.format(new Date());
			tsPath = ServerEnv.getInstance().getProperty(INPRConst.NPR_DEF_LOG)
					+ "/" + dir;
		} else
			tsPath = toPath;
		toPath = tsPath
				+ "/"
				+ (isNMCLogs ? INPRConst.NMC_LOG_DIR_NAME
						: INPRConst.NC_LOG_DIR_NAME);
		for (FileInfo f : fileInfos) {
			String fromPath = f.getAbsFilePath();
			Toolkit.copyFiles2Dir(fromPath, toPath + "/" + f.getRelativePath());
		}
		return isNMCLogs ? tsPath : toPath;
	}

	/**
	 * ��Զ���Ѽ�
	 */
	private void remoteCollect(String destPath) {
		String targeturl = null;
		ServiceRegister sg = (ServiceRegister) ServiceHolder.getInstance()
				.getServer(IConfigConst.SERVICERGISTERSERVICE);
		String[] remotes = sg.getRemoteHost();
		if (!Toolkit.isEmpty(remotes)) {
			for (int i = 0; i < remotes.length; i++) {
				targeturl = "http://" + remotes[i] + "/remotecall";
				FileInfo[] fileInfos = this.getFileInfo(targeturl);
				this.downLoadFiles(fileInfos, targeturl, destPath);
			}
		}
	}

	/**
	 * ���FileInfo
	 * 
	 * @param url
	 * @return
	 */
	private FileInfo[] getFileInfo(String url) {
		RequestInfo req = new RequestInfo();
		req.setServiceName(INPRConst.NPR_SERVICE_NAME);
		req.setMethodName(INPRConst.FILEINFO_METHOD_NAME);
		req.setParameter(new Object[] { new Filter[] { new Filter(null, null,
				INPRConst.COLLECT_TYPE_NC) } });
		req.setParametertypes(new Class[] { Filter[].class });
		req.setServer2Server(true);
		Object res = URLCommuniatcor.remoteCall(req, url);
		if (null == res)
			return null;
		return (FileInfo[]) res;
	}

	/**
	 * �����ļ�
	 * 
	 * @param fileInfos
	 * @param serverURL
	 */
	private void downLoadFiles(FileInfo[] fileInfos, String serverURL,
			String destPath) {

		File nprLogDir = new File(destPath);
		if (!nprLogDir.exists())
			nprLogDir.mkdirs();

		for (FileInfo f : fileInfos) {
			String newPath;
			String fullfilename;
			RequestInfo request = new RequestInfo();
			request.setMethodName(INPRConst.FILE_DOWN_METHOD_NAME);
			request.setServiceName(IConfigConst.SERVICEFILETRANS);
			request.setServer2Server(true);
			request.setParameter(new Object[] { f.getAbsFilePath() });
			DataInputStream inputStream = null;
			DataOutputStream fileOut = null;
			ObjectOutputStream outPut = null;
			try {
				URL url = new URL(serverURL);
				URLConnection con = url.openConnection();
				con.setDoOutput(true);
				con.setDoInput(true);
				outPut = new ObjectOutputStream(con.getOutputStream());
				outPut.writeObject(request);
				inputStream = new DataInputStream(con.getInputStream());
				byte[] buf = new byte[8192];
				newPath = destPath + f.getRelativePath();
				File newp = new File(newPath);
				if (!newp.exists()) {
					newp.mkdirs();
				}
				fullfilename = newPath + "/" + inputStream.readUTF();
				fileOut = new DataOutputStream(new BufferedOutputStream(
						new FileOutputStream(fullfilename)));
				inputStream.readLong();
				while (true) {
					int read = 0;
					if (inputStream != null) {
						read = inputStream.read(buf);
					}

					if (read == -1) {
						break;
					}
					fileOut.write(buf, 0, read);

				}
			} catch (Exception e) {
				ExceptionHandler.handleException(e);
				return;
			} finally {
				try {
					try {
						fileOut.close();
					} catch (Exception e) {
					}
					try {
						outPut.close();
					} catch (Exception e) {
					}
					try {
						inputStream.close();
					} catch (Exception e) {
					}
				} catch (Exception e) {

				}
			}
			if (new File(fullfilename).exists()) {
				new File(fullfilename).setLastModified(new UFDateTime(f
						.getLast_modify()).getMillis());
			}

		}
	}
}
