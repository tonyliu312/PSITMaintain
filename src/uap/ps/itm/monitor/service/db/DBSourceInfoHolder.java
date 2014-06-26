package uap.ps.itm.monitor.service.db;

import java.util.ArrayList;

import uap.ps.itm.monitor.pub.Toolkit;

public class DBSourceInfoHolder {
	private static DBSourceInfoHolder m_instance = null;
	private ArrayList<DbSourceInfo> alDBInfo = new ArrayList<DbSourceInfo>();

	public static DBSourceInfoHolder getInstance() {
		if (m_instance == null) {
			m_instance = new DBSourceInfoHolder();
		}
		return m_instance;
	}

	public void addDBInfo(DbSourceInfo dbinfo) {
		alDBInfo.add(dbinfo);
	}

	public DbSourceInfo getDBserverInfo(String dbname) {
		for (int i = 0; i < alDBInfo.size(); i++) {
			if (Toolkit.isEqual(alDBInfo.get(i).getDatasourcename(), dbname))
				return alDBInfo.get(i);
		}
		return null;
	}

	public DbSourceInfo[] getAllServerInfo() {
		DbSourceInfo[] infos = alDBInfo.toArray(new DbSourceInfo[alDBInfo.size()]);
		return infos;
	}
}
