/**
 * 
 */
package uap.ps.itm.npr.service.plugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import nc.monitor.Exception.RemoteException;
import nc.monitor.pub.HashVO;
import nc.monitor.pub.Toolkit;
import uap.ps.itm.npr.service.INPRConst;
import uap.ps.itm.npr.service.report.HTMLFrame;

/**
 * @author liuzy
 * 
 */
public class MsgSnapshotPageImpl extends AbsNPRPlugin {


	public void process(File file, HashVO[] dataVOs) throws RemoteException {
	}

	public void process(String destDirName, File file, HashVO[] dataVOs) {

		if (Toolkit.isEmpty(destDirName) || Toolkit.isEmpty(file)
				|| file.isDirectory())
			return;
		if (file.getName().indexOf(INPRConst.NMC_SNAPSHOT_FILE_NAME) >= 0)
			return;
		try {
			String content = this.getFileContent(file.getAbsolutePath());
			if (Toolkit.isEmpty(content))
				return;
			String destFileName = file.getParentFile().getName() + "-"
					+ file.getName();
			String htmlString = new HTMLFrame().getSimpleHtml(content);
			destDirName += "/snapshot/";
			File f = new File(destDirName);
			if (!f.exists())
				f.mkdirs();
			Toolkit.writeFile(destDirName + destFileName + ".html",
					htmlString.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getFileContent(String filename) throws Exception {
		InputStreamReader input = null;
		LineNumberReader lineReader = null;
		try {
			input = new InputStreamReader(new BufferedInputStream(
					new FileInputStream(filename)));
			lineReader = new LineNumberReader(input);
			String curline = null;
			StringBuffer sb = new StringBuffer();
			while ((curline = lineReader.readLine()) != null) {
				sb.append("<P>").append(curline);
			}
			return sb.toString();
		} finally {
			try {
				input.close();
			} catch (Exception e) {

			}
			try {
				lineReader.close();
			} catch (Exception e) {

			}
		}
	}

}
