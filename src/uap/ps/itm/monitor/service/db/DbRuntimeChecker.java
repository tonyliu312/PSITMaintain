package uap.ps.itm.monitor.service.db;

import java.util.ArrayList;

import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.center.ServerEnv;
import uap.ps.itm.monitor.service.center.ServiceResource;
import uap.ps.itm.monitor.service.message.MessageEntry;
import uap.ps.itm.monitor.service.message.MsgSender;

import com.ibm.icu.util.StringTokenizer;

public class DbRuntimeChecker extends Thread {
	private boolean isRuning = true;
	private DBExecutor excecutor = null;
	private String[] checkSqls = null;
	private ServiceResource resource = null;
	private String[] dumpSqls = null;
	private String msg = null;
	private ArrayList<HashVO> curRes = null;
	private DbSourceInfo dbServerinfo = null;
	private ArrayList<HashVO> lastRes = null;
	private int isleep = -1;

	public void setServiceResource(ServiceResource resource) {
		this.resource = resource;
	}

	public DBExecutor getExecutor() {
		if (excecutor == null) {
			excecutor = new DBExecutor(dbServerinfo.getDriverClassName(), dbServerinfo.getUrl(), dbServerinfo.getUsername(), dbServerinfo.getPassword());
		}
		return excecutor;
	}

	public void run() {
		setName(dbServerinfo.getDatasourcename() + " checker");
		// 获得SQL执行器
		excecutor = getExecutor();
		// 只是支持对oracle的检查。
		if (dbServerinfo.getDbtype().toLowerCase().indexOf("ora") < 0)
			return;
		// 加载需要执行的sql
		loadCfg();
		if (isleep <= 0)
			return;

		while (isRuning) {
			msg = "";
			dbServerinfo.setLastcheckts(Toolkit.getCurTime());
			if (!Toolkit.isEmpty(curRes)) {
				lastRes = curRes;
			}
			curRes = new ArrayList<HashVO>();
			Object res = null;
			for (int i = 0; i < checkSqls.length; i++) {
				if (resource == null)
					break;
				try {
					res = exec(checkSqls[i]);
					if (!Toolkit.isEmpty(res) && res instanceof HashVO[]) {
						addMsg((HashVO[]) res, checkSqls[i]);
					}
				} catch (Exception e) {
					if (resource != null) {
						MessageEntry entry = new MessageEntry();
						entry.setMsg("[ERROR]" + e.getMessage());
						entry.setTs(Toolkit.getCurTime());
						entry.setMsgResource(resource);
						entry.setNeedDump(false);
						MsgSender.send(entry);
						dbServerinfo.setLastcheckMsg(entry.getMsg());
					}
				}
			}
			dbServerinfo.setLastcheckts(Toolkit.getCurTime());
			dbServerinfo.setLastcheckMsg(msg);
			if (!Toolkit.isEmpty(msg)) {
				sendMsg();
			}
			try {
				if (isleep <= 0)
					break;
				Thread.sleep(isleep);
			} catch (Exception e) {
			}
		}
	}

	private void addMsg(HashVO[] checkRes, String checkSql) {
		msg += "checkSql=" + checkSql + ";";
		for (int i = 0; i < checkRes.length; i++) {
			boolean isExist = false;
			for (int j = 0; j < curRes.size(); j++) {
				if (checkRes[i].isEqual(curRes.get(j))) {
					isExist = true;
					break;
				}
			}
			if (!isExist) {
				curRes.add(checkRes[i]);
			}
		}
	}

	private String[] getMachines() {
		ArrayList<String> alMachines = new ArrayList<String>();
		String machine = null;
		for (int i = 0; i < curRes.size(); i++) {
			machine = curRes.get(i).getStringValue("machine");
			if (Toolkit.isEmpty(machine))
				continue;
			boolean isExist = false;
			for (int j = 0; j < alMachines.size(); j++) {
				if (alMachines.get(j).equalsIgnoreCase(machine)) {
					isExist = true;
					break;
				}
			}
			if (!isExist) {
				alMachines.add(machine);
			}
		}
		for (int i = 0; i < alMachines.size(); i++) {
			machine = alMachines.get(i);
			if (!Toolkit.isEmpty(machine)) {
				machine = machine.replace("\\", "/");
				if (machine.indexOf("/") > -1) {
					StringTokenizer st = new StringTokenizer(machine, "/");
					st.nextToken();
					if (st.hasMoreElements()) {
						machine = st.nextToken();
						alMachines.set(i, machine);
					}
				}
			}
		}
		return alMachines.toArray(new String[alMachines.size()]);
	}

	private String[] getServers() {
		ArrayList<String> alServers = new ArrayList<String>();
		String server = null;
		for (int i = 0; i < curRes.size(); i++) {
			server = curRes.get(i).getStringValue("client_identifier");
			if (Toolkit.isEmpty(server))
				continue;
			boolean isExist = false;
			for (int j = 0; j < alServers.size(); j++) {
				if (alServers.get(j).equalsIgnoreCase(server)) {
					isExist = true;
					break;
				}
			}
			if (!isExist) {
				alServers.add(server);
			}
		}
		for (int i = 0; i < alServers.size(); i++) {
			server = alServers.get(i);
			if (!Toolkit.isEmpty(server)) {
				if (!Toolkit.isEmpty(server)) {
					StringTokenizer st = new StringTokenizer(server, "#");
					if (st.hasMoreElements()) {
						alServers.set(i, st.nextToken());
					}
				}
			}
		}
		return alServers.toArray(new String[alServers.size()]);
	}

	private void sendMsg() {
		// 先确定此次发送的信息是否和上次的有重合.如果有重合则不发送.
		if (Toolkit.isEmpty(curRes) || Toolkit.isEmpty(msg) || checkMsgDup())
			return;
		// construst msg.
		MessageEntry entry = new MessageEntry();
		msg = "[WARNING]" + msg;
		msg = msg.replace("$", "*");
		entry.setMsg(msg);
		entry.setMsgResource(resource);
		entry.setTs(Toolkit.getCurTime());
		HashVO voKey = new HashVO();
		voKey.setAttributeValue("server", getServers());
		voKey.setAttributeValue("machine", getMachines());
		entry.setVoKeyInfo(voKey);
		MsgSender.send(entry);
	}

	private boolean checkMsgDup() {
		if (Toolkit.isEmpty(lastRes) || Toolkit.isEmpty(curRes))
			return false;
		for (int i = 0; i < curRes.size(); i++) {
			for (int j = 0; j < lastRes.size(); j++) {
				if (((HashVO) curRes.get(i)).isEqual((HashVO) lastRes.get(j))) {
					return true;
				}
			}
		}
		return false;
	}

	public void loadCfg() {
		// 只是支持最多8个sql.
		checkSqls = new String[8];
		dumpSqls = new String[8];
		HashVO vo = new HashVO();
		vo.setAttributeValue("user", dbServerinfo.getUsername());
		for (int i = 0; i < checkSqls.length; i++) {
			checkSqls[i] = ServerEnv.getInstance().getProperty("HEALTH_RUNTIME_CHECK_DB__SQL_" + i);
			dumpSqls[i] = ServerEnv.getInstance().getProperty("DB_DUMP_SQL_" + i);
			// 对sql变量进行替换。
			checkSqls[i] = replace(checkSqls[i], vo);
			dumpSqls[i] = replace(dumpSqls[i], vo);
		}
		checkSqls = (String[]) Toolkit.pack(checkSqls);
		dumpSqls = (String[]) Toolkit.pack(dumpSqls);
		// sleep.
		String checkduration = ServerEnv.getInstance().getProperty("DB_RUNTIMECHECK_DURATION");
		isleep = (checkduration == null ? 1000 * 10 : Integer.parseInt(checkduration) * 1000);
	}

	public Object exec(String sql) throws Exception {
		if (sql == null || sql.length() < 8)
			return null;
		return excecutor.exec(sql);
	}
	
	public void assignClientTask() throws Exception {
		excecutor.assignClientTask();
	}

	public void setRuning(boolean b) {
		isRuning = b;
	}

	public boolean isRuning() {
		return isRuning;
	}

	public String replace(String src, HashVO vo) {
		if (src == null)
			return src;
		int ibegin = src.indexOf("[");
		while (ibegin < 0)
			return src;
		int iend = src.indexOf("]");
		String fld = src.substring(ibegin + 1, iend);
		String value = vo.getStringValue(fld);
		src = src.substring(0, ibegin) + value + src.substring(iend + 1, src.length());
		return replace(src, vo);
	}

	public Object genDump() {
		// dump sqls.
		ArrayList<HashVO> al = new ArrayList<HashVO>();
		for (int i = 0; i < dumpSqls.length; i++) {
			Object res = null;
			try {
				res = exec(dumpSqls[i]);
			} catch (Exception e) {
				// e.printStackTrace();
			}
			if (!Toolkit.isEmpty(res) && (res instanceof HashVO[])) {
				HashVO vo = new HashVO();
				vo.setAttributeValue("dumpsql", dumpSqls[i]);
				al.add(vo);
				HashVO[] vos = (HashVO[]) res;
				for (int j = 0; j < vos.length; j++) {
					al.add(vos[j]);
				}
			}
		}
		return al.toArray(new HashVO[al.size()]);
	}

	public void setDBServerInfo(DbSourceInfo serverinfo) {
		dbServerinfo = serverinfo;
	}

	public DbSourceInfo getDBServerInfo() {
		return dbServerinfo;
	}

	public ServiceResource getResource() {
		return resource;
	}
}
