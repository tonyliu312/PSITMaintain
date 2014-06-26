package uap.ps.itm.monitor.connector.pub;

public interface IMsg {
	// 消息类型
	public String getMsgType();


	public String getTitle();

	public Object getObj();

	public String getBody();

	public String getEmailAddr();

	public String getCallID();
	public boolean isValid();

}
