package uap.ps.itm.monitor.connector.pub;

public interface IMsg {
	// ��Ϣ����
	public String getMsgType();


	public String getTitle();

	public Object getObj();

	public String getBody();

	public String getEmailAddr();

	public String getCallID();
	public boolean isValid();

}
