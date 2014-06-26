/**
 *
 */
package uap.ps.itm.npr.service.plugin;

/**
 * NPR插件常量定义
 *
 * @author liuzy
 *
 */
public interface INPRPluginConst {

	String MSG_TOKEN_SEMICOLON = ";";

	String MSG_TOKEN_STICK = "|";

	String UNKOWN = "unkown";

	String SNAPSHOT_PATH = "snapshot_path";

	String CAPTION = "caption";

	String FORMAT = "format";

	String CHART_TYPE = "chart-type";

	String X_FIELD = "x-field";

	String SYS_INTERVAL = "sys-interval";

	String TIME_BUCKET = "time-bucket";

	String Y_FIELD = "y-field";

	String IMPL_CLASS = "impl-class";

	String SHOW_LEVEL = "show-level";

	String FORMAT_TEXT = "text";

	String FORMAT_TABLE = "table";

	String FORMAT_CHART = "chart";

	String FORMAT_SNAPSHOT = "snapshot";

	String LINE_CHART = "LineChart";

	String CHART_TITLE = "chart-title";

	String LINE_NAME = "line-name";

	String CHART_WIDTH = "chart-width";

	String CHART_HIGH = "chart-high";

	String DATA_SRC = "datasrc";

	String SHOW_ORDER = "show-order";

	String LINK_FIELD = "link-filed";

	String Y_TITAL = "y-tital";

	String PARA1 = "para1";

	String PARA2 = "para2";

	String FILTER_FILE_NAME = "filter-file";

	String DATA_SRC_NMC_ONLINEUSER = "nmc.onlineuser";

	String DATA_SRC_NMC_DB = "nmc.db";

	String DATA_SRC_NMC_MW = "nmc.mw";

	String DATA_SRC_NMC_SNAPSHOT = "nmc.snapshot";

	String DATA_SRC_NAME_SYSTEMMONITOR = "nmc.systemmonitor";

	String DATA_SRC_NC_MW = "nc.mw";

	String DATA_SRC_NC_WARNINGSQL = "nc.warningsql";

	String DATA_SRC_NC_MASTER_MW = "nc.master.mw";

	String DATA_SRC_NPR_MWINFO = "npr.mwinfo";

	String DATA_SRC_NPR_DBINFO = "npr.dbinfo";

	String DATA_SRC_NULL = "data_src_null";


	String NMC_MW_SERVERNAME = "servername";

	String NMC_MW_THROUGHPUT = "throughput";

	String NMC_MW_WCTHREADNUM = "wcthreadnum";

	String NMC_MW_TOTAL = "total";

	String NMC_MW_FREE = "free";

	String NMC_MW_RESPCOST = "respcost";

	String NMC_ONLINEUSER_USERCOUNT = "usercount";

	String NMC_SNAPSHOT_SERVICE = "service";

	String NMC_SNAPSHOT_MESSAGE = "message";

	String NMC_SNAPSHOT_SNAPSHOTFILE = "snapshotfile";

	String NMC_SNAPSHOT_SNAPSHOTID = "snapshotid";

	String NC_COSTTIME = "costtime";

	String NC_MW_SQLCOSTTIME = "sqlcosttime";

	String NC_MW_SQLNUM = "sqlnum";

	String NC_MW_NOTCLOSEDCONNECTIONCOUNT = "notclosedconnectioncount";

	String NC_MW_READRESULTTIME = "readresulttime";

	String NC_MW_READFROMCLIENTTIME = "readfromclienttime";

	String NC_MW_WRITETOCLIENTTIME = "writetoclienttime";

	String NC_MW_WRITETOCLIENTBYTES = "writetoclientbytes";

	String NC_MW_READFROMCLIENTBYTES = "readfromclientbytes";

	String NC_MW_READROWNUM = "readrownum";

	String NC_WARNINGSQL_DEFCOL1 = "defcol1";

	String NC_WARNINGSQL_ROWNUM = "rownum";

	String NC_MSG = "msg";

	String TS = "ts";

	String NC_MW_BEGINTIME = "begintime";

	String NC_MW_BUSIACTION = "busiaction";

	String NC_MW_USERID = "userid";

	String NC_MW_CALLID = "callid";

	String NC_MW_REMOTECALLMETHOD = "remoteCallMethod";

	String NC_MW_CMC = "cmc";

	String NC_MW_CC = "cc";

	String NC_MW_REMOTECALLID = "callid";

	String NC_MW_REMOTEADDR = "remoteAddr";

	String CLUSTER = "cluster";

	String MASTER = "master";

	String SERVERS = "servers";

	String PLUGIN_COLUMN_BEGINTIME = "起始时间";

	String PLUGIN_COLUMN_ENDTIME = "截止时间";

	String PLUGIN_COLUMN_TIMES = "时长(分)";

	String PLUGIN_COLUMN_USERID = "操作员编码(名称)";

	String PLUGIN_COLUMN_REMOTEADDR = "客户端地址";

	String PLUGIN_COLUMN_BUSIACTION = "业务动作";

	String PLUGIN_COLUMN_REMOTECALLMETHOD = "方法名称";

	String PLUGIN_COLUMN_TS = "发生时间";

}
