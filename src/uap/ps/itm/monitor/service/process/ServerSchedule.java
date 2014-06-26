package uap.ps.itm.monitor.service.process;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.pub.Xml2HashVO;
import uap.ps.itm.pub.HashVO2XML;

/**
 * @author 赵国滨　2010-12-23
 * 服务器重启时间调度
 *
 */
public class ServerSchedule {

	private static String name = "./conf/serverschedule.xml";
	private static Map<String, HashVO> vos = new HashMap<String, HashVO>();
	private static ServerSchedule instance = null;

	public void addSchedule(HashVO vo) {
		String servername = vo.getStringValue("servername");
		vos.put(servername, vo);
	}

	public void saveServerSchedule() {
		HashVO[] allvos = new HashVO[vos.size()];
		int i = 0;
		for (HashVO vo : vos.values()) {
			allvos[i++] = vo;
		}
		HashVO2XML.writeToXmlFile(name, allvos, "schedule");
	}

	public void loadServerSchedule() {
		try {
			if (new File(name).exists()) {
				HashVO[] allvos = Xml2HashVO.convertToVO(name, "schedule");
				if (!Toolkit.isEmpty(allvos)) {
					for (HashVO vo : allvos) {
						addSchedule(vo);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ServerSchedule getInstance() {
		if (null == instance) {
			instance = new ServerSchedule();
		}
		return instance;
	}

	public HashVO getSchedule(String servername) {
		if (!vos.containsKey(servername)) {
			HashVO vo = new HashVO();
			vo.setAttributeValue("servername", servername);
			vo.setAttributeValue("scheduletime", "25:00");
			vo.setAttributeValue("scheduledays", "0");
			vos.put(servername, vo);
		}
		return vos.get(servername);
	}

	private ServerSchedule() {
		loadServerSchedule();
	}
}
