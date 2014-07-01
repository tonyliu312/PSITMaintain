/**
 *
 */
package uap.ps.itm.npr.service.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.monitor.Exception.RemoteException;
import nc.monitor.pub.HashVO;
import nc.monitor.pub.Toolkit;
import uap.ps.itm.npr.service.INPRConst;
import uap.ps.itm.npr.vo.XPRPluginResult;

/**
 * 消息快照信息插件
 *
 * @author liuzy
 *
 */
public class MsgSnapshotPlugImpl extends AbsNPRPlugin {

	private final List<HashVO> list = new ArrayList<HashVO>();
	private final Map<String, Set<String>> map = new HashMap<String, Set<String>>();

	/*
	 * (non-Javadoc)
	 *
	 * @see uap.ps.itm.npr.service.plugin.INPRPluginItf#process(java.io.File,
	 * nc.monitor.pub.HashVO[])
	 */

	@Override
	public void process(File file, HashVO[] dataVOs) throws RemoteException {
		if (Toolkit.isEmpty(file))
			return;
		if (file.getName().indexOf(INPRConst.NMC_SNAPSHOT_FILE_NAME) < 0) {
			String parent = file.getParentFile().getName();
			if (parent.indexOf(INPRConst.NMC_LOG_SNAPSHOT_DIR_NAME) < 0)
				return;
			parent = parent.substring(9, parent.length());
			Set<String> snapSet = map.get(parent);
			if (Toolkit.isEmpty(snapSet)) {
				snapSet = new HashSet<String>();
				map.put(parent, snapSet);
			}
			snapSet.add(file.getName());
		} else {
			for (HashVO vo : dataVOs) {
				if (!checkVOBeginTime(vo))
					continue;
				try {
				if (!checkVOEndTime(vo))
						break;
				} catch (Exception e) {
					continue;
				}
				String service = vo
						.getStringValue(INPRPluginConst.NMC_SNAPSHOT_SERVICE);
				if (Toolkit.isEmpty(service))
					continue;
				String snapshotfile = vo
						.getStringValue(INPRPluginConst.NMC_SNAPSHOT_SNAPSHOTFILE);
				if (Toolkit.isEmpty(snapshotfile) || "N/A".equals(snapshotfile))
					continue;
				HashVO hsvo = new HashVO();
				hsvo.setAttributeValue("快照时间",
						vo.getStringValue(INPRPluginConst.TS));
				hsvo.setAttributeValue("服务",
						vo.getStringValue(INPRPluginConst.NMC_SNAPSHOT_SERVICE));
				hsvo.setAttributeValue("消息",
						vo.getStringValue(INPRPluginConst.NMC_SNAPSHOT_MESSAGE));
				hsvo.setAttributeValue(
						"快照ID",
						vo.getStringValue(INPRPluginConst.NMC_SNAPSHOT_SNAPSHOTID));
				list.add(hsvo);
			}
			// this.addContentVOs(list.toArray(new HashVO[list.size()]));
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uap.ps.itm.npr.service.plugin.AbsNPRPlugin#getResultVO()
	 */
	@Override
	public XPRPluginResult getResultVO() throws RemoteException {
		if (Toolkit.isEmpty(list))
			return null;
		String href_pre = "<p><a href=\"snapshot/snapshot-";
		String href_mid = ".html\" target=\"_blank\" >";
		String href_end = "</a></p>";
		for (HashVO vo : list) {
			String snapid = vo.getStringValue("快照ID");
			if (Toolkit.isEmpty(snapid))
				continue;
			Set<String> snapFiles = map.get(snapid);
			if (Toolkit.isEmpty(snapFiles))
				continue;
			StringBuilder s = new StringBuilder();
			for (String fileName : snapFiles) {
				s.append(href_pre);
				s.append(snapid);
				s.append("-");
				s.append(fileName);
				s.append(href_mid);
				s.append(snapid);
				s.append("-");
				s.append(fileName);
				s.append(href_end);
				vo.setAttributeValue("快照详细", s.toString());
			}
		}
		this.addContentVOs(list.toArray(new HashVO[list.size()]));
		return super.getResultVO();
	}

}
