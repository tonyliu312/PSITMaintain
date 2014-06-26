package uap.ps.itm.monitor.service.message;

import java.io.File;
import java.util.ArrayList;

import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Text2HashVO;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.loganalyze.Filter;
import uap.ps.itm.monitor.service.loganalyze.TextQueryEngine;

public class MessageQuery {
	private String filedir = "./logs/snapshot/";

	public HashVO[] queryAllMessage() {
		File file = new File(filedir);
		if (!file.exists()) {
			return null;
		}
		ArrayList<HashVO> al = new ArrayList<HashVO>();
		File[] files = file.listFiles();
		HashVO[] vos = null;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory() ||files[i].getName().indexOf("lck") > -1) {
				continue;
			}
			try {
				vos = new Text2HashVO().convertToVO(files[i].getCanonicalPath(), "$$");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (Toolkit.isEmpty(vos))
				continue;
			for (int j = 0; j < vos.length; j++) {
				al.add(vos[j]);
			}
		}
		return (HashVO[]) al.toArray(new HashVO[al.size()]);
	}

	public HashVO[] queryMessage(String fromTime, String endTime) {
		if (Toolkit.isEmpty(fromTime))
			return queryAllMessage();
		Filter[] filters = null;
		Filter filter = new Filter("ts", ">=", fromTime);
		if (endTime != null) {
			Filter endFilter = new Filter("ts", "<=", endTime);
			filters = new Filter[] { filter, endFilter } ;
		} else {
			filters = new Filter[] { filter };
		}
		return new TextQueryEngine().filter(queryAllMessage(), filters );
	}
	
	public HashVO[] querySnapShot(String fileDir) {
		File file = new File(fileDir);
		if (!file.exists()) {
			return null;
		}
		ArrayList<HashVO> al = new ArrayList<HashVO>();
		File[] files = file.listFiles();
		HashVO vo = null;
		for (int i = 0; i < files.length; i++) {
			String content = null;
			vo = new HashVO();
			try {
				content = Toolkit.getFileContent(files[i].getCanonicalPath());
			} catch (Exception e) {
			}
			if (Toolkit.isEmpty(content))
				continue;
			vo.setAttributeValue("filename", files[i].getName());
			vo.setAttributeValue("content", content);
			al.add(vo);
		}
		return (HashVO[]) al.toArray(new HashVO[al.size()]);
	}

	public static final void main(String[] args) {
		MessageQuery query = new MessageQuery();
		query.queryMessage("2009-01-01 00:00:00", null);
	}
}
