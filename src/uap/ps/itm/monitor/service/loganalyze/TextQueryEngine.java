package uap.ps.itm.monitor.service.loganalyze;

import java.io.File;

import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Text2HashVO;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.center.ServerEnv;

public class TextQueryEngine extends AbstraceEngine {

	public HashVO[] query(File file, Filter[] filter) throws Exception {
		// 将文件转为hashvo.
		HashVO[] vos = loadData(file, getPrefix());
		// 过虑
		return super.filter(vos, filter);
	} 

	public HashVO[] loadData(File file, String prefix) {
		HashVO[] vos = new Text2HashVO().convertToVO(file.getAbsolutePath(), prefix);
		if (Toolkit.isEmpty(vos))
			return null;
		String path = file.getPath();
		for (int i = 0; i < vos.length; i++) {
			if (Toolkit.isEmpty(vos[i].getAttributeValue("server"))) {
				vos[i].setAttributeValue("server", ServerEnv.getHostName());
			}
			if (Toolkit.isEmpty(vos[i].getAttributeValue("file"))) {
				vos[i].setAttributeValue("file", path);
			}
		}
		return vos;
	}

	private String getPrefix() {
		return ServerEnv.getInstance().getProperty("LOG_QUERY_PREFIX");
	}
}
