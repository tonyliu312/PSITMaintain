package uap.ps.itm.monitor.service.process;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;

public class PluginAdmin {
	public static String key = null;

	public static String pluginCfgfile = null;

	private static final String DOMAINNAME = "<PrimaryServers>";

	public synchronized static void addServer() {
		try {
			if (!isExist(key))
				modify(true);
		} catch (Exception e) {
			e.printStackTrace();
			key = null;
		}
	}

	public synchronized static void removeServer() {
		try {
			if (isExist(key))
				modify(false);
		} catch (Exception e) {
			e.printStackTrace();
			key = null;
		}
	}

	public static boolean isExist(String key) {
		LineNumberReader reader = null;
		try {
			reader = new LineNumberReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(pluginCfgfile))));
			String line = reader.readLine();
			while (line != null) {
				line = reader.readLine();
				if (line == null)
					return false;
				line = line.trim();
				if (line.equalsIgnoreCase(key))
					return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (Exception e) {

			}
		}
		return false;
	}

	public static void modify(boolean add) {
		LineNumberReader reader = null;
		OutputStreamWriter writer = null;
		try {
			StringBuffer sb = new StringBuffer();
			reader = new LineNumberReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(pluginCfgfile))));
			String line = reader.readLine();
			sb.append(line);
			sb.append("\n");
			while (line != null) {
				line = reader.readLine();
				if (line == null)
					break;
				line = line.trim();
				if (!key.equalsIgnoreCase(line) || add) {
					sb.append(line);
					sb.append("\n");
				}
				if (add && line != null) {
					if (DOMAINNAME.equalsIgnoreCase(line)) {
						sb.append(key + "\n");
					}
				}
			}
			try {
				reader.close();
			} catch (Exception e) {

			}
			writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(pluginCfgfile)));
			writer.write(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (Exception e) {

			}
		}
	}

	public final static void main(String[] args) {
		// addServer();
		removeServer();
	}
}
