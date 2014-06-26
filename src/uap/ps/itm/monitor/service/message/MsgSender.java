package uap.ps.itm.monitor.service.message;

import uap.ps.itm.monitor.pub.IConfigConst;
import uap.ps.itm.monitor.service.center.ServerEnv;
import uap.ps.itm.monitor.service.center.ServiceHolder;
import uap.ps.itm.pub.servlet.RequestInfo;
import uap.ps.itm.pub.servlet.URLCommuniatcor;

public class MsgSender {
	/*
	 * 将消息发送到消息队列.
	 */
	public static void send(MessageEntry entry) {
		try {
			if(entry==null)
				return;
			if(entry.getMsg()==null)
				return;
			if (ServerEnv.getInstance().isServiceCenter()) {
				Object obj = ServiceHolder.getInstance().getServer(IConfigConst.SERVICEMESSAGE);
				if (obj == null)
					return;
				IMsgReceiver msgReceiver = (MsgProcessor) obj;
				msgReceiver.receive(entry);
			} else {
				RequestInfo req = new RequestInfo();
				req.setMethodName("receive");
				req.setParameter(new Object[] { entry });
				req.setParametertypes(new Class[] { MessageEntry.class });
				req.setServiceName(IConfigConst.SERVICEMESSAGE);
				URLCommuniatcor.remoteCall(req, ServerEnv.getInstance().getServiceCenterUrl());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
