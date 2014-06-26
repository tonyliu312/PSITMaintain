package uap.ps.itm.npr.service.log.resolve;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nc.vo.pub.lang.UFDateTime;
import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Text2HashVO;
import uap.ps.itm.monitor.pub.Toolkit;

/**
 * 日志抽取
 * 
 * @author liuzy
 * 
 */
public class LogExtract {

	public HashVO[] getHashVOs(String path, UFDateTime beginTime,
			UFDateTime endTime) {
		File fileDir = new File(path);
		File[] files = fileDir.listFiles();
		File[] currectFiles = this.chooseFileByTime(files, beginTime, endTime);
		return this.getHashVOsByFiles(currectFiles);
	}

	public HashVO[] getHashVOs(String path, String fileNamePre,
			UFDateTime beginTime, UFDateTime endTime) {
		File[] currectFiles = this.getCorrectFiles(path, fileNamePre,
				beginTime, endTime, new ArrayList<File>());
		return this.getHashVOsByFiles(currectFiles);
	}

	public File[] getFiles(String path, UFDateTime beginTime, UFDateTime endTime) {
		File fileDir = new File(path);
		File[] files = fileDir.listFiles();
		if (Toolkit.isEmpty(files))
			return null;
		return this.chooseFileByTime(files, beginTime, endTime);
	}

	public File[] getFiles(String path, String fileNamePre,
			UFDateTime beginTime, UFDateTime endTime) {
		return this.getCorrectFiles(path, fileNamePre, beginTime, endTime,
				new ArrayList<File>());
	}

	private File[] getCorrectFiles(String path, String fileNamePre,
			UFDateTime beginTime, UFDateTime endTime, List<File> allFiles) {
		File fileDir = new File(path);
		File[] files = fileDir.listFiles();
		if (Toolkit.isEmpty(files))
			return null;
		List<File> filteFiles = new ArrayList<File>();
		for (File f : files) {
			if (f.isDirectory()) {
				this.getCorrectFiles(path + "/" + f.getName(), fileNamePre,
						beginTime, endTime, allFiles);
				continue;
			}
			if (!Toolkit.isEmpty(fileNamePre)
					&& f.getName().indexOf(fileNamePre) < 0)
				continue;
			filteFiles.add(f);
		}
		File[] currectFiles = this.chooseFileByTime(
				filteFiles.toArray(new File[filteFiles.size()]), beginTime,
				endTime);

		allFiles.addAll(Arrays.asList(currectFiles));

		return allFiles.toArray(new File[allFiles.size()]);
	}

	/**
	 * 从一组文件中返回符合时间段条件的文件
	 * 
	 * @param files
	 * @param beginTime
	 * @param endTime
	 * @return
	 */
	private File[] chooseFileByTime(File[] files, UFDateTime beginTime,
			UFDateTime endTime) {
		if (Toolkit.isEmpty(beginTime))
			beginTime = new UFDateTime();
		List<File> retFiles = new ArrayList<File>();
		for (File f : files) {
			if(f.isDirectory())
				continue;
			long fileTime = f.lastModified();
			UFDateTime uft = new UFDateTime(fileTime);
			if (uft.compareTo(beginTime) >= 0)
				retFiles.add(f);
		}
		Toolkit.sort(retFiles, new FileSortComparetor());
		return retFiles.toArray(new File[retFiles.size()]);
	}

	/**
	 * 将一组给定的文件转换为HashVO返回
	 * 
	 * @param files
	 * @return
	 */
	public HashVO[] getHashVOsByFiles(File[] files) {
		List<HashVO> retVOs = new ArrayList<HashVO>(files.length);
		for (File f : files) {
			String filePath = f.getAbsolutePath();
			HashVO[] vos = new Text2HashVO().convertToVO(filePath, "$$");
			retVOs.addAll(Arrays.asList(vos));
		}
		return retVOs.toArray(new HashVO[retVOs.size()]);
	}

	/**
	 * 将给定的文件转换为HashVO返回
	 * 
	 * @param files
	 * @return
	 */
	public HashVO[] getHashVOsByFile(File file) {
		return this.getHashVOsByFiles(new File[] { file });
	}

}
