package uap.ps.itm.monitor.service.message;

import java.util.LinkedList;

import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.center.ServerEnv;

public class MsgQueue {
	private static MsgQueue m_inst = null;

	private static int length = 10;

	private LinkedList<Object> list = new LinkedList<Object>();

	public static MsgQueue getInstance() {
		if (m_inst == null) {
			m_inst = new MsgQueue();
		}
		return m_inst;
	}

	public Object poll() {
		synchronized (list) {
			return list.poll();
		}
	}

	public boolean offer(Object o) {
		synchronized (list) {
			String sLength = ServerEnv.getInstance().getProperty("MESSAGE_QUENE_LENGTH");
			length = (Toolkit.isEmpty(sLength) ? 10 : Integer.parseInt(sLength));
			if (list.size() >= length)
				list.removeFirst();
			return list.offer(o);
		}
	}

	public Object element() {
		return list.element();
	}

}
