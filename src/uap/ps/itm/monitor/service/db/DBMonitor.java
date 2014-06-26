package uap.ps.itm.monitor.service.db;

import nc.vo.framework.rsa.Encode;
import uap.ps.itm.monitor.RemoteException;
import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.IConfigConst;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.pub.Xml2HashVO;
import uap.ps.itm.monitor.service.center.IServer;
import uap.ps.itm.monitor.service.center.ISnapShot;
import uap.ps.itm.monitor.service.center.ServerEnv;
import uap.ps.itm.monitor.service.center.ServiceDesc;
import uap.ps.itm.monitor.service.center.ServiceResource;

public class DBMonitor implements ISnapShot, IServer {
	HashVO[] vosDataSource = null;
	DbRuntimeChecker[] checkers = new DbRuntimeChecker[0];
	DBSessionChecker[] sessionCheckers = new DBSessionChecker[0];
	DBCfgChecker[] cfgCheckers = new DBCfgChecker[0];
	ServiceResource[] resources = null;

	@Override
	public void startServer() {
		// 读取配置文件。
		try {
			vosDataSource = Xml2HashVO.convertToVO(ServerEnv.getInstance()
					.getProperty("propfile"), "dataSource");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (Toolkit.isEmpty(vosDataSource))
			return;
		// 一个数据源一个检测线程。
		checkers = new DbRuntimeChecker[vosDataSource.length];
		DbSourceInfo serverinfo = null;
		sessionCheckers = new DBSessionChecker[vosDataSource.length];
		cfgCheckers = new DBCfgChecker[vosDataSource.length];
		for (int i = 0; i < vosDataSource.length; i++) {
			serverinfo = new DbSourceInfo();
			serverinfo.setUrl(vosDataSource[i].getStringValue("databaseUrl"));
			serverinfo.setUsername(vosDataSource[i].getStringValue("user"));
			serverinfo.setPassword(new Encode().decode(vosDataSource[i]
					.getStringValue("password")));
			serverinfo.setDriverClassName(vosDataSource[i]
					.getStringValue("driverClassName"));
			serverinfo.setDbtype(vosDataSource[i]
					.getStringValue("databaseType"));
			String datasourceName = vosDataSource[i]
					.getStringValue("dataSourceName");
			serverinfo.setDatasourcename(datasourceName);
			serverinfo.setMinCon(vosDataSource[i].getStringValue("minCon"));
			serverinfo.setMaxCon(vosDataSource[i].getStringValue("maxCon"));
			DBSourceInfoHolder.getInstance().addDBInfo(serverinfo);
			checkers[i] = new DbRuntimeChecker();
			checkers[i].setDBServerInfo(serverinfo);
			checkers[i].start();
			sessionCheckers[i] = new DBSessionChecker();
			sessionCheckers[i].setDBServerInfo(serverinfo);
			sessionCheckers[i].start();
			cfgCheckers[i] = new DBCfgChecker();
			cfgCheckers[i].setDBServerInfo(serverinfo);
			cfgCheckers[i].start();
		}
	}

	@Override
	public void stopServer() {
		for (int i = 0; i < checkers.length; i++) {
			checkers[i].setRuning(false);
			sessionCheckers[i].setRunning(false);
			cfgCheckers[i].setRunning(false);
		}
	}

	@Override
	public ServiceResource[] getServerResouce(ServiceDesc serviceDesc) {
		if (resources == null) {
			resources = new ServiceResource[checkers.length];
			for (int i = 0; i < checkers.length; i++) {
				resources[i] = new ServiceResource();
				resources[i].setDisplayName(checkers[i].getDBServerInfo()
						.getDatasourcename());
				resources[i].setHost(ServerEnv.getHostName());
				resources[i].setPort(ServerEnv.getInstance().getLocalSocket()
						.getPort());
				resources[i].setRequestUrl(ServerEnv.getInstance()
						.getLocalUrl());
				resources[i].setServiceName(serviceDesc.getServiceName());
				resources[i].setServiceDesc(serviceDesc);
				resources[i].setInstanceName(checkers[i].getDBServerInfo()
						.getUrl());
				resources[i].setUserName(checkers[i].getDBServerInfo()
						.getUsername());
				checkers[i].setServiceResource(resources[i]);
				sessionCheckers[i].setServiceResource(resources[i]);
				cfgCheckers[i].setServiceResource(resources[i]);
			}
		}
		return resources;
	}

	public Object exec(String dataSource, String sql) throws Exception {
		for (int i = 0; i < checkers.length; i++) {
			if (Toolkit.isEqual(checkers[i].getDBServerInfo()
					.getDatasourcename(), dataSource))
				return checkers[i].exec(sql);
		}
		return null;
	}

	public void assignClientTask(String dataSource) throws Exception {
		for (int i = 0; i < checkers.length; i++) {
			if (Toolkit.isEqual(checkers[i].getDBServerInfo()
					.getDatasourcename(), dataSource)) {
				checkers[i].assignClientTask();
				return;
			}
		}
	}

	@Override
	public Object dump(ServiceResource dumpResource) {
		// 目前只是对ORACLE生成DUMP.
		DbRuntimeChecker checker = getCheck(dumpResource);
		if (checker == null)
			return null;
		if (checker.getDBServerInfo().getDbtype().toLowerCase()
				.indexOf("oracle") < 0)
			return null;
		return checker.genDump();
	}

	public ServiceResource getResource(DbRuntimeChecker checker) {
		if (Toolkit.isEmpty(checkers))
			return null;
		for (int i = 0; i < resources.length; i++) {
			if (Toolkit.isEqual(resources[i].getDisplayName(), checker
					.getDBServerInfo().getDatasourcename())) {
				return resources[i];
			}
		}
		return null;
	}

	private DbRuntimeChecker getCheck(ServiceResource dumpResource) {
		if (Toolkit.isEmpty(checkers))
			return null;
		for (int i = 0; i < checkers.length; i++) {
			if (Toolkit.isEqual(dumpResource.getDisplayName(), checkers[i]
					.getDBServerInfo().getDatasourcename())) {
				return checkers[i];
			}
		}
		return null;
	}

	public HashVO[] generateIndex(String datasourcename) throws RemoteException {
		DbSourceInfo[] infos = queryServerInfo();
		DbSourceInfo selecteddb = null;
		for (DbSourceInfo info : infos) {
			String name = info.getDatasourcename();
			if (datasourcename.equals(name)) {
				selecteddb = info;
				break;
			}
		}
		DBExecutor excutor = new DBExecutor(selecteddb.getDriverClassName(),
				selecteddb.getUrl(), selecteddb.getUsername(),
				selecteddb.getPassword());
		String url = selecteddb.getUrl();
		int dbtype = url.indexOf("oracle") > -1 ? IConfigConst.ORACLE : (url
				.indexOf("db2") > -1 ? IConfigConst.DB2
				: IConfigConst.SQLSERVER);
		IGenerateIndex indexgenerate = GenerateIndexFactory
				.createGenerateIndex(dbtype);
		return indexgenerate.generateIndex(excutor);

	}
	public DbSourceInfo[] queryServerInfo() {
		return DBSourceInfoHolder.getInstance().getAllServerInfo();
	}

	@Override
	public void refreshEnv() {
		for (int i = 0; i < sessionCheckers.length; i++) {
			sessionCheckers[i].loadCfg();
		}
		for (int i = 0; i < cfgCheckers.length; i++) {
			cfgCheckers[i].loadCfg();
		}
		for (int i = 0; i < checkers.length; i++) {
			checkers[i].loadCfg();
		}
	}
}
