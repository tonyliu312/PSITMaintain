package uap.ps.itm.monitor.service.loganalyze;


import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.XMLFormatter;

public class LogXmlFormater extends XMLFormatter {
//	private final static String PREFIX="@!";
	public String format(LogRecord record) {
		return "\n"+record.getMessage();
	}

	public String getHead(Handler h) {
		return "";
	}

	public String getTail(Handler h) {
		return "";
	}
}
