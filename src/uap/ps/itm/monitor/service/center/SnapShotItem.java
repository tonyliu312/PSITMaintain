package uap.ps.itm.monitor.service.center;

import java.io.Serializable;

public class SnapShotItem  implements Serializable{

	private static final long serialVersionUID = 1L;

	private String service = null;

	private String scope = null;

	public String getScope() {
		return scope;
	}

	public void setScopes(String scope) {
		this.scope = scope;
	}

	public String getService() {
		return service;
	}

	public void setServices(String service) {
		this.service = service;
	}
}
