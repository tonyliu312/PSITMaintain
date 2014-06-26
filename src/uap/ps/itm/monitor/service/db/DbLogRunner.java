package uap.ps.itm.monitor.service.db;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.LogTool;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.process.MonitorEnv;

/**
 *
 * <code>DbLogRunner<code>
 * <strong></strong>
 * <p>说明：
 * <li></li>
 * </p>
 * @since NC6.3
 * @version 2013-6-14 下午04:44:50
 * @author tangxx
 */

public class DbLogRunner extends DbRuntimeChecker {
	private static Map<String, Logger> logMap = new HashMap<String, Logger>();

	@Override
	public void run() {

		while (!MonitorEnv.getInstance().isShutdown()) {
			DbSourceInfo[] dbinfos = DBSourceInfoHolder.getInstance()
					.getAllServerInfo();
			if (!Toolkit.isEmpty(dbinfos)) {
				for (DbSourceInfo info : dbinfos) {
					HashVO vo = new HashVO();
					vo.setAttributeValue("ts", Toolkit.getCurTime());
					vo.setAttributeValue("activesessions",
							info.getActiveSession());
					vo.setAttributeValue("redo_size", info.getRedo_size());
					vo.setAttributeValue("user_commits", info.getUser_commits());
					vo.setAttributeValue("physical_reads",
							info.getPhysical_reads());
					vo.setAttributeValue("physical_writes",
							info.getPhysical_write());
					vo.setAttributeValue("user_calls", info.getUser_calls());
					vo.setAttributeValue("db_block_gets",
							info.getDb_block_gets());
					vo.setAttributeValue("sorts_(disk)", info.getSorts());
					getDbLogger(info.getDatasourcename()).info(vo.toString0());
				}
				try {
					Thread.sleep(1000 * 600);// 十分钟记录一次
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				try {
					Thread.sleep(1000 * 10);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}

		}

	}

	private Logger getDbLogger(String datasourcename) {
		Logger dbLogger = logMap.get(datasourcename);
		if (dbLogger == null) {
			dbLogger = LogTool.createLogger("db", "db", datasourcename);
			logMap.put(datasourcename, dbLogger);
		}
		return dbLogger;
	}

}
