package uap.ps.itm.npr.service.plugin;

import java.io.File;

import nc.monitor.Exception.RemoteException;
import nc.monitor.pub.HashVO;
import uap.ps.itm.npr.vo.XPRPluginResult;

public class NprSummaryImpl extends AbsNPRPlugin {


	public void process(File file, HashVO[] dataVOs) throws RemoteException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uap.ps.itm.npr.service.plugin.AbsNPRPlugin#getResultVO(nc.monitor.pub.HashVO)
	 */

	public XPRPluginResult getResultVO(HashVO pluginInfo) throws RemoteException {

		XPRPluginResult retVO = new XPRPluginResult(this.getPluginInfo());

		HashVO hashvo = new HashVO();
		hashvo.setAttributeValue("��ʼʱ��", this.getBeginTime().toString());
		hashvo.setAttributeValue("����ʱ��", this.getEndTime().toString());
		hashvo.setAttributeValue("����ϵͳ", "Redhat Linux 6 64bit");
		hashvo.setAttributeValue("���ݿ�", "Oracle Database 11g release2");
		retVO.setContentVos(new HashVO[] { hashvo });

		HashVO hashvo2 = new HashVO();
		hashvo2.setAttributeValue("��ʼʱ��", this.getBeginTime().toString());
		hashvo2.setAttributeValue("����ʱ��", this.getEndTime().toString());
		hashvo2.setAttributeValue("����ϵͳ", "Redhat Linux 6 64bit");
		hashvo2.setAttributeValue("���ݿ�", "Oracle Database 11g release2");

		HashVO hashvo3 = new HashVO();
		hashvo3.setAttributeValue("��ʼʱ��", this.getBeginTime().toString());
		hashvo3.setAttributeValue("����ʱ��", this.getEndTime().toString());
		hashvo3.setAttributeValue("����ϵͳ", "Redhat Linux 6 64bit");
		hashvo3.setAttributeValue("���ݿ�", "Oracle Database 11g release2");
		retVO.setContentVos(new HashVO[] { hashvo, hashvo2, hashvo3 });
		return retVO;

	}

}
