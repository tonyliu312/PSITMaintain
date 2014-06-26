package uap.ps.itm.monitor;

public class RemoteException extends Exception {
	private static final long serialVersionUID = 1L;
	private String msg = null;

	@Override
	public String toString() {
		return "远程调用异常:\n" + msg;
	}

	public RemoteException(String msg) {
		this.msg = msg;
	}

	public String getMessage() {
		return msg;
	}
}
