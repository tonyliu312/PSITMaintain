package uap.ps.itm.monitor.service.privilege;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class UserVO implements Serializable {

	private static final long serialVersionUID = -1090663530488766816L;

	private String user = null;

	private String password = null;

	private Set<String> resources = new HashSet<String>();
	
	public UserVO(String user, String password) {
		this.user = user;
		this.password = password;
	}

	public void addResource(String re) {
		resources.add(re);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Set<String> getResources() {
		return resources;
	}

	public void setResources(Set<String> resources) {
		this.resources = resources;
	}

}
