package uap.ps.itm.monitor.pub;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Toolkit {
	private static final String[] NUM = new String[] { "0", "1", "2", "3", "4",
			"5", "6", "7", "8", "9", "." };

	private static Hashtable<String, Icon> hIcon = new Hashtable<String, Icon>();

	public static boolean isEmpty(Object value) {
		if (value == null)
			return true;
		if ((value instanceof String)
				&& (((String) value).trim().length() <= 0))
			return true;
		if ((value instanceof Object[]) && (((Object[]) value).length <= 0))
			return true;
		if ((value instanceof Collection) && ((Collection) value).size() <= 0)
			return true;
		if ((value instanceof Dictionary) && ((Dictionary) value).size() <= 0)
			return true;
		return false;
	}

	public static boolean isEqual(Object o1, Object o2) {
		if (isEmpty(o1) && isEmpty(o2))
			return true;
		if (isEmpty(o1) && !isEmpty(o2))
			return false;
		if (!isEmpty(o1) && isEmpty(o2))
			return false;
		return o1.toString().trim().equalsIgnoreCase(o2.toString().trim());
	}

	public static String getOracleUrl(String host, String port, String instance) {
		return "jdbc:oracle:thin:@" + host + ":" + port + ":" + instance;
	}

	public static String getDB2Url(String host, String port, String instance) {
		return "jdbc:db2://" + host + ":" + port + "/" + instance;
	}

	public static String getSqlserverUrl(String host, String port, String dbname) {
		return "jdbc:sqlserver://" + host + ":" + port + ";database=" + dbname
				+ ";sendStringParametersAsUnicode=false";
	}

	public static String getJMXURL(String host, String port) {
		return "service:jmx:rmi:///jndi/rmi://" + host + ":" + port
				+ "/jmxconnector";
	}

	/**
	 * 判断字符串由数字和.构成，可能是数字，也可能是IP地址等
	 */
	public static boolean isNum(String src) {
		if (Toolkit.isEmpty(src))
			return true;
		char[] cs = src.toCharArray();
		for (int i = 0; i < cs.length; i++) {
			boolean isNum = false;
			for (int j = 0; j < NUM.length; j++) {
				if (Toolkit.isEqual(cs[i], NUM[j].toCharArray()[0])) {
					isNum = true;
					break;
				}
			}
			if (!isNum) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断字符串为数字
	 */
	public static boolean isNum1(String src) {
		if (Toolkit.isEmpty(src)) {
			return true;
		}
		return src.matches("-?(\\d+\\.)?\\d+");

	}

	public static double getdouble(String src) {
		if (src == null || src.length() == 0)
			return 0.0;
		return Double.valueOf(src);
	}

	public static int getInt(String src) {
		if (src == null || src.length() == 0)
			return 0;
		return Integer.valueOf(src);
	}

	public static String getCurTime() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}

	public static Icon getIcon(String imagename) {
		if (hIcon.get(imagename) != null)
			return hIcon.get(imagename);
		URL url = Toolkit.class.getClassLoader().getResource(imagename);
		if (url == null)
			return null;
		hIcon.put(imagename, new ImageIcon(url));
		return (Icon) hIcon.get(imagename);
	}

	public static boolean isInterface(Class c, String szInterface) {
		Class[] face = c.getInterfaces();
		for (int i = 0, j = face.length; i < j; i++) {
			if (face[i].getName().equals(szInterface)) {
				return true;
			} else {
				Class[] face1 = face[i].getInterfaces();
				for (int x = 0; x < face1.length; x++) {
					if (face1[x].getName().equals(szInterface)) {
						return true;
					} else if (isInterface(face1[x], szInterface)) {
						return true;
					}
				}
			}
		}
		if (null != c.getSuperclass()) {
			return isInterface(c.getSuperclass(), szInterface);
		}
		return false;
	}

	public static String[] getFldsByDDL(String strTblDDL) {
		if (Toolkit.isEmpty(strTblDDL))
			return new String[0];
		strTblDDL = strTblDDL.trim().replace("\"", "");
		String subStr = strTblDDL.substring(strTblDDL.indexOf("(") + 1,
				strTblDDL.lastIndexOf(")") - 1);
		StringTokenizer st = new StringTokenizer(subStr, ",");
		ArrayList<String> al = new ArrayList<String>();
		StringTokenizer stsub = null;
		while (st.hasMoreTokens()) {
			String ele = st.nextToken();
			if (Toolkit.isEmpty(ele))
				continue;
			stsub = new StringTokenizer(ele);
			if (stsub.countTokens() > 2)
				continue;
			if (stsub.hasMoreTokens()) {
				al.add(stsub.nextToken().trim());
			}
		}
		return al.toArray(new String[al.size()]);
	}

	public static final void main(String[] args) {
		Toolkit.delDir("d:/logs/logs");
	}

	public static Object[] pack(String[] objs) {
		if (Toolkit.isEmpty(objs))
			return new String[0];
		ArrayList<String> al = new ArrayList<String>();
		for (int i = 0; i < objs.length; i++) {
			if (objs[i] != null)
				al.add(objs[i]);
		}
		return al.toArray(new String[] {});
	}

	public static String getFileContent(String filename) throws Exception {
		InputStreamReader input = null;
		LineNumberReader lineReader = null;
		try {
			input = new InputStreamReader(new BufferedInputStream(
					new FileInputStream(filename)));
			lineReader = new LineNumberReader(input);
			String curline = null;
			StringBuffer sb = new StringBuffer();
			while ((curline = lineReader.readLine()) != null) {
				sb.append(curline).append("\n");
			}
			return sb.toString();
		} finally {
			try {
				input.close();
			} catch (Exception e) {

			}
			try {
				lineReader.close();
			} catch (Exception e) {

			}
		}
	}

	public static void sort(Object o, Comparator comparator) {
		if (o instanceof Object[]) {
			Arrays.sort((Object[]) o, comparator);
		} else if (o instanceof List) {
			Collections.sort((List) o, comparator);
		}
	}

	public static void delDir(String dirName) {
		File file = new File(dirName);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			files = (files == null ? new File[0] : files);
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					delDir(files[i].getAbsolutePath());
				} else {
					try {
						files[i].delete();
					} catch (Exception e) {

					}
				}
			}
			try {
				file.delete();
			} catch (Exception e) {

			}
		} else {
			try {
				file.delete();
			} catch (Exception e) {

			}
		}
	}

	/**
	 * 根据指定数组中数据生成部分SQL语句。
	 */
	public static String getWherePartByKeys(String fld, String[] arsKey) {
		final int MAX = 500;
		if (isEmpty(arsKey))
			return " 1 = 1 ";
		if (arsKey.length == 1)
			return fld + "='" + arsKey[0] + "'";
		if (arsKey.length <= MAX) {
			String sTmp = fld + " in (";
			for (int i = 0; i < arsKey.length; i++) {
				if (i == arsKey.length - 1) {
					sTmp += "'" + arsKey[i] + "')";
					break;
				}
				sTmp += "'" + arsKey[i] + "',";
			}
			return sTmp;
		}
		int ipos = 0;
		int itimes = arsKey.length / MAX;
		int mode = arsKey.length % MAX;
		String where = null;
		for (int i = 0; i < itimes; i++) {
			if (where == null)
				where = " ( " + fld + " in ( ";
			else
				where += " or " + fld + " in (";
			for (int j = 0; j < MAX; j++) {
				if (j == MAX - 1) {
					where += "'" + arsKey[ipos + j] + "')";
					break;
				}
				where += "'" + arsKey[ipos + j] + "',";
			}
			ipos += MAX;
		}
		if (mode == 0)
			where += " )";
		else {
			where += " or " + fld + " in (";
			for (int k = 0; k < mode; k++) {
				if (k == mode - 1) {
					where += "'" + arsKey[ipos + k] + "'))";
					break;
				}
				where += "'" + arsKey[ipos + k] + "',";
			}
		}
		return where;
	}

	public static byte[] getFileBytes(String filename) {
		FileInputStream fis = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			fis = new FileInputStream(new File(filename));
			byte[] buf = new byte[1024];
			int i = 0;
			while ((i = fis.read(buf)) != -1) {
				bos.write(buf, 0, i);
			}
			return bos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (Exception e) {

			}
			try {
				bos.close();
			} catch (Exception e) {
			}
		}
		return null;
	}

	public static void writeFile(String filename, byte[] bs) {
		if (Toolkit.isEmpty(bs))
			return;
		File file = new File(filename);
		if(!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(filename));
			fos.write(bs);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (Exception e) {

			}
		}
	}

	public static int getTotalLine(String txt) {
		if (Toolkit.isEmpty(txt))
			return -1;
		int ipos = txt.indexOf("\n");
		int count = 0;
		while (ipos > -1) {
			count++;
			ipos = txt.indexOf("\n", ipos + 1);
		}
		return count;
	}

	public static int getLineAt(String txt, String chars) {
		if (Toolkit.isEmpty(chars))
			return -1;
		int ipos = txt.indexOf(chars);
		if (ipos < 0)
			return -1;
		int iipos = txt.indexOf("\n");
		if (iipos < 0)
			return 0;
		int count = 0;
		while (iipos < ipos) {
			count++;
			iipos = txt.indexOf("\n", iipos + 1);
		}
		return count;
	}

	public static String getExceptionStack(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String stackTrace = sw.toString();
		return stackTrace;
	}

	public static String byte2hex(String s) // 二进制转字符串
	{
		byte[] b = s.getBytes();
		if (b == null || b.length == 0)
			return "0x0";
		String hs = "0x";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
		}
		return hs;
	}

	public static String hex2String(String str) {
		if (str == null) {
			return null;
		}

		str = str.trim();

		if (str.startsWith("0x")) {
			str = str.substring(2);
		}
		int len = str.length();

		if ((len == 0) || (len % 2 == 1)) {
			return null;
		}
		byte[] b = new byte[len / 2];
		try {
			for (int i = 0; i < str.length(); i += 2) {
				b[(i / 2)] = (byte) Integer.decode(
						"0x" + str.substring(i, i + 2)).intValue();
			}

			return new String(b);
		} catch (Exception e) {
		}
		return null;
	}

	public static Object serializableClone(Object oIn) throws Exception {
		try {
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			ObjectOutputStream o = new ObjectOutputStream(buf);
			o.writeObject(oIn);
			ObjectInputStream in = new ObjectInputStream(
					new ByteArrayInputStream(buf.toByteArray()));
			return in.readObject();
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
			throw ex;
		} catch (ClassNotFoundException ex) {
			System.out.println(ex.getMessage());
			throw ex;
		}

	}

	public static void copyFiles2Dir(String filePath, String destDir) {
		DataInputStream inputStream = null;
		FileOutputStream outputStream = null;
		File srcFile = new File(filePath);
		File destFile = new File(destDir);
		File newFile = new File(destFile + "/" + srcFile.getName());
		try {
			inputStream = new DataInputStream(new FileInputStream(srcFile));
			if (!destFile.exists())
				destFile.mkdirs();
			outputStream = new FileOutputStream(newFile);
			byte[] buf = new byte[8192];
			while (true) {
				int read = 0;
				if (inputStream != null) {
					read = inputStream.read(buf);
				}

				if (read == -1) {
					break;
				}
				outputStream.write(buf, 0, read);
			}
		} catch (Exception e) {
//			ExceptionHandler.handleException(e);
			System.out.println(e.getMessage());
			return;
		} finally {
			try {
				outputStream.close();
			} catch (Exception e) {
//				ExceptionHandler.handleException(e);
				System.out.println(e.getMessage());
			}
			try {
				inputStream.close();
			} catch (Exception e) {
//				ExceptionHandler.handleException(e);
				System.out.println(e.getMessage());
			}
		}
		newFile.setLastModified(srcFile.lastModified());
	}
	
	public static String getLangRes(String chs_name){
//		return LangResUtil.getInstance().getRes(chs_name);
		return chs_name;
	}
}
