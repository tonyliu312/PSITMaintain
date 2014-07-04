/**
 * 
 */
package uap.ps.itm.npr.service.plugin;

import java.io.File;
import java.util.ArrayList;

import nc.monitor.Exception.RemoteException;
import nc.monitor.pub.HashVO;
import nc.monitor.pub.IConfigConst;
import nc.monitor.pub.Toolkit;
import nc.monitor.service.center.ServiceHolder;
import nc.monitor.service.center.ServiceRegister;
import nc.monitor.servlet.RequestInfo;
import nc.monitor.servlet.URLCommuniatcor;
import uap.ps.itm.npr.service.ReportCentral;
import uap.ps.itm.npr.vo.XPRPluginResult;

/**
 * @author liuzy
 * 
 */
public class ServerHardInfoPluginImpl extends AbsNPRPlugin {

	/*
	 * (non-Javadoc)
	 * 
	 * @see uap.ps.itm.npr.service.plugin.INPRPluginItf#process(java.io.File,
	 * nc.monitor.pub.HashVO[])
	 */
	@Override
	public void process(File file, HashVO[] dataVOs) throws RemoteException {
		System.out.println(System.getProperty("java.library.path"));
		if (!Toolkit.isEmpty(this.getContentVOList()))
			return;
		HashVO localVO = new ReportCentral().getServerHardInfo();
		this.addContentVO(localVO);
		String targeturl = null;
		ServiceRegister sg = (ServiceRegister) ServiceHolder.getInstance()
				.getServer(IConfigConst.SERVICERGISTERSERVICE);
		String[] remotes = sg.getRemoteHost();
		if (!Toolkit.isEmpty(remotes)) {
			for (int i = 0; i < remotes.length; i++) {
				targeturl = "http://" + remotes[i] + "/remotecall";
				RequestInfo request = new RequestInfo();
				request.setMethodName("getServerHardInfo");
				request.setServiceName("itmcentral");
				request.setServer2Server(true);
				HashVO vo = (HashVO) URLCommuniatcor.remoteCall(request,
						targeturl);
				this.addContentVO(vo);
			}
		}
	}

}
