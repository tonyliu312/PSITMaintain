package uap.ps.itm.monitor.pub;

public class IConfigConst {
	public static String SERVICECFGFILE = null;

	public static String PROPCONFIGFILE = null;

	public static String CLIENTPLUGINFILE = null;

	public static String LOGDBCFGFILE = null;

	public static String LOGCFGFILE = null;

	public static String LOGDBRESFILE = null;

	public static String LOGDBFILE = null;

	public static String LOGSQLFILE = null;

	public static String CLIENTDOMAINFILE = null;

	public static String DBCFGPATH = null;

	public static String SECURITY_CONF_FILE = null;

	public static String MAILCONFIGFILE = null;

	public static String PRIVILEGE_CONF_FILE = null;

	public static String RES_FILE = null;

	public static final String AIX = "AIX";

	public static final String HP = "HP";

	public static final String SALORIS = "SUN";

	public static final String LINUX = "LINUX";

	public static final String WINDOWS = "WIN";

	public static final String SCOPE_ALL = "ALL";

	public static final String SCOPE_MACHINE = "MACHINE";

	public static final String SCOPE_INST = "INSTANCE";

	public static final String SERVICERGISTERSERVICE = "serviceregister";

	public static final String REMOTECALLSERVLET = "remotecall";

	public static final String SERVICE_CPU_MONITOR = "cpumonitor";

	public static final String SERVICE_MEM_MONITOR = "memorymonitor";

	public static final String SERVICEPROCESSMONITOR = "processmonitor";

	public static final String SERVICECLIENTMONITOR = "clientmonitor";

	public static final String SERVICEMWMONITOR = "mwmonitor";

	public static final String SERVICEDB = "db";

	public static final String SERVICEDBMONITOR = "dbmonitor";

	public static final String SERIVCELOGANALYZE = "loganalyze";

	public static final String SERVICESECURITYCHECK = "securitycheck";

	public static final String SERVICEMESSAGE = "messageprocessor";

	public static final String SERVICEVTUNE = "vtune";

	public static final String SERVICEFILETRANS = "filetransfer";

	public static final String SERVICECONFIG = "config";

	public static final String SERVICECACHEMONITOR = "cache";

	public static final String SERVICEPATCHMANAGER = "patchmanager";

	public static final String SERVICE6XPATCHMANGER = "newpatchmanager";

	public static final String SERVICESYSTEMMONITOR = "systemmonitor";

	public static final String SERVICEAWR = "awrcentral";

	public static final String SERVICEPRIVILEGE = "privilegemanager";

	public static final String SERVICETASKMANAGER = "taskmanager";

	public static final int MAXTITLELENGTH = 20;

	public static final String LOGTYPE_MEMORY = "memory";

	public static final String LOGTYPE_CPU = "cpu";

	public static final String LOGTYPE_JVM = "jvm";

	public static final String LOGTYPE_ONLINEUSER = "onlineuser";

	public static final String LOGTYPE_CLIENT = "client";

	public static final String LOGTYPE_NCLOG = "nclogs";

	public static String BASEDIR = null;

	public static String INTERFRAME_FILE = null;

	public static String PORTAL_CONFIG_FILE = null;

	public static String NPRCFGFILE = null;
	public static String BPRCFGFILE = null;
	public static String PATCHDATASOURCE = null;
	public static String PATCH_CLIENT_PROJECT = null;// 保存服务器上面的项目信息
	public static String PATCH_CLIENT_INFO = null;// 客户端保存补丁服务器的信息
	public static String AWRFILE = null;// awr服务器配置的信息

	public static String INDEXTABLE = "tablename";
	public static String INDEXNAME = "indexname";
	public static String INDEXCOLUMNS = "indexcolumns";
	public static String INDEXFILE = null;
	public static String INDEXID = "indexid";

	public static String SERVER_XML = null;
	public static final int ORACLE = 0;
	public static final int DB2 = 1;
	public static final int SQLSERVER = 2;
	public static final int ACCESS = 9;

	public static void init() {
		String path = System.getProperty("basedir");
		path = Toolkit.isEmpty(path) ? "." : path;
		BASEDIR = path;
		LOGDBCFGFILE = path + "/conf/resource/logdb.xml";
		LOGDBRESFILE = path + "/conf/resource/logdb.mdb";
		LOGSQLFILE = path + "/conf/sql/logsql.cfg";
		CLIENTDOMAINFILE = path + "/conf/resource/domain.xml";
		LOGDBFILE = path + "/logdb/logdb.mdb";
		CLIENTPLUGINFILE = path + "/conf/resource/resource-client.xml"; 
		DBCFGPATH = path + "/conf/sql/fixsql.cfg";
		SERVICECFGFILE = path + "/conf/services.xml";
		PROPCONFIGFILE = path + "/conf/monitor.properties";
		SECURITY_CONF_FILE = path + "/conf/security.properties";
		MAILCONFIGFILE = path + "/conf/mail.properties";
		PRIVILEGE_CONF_FILE = path + "/conf/monitor-users.xml";
		LOGCFGFILE = path + "/conf/monitor-log.properties";
		INTERFRAME_FILE = path + "/conf/resource/internalframe.xml";
		PORTAL_CONFIG_FILE = path + "/conf/resource/portal-config.xml";
		SERVER_XML = path + "/conf/server.xml";
		RES_FILE = path + "/conf/resource/nmc_zh_CN.properties";
		PATCHDATASOURCE = path + "/conf/patchdatasource.xml";
		NPRCFGFILE = path + "/conf/npr.properties";
		BPRCFGFILE = path + "/conf/bpr.properties";
		AWRFILE = path + "/conf/awr.properties";
		PATCH_CLIENT_PROJECT = path + "/conf/resource/projectinfo.txt";
		PATCH_CLIENT_INFO = path + "/conf/resource/patchinfo.xml";
		INDEXFILE = path + "/conf/resource/standindex.txt";
	}
}
