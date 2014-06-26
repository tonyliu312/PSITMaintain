package uap.ps.itm.monitor.service.process;

import java.io.File;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.IConfigConst;
import uap.ps.itm.monitor.pub.LogTool;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.center.ServerEnv;
import uap.ps.itm.monitor.service.message.MessageQuery;

import com.ibm.icu.util.Calendar;

public class PurgeSnapshotThread extends Thread {
	private boolean isRun = false;

	private Logger logger = null;

	private static PurgeSnapshotThread instance = null;

	public static long purgeSnapshotInterval = 60 * 60 * 1000;
	public static long purgeSnapshotHour = 0;

	static {
		try {
			IConfigConst.init();
			purgeSnapshotHour = Long.parseLong(ServerEnv.getInstance()
					.getProp().getProperty("PURGE_SNAPSHOT_HOUR", "0"));
		} catch (Exception e) {
		}
	}

	public static PurgeSnapshotThread getInstance() {
		if (null == instance) {
			instance = new PurgeSnapshotThread();
		}
		return instance;
	}

	public void run() {

		while (isRun) {
			try {
				Thread.sleep(purgeSnapshotInterval);
				purgeSnapshot();
			} catch (Exception e) {
			}
		}

	}

	public void setRun(boolean isRun) {
		this.isRun = isRun;
	}

	private void purgeSnapshot() {

		GregorianCalendar cal = new GregorianCalendar();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		if (purgeSnapshotHour == hour) {
			purge();
			delLog();
			getLogger().info("purge snapshot! ts=" + Toolkit.getCurTime());
		}		
	}
private void delLog(){
	try{
		Toolkit.delDir("./logs/temp");
	}catch(Throwable e){
		
	}
}
	private void purge() {
		HashVO[] vos = new MessageQuery().queryAllMessage();
		if (null == vos || 0 == vos.length) {
			return;
		}

		Set<String> idxs = new HashSet<String>();
		for (HashVO vo : vos) {
			idxs.add(vo.getStringValue("snapshotid"));
		}

		File dir = new File("./logs/snapshot");
		if (!dir.exists()) {
			return;
		}
		File[] allfiles = dir.listFiles();
		if (null == allfiles || 0 == allfiles.length) {
			return;
		}
		String pre = "snapshot-";
		for (File file : allfiles) {
			if (!file.isDirectory()) {
				continue;
			}
			String fileName = file.getName();
			if (!fileName.startsWith(pre)) {
				continue;
			}
			String snapshotid = fileName.substring(fileName.indexOf(pre) + 9);
			if (!idxs.contains(snapshotid)) {
				Toolkit.delDir(file.getAbsolutePath());
			}
		}
	}

	public Logger getLogger() {
		synchronized (this) {
			if (logger == null) {
				logger = LogTool.createLogger("purgelog", "purgelog",
						"purgelog");
			}
		}
		return logger;
	}

	private PurgeSnapshotThread() {

	}

	public static void main(String[] args) {

		new PurgeSnapshotThread().purge();
	}
}
