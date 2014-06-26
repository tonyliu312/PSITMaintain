package uap.ps.itm.pub;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Toolkit;

public class HashVO2XML {
	public static void writeToXmlFile(String filename, HashVO[] vos,
			String flagName) {
		if (Toolkit.isEmpty(filename))
			return;
		if (Toolkit.isEmpty(vos))
			return;
		File file = new File(filename);
		try {
			if (!file.exists())
				file.createNewFile();
			writeToXmlFile(file, vos, flagName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getXml(HashVO[] vos, String flagName) {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding='UTF-8'?>\n<root>\n");
		if (!Toolkit.isEmpty(vos)) {
			for (int i = 0; i < vos.length; i++) {
				vos[i].setRoot(flagName);
				sb.append(vos[i].toXml0());
				sb.append("\n");
			}
		}
		sb.append("\n</root>");
		return sb.toString();
	}

	public static void writeToXmlFile(File file, HashVO[] vos, String flagName) {
		if (Toolkit.isEmpty(vos))
			return;
		DataOutputStream out = null;
		try {
			if (!file.exists())
				file.createNewFile();
			out = new DataOutputStream(new FileOutputStream(file));
			out.write(getXml(vos, flagName).getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {

				}
			}
		}
	}
}
