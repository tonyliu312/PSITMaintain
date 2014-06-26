/**
 *
 */
package uap.ps.itm.npr.service.report;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import nc.vo.pub.lang.UFDouble;
import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.IMonitorConst;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.npr.service.INPRConst;
import uap.ps.itm.npr.service.NPRGlobalControl;
import uap.ps.itm.npr.service.plugin.INPRPluginConst;
import uap.ps.itm.npr.vo.BPRReport;
import uap.ps.itm.npr.vo.XPRPluginResult;
import ChartDirector.Chart;
import ChartDirector.ChartViewer;
import ChartDirector.LegendBox;
import ChartDirector.LineLayer;
import ChartDirector.PlotArea;
import ChartDirector.XYChart;

/**
 * 生成报表的HTML框架
 *
 * @author liuzy
 *
 */
public class HTMLFrame {

	// private String reportName = "NC Performance Report";

	private XPRPluginResult[] pluginResults = null;

	private BPRReport report = null;

	private String destFileName = null;

	private final int chartDefWidth = 1024;

	private final int multiple = 20;

	private final int topn = NPRGlobalControl.getInstance().getTopN();

	private Map<Integer, Map<String, List<StringBuilder>>> mapAll;

	private Map<String, StringBuilder> map0Level;

	private List<String> list0Level;

	private static final char intToAltBase64[] = { '!', '"', '#', '$', '%',
			'&', '\'', '(', ')', ',', '-', '.', ':', ';', '<', '>', '@', '[',
			']', '^', '`', '_', '{', '|', '}', '~', 'a', 'b', 'c', 'd', 'e',
			'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
			's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4',
			'5', '6', '7', '8', '9', '+', '?' };

	private static final char intToBase64[] = { 'A', 'B', 'C', 'D', 'E', 'F',
			'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
			'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
			'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
			't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', '+', '/' };

	private final int[] colors = { 0x663300, 0xFF99FF, 0xCCFF00, 0x9999FF, 0xFF9900,
			0xFF3300, 0x66FFFF, 0x000000, 0x00CCFF, 0x00FF00, 0x6666FF /*
																		 * ,
																		 * 0x666600
																		 * ,
																		 * 0x0033FF
																		 */};

	private int maxShowLevel = Integer.MIN_VALUE;

	private String REPORT_TYPE = null;

	public HTMLFrame() {

	}

	public HTMLFrame(BPRReport rep, String reportType) {
		this.REPORT_TYPE = reportType;
		this.setReport(rep);
		this.pluginResults = rep.getPluginResult();
		this.setDestFileName(rep.getDestFileName());
	}

	// private String barChart2HTML(NPRPluginResult result) {
	// return null;
	// }

	private String byteArrayToBase64(byte[] a, boolean alternate) {
		int aLen = a.length;
		int numFullGroups = aLen / 3;
		int numBytesInPartialGroup = aLen - 3 * numFullGroups;
		int resultLen = 4 * ((aLen + 2) / 3);
		StringBuffer result = new StringBuffer(resultLen);
		char[] intToAlpha = (alternate ? intToAltBase64 : intToBase64);

		int inCursor = 0;
		for (int i = 0; i < numFullGroups; i++) {
			int byte0 = a[inCursor++] & 0xff;
			int byte1 = a[inCursor++] & 0xff;
			int byte2 = a[inCursor++] & 0xff;
			result.append(intToAlpha[byte0 >> 2]);
			result.append(intToAlpha[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
			result.append(intToAlpha[(byte1 << 2) & 0x3f | (byte2 >> 6)]);
			result.append(intToAlpha[byte2 & 0x3f]);
		}

		if (numBytesInPartialGroup != 0) {
			int byte0 = a[inCursor++] & 0xff;
			result.append(intToAlpha[byte0 >> 2]);
			if (numBytesInPartialGroup == 1) {
				result.append(intToAlpha[(byte0 << 4) & 0x3f]);
				result.append("==");
			} else {
				// assert numBytesInPartialGroup == 2;
				int byte1 = a[inCursor++] & 0xff;
				result.append(intToAlpha[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
				result.append(intToAlpha[(byte1 << 2) & 0x3f]);
				result.append('=');
			}
		}

		return result.toString();
	}

	private String chart2HTML(XPRPluginResult result) {
		String chart_type = result.getChart_type();
		if (chart_type.equals(INPRPluginConst.LINE_CHART)) {
			Map<String, List<HashVO>> map = result.getDataMap();
			if (null == map || map.size() == 0)
				return null;
			if (map.size() <= this.colors.length)
				return this.lineChart2HTML(result);
			StringBuilder sb = new StringBuilder();
			List<XPRPluginResult> list = this.splitLineChartData(result,
					new ArrayList<XPRPluginResult>());
			for (XPRPluginResult r : list)
				sb.append(this.lineChart2HTML(r));
			return sb.toString();
		}
		return null;
	}

	private List<XPRPluginResult> splitLineChartData(XPRPluginResult srcResult,
			List<XPRPluginResult> list) {
		Map<String, List<HashVO>> map = srcResult.getDataMap();
		List<String> removeKeys = new ArrayList<String>();
		try {
			XPRPluginResult newRes = (XPRPluginResult) Toolkit
					.serializableClone(srcResult);
			newRes.setDataMap(null);
			for (Map.Entry<String, List<HashVO>> entry : map.entrySet()) {
				newRes.addData2Map(entry.getKey(), entry.getValue());
				removeKeys.add(entry.getKey());
				if (newRes.getDataMap().size() == this.colors.length)
					break;
			}
			list.add(newRes);
			for (String key : removeKeys)
				map.remove(key);
			if (map.size() > 0)
				this.splitLineChartData(srcResult, list);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return list;
	}

	private XYChart createSnmpLineChart(XPRPluginResult result) {
		String xField = result.getxFieldName();
		int width = result.getChart_width();
		int high = result.getChart_high();

		int maxLength = -1;

		Map<String, List<HashVO>> dataMap = result.getDataMap();

		if (Toolkit.isEmpty(dataMap))
			return null;

		for (Map.Entry<String, List<HashVO>> entry : dataMap.entrySet()) {
			int length = entry.getValue().size();
			if (length > maxLength)
				maxLength = length;
		}

		if (width == 0) {
			width = maxLength * multiple;
			if (width < chartDefWidth)
				width = chartDefWidth;
		}

		XYChart c = new XYChart(width, high, 0xeeeeff, 0x000000, 1);
		c.setRoundedFrame();
		c.setPlotArea(40, 80, width - 80, high - 130, 0xffffff, -1, -1,
				0xcccccc, 0xcccccc);
		c.addLegend(50, 30, false, "Arial Bold", 9).setBackground(
				Chart.Transparent);
		c.addTitle(result.getChart_title(), "Times New Roman Bold Italic", 15)
				.setBackground(0xccccff, 0x000000, Chart.glassEffect());
		int color = 0;
		for (Map.Entry<String, List<HashVO>> entry : dataMap.entrySet()) {
			String linename = entry.getKey();
			HashVO[] vos = entry.getValue().toArray(new HashVO[0]);
			if (Toolkit.isEmpty(vos))
				continue;
			Date[] xVals = new Date[vos.length];
			double[] yVals = new double[vos.length];
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				for (int i = 0; i < vos.length; i++) {
					try {
					UFDouble temp = vos[i].getDoubleValue(linename);
					if (Toolkit.isEmpty(temp))
						continue;
					yVals[i] = temp.toDouble();
					xVals[i] = sdf.parse(vos[i].getStringValue(xField));
					} catch (Exception e) {
						continue;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			c.setTrimData(0);// 设置
			LineLayer layer = c.addLineLayer2();
			layer.setLineWidth(2);
			layer.setXData(xVals);
			layer.addDataSet(yVals, colors[color++], linename);
		}

		return c;

	}

	private XYChart createMultiLineChart(XPRPluginResult result) {
		String xField = result.getxFieldName();
		String yField = result.getyFieldName();
		int width = result.getChart_width();
		int high = result.getChart_high();

		int maxLength = -1;

		Map<String, List<HashVO>> dataMap = result.getDataMap();

		if (Toolkit.isEmpty(dataMap))
			return null;

		for (Map.Entry<String, List<HashVO>> entry : dataMap.entrySet()) {
			int length = entry.getValue().size();
			if (length > maxLength)
				maxLength = length;
		}

		if (width == 0) {
			width = maxLength * multiple;
			if (width < chartDefWidth)
				width = chartDefWidth;
		}

		XYChart c = new XYChart(width, high, 0xeeeeff, 0x000000, 1);
		c.setRoundedFrame();
		c.setPlotArea(40, 80, width - 80, high - 130, 0xffffff, -1, -1,
				0xcccccc, 0xcccccc);
		c.addLegend(50, 30, false, "Arial Bold", 9).setBackground(
				Chart.Transparent);
		c.addTitle(result.getChart_title(), "Times New Roman Bold Italic", 15)
				.setBackground(0xccccff, 0x000000, Chart.glassEffect());
		c.yAxis().setTitle(result.getyFieldName());


		int color = 0;
		for (Map.Entry<String, List<HashVO>> entry : dataMap.entrySet()) {
			String linename = entry.getKey();
			HashVO[] vos = entry.getValue().toArray(new HashVO[0]);
			if (Toolkit.isEmpty(vos))
				continue;
			Date[] xVals = new Date[vos.length];
			double[] yVals = new double[vos.length];
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				for (int i = 0; i < vos.length; i++) {
					UFDouble temp = vos[i].getDoubleValue(yField);
					if (Toolkit.isEmpty(temp))
						continue;
					yVals[i] = temp.toDouble();
					xVals[i] = sdf.parse(vos[i].getStringValue(xField));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			LineLayer layer = c.addLineLayer2();
			layer.setLineWidth(2);
			layer.setXData(xVals);
			layer.addDataSet(yVals, colors[color++], linename);
		}

		return c;
	}

	private XYChart createSigleLineChart(XPRPluginResult result) {
		String xField = result.getxFieldName();
		String yField = result.getyFieldName();
		int width = result.getChart_width();
		int high = result.getChart_high();
		int maxLength = -1;
		Map<String, List<HashVO>> dataMap = result.getDataMap();
		if (Toolkit.isEmpty(dataMap))
			return null;

		for (Map.Entry<String, List<HashVO>> entry : dataMap.entrySet()) {
			int length = entry.getValue().size();
			if (length > maxLength)
				maxLength = length;
		}

		if (width == 0) {
			width = maxLength * multiple;
			if (width < chartDefWidth)
				width = chartDefWidth;
		}

		XYChart c = new XYChart(width, high);
		c.setBackground(c.linearGradientColor(0, 0, 0, 50, 0x99ccff, 0xffffff),
				0x888888);

		c.addTitle(result.getChart_title(), "Times New Roman Bold Italic", 18)
				.setMargin2(0, 0, 16, 0);

		PlotArea plotArea = c.setPlotArea(40, 80, width - 80, high - 130, -1,
				-1, Chart.Transparent, c.dashLineColor(0x444444, 0x000101), -1);

		LegendBox legendBox = c.addLegend(
				plotArea.getLeftX() + plotArea.getWidth() / 2, 45, false,
				"Arial Bold", 10);
		legendBox.setAlignment(Chart.TopCenter);
		legendBox.setBackground(Chart.Transparent, Chart.Transparent);

		c.xAxis().setColors(Chart.Transparent);
		c.yAxis().setColors(Chart.Transparent);
		c.yAxis().setTitle(result.getyFieldName());

		c.xAxis().setMargin(15, 15);

		c.xAxis().setLabelStyle("Arial Bold", 8);
		c.yAxis().setLabelStyle("Arial Bold", 8);

		for (Map.Entry<String, List<HashVO>> entry : dataMap.entrySet()) {
			String linename = entry.getKey();
			HashVO[] vos = entry.getValue().toArray(new HashVO[0]);
			if (Toolkit.isEmpty(vos))
				continue;
			Date[] xVals = new Date[vos.length];
			double[] yVals = new double[vos.length];
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				for (int i = 0; i < vos.length; i++) {
					UFDouble temp = vos[i].getDoubleValue(yField);
					if (Toolkit.isEmpty(temp))
						continue;
					yVals[i] = temp.toDouble();
					xVals[i] = sdf.parse(vos[i].getStringValue(xField));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			LineLayer layer = c.addLineLayer2();
			layer.addDataSet(yVals, 0x6666FF, linename).setDataSymbol(
					Chart.GlassSphere2Shape, 11);
			layer.setXData(xVals);
			layer.setLineWidth(2);
		}

		return c;
	}

	public String getBody() {
		StringBuilder sb = new StringBuilder();
		sb.append("<BODY class='npr'>");
		sb.append(this.getReportHeader());
		if (IMonitorConst.REPORT_TYPE_NPR.equals(this.REPORT_TYPE))
			sb.append(null == this.getNavigate4NPR() ? "" : this
					.getNavigate4NPR());
		else if (IMonitorConst.REPORT_TYPE_BPR.equals(this.REPORT_TYPE))
			sb.append(null == this.getNavigate() ? "" : this.getNavigate());
		sb.append(this.getPluginsHtml());
		sb.append("<P>End of Report");
		sb.append("</BODY>");
		return sb.toString();
	}

	public String getHtml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<HTML><HEAD><TITLE>");
		sb.append(this.getReportTitle());
		sb.append("</TITLE>");
		sb.append(this.getStyleType());
		sb.append("</HEAD>");
		sb.append(this.getBody());
		sb.append("</HTML>");
		return sb.toString();
	}

	private String getReportTitle() {
		return this.report.getReportTitle();
	}

	public String getNavigate() {
		if (this.getPluginResults().length <= 1)
			return null;
		procNavigateMap();
		Map<String, StringBuilder> maxLevelMap2String = getMaxLevelStringMap();
		Map<String, StringBuilder> map = this.mergeNavigate(
				this.maxShowLevel - 1, maxLevelMap2String);
		StringBuilder sb = new StringBuilder();
		sb.append("<H2 class='npr'> Navigate </H2>");
		for (String caption : this.list0Level) {
			sb.append("<UL>");
			sb.append(this.map0Level.get(caption));
			if (!Toolkit.isEmpty(map.get(caption)))
				sb.append(map.get(caption));
			sb.append("</UL>");
		}
		return sb.toString();
	}

	private Map<String, StringBuilder> mergeNavigate(int level,
			Map<String, StringBuilder> map) {
		Map<String, List<StringBuilder>> mapList = this.mapAll.get(level);
		Map<String, StringBuilder> newMap = new HashMap<String, StringBuilder>();
		for (Map.Entry<String, List<StringBuilder>> entry : mapList.entrySet()) {
			List<StringBuilder> list = entry.getValue();
			StringBuilder sb = new StringBuilder();
			for (StringBuilder s : list) {
				sb.append("<UL>");
				sb.append(s);
				String key = s.substring(s.indexOf("#") + 1,
						s.indexOf("'", s.indexOf("#")));
				sb.append(map.get(key));
				sb.append("</UL>");
			}
			newMap.put(entry.getKey(), sb);
		}
		level--;
		if (level > 0)
			this.mergeNavigate(level, newMap);
		return newMap;
	}

	private Map<String, StringBuilder> getMaxLevelStringMap() {
		Map<String, List<StringBuilder>> maxLevelMap = mapAll
				.get(this.maxShowLevel);
		Map<String, StringBuilder> maxLevelMap2String = new HashMap<String, StringBuilder>();
		for (Map.Entry<String, List<StringBuilder>> entry : maxLevelMap
				.entrySet()) {
			List<StringBuilder> list = entry.getValue();
			StringBuilder sb = new StringBuilder();
			sb.append("<UL>");
			for (StringBuilder s : list)
				sb.append(s);
			sb.append("</UL>");
			maxLevelMap2String.put(entry.getKey(), sb);
		}

		return maxLevelMap2String;
	}

	private void procNavigateMap() {
		mapAll = new HashMap<Integer, Map<String, List<StringBuilder>>>();
		map0Level = new HashMap<String, StringBuilder>();
		list0Level = new ArrayList<String>();
		for (XPRPluginResult res : this.getPluginResults()) {
			Object showLevelObje = res.getPluginInfo().getAttributeValue(
					INPRPluginConst.SHOW_LEVEL);
			if (Toolkit.isEmpty(showLevelObje))
				continue;
			Integer showLevel = (Integer) showLevelObje;
			if (this.maxShowLevel < showLevel)
				this.maxShowLevel = showLevel;
			StringBuilder sb = new StringBuilder();
			sb.append("<LI class='npr'><A class='npr' HREF='#");
			String caption = res.getCaption();
			caption = caption.replace("TopN", "Top" + topn);
			sb.append(caption);
			sb.append("'> ");
			if (Toolkit.isEmpty(res.getContentVos())
					&& Toolkit.isEmpty(res.getDataMap())
					&& showLevel == this.maxShowLevel)
				sb.append(caption).append("(无内容)");
			else
				sb.append(caption);
			sb.append("</A>");
			if (showLevel == 0) {
				this.list0Level.add(res.getCaption());
				map0Level.put(res.getCaption(), sb);
				continue;
			}
			String parentNode = res.getParentNode();
			Map<String, List<StringBuilder>> map = mapAll.get(showLevel);
			if (Toolkit.isEmpty(map)) {
				map = new HashMap<String, List<StringBuilder>>();
				mapAll.put(showLevel, map);
			}
			List<StringBuilder> list = map.get(parentNode);
			if (Toolkit.isEmpty(list)) {
				list = new ArrayList<StringBuilder>();
				map.put(parentNode, list);
			}
			list.add(sb);
		}
	}

	public String getNavigate4NPR() {
		if (this.getPluginResults().length <= 1)
			return null;
		StringBuilder sb = new StringBuilder();
		sb.append("<H2 class='npr'> Navigate </H2>");
		sb.append("<UL>");
		for (XPRPluginResult res : this.getPluginResults()) {
			int showLevel = res.getPluginInfo().getIntegerValue(
					INPRPluginConst.SHOW_LEVEL);
			if (showLevel == 1)
				sb.append("<UL>");
			sb.append("<LI class='npr'><A class='npr' HREF='#");
			String caption = res.getCaption();
			caption = caption.replace("TopN", "Top" + topn);
			sb.append(caption);
			sb.append("'> ");
			if (Toolkit.isEmpty(res.getContentVos())
					&& Toolkit.isEmpty(res.getDataMap()) && showLevel == 1)
				sb.append(caption).append("(无内容)");
			else
				sb.append(caption);
			sb.append("</A>");
			if (showLevel == 1)
				sb.append("</UL>");
		}
		sb.append("</UL><A class='npr' HREF='#top'>Back to Top</A>");
		return sb.toString();
	}

	public String getPluginContent(XPRPluginResult res) {
		String format = res.getFormat();
		if (format.equals(INPRPluginConst.FORMAT_TEXT))
			return text2HTML(res);
		else if (format.equals(INPRPluginConst.FORMAT_TABLE))
			return table2HTML(res);
		else if (format.equals(INPRPluginConst.FORMAT_CHART))
			return chart2HTML(res);
		else if (format.equals(INPRPluginConst.FORMAT_SNAPSHOT))
			return snapshot2HTML(res);
		return null;
	}

	/**
	 * @return the pluginResults
	 */
	public XPRPluginResult[] getPluginResults() {
		return pluginResults;
	}

	public String getPluginsHtml() {
		StringBuilder sb = new StringBuilder();
		String plugContent = null;
		for (XPRPluginResult res : this.getPluginResults()) {
			// int showLevel = res.getPluginInfo().getIntegerValue(
			// INPRPluginConst.SHOW_LEVEL);
			// if (showLevel != this.maxShowLevel
			// && Toolkit.isEmpty(res.getContentVos()))
			// continue;
			sb.append("<P> <A class='npr' NAME='");
			String caption = res.getCaption();
			caption = caption.replace("TopN", "Top" + topn);
			sb.append(caption);
			sb.append("'></A>");
			sb.append("<H3 class='npr'>");
			sb.append(caption);
			sb.append("</H3>");
			if (!Toolkit.isEmpty(res.getNoteLines())) {
				sb.append("<UL>");
				for (String s : res.getNoteLines())
					sb.append("<LI class='npr'>").append(s);
				sb.append("</UL>");
			}
			if (Toolkit.isEmpty(res.getFormat()))
				continue;
			plugContent = this.getPluginContent(res);
			if (Toolkit.isEmpty(plugContent))
				sb.append("<UL><LI class='npr'>(无内容)</UL>");
			else
				sb.append(plugContent);
			sb.append("<BR><A class='npr' HREF='#top'>Back to Top</A>");
		}
		return sb.toString();
	}

	public String getReportHeader() {
		return "<H1 class='npr'> " + this.getReportTitle() + "</H1>";
	}

	// /**
	// * @return the reportName
	// */
	// public String getReportName() {
	// return reportName;
	// }

	public String getSimpleHtml(String content) {
		StringBuilder sb = new StringBuilder("<HTML><HEAD></HEAD><BODY>");
		sb.append(content);
		sb.append("</BODY></HTML>");
		return sb.toString();
	}

	public String getStyleType() {
		StringBuilder sb = new StringBuilder();
		sb.append("<style type=\"text/css\"> body.npr { font:bold 10pt Arial, Helvetica, Geneva, sans-serif;");
		sb.append("color:black;");
		sb.append("background:White;");
		sb.append("} pre.npr { font:8pt Courier;");
		sb.append("color:black;");
		sb.append("background:White;");
		sb.append("} h1.npr { font:bold 20pt Arial, Helvetica, Geneva, sans-serif;");
		sb.append("color:#336699;");
		sb.append("background-color:White;");
		sb.append("border-bottom:1px solid #cccc99;");
		sb.append("margin-top:0pt;");
		sb.append("margin-bottom:0pt;");
		sb.append("padding:0px 0px 0px 0px;");
		sb.append("} h2.npr { font:bold 18pt Arial, Helvetica, Geneva, sans-serif;");
		sb.append("color:#336699;");
		sb.append("background-color:White;");
		sb.append("margin-top:4pt;");
		sb.append("margin-bottom:0pt;");
		sb.append("} h3.npr { font:bold 16pt Arial, Helvetica, Geneva, sans-serif;");
		sb.append("color:#336699;");
		sb.append("background-color:White;");
		sb.append("margin-top:4pt;");
		sb.append("margin-bottom:0pt;");
		sb.append("} li.npr { font: 8pt Arial, Helvetica, Geneva, sans-serif;");
		sb.append("color:black;");
		sb.append("background:White;");
		sb.append("} th.nprnobg { font:bold 8pt Arial, Helvetica, Geneva, sans-serif;");
		sb.append("color:black;");
		sb.append("background:White;");
		sb.append("padding-LEFT:4px;");
		sb.append("padding-RIGHT:4px;");
		sb.append("padding-bottom:2px } th.nprbg { font:bold 8pt Arial, Helvetica, Geneva, sans-serif;");
		sb.append("color:White;");
		sb.append("background:#0066CC;");
		sb.append("padding-LEFT:4px;");
		sb.append("padding-RIGHT:4px;");
		sb.append("padding-bottom:2px } td.nprnc { font:8pt Arial, Helvetica, Geneva, sans-serif;");
		sb.append("color:black;");
		sb.append("background:White;");
		sb.append("vertical-align:top;");
		sb.append("} td.nprc { font:8pt Arial, Helvetica, Geneva, sans-serif;");
		sb.append("color:black;");
		sb.append("background:#FFFFCC;");
		sb.append("vertical-align:top;");
		sb.append("} a.npr { font:bold 8pt Arial, Helvetica, sans-serif;");
		sb.append("color:#663300;");
		sb.append("vertical-align:top;");
		sb.append("margin-top:0pt;");
		sb.append("margin-bottom:0pt;");
		sb.append("} </style>");
		return sb.toString();
	}

	private String lineChart2HTML(XPRPluginResult result) {
		StringBuilder sb = new StringBuilder();
		XYChart c = null;
		// Map<String, List<HashVO>> map = result.getDataMap();
		// if (null == map || map.size() == 0)
		// return null;
		// if (map.size() > 1)
		if (result.getCaption() != null
				&& (result.getCaption().indexOf("系统监控") > -1 || result
						.getCaption().indexOf("数据库负载") > -1))
			c = this.createSnmpLineChart(result);
		else {
		c = this.createMultiLineChart(result);
		}
		// else
		// c = this.createSigleLineChart(result);

		ChartViewer viewer = new ChartViewer();
		viewer.setImage(c.makeImage());

		BufferedImage image = (BufferedImage) viewer.getImage();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "jpeg", bos);
			byte[] bytes = bos.toByteArray();
			String base64String = this.byteArrayToBase64(bytes, false);
			if (!Toolkit.isEmpty(base64String)) {
				sb.append("<img src=\"data:image/jpeg;base64,");
				sb.append(base64String);
				sb.append("\"/> ");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.length() == 0 ? "" : sb.toString();
	}

	/**
	 * @param pluginResults
	 *            the pluginResults to set
	 */
	public void setPluginResults(XPRPluginResult[] pluginResults) {
		this.pluginResults = pluginResults;
	}

	/**
	 * @param reportName
	 *            the reportName to set
	 */
	// public void setReportName(String reportName) {
	// this.reportName = reportName;
	// }

	private String snapshot2HTML(XPRPluginResult result) {
		StringBuilder sb = new StringBuilder();
		if (Toolkit.isEmpty(result.getContentVos())
				|| result.getContentVos()[0] == null)
			return "<UL><LI class='npr'>(无内容)</UL>";
		sb.append("<TABLE BORDER=1>");
		HashVO[] hashvos = result.getContentVos();
		String[] columns = hashvos[0].getAttributeNames();
		sb.append("<TR>");
		for (String s : columns) {
			sb.append("<TH class='nprbg'>").append(s).append("</TH>");
		}
		sb.append("</TR>");
		boolean flag = true;
		String colorStyle = null;
		for (HashVO vo : hashvos) {
			if (flag) {
				colorStyle = "nprc";
				flag = false;
			} else {
				colorStyle = "nprnc";
				flag = true;
			}
			sb.append("<TR>");
			for (String s : columns) {
				sb.append("<TD class='").append(colorStyle).append("'>");
				if (!s.equals(result.getLinkFieldName()))
					sb.append(vo.getStringValue(s));
				else
					// sb.append("<a href=\"").append("snapshot/snapshot-")
					// .append(vo.getStringValue(s))
					// .append(".html\" target=\"_blank\" >")
					// .append(vo.getStringValue(s)).append("</a>");
					sb.append(vo.getStringValue(s));
				sb.append("</TH>");
			}
			sb.append("</TR>");
		}
		sb.append("</TABLE>");
		return sb.toString();
	}

	private String table2HTML(XPRPluginResult result) {
		StringBuilder sb = new StringBuilder();
		if (Toolkit.isEmpty(result.getContentVos())
				|| result.getContentVos()[0] == null)
			return "<UL><LI class='npr'>(无内容)</UL>";
		sb.append("<TABLE BORDER=1>");
		HashVO[] hashvos = result.getContentVos();
		String[] columns = hashvos[0].getAttributeNames();
		sb.append("<TR>");
		for (String s : columns) {
			sb.append("<TH class='nprbg'>").append(s).append("</TH>");
		}
		sb.append("</TR>");
		boolean flag = true;
		String colorStyle = null;
		for (HashVO vo : hashvos) {
			if (Toolkit.isEmpty(vo))
				continue;
			if (flag) {
				colorStyle = "nprc";
				flag = false;
			} else {
				colorStyle = "nprnc";
				flag = true;
			}
			sb.append("<TR>");
			for (String s : columns) {
				sb.append("<TD class='").append(colorStyle).append("'>");
				sb.append(vo.getStringValue(s));
				sb.append("</TH>");
			}
			sb.append("</TR>");
		}
		sb.append("</TABLE>");
		return sb.toString();
	}

	private String text2HTML(XPRPluginResult result) {
		StringBuilder sb = new StringBuilder();
		sb.append("<UL><LI class='npr'>");
		sb.append(result.getContentByText());
		sb.append("</UL>");
		return result.getContentByText();
	}

	// private String xyChart2HTML(NPRPluginResult result) {
	// return null;
	// }

	public String getIndexHtml(HashVO[] reportLogVOs) {
		StringBuilder sb = new StringBuilder();
		sb.append("<HTML><HEAD><TITLE>NPR Report index</TITLE>");
		sb.append(this.getStyleType());
		sb.append("</HEAD>");
		sb.append("<BODY class='npr'>");
		sb.append("<H1 class='npr'>NPR Report index</H1>");
		sb.append("<H3 class='npr'>报告索引</H3>");
		sb.append("<TABLE BORDER=1>");
		sb.append("<TR><TH class='nprbg'>创建时间</TH><TH class='nprbg'>快照ID</TH><TH class='nprbg'>报告链接</TH></TR>");
		for (HashVO vo : reportLogVOs) {
			sb.append("<TR>");
			sb.append("<TD class='nprc'>");
			sb.append(vo.getStringValue(INPRConst.NPR_COL_CREATE_TIME));
			sb.append("</TH>");
			sb.append("<TD class='nprc'>");
			sb.append(vo.getStringValue(INPRConst.NPR_COL_SNAPSHOT_ADDR));
			sb.append("</TH>");
			sb.append("<TD class='nprc'>");
			sb.append("<a href=\"");
			sb.append(vo.getStringValue(INPRConst.NPR_COL_BODY_REPORT_URL));
			sb.append("\" target=\"_blank\" >打开报告链接</a>");
			sb.append("</TH>");
			sb.append("</TR>");
		}
		sb.append("</TABLE>");
		sb.append("</BODY>");
		sb.append("</HTML>");
		return sb.toString();
	}

	public BPRReport getReport() {
		return report;
	}

	public void setReport(BPRReport report) {
		this.report = report;
	}

	public String getDestFileName() {
		return destFileName;
	}

	public void setDestFileName(String destFileName) {
		this.destFileName = destFileName;
	}

}
