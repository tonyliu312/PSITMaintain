package uap.ps.itm.monitor.service.db;

import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.center.ServerEnv;
import uap.ps.itm.monitor.service.message.MessageEntry;
import uap.ps.itm.monitor.service.message.MsgSender;

public class DBCfgChecker extends DbRuntimeChecker {
	private boolean isRunning = false;
	private String[] checkSqls = null;
	private int sleep = -1;
 
	public void run() {
		isRunning = true;
		DbSourceInfo dbServerInfo = getDBServerInfo();
		setName(dbServerInfo.getDatasourcename() + " cfgchecer");
		if (dbServerInfo.getDbtype().toLowerCase().indexOf("ora") < 0)
			return;
		// 加载配置
		loadCfg();
		if (sleep <=0)
			return;
		HashVO[] vos = null;
		boolean checkPass = true;
		while (isRunning) {
			checkPass = true;
			try {
				if (getResource() == null)
					continue;
				if (Toolkit.isEmpty(checkSqls))
					break;
				dbServerInfo.setLastcheckts(Toolkit.getCurTime());
				for (int i = 0; i < checkSqls.length; i++) {
					Object res = getExecutor().queryVOBySql(checkSqls[i]);
					if (Toolkit.isEmpty(res))
						continue;
					checkPass = false;
					vos = (HashVO[]) res;
					updateMsg(checkSqls[i], vos);
				}
			} catch (Exception e) {
				//e.printStackTrace();
				MessageEntry entry = new MessageEntry();
				entry.setMsg("[ERROR]" + e.getMessage());
				entry.setTs(Toolkit.getCurTime());
				entry.setMsgResource(getResource());
				entry.setNeedDump(false);
				MsgSender.send(entry);
				break;
			} finally {
				try {
					if (sleep <= 0)
						break;
					Thread.sleep(1000 * sleep);
				} catch (Exception e) {
				}
			}
			if (checkPass) {
				dbServerInfo.setLastcheckMsg(null);
			}
		}
	}

	private void updateMsg(String sql, HashVO[] vos) {
		if (Toolkit.isEmpty(vos) || sql == null)
			return;
		String msg = "[WARNING]" + sql;
		msg = msg.replace("$", "*");
		msg += "\nresult:";
		for (int i = 0; i < vos.length; i++) {
			msg += vos[i].toXml0();
		}
		MessageEntry entry = new MessageEntry();
		entry.setMsg(msg);
		entry.setMsgResource(getResource());
		entry.setTs(Toolkit.getCurTime());
		entry.setNeedDump(false);
		MsgSender.send(entry);
		getDBServerInfo().setLastcheckts(Toolkit.getCurTime());
		getDBServerInfo().setLastcheckMsg(msg);
	}

	public void setRunning(boolean b) {
		isRunning = b;
	}

	public void loadCfg() {
		String prop = ServerEnv.getInstance().getProperty("DB_CONFCHECK_DURATION");
		sleep = (prop == null ? 5 : Integer.parseInt(prop));
		// 只是支持最多4个sql.
		checkSqls = new String[4];
		HashVO vo = new HashVO();
		vo.setAttributeValue("user", getDBServerInfo().getUsername());
		for (int i = 0; i < checkSqls.length; i++) {
			checkSqls[i] = ServerEnv.getInstance().getProperty("HEALTH_CONF_CHECK_DB__SQL_" + i);
			// 对sql变量进行替换。
			checkSqls[i] = replace(checkSqls[i], vo);
		}
		checkSqls = (String[]) Toolkit.pack(checkSqls);
	}
}
