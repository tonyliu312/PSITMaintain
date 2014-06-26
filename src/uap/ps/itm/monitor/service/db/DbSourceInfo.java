package uap.ps.itm.monitor.service.db;

import java.io.Serializable;

public class DbSourceInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String url = null;
	private String username = null;
	private String password = null;
	private String datasourcename = null;
	private String dbtype = null;
	private String driverClassName = null;
	private String lastcheckts = null;
	private String lastcheckMsg = null;
	private String minCon = null;
	private String maxCon = null;
	private int activeSession = 0;
	// 'redo size','DB time','logons current','session logical reads','user
	// commits','physical reads','physical writes','physical read
	// bytes','physical write bytes'
	// private Long redo_size = null;
	// private Long DB_time = null;
	// private Long logons_current = null;
	// private Long logical_reads = null;
	// private Long user_commits = null;
	// private Long physical_reads = null;
	// private Long physical_writes = null;
	// private Long physical_read_bytes = null;
	// private Long physical_write_bytes = null;
	// 'redo size','user commits','physical reads','physical writes','user
	// calls','db block gets','sorts (disk)'
	private long redo_size = 0;

	private long user_commits = 0;

	private long physical_reads = 0;

	private long physical_write = 0;

	private long user_calls = 0;

	private long db_block_gets = 0;

	private long sorts = 0;

	public long getRedo_size() {
		return this.redo_size;
	}

	public void setRedo_size(long redo_size) {
		this.redo_size = redo_size;
	}

	public long getUser_commits() {
		return this.user_commits;
	}

	public void setUser_commits(long user_commits) {
		this.user_commits = user_commits;
	}

	public long getPhysical_reads() {
		return this.physical_reads;
	}

	public void setPhysical_reads(long physical_reads) {
		this.physical_reads = physical_reads;
	}

	public long getPhysical_write() {
		return this.physical_write;
	}

	public void setPhysical_write(long physical_write) {
		this.physical_write = physical_write;
	}

	public long getUser_calls() {
		return this.user_calls;
	}

	public void setUser_calls(long user_calls) {
		this.user_calls = user_calls;
	}

	public long getDb_block_gets() {
		return this.db_block_gets;
	}

	public void setDb_block_gets(long db_block_gets) {
		this.db_block_gets = db_block_gets;
	}

	public long getSorts() {
		return this.sorts;
	}

	public void setSorts(long sorts) {
		this.sorts = sorts;
	}

	public boolean isRac() {
		return url.toUpperCase().indexOf("ADDRESS") > -1;
	}

	public String getLastcheckMsg() {
		return lastcheckMsg;
	}

	public void setLastcheckMsg(String lastcheckMsg) {
		this.lastcheckMsg = lastcheckMsg;
	}

	public String getLastcheckts() {
		return lastcheckts;
	}

	public void setLastcheckts(String lastcheckts) {
		this.lastcheckts = lastcheckts;
	}

	public String getDatasourcename() {
		return datasourcename;
	}

	public void setDatasourcename(String datasourcename) {
		this.datasourcename = datasourcename;
	}

	public String getDbtype() {
		return dbtype;
	}

	public void setDbtype(String dbtype) {
		this.dbtype = dbtype;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getActiveSession() {
		return activeSession;
	}

	public void setActiveSession(int activeSession) {
		this.activeSession = activeSession;
	}

	public String getMaxCon() {
		return maxCon;
	}

	public void setMaxCon(String maxCon) {
		this.maxCon = maxCon;
	}

	public String getMinCon() {
		return minCon;
	}

	public void setMinCon(String minCon) {
		this.minCon = minCon;
	}

	public String getToolTipText() {
		StringBuffer sb = new StringBuffer();
		sb.append("数据源名称").append(":").append(getDatasourcename());
		sb.append("\n");
		sb.append("数据库类型").append(":").append(getDbtype());
		sb.append("用户名").append(":").append(getUsername());
		sb.append("\n");
		sb.append("\nURL:").append(getUrl());
		sb.append("\n");
		sb.append("连接池大小").append(":").append(getMinCon() + "~" + getMaxCon());
		sb.append("\n");
		sb.append("活动会话数").append(":").append(getActiveSession());
		sb.append("\n");
		sb.append("上次检查时间").append(":").append(getLastcheckts());
		sb.append("\n");
		sb.append("上次检查事件").append(":").append(getLastcheckMsg());
		return sb.toString();
	}
}
