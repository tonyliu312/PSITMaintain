package uap.ps.itm.monitor.service.db;

import java.util.HashMap;
import java.util.Map;

import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.center.ServerEnv;
import uap.ps.itm.monitor.service.message.MessageEntry;
import uap.ps.itm.monitor.service.message.MsgSender;

public class DBSessionChecker extends DbRuntimeChecker {
	private boolean isRunning = false;
	private int sleep = -1;
	// private Logger logger = null;
	private final static String addindex = "'redo size','user commits','physical reads','physical writes','user calls','db block gets','sorts (disk)'";
	private static Map<String, HashVO[]> datasourceMapHashvo = new HashMap<String, HashVO[]>();// 记录该数据源的这几个指标值

	@Override
	public void run() {
		isRunning = true;
		DbSourceInfo dbServerInfo = getDBServerInfo();
		setName(dbServerInfo.getDatasourcename() + " session checer");
		String sql = null;
		String addindexsql = null;
		if (dbServerInfo.getDbtype().toLowerCase().indexOf("ora") > -1) {
			String tablename = dbServerInfo.isRac() ? "gv$session" : "v$session";
			sql = "select count(*) as  count from " + tablename + " where status='ACTIVE' ";
			sql += " and username='" + dbServerInfo.getUsername().toUpperCase() + "'";
			addindexsql = "select name,value from v$sysstat where name in ("
					+ addindex + ")order by name";
		} else if(dbServerInfo.getDbtype().toLowerCase().indexOf("sqlserver") > -1){
			sql = "select count(*) as count from  sys.dm_exec_sessions where status='running' ";
		}else if(dbServerInfo.getDbtype().toLowerCase().indexOf("db2") > -1){
			sql="select count(*) as count from sysibmadm.applications a where  a.appl_status not in ('UOWWAIT','CONNECTED')";

		}else{
			return;
		}
		loadCfg();
		if (sleep <= 0)
			return;
		while (isRunning) {
			try {
				if (getResource() == null)
					continue;
				Object res = getExecutor().queryVOBySql(sql);
				if (Toolkit.isEmpty(res))
					break;
				HashVO[] vos = (HashVO[]) res;
				dbServerInfo.setActiveSession(vos[0].getIntegerValue("count").intValue());
				HashVO vo=new HashVO();
				vo.setAttributeValue("ts", Toolkit.getCurTime());
				vo.setAttributeValue("activesessions", vos[0].getStringValue("count"));
				vo.setAttributeValue("redo_size", 0);
				vo.setAttributeValue("user_commits", 0);
				vo.setAttributeValue("physical_reads", 0);
				vo.setAttributeValue("physical_writes", 0);
				vo.setAttributeValue("user_calls", 0);
				vo.setAttributeValue("db_block_gets", 0);
				vo.setAttributeValue("sorts_(disk)", 0);
				if (dbServerInfo.getDbtype().toLowerCase().indexOf("orac") != -1) {
					res = getExecutor().queryVOBySql(addindexsql);
					if (res != null) {
						vos = (HashVO[]) res;
						boolean isFirstflag = false;
						HashVO[] oldVos = datasourceMapHashvo.get(dbServerInfo
								.getDatasourcename());
						if (oldVos == null || oldVos.length == 0) {
							isFirstflag = true;
						}

						for (int i = 0; i < vos.length; i++) {
							String name = vos[i].getStringValue("name"
									.toUpperCase());
							String value = vos[i].getStringValue("value"
									.toUpperCase());
							Long varvalue = new Long(0);
							if (!isFirstflag)
								varvalue = (Long.parseLong(value) - Long
										.parseLong(oldVos[i]
												.getStringValue("value"
														.toUpperCase())))
										/ sleep;

							if (name.equals("redo size")) {
								vo.setAttributeValue("redo_size", varvalue);
								dbServerInfo.setRedo_size(varvalue);
								continue;
							} else if (name.equals("user commits")) {
								vo.setAttributeValue("user_commits", varvalue);
								dbServerInfo.setUser_commits(varvalue);
								continue;
							} else if (name.equals("physical reads")) {
								vo.setAttributeValue("physical_reads", varvalue);
								dbServerInfo.setPhysical_reads(varvalue);
								continue;
							} else if (name.equals("physical writes")) {
								vo.setAttributeValue("physical_writes",
										varvalue);
								dbServerInfo.setPhysical_write(varvalue);
								continue;
							} else if (name.equals("user calls")) {
								vo.setAttributeValue("user_calls", varvalue);
								dbServerInfo.setUser_calls(varvalue);
								continue;
							} else if (name.equals("db block gets")) {
								vo.setAttributeValue("db_block_gets", varvalue);
								dbServerInfo.setDb_block_gets(varvalue);
								continue;
							} else if (name.equals("sorts (disk)")) {
								vo.setAttributeValue("sorts_(disk)", varvalue);
								dbServerInfo.setSorts(varvalue);
								continue;
							}
						}

						datasourceMapHashvo.put(
								dbServerInfo.getDatasourcename(), vos);
					}
				}
				// getLogger().info(vo.toString0());
			} catch (Exception e) {
				//e.printStackTrace();
				MessageEntry entry = new MessageEntry();
				entry.setMsg("[ERROR] sql=" + sql.replace("$", "*") + "\n" + e.getMessage());
				entry.setTs(Toolkit.getCurTime());
				entry.setMsgResource(getResource());
				entry.setNeedDump(false);
				MsgSender.send(entry);
				HashVO vo = new HashVO();
				vo.setAttributeValue("ts", Toolkit.getCurTime());
				vo.setAttributeValue("msg", entry.getMsg());

				// getLogger().info(vo.toString0());
				//break;
			} finally {
				try {
					if (sleep <= 0)
						break;
					Thread.sleep(1000 * sleep);
				} catch (Exception e) {
				}
			}
		}
	}

	// private Logger getLogger() {
	// if (logger == null) {
	// logger = LogTool.createLogger("db", "db",
	// getDBServerInfo().getDatasourcename());
	// }
	// return logger;
	// }

	@Override
	public void loadCfg() {
		String prop = ServerEnv.getInstance().getProperty("ACTIVESESSSION_CHECK_DURATION");
		sleep = (prop == null ? 10 : Integer.parseInt(prop));
	}

	public void setRunning(boolean b) {
		isRunning = b;
	}
}
