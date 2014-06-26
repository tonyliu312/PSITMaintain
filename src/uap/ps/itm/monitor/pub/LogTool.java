package uap.ps.itm.monitor.pub;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import uap.ps.itm.monitor.service.loganalyze.LogXmlFormater;
  
public class LogTool {
	private static Properties prop = null;
	// get log config.
	public static Properties getProp() {
		if (prop == null) {
			prop = new Properties();
			try {
				prop.load(new FileInputStream(IConfigConst.LOGCFGFILE));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return prop;
	}

	public static int getLogSize(String type) {
		Object o = getProp().getProperty("log." + type + ".size");
		if (o == null)
			return 2048000;
		return Integer.parseInt(o.toString());
	}
	public static int getLogFileNumber(String type) {
		Object o = getProp().getProperty("log." + type + ".filenumber");
		if (o == null)
			return 2;
		return Integer.parseInt(o.toString());
	}
	// create a logger.

	public static Logger createLogger(String type, String dir, String filename) {
		type = (type == null ? "default" : type);
		dir = (dir == null ? "default" : dir);
		filename = (filename == null ? "default" : filename);
		if (!new File("./logs").exists())
			new File("./logs").mkdir();
		dir = "./logs/" + dir;
		if (!new File(dir).exists())
			new File(dir).mkdir();
		Logger logger = Logger.getLogger(filename);
		Handler[] handler = logger.getHandlers();
		if (handler != null) {
			for (int i = 0; i < handler.length; i++)
				logger.removeHandler(handler[i]);
		}
		try {
			FileHandler fh1 = new FileHandler(dir + "/" + filename + ".log", getLogSize(type), getLogFileNumber(type), true);
			fh1.setFormatter(new LogXmlFormater());
			logger.addHandler(fh1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Handler h[] = logger.getParent().getHandlers();
		for (int i = 0; i < h.length; i++) {
			logger.getParent().removeHandler(h[i]);
		}

		return logger;
	}
}
