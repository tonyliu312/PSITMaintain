package uap.ps.itm.npr.service.log.collect;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import uap.ps.itm.monitor.RemoteException;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.center.ServerEnv;
import uap.ps.itm.monitor.service.filetransfer.FileInfo;
import uap.ps.itm.monitor.service.loganalyze.Filter;
import uap.ps.itm.npr.service.INPRConst;

/**
 * 为NPR文件搜集者提供FileInfo信息
 *
 * @author liuzy
 *
 */
public class FileInfoService {

	public FileInfo[] getFileInfos(Filter[] filters) throws RemoteException {
		boolean isNMCLogs = false;
		for (Filter f : filters) {
			if (!Toolkit.isEmpty(f.getValue())
					&& INPRConst.COLLECT_TYPE_NMC
							.equalsIgnoreCase(f.getValue()))
				isNMCLogs = true;
		}
		Set<FileInfo> set = new HashSet<FileInfo>();
		String logDir = this.getLogDir(isNMCLogs);
		if (isNMCLogs) {
			String[] childDir = new String[] { INPRConst.NMC_LOG_DB_DIR_NAME,
					INPRConst.NMC_LOG_MW_DIR_NAME,
					INPRConst.NMC_LOG_ONLINEUSER_DIR_NAME,
					INPRConst.NMC_LOG_SNAPSHOT_DIR_NAME,
					INPRConst.NMC_LOG_SYSTEM_MONITOR_NAME };
			for (String path : childDir) {
				String relaPath = "./" + path;
				String absPath = logDir + "/" + path;
				procFileInfos(new File(absPath), set, relaPath, isNMCLogs);
			}
		} else
			procFileInfos(new File(logDir), set, null, isNMCLogs);
		return set.toArray(new FileInfo[0]);
	}

	public FileInfo[] getNMCSnapshotFileInfos(String path) throws Exception {
		Set<FileInfo> set = new HashSet<FileInfo>();
		procFileInfos(new File(path), set, null, true);
		return set.toArray(new FileInfo[0]);
	}

	private void procFileInfos(File file, Set<FileInfo> set, String relapath,
			boolean isNMCLogs) throws RemoteException {
		if (Toolkit.isEmpty(relapath)) {
			relapath = ".";
		}
		File[] files = file.listFiles();
		if (Toolkit.isEmpty(files))
			return;
		for (File f : files) {
			if (f.isDirectory())
				procFileInfos(f, set, relapath + "/" + f.getName(), isNMCLogs);
			else if (checkFile(f, isNMCLogs)) {
				String fileName = f.getName();
				if (fileName.indexOf(".lck") > -1)
					continue;
				FileInfo fileinfo = new FileInfo();
				fileinfo.setRelativePath(f.getAbsolutePath());
				fileinfo.setAbsFilePath(f.getAbsolutePath());
				fileinfo.setFilename(Toolkit.isEmpty(f.getName()) ? f
						.getAbsolutePath() : f.getName());
				fileinfo.setFilesize(f.length());
				fileinfo.setRelativePath(relapath);
				String strDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(new Date(f.lastModified()));
				fileinfo.setLast_modify(strDate);
				fileinfo.setDIR(f.isDirectory());
				set.add(fileinfo);
			}
		}
	}

	private String getLogDir(boolean isNMCLogs) {
		String baseDir = ServerEnv.getInstance().getProperty(
				INPRConst.NC_LOG_DIR);
		if (isNMCLogs)
			baseDir = INPRConst.NMC_LOG_DIR;
		return baseDir;
	}

	private boolean checkFile(File f, boolean isNMCLogs) {
		boolean ret = f.getName().indexOf(INPRConst.MWSMRY_FILE_NAME) > -1
				|| f.getName().indexOf(INPRConst.WNGSQL_FILE_NAME) > -1;
		if (isNMCLogs)
			ret = f.getParent().indexOf(INPRConst.NMC_LOG_DB_DIR_NAME) > -1
					|| f.getParent().indexOf(INPRConst.NMC_LOG_MW_DIR_NAME) > -1
					|| f.getParent().indexOf(
							INPRConst.NMC_LOG_ONLINEUSER_DIR_NAME) > -1
					|| f.getParent().indexOf(
							INPRConst.NMC_LOG_SNAPSHOT_DIR_NAME) > -1
					|| f.getParent().indexOf(
							INPRConst.NMC_LOG_SYSTEM_MONITOR_NAME) > -1;
		return ret;
	}

}
