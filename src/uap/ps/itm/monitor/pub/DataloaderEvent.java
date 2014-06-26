package uap.ps.itm.monitor.pub;

public class DataloaderEvent {
	private HashVO vo = null;

	private String event = null;

	// ADD,END
	public DataloaderEvent(HashVO vo, String eventname) {
		this.vo = vo;
		this.event = eventname;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public HashVO getVo() {
		return vo;
	}

	public void setVo(HashVO vo) {
		this.vo = vo;
	}

}
