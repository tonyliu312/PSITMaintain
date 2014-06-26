package uap.ps.itm.monitor.service.message;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Handler;
import java.util.logging.Logger;

import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.HashVO2Text;
import uap.ps.itm.monitor.pub.IConfigConst;
import uap.ps.itm.monitor.pub.LogTool;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.center.IServer;
import uap.ps.itm.monitor.service.center.ServerEnv;
import uap.ps.itm.monitor.service.center.ServiceDesc;
import uap.ps.itm.monitor.service.center.ServiceHolder;
import uap.ps.itm.monitor.service.center.ServiceRegister;
import uap.ps.itm.monitor.service.center.ServiceResource;
import uap.ps.itm.monitor.service.center.SnapShotItem;

public class MsgProcessor extends Thread implements IMsgReceiver, IServer {
	private boolean isRunning = true;

	private Logger logger = null;

	private static final String HANDLE_SNAPSHOT = "SNAPSHOT";

	private static final String HANDLE_WARNING = "WARNING";

	private long snapshotid = 0l;

	private ServiceResource resource = null;

	private ServiceRegister register = (ServiceRegister) ServiceHolder
			.getInstance().getServer(IConfigConst.SERVICERGISTERSERVICE);

	private long checkduration = 20l;

	// �����������Ϣ
	private LinkedList<HashVO> msgCache = new LinkedList<HashVO>();
	// ��Ϣ�����С
	private static int cacheSize = 10;

	public void receive(MessageEntry entry) {
		// ����Ϣ������Ϣ����.
		MsgQueue.getInstance().offer(entry);
	}

	public ServiceResource[] getServerResouce(ServiceDesc serviceDesc) {
		if (resource == null) {
			resource = new ServiceResource();
			resource = new ServiceResource();
			resource.setDisplayName(ServerEnv.getHostName());
			resource.setHost(ServerEnv.getLocalAddr());
			resource.setPort(ServerEnv.getInstance().getLocalSocket().getPort());
			resource.setRequestUrl(ServerEnv.getInstance().getLocalUrl());
			resource.setServiceName(serviceDesc.getServiceName());
			resource.setServiceDesc(serviceDesc);
		}
		return new ServiceResource[] { resource };
	}

	public void startServer() {
		this.start();
		setCfg();
		new Thread(new Runnable() {
			public void run() {
				delMsg();
			}
		}).start();

	}

	public void stopServer() {
		isRunning = false;

	}

	private void setCfg() {
		String duration = ServerEnv.getInstance().getProperty(
				"MESSAGE_FETCH_DURATION");
		checkduration = (Toolkit.isEmpty(duration) ? 20l : Long
				.parseLong(duration));
	}

	public void run() {
		setName("messageprocessor");
		while (isRunning) {
			// ����Ϣ������ȡ��һ����Ϣ.
			MessageEntry entry = getMessageFromQueue();
			// ����������Ϣ.
			if (entry != null) {
				try {
					processMsg(entry);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(checkduration);
			} catch (Exception e) {
			}
		}
	}

	private void processMsg(MessageEntry entry) {
		// message�Ĵ���ʽ.
		String snapshotFileName = null;
		String strMsgHandle = getMsgHandle(entry);
		snapshotid = System.currentTimeMillis();
		// �����warning.��ֱ��д����־.
		if (Toolkit.isEqual(strMsgHandle, HANDLE_SNAPSHOT)
				&& entry.getMsgResource() != null && entry.isNeedDump()) {
			// �����snapshot.��ʼ��snapshot.
			snapshotFileName = doSnapshot(entry);
		}
		logMsg(entry, snapshotFileName);
		boolean smok = false;
		if (Toolkit.isEqual(
				ServerEnv.getInstance().getProperty("MESSAGE_SEND_SM"), "true")) {
			String smclass = ServerEnv.getInstance().getProperty(
					"SM_SENDER_IMPCLASS");
			if (!Toolkit.isEmpty(smclass)) {
				try {
					IMsgSender sender = (IMsgSender) this.getClass()
							.getClassLoader().loadClass(smclass).newInstance();
					smok = sender.sendMsg(entry);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (!smok) {
			if (Toolkit.isEqual(
					ServerEnv.getInstance().getProperty("MESSAGE_SEND_MAIL"),
					"true")) {
				MailSender.getInstance().sendMail(entry.toString(),
						snapshotFileName);
			}
		}
	}

	private String doSnapshot(MessageEntry entry) {
		ServiceResource msgResource = entry.getMsgResource();
		// �õ���ҪDUMP��item.
		SnapShotItem[] dumpItems = msgResource.getServiceDesc().getDumpItems();
		if (Toolkit.isEmpty(dumpItems))
			return null;
		// ��Ҫdump��Resource.
		ServiceResource[] resources = null;
		// mkdir for this snapshot.
		String dir = mkdir(snapshotid).getAbsolutePath();
		boolean hasRes = false;
		for (int i = 0; i < dumpItems.length; i++) {
			// �õ���ҪDUMP��RESOURCE.
			resources = getDumpResources(dumpItems[i], entry);
			if (Toolkit.isEmpty(resources))
				continue;
			// �õ�ÿ��resource��DUMP.
			for (int j = 0; j < resources.length; j++) {
				String res = writeDump(dumpResource(resources[j]),
						resources[j], dir);
				if (!Toolkit.isEmpty(res))
					hasRes = true;
			}
		}
		// û�н����ɾ�����Ŀ¼.
		if (!hasRes) {
			new File(dir).delete();
			return null;
		}
		return dir;
	}

	private Object dumpResource(ServiceResource resource) {
		DumpThread t = new DumpThread();
		t.setResource(resource);
		t.start();
		try {
			t.join(1000 * 60);
		} catch (Exception e) {
		}
		Object res = t.getDumpRes();
		if (t.isAlive()) {
			try {
				t.interrupt();
			} catch (Exception e) {
			}
		}
		return res;
	}

	private ServiceResource[] getDumpResources(SnapShotItem item,
			MessageEntry msgEntry) {
		String serviceName = item.getService();
		ServiceDesc service = register.getServiceByname(serviceName);
		if (service == null)
			return null;
		ServiceResource[] resources = register.getResourcesByService(service);
		if (Toolkit.isEmpty(resources))
			return null;
		// get scope.
		String scope = item.getScope();
		if (Toolkit.isEqual(scope, IConfigConst.SCOPE_ALL))
			return resources;
		// machine.
		HashVO vo = msgEntry.getVosKeyInfo();
		ServiceResource msgResource = msgEntry.getMsgResource();
		ArrayList<ServiceResource> al = new ArrayList<ServiceResource>();
		if (Toolkit.isEqual(scope, IConfigConst.SCOPE_MACHINE)) {
			Object res = vo.getAttributeValue("machine");
			if (Toolkit.isEmpty(res))
				return resources;
			String[] hosts = (String[]) res;
			for (int i = 0; i < resources.length; i++) {
				for (int j = 0; j < hosts.length; i++) {
					if (Toolkit.isEqual(hosts[j], resources[i].getHost())
							|| Toolkit.isEqual(resources[i].getHost(),
									msgResource.getHost())) {
						al.add(resources[i]);
					}
				}
			}
			// �����Ȼû��,��ȫ��.
			if (Toolkit.isEmpty(al)) {
				return resources;
			}
			// instance.
		} else {
			String instName = msgEntry.getMsgResource().getDisplayName();
			for (int i = 0; i < resources.length; i++) {
				if (Toolkit.isEqual(instName, resources[i].getDisplayName())
						&& Toolkit.isEqual(serviceName,
								resources[i].getServiceName())) {
					al.add(resources[i]);
				} else {
					Object res = (msgEntry.getVosKeyInfo() == null ? null
							: msgEntry.getVosKeyInfo().getAttributeValue(
									"server"));
					if (!Toolkit.isEmpty(res)) {
						String[] servers = (String[]) res;
						for (int j = 0; j < servers.length; j++) {
							if (Toolkit.isEqual(servers[j],
									resources[i].getDisplayName())) {
								al.add(resources[i]);
							}
						}
					}
				}
			}
		}
		return al.toArray(new ServiceResource[al.size()]);
	}

	private String getMsgHandle(MessageEntry entry) {
		if (entry.getMsgResource() == null)
			return HANDLE_WARNING;
		return entry.getMsgResource().getServiceDesc().getMsgHandler();
	}

	private MessageEntry getMessageFromQueue() {
		Object objRes = MsgQueue.getInstance().poll();
		if (objRes == null)
			return null;
		return (MessageEntry) objRes;
	}

	public Logger getLogger() {
		synchronized (this) {
			if (logger == null) {
				logger = LogTool.createLogger("snapshot", "snapshot",
						"snapshotidx");
			}
		}
		return logger;
	}

	public void logMsg(MessageEntry msgEntry, String snapshotFile) {
		if (msgEntry.getMsgResource() == null) {
			System.out.println("resource is null");
			return;
		}
		if (Toolkit.isEmpty(msgEntry.getMsg())) {
			System.out.println("message is null");
			return;
		}
		String msg = msgEntry.getMsg();
		msg = msg.replace("\n", "#");
		HashVO voMsg = new HashVO();
		voMsg.setAttributeValue("ts", msgEntry.getTs());
		voMsg.setAttributeValue("service", msgEntry.getMsgResource()
				.getServiceName()
				+ ":"
				+ msgEntry.getMsgResource().getDisplayName());
		voMsg.setAttributeValue("message", msg);
		voMsg.setAttributeValue("snapshotfile", snapshotFile == null ? "N/A"
				: snapshotFile);
		voMsg.setAttributeValue("snapshotid", snapshotid);
		// ������Ϣ
		cacheMsg(voMsg);
		getLogger().info(voMsg.toString0());
	}

	/**
	 * ������Ϣ
	 * 
	 * @param entry
	 *            ��Ϣ��
	 */
	private void cacheMsg(HashVO vo) {
		if (msgCache.size() > cacheSize) {
			msgCache.removeFirst();
		}
		msgCache.offer(vo);
	}

	/**
	 * ��ȡ���µ���Ϣ��Ϊportal�ṩ����
	 * 
	 * @return ���µ���Ϣ
	 */
	public HashVO[] getLatestMsg() {
		return msgCache.toArray(new HashVO[0]);
	}

	private File mkdir(long curtime) {
		if (!new File("./logs").exists())
			new File("./logs").mkdir();
		if (!new File("./logs/snapshot").exists())
			new File("./logs/snapshot").mkdir();
		String filedir = "./logs/snapshot/snapshot-" + curtime;
		File fileDir = new File(filedir);
		fileDir.mkdir();
		return fileDir;
	}

	private String writeDump(Object dumpContent, ServiceResource resource,
			String fileDir) {
		if (Toolkit.isEmpty(dumpContent))
			return null;
		String filename = fileDir + "/" + resource.getServiceName() + "-"
				+ resource.getDisplayName();
		if (dumpContent instanceof HashVO[]) {
			HashVO2Text.writeToFile(filename, (HashVO[]) dumpContent, "$");
		} else {
			HashVO[] vos = new HashVO[] { new HashVO() };
			vos[0].setAttributeValue("msg", dumpContent.toString());
			HashVO2Text.writeToFile(filename, vos, "$");
		}
		return filename;
	}

	public void doSnapManully() {
		MessageEntry entry = new MessageEntry();
		entry.setMsg("�����ֹ�����");
		entry.setTs(Toolkit.getCurTime());
		entry.setMsgResource(resource);
		MsgSender.send(entry);
	}

	public HashVO[] querySnapshot(String filename) {
		return new MessageQuery().querySnapShot(filename);
	}

	public HashVO[] getMsg(String timefrom) {
		return new MessageQuery().queryMessage(timefrom, null);
	}

	public HashVO[] getMsg(String fromTime, String endTime) {
		return new MessageQuery().queryMessage(fromTime, endTime);
	}

	public void delMsg() {
		Handler[] handlers = getLogger().getHandlers();
		for (int i = 0; i < handlers.length; i++) {
			handlers[i].close();
		}
		Toolkit.delDir("./logs/snapshot");
		Toolkit.delDir("./logs/temp");
		logger = null;
	}

	public void refreshEnv() {
		setCfg();
	}
}
