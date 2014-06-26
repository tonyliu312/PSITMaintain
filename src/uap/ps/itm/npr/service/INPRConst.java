package uap.ps.itm.npr.service;

import nc.vo.pub.lang.UFDouble;

public interface INPRConst {

	String NC_HOME = "NC_HOME";

	String NC_LOG_DIR = "NC_LOG_DIR";

	String MWSMRY_FILE_NAME = "mwsummary";

	String WNGSQL_FILE_NAME = "warningsql";

	String NMC_SNAPSHOT_FILE_NAME = "snapshotidx";

	String NPR_MWINFO_FILE_NAME = "mwinfo";

	String NPR_DBINFO_FILE_NAME = "dbinfo";

	String NMC_LOG_DIR = "./logs";

	// String NMC_LOG_DIR = "d:\\\\dev_env\\\\20120911\\nmc\\server\\logs";

	String NPR_PLUGIN_FILTER_DIR = "./conf/nprfilter";

	String NPR_PLUGIN_ALL_FILTER_FILE_NAME = "allfilter.flt";

	String NPR_PLUGIN_FILTER_EXTEND_NAME = ".flt";

	String NMC_LOG_DB_DIR_NAME = "db";

	String NMC_LOG_MW_DIR_NAME = "mw";

	String NMC_LOG_ONLINEUSER_DIR_NAME = "onlineuser";

	String NMC_LOG_SNAPSHOT_DIR_NAME = "snapshot";

	String NMC_LOG_SYSTEM_MONITOR_NAME = "systemmonitor";

	String NPR_DEF_LOG = "NPR_DEF_LOG";

	String FILE_DOWN_METHOD_NAME = "downloadFile";

	String FILEINFO_METHOD_NAME = "getFileInfosFromServer";

	String NPR_SERVICE_NAME = "nprcentral";

	String NC_LOG_DIR_NAME = "nclogs";

	String NMC_LOG_DIR_NAME = "nmclogs";

	String COLLECT_TYPE_NC = "COLLECT_TYPE_NC";

	String COLLECT_TYPE_NMC = "COLLECT_TYPE_NMC";

	String NPR_PLUGIN_NODE_NAME = "plugin";

	String NPR_REMOTECALL_SQLNUM_LIMIT = "NPR_REMOTECALL_SQLNUM_LIMIT";

	String NPR_CONFIG_FIRST_RUN_TIME = "NPR_CONFIG_FIRST_RUN_TIME";

	String NPR_CONFIG_INTERVAL_HOURS = "NPR_CONFIG_INTERVAL_HOURS";

	String NPR_CONFIG_EMAIL_ADDR = "NPR_CONFIG_EMAIL_ADDR";

	String NPR_CONFIG_SCHEDULE_ENABLE = "NPR_CONFIG_SCHEDULE_ENABLE";

	String NPR_CONFIG_CLEAR_TIME = "NPR_CONFIG_CLEAR_TIME";

	String NPR_CONFIG_CLEAR_SAVE_DAYS = "NPR_CONFIG_CLEAR_SAVE_DAYS";

	String NPR_CONFIG_CLEAR_TIMEING_ENABLE = "NPR_CONFIG_CLEAR_TIMEING_ENABLE";

	String NPR_CONFIG_CLEAR_RESTART_ENABLE = "NPR_CONFIG_CLEAR_RESTART_ENABLE";

	String NPR_CHART_POINT_COUNT = "NPR_CHART_POINT_COUNT";

	String NPR_COL_SNAPSHOT_ADDR = "快照路径";

	String NPR_COL_CREATE_TIME = "创建时间";

	String NPR_COL_BODY_REPORT_URL = "报告url";

	String NPR_COL_BODY_BEGINTIME = "分析起始时间";

	String NPR_COL_BODY_ENDTIME = "分析结束时间";

	String NPR_COL_CREATE_COST = "耗时";

	String NPR_REMOTECALL_TIMES_LIMIT = "NPR_REMOTECALL_TIMES_LIMIT";

	UFDouble UFDOUBLE_ZERO = new UFDouble(0.0);

	String NPR_CLEAR_DEL_SNAPSHOT = "NPR_CLEAR_DEL_SNAPSHOT";

	String NPR_CLEAR_DEL_REPORT = "NPR_CLEAR_DEL_REPORT";

	String NPR_TOP_N = "NPR_TOP_N";

}
