package uap.ps.itm.monitor.pub;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import uap.ps.itm.monitor.service.center.ServerEnv;
import uap.ps.itm.pub.utils.ExceptionHandler;

public class Text2HashVO {

	private LineNumberReader lineReader = null;

	private String fldPrefix = null;

	private ArrayList<HashVO> alRes = new ArrayList<HashVO>();

	private HashVO curWorkVO = null;

	private String lastAttr = null;

	private StringBuffer lastAttrValue = new StringBuffer();

	private ArrayList<IDataLoaderListener> al = new ArrayList<IDataLoaderListener>();

	private String filename = null;

	public HashVO[] convertToVO(String fileName, String fldPrefix) {
		if (fileName == null || fldPrefix == null)
			return null;
		this.filename = fileName;
		this.fldPrefix = fldPrefix;
		InputStreamReader input = null;
		try {
			input = new InputStreamReader(new BufferedInputStream(new FileInputStream(fileName)));
			lineReader = new LineNumberReader(input);
			String curline = null;
			while ((curline = lineReader.readLine()) != null) {
				processLine(curline);
			}
			if (curWorkVO != null) {
				curWorkVO.setAttributeValue(lastAttr, lastAttrValue.toString());
				firedListener(curWorkVO, "add");
				firedListener(null, "end");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (input != null) {
					input.close();
				}
				if (lineReader != null) {
					lineReader.close();
				}
			} catch (Exception e) {
			}
		}
		return alRes.toArray(new HashVO[alRes.size()]);
	}

	private void processLine(String curline) {
		if (curline == null)
			return;
		curline = curline.trim();
		if (!curline.startsWith(fldPrefix)) {
			if (lastAttr == null)
				return;
			lastAttrValue.append("\n").append(curline);
			return;
		} else {
			if (curWorkVO != null) {
				curWorkVO.setAttributeValue(lastAttr, lastAttrValue);
				firedListener(curWorkVO, "add");
			}
			lastAttr = null;
			lastAttrValue = new StringBuffer();
			curWorkVO = new HashVO();
			if (Toolkit.isEmpty(al))
				alRes.add(curWorkVO);
			stringToHashVO(curline);
		}
	}

	private void stringToHashVO(String src) {
		if (src == null || !src.startsWith(fldPrefix))
			return;
		StringTokenizer st = new StringTokenizer(src, fldPrefix, false);
		while (st.hasMoreTokens()) {
			if (lastAttr != null) {
				curWorkVO.setAttributeValue(lastAttr, lastAttrValue.toString());
			}
			String token = st.nextToken();
			processEleAttr(token);
		}
	}

	private void processEleAttr(String token) {
		if (token == null)
			return;
		token = token.trim();
		int iPos = token.indexOf("=");
		int ispace = -1;
		if (iPos > 0)
			ispace = token.substring(0, iPos).indexOf(" ");
		if (iPos < 0 || ispace > -1) {
			if (lastAttr == null)
				return;
			lastAttrValue.append("$").append(token);
			if (lastAttr != null) {
				curWorkVO.setAttributeValue(lastAttr, lastAttrValue.toString());
			}
			return;
		}
		lastAttr = token.substring(0, iPos);
		lastAttrValue=new StringBuffer((token.substring(iPos + 1, token.length())));
	}

	public static final void main(String[] args) {
		new Text2HashVO().convertToVO("c:/sql.log", "$$");

	}

	public void addListener(IDataLoaderListener l) {
		al.add(l);
	}

	private void firedListener(HashVO vo, String eventname) {
		if (Toolkit.isEmpty(al))
			return;
		if (vo != null) {
			if (Toolkit.isEmpty(vo.getAttributeValue("server"))) {
				vo.setAttributeValue("server", ServerEnv.getHostName());
			}
			if (Toolkit.isEmpty(vo.getAttributeValue("file"))) {
				vo.setAttributeValue("file", filename);
			}
		}
		for (int i = 0; i < al.size(); i++) {
			try {
				al.get(i).processEvent(new DataloaderEvent(vo, eventname));
			} catch (Exception e) {
				ExceptionHandler.handleException(e);
			}
		}
	}
}
