package uap.ps.itm.npr.service.log.collect;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;

import nc.vo.pub.lang.UFDateTime;
import uap.ps.itm.monitor.pub.IConfigConst;
import uap.ps.itm.monitor.service.center.ServerEnv;
import uap.ps.itm.monitor.service.filetransfer.FileInfo;
import uap.ps.itm.npr.service.INPRConst;
import uap.ps.itm.pub.servlet.RequestInfo;
import uap.ps.itm.pub.servlet.URLCommuniatcor;
import uap.ps.itm.pub.utils.ExceptionHandler;

public class FileDownLoadThread extends Thread {

	private String url;

	public FileDownLoadThread(String url) {
		this.url = url;
	}

	@Override
	public void run() {
		FileInfo[] fileInfos = getFileInfo(this.url);
		this.downLoadFiles(fileInfos);
	}

	/**
	 * »ñµÃFileInfo
	 * 
	 * @param url
	 * @return
	 */
	private FileInfo[] getFileInfo(String url) {
		RequestInfo req = new RequestInfo();
		req.setServiceName(INPRConst.NPR_SERVICE_NAME);
		req.setMethodName(INPRConst.FILEINFO_METHOD_NAME);
		req.setServer2Server(true);
		Object res = URLCommuniatcor.remoteCall(req, url);
		if (null == res)
			return null;
		return (FileInfo[]) res;
	}

	private void downLoadFiles(FileInfo[] fileInfos) {

		String path = ServerEnv.getInstance()
				.getProperty(INPRConst.NPR_DEF_LOG);
		File nprLogDir = new File(path);
		if (!nprLogDir.exists())
			nprLogDir.mkdirs();

		for (FileInfo f : fileInfos) {
			int bufferSize = 8192;
			RequestInfo request = new RequestInfo();
			request.setMethodName(INPRConst.FILE_DOWN_METHOD_NAME);
			request.setServiceName(IConfigConst.SERVICEFILETRANS);
			// Domain domain = new Domain();
			// domain.setUsername(getJTextUsername().getText());
			// domain.setPassword(new String(getJPassword().getPassword()));
			// request.setDomain(domain);
			request.setParameter(new Object[] { f.getAbsFilePath() });
			DataOutputStream fileOut = null;
			ObjectOutputStream outPut = null;
			DataInputStream inputStream = null;
			try {
				URL url = new URL(this.url);
				URLConnection con = url.openConnection();
				con.setDoOutput(true);
				con.setDoInput(true);
				outPut = new ObjectOutputStream(con.getOutputStream());
				outPut.writeObject(request);
				inputStream = new DataInputStream(con.getInputStream());
				byte[] buf = new byte[bufferSize];

				fileOut = new DataOutputStream(new BufferedOutputStream(
						new FileOutputStream(path)));

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
			if (new File(path).exists()) {
				new File(path).setLastModified(new UFDateTime(f
						.getLast_modify()).getMillis());
			}

		}
	}

}
