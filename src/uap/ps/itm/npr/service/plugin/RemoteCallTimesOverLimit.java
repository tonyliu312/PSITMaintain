/**
 *
 */
package uap.ps.itm.npr.service.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nc.monitor.Exception.RemoteException;
import nc.monitor.pub.HashVO;
import nc.monitor.pub.Toolkit;
import nc.monitor.service.center.ServerEnv;
import uap.ps.itm.npr.service.INPRConst;
import uap.ps.itm.npr.vo.XPRPluginResult;

/**
 * 远程调用次数超标统计
 *
 * @author liuzy
 *
 */
public class RemoteCallTimesOverLimit extends DefaultTop10PluginImpl {

	private final List<HashVO> list = new ArrayList<HashVO>();

	private final String remoteCallTimesColName = "远程调用次数";

	// private int REMOTE_CALL_TIMES_LIMIT = 8;

	private final int REMOTE_CALL_TIMES_LIMIT = Integer.valueOf(ServerEnv
			.getInstance().getProperty(INPRConst.NPR_REMOTECALL_TIMES_LIMIT));

	/*
	 * (non-Javadoc)
	 *
	 * @see uap.ps.itm.npr.service.plugin.DefaultTop10PluginImpl#process(java.io.File,
	 * nc.monitor.pub.HashVO[])
	 */
	@Override
	public void process(File file, HashVO[] dataVOs) throws RemoteException {
		if (!checkProc(file, dataVOs))
			return;
		if (isMasterFile(file))
			return;
		String currBusiAction = null;
		HashVO currVO = null;
		for (HashVO vo : dataVOs) {
			try {
			if (!checkVO(vo))
				continue;
			if (!checkVOBeginTime(vo))
				continue;
			if (!checkVOEndTime(vo))
					break;
			if (checkVOByFilter(vo))
				continue;
			String busi = vo.getMsgStringValueByField(
					INPRPluginConst.NC_MW_BUSIACTION, null);
			String addr = vo.getMsgStringValueByField(
					INPRPluginConst.NC_MW_REMOTEADDR, null);
			if (Toolkit.isEmpty(busi) || busi.equals(INPRPluginConst.UNKOWN))
				continue;
			if ((busi + addr).equals(currBusiAction)) {
				int count = currVO.getIntegerValue(remoteCallTimesColName);
				count += 1;
				currVO.setAttributeValue(remoteCallTimesColName, count);
				// String method = currVO
				// .getStringValue(INPRPluginConst.PLUGIN_COLUMN_REMOTECALLMETHOD);
				// method += vo.getMsgStringValueByField(
				// INPRPluginConst.NC_MW_REMOTECALLMETHOD, null) + ";\n";
				// currVO.setAttributeValue(
				// INPRPluginConst.PLUGIN_COLUMN_REMOTECALLMETHOD, method);
			} else {
				if (list.size() > 0) {
					HashVO lastVO = list.get(list.size() - 1);
					int times = lastVO.getIntegerValue(remoteCallTimesColName);
					if (times <= REMOTE_CALL_TIMES_LIMIT)
						list.remove(list.size() - 1);
				}
				currVO = new HashVO();
				currVO.setAttributeValue("排名", "");
				currVO.setAttributeValue(
						INPRPluginConst.PLUGIN_COLUMN_BUSIACTION, busi);
				currVO.setAttributeValue(
						INPRPluginConst.PLUGIN_COLUMN_REMOTEADDR, addr);
				currVO.setAttributeValue(remoteCallTimesColName, 1);
				// currVO.setAttributeValue(
				// INPRPluginConst.PLUGIN_COLUMN_REMOTECALLMETHOD,
				// vo.getMsgStringValueByField(
				// INPRPluginConst.NC_MW_REMOTECALLMETHOD, null)
				// + ";\n");
				currBusiAction = busi + addr;
				list.add(currVO);
			}
			} catch (Exception e) {
				continue;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uap.ps.itm.npr.service.plugin.DefaultTop10PluginImpl#getResultVO()
	 */
	@Override
	public XPRPluginResult getResultVO() throws RemoteException {
		HashVO[] vos = list.toArray(new HashVO[list.size()]);
		Toolkit.sort(vos, new Top10DefComparetor(this.remoteCallTimesColName,
				false));
		List<String> l1 = new ArrayList<String>();
		List<HashVO> contentVOs = new ArrayList<HashVO>();
		int i = 1;
		int j = vos.length;
		while (true) {
			j--;
			if (j == -1)
				break;
			vos[j].setAttributeValue("排名", String.valueOf(i));
			int count = vos[j].getIntegerValue(this.remoteCallTimesColName);
			if (count < this.REMOTE_CALL_TIMES_LIMIT)
				continue;
			String busi = vos[j]
					.getStringValue(INPRPluginConst.PLUGIN_COLUMN_BUSIACTION);
			if (l1.contains(busi))
				continue;
			l1.add(busi);
			contentVOs.add(vos[j]);
			i++;
			if (j == 0)
				break;
		}
		XPRPluginResult result = new XPRPluginResult(this.getPluginInfo());
		String[] comments = { "该部分数据在并发较大的时段更为精确" };
		result.setNoteLines(comments);
		result.setContentVos(contentVOs.toArray(new HashVO[contentVOs.size()]));
		return result;
	}

}
