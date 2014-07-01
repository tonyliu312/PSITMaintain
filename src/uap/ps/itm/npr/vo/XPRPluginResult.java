package uap.ps.itm.npr.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.monitor.pub.HashVO;
import nc.monitor.pub.Toolkit;
import uap.ps.itm.npr.service.plugin.INPRPluginConst;
import nc.vo.pub.lang.UFDateTime;

/**
 * 一个插件的输出结果数据结构
 * 
 * @author liuzy
 * 
 */
public class XPRPluginResult implements Comparable<XPRPluginResult>,
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5500795410721251632L;

	private String caption = null;

	private String format = null;

	private String chart_type = null;

	private UFDateTime beginTime = null;

	private UFDateTime endTime = null;

	private String xFieldName = null;

	private String yFieldName = null;

	private String yTital = null;

	private int timeBucket = 0;

	private HashVO[] contentVos = null;

	private String contentByText = null;

	private String[] noteLines = null;

	private String chart_title = null;

	private String line_name = null;

	private int chart_width = 800;

	private int chart_high = 600;

	private int show_order = -1;

	private String linkFieldName = null;

	private HashVO pluginInfo = null;

	private Map<String, List<HashVO>> dataMap = null;

	private String parentNode = null;

	public XPRPluginResult(String caption) {
		this.setCaption(caption);
	}

	public XPRPluginResult(HashVO vo) {
		this.pluginInfo = vo;
		this.setCaption((String) vo.getAttributeValue(INPRPluginConst.CAPTION));
		this.setFormat((String) vo.getAttributeValue(INPRPluginConst.FORMAT));
		this.setChart_type((String) vo
				.getAttributeValue(INPRPluginConst.CHART_TYPE));
		this.setxFiledName((String) vo
				.getAttributeValue(INPRPluginConst.X_FIELD));
		this.setTimeBucket(vo.getIntegerValue(INPRPluginConst.TIME_BUCKET));
		this.setyFieldName((String) vo
				.getAttributeValue(INPRPluginConst.Y_FIELD));
		this.setyTital((String) vo.getAttributeValue(INPRPluginConst.Y_TITAL));
		this.setChart_title(vo.getStringValue(INPRPluginConst.CHART_TITLE));
		this.setLine_name(vo.getStringValue(INPRPluginConst.LINE_NAME));
		this.setChart_width(vo.getIntegerValue(INPRPluginConst.CHART_WIDTH));
		this.setChart_high(vo.getIntegerValue(INPRPluginConst.CHART_HIGH));
		this.setShow_order(vo.getIntegerValue(INPRPluginConst.SHOW_ORDER));
		this.setLinkFieldName(vo.getStringValue(INPRPluginConst.LINK_FIELD));
	}

	/**
	 * @param dataMap
	 *            the dataMap to set
	 */
	public void addData2Map(String name, HashVO[] vos) {
		if (null == this.dataMap)
			this.dataMap = new HashMap<String, List<HashVO>>();
		List<HashVO> list = this.dataMap.get(name);
		if (null == list)
			list = new ArrayList<HashVO>();
		list.addAll(Arrays.asList(vos));
		this.dataMap.put(name, list);
	}

	public void addData2Map(String name, List<HashVO> listVOs) {
		if (null == this.dataMap)
			this.dataMap = new HashMap<String, List<HashVO>>();
		List<HashVO> list = this.dataMap.get(name);
		if (null == list)
			list = new ArrayList<HashVO>();
		this.dataMap.put(name, list);
		list.addAll(listVOs);
	}

	public void addData2Map(String name, HashVO vo) {
		if (null == this.dataMap)
			this.dataMap = new HashMap<String, List<HashVO>>();
		List<HashVO> list = this.dataMap.get(name);
		if (null == list)
			list = new ArrayList<HashVO>();
		list.add(vo);
		this.dataMap.put(name, list);
	}


	public int compareTo(XPRPluginResult o) {
		if (Toolkit.isEmpty(o))
			return -1;
		int thisOrder = this.getShow_order();
		int oOrder = o.getShow_order();
		if (thisOrder == oOrder)
			return 0;
		else if (thisOrder > oOrder)
			return 1;
		else
			return -1;
	}

	/**
	 * @return the beginTime
	 */
	public UFDateTime getBeginTime() {
		return beginTime;
	}

	/**
	 * @return the caption
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * @return the chart_high
	 */
	public int getChart_high() {
		return chart_high;
	}

	/**
	 * @return the chart_title
	 */
	public String getChart_title() {
		return chart_title;
	}

	/**
	 * @return the format_type
	 */
	public String getChart_type() {
		return chart_type;
	}

	/**
	 * @return the chart_width
	 */
	public int getChart_width() {
		return chart_width;
	}

	/**
	 * @return the contentByText
	 */
	public String getContentByText() {
		return contentByText;
	}

	/**
	 * @return the contentVos
	 */
	public HashVO[] getContentVos() {
		return contentVos;
	}

	/**
	 * @return the dataMap
	 */
	public Map<String, List<HashVO>> getDataMap() {
		return dataMap;
	}

	/**
	 * @return the endTime
	 */
	public UFDateTime getEndTime() {
		return endTime;
	}

	/**
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @return the line_name
	 */
	public String getLine_name() {
		return line_name;
	}

	/**
	 * @return the linkFieldName
	 */
	public String getLinkFieldName() {
		return linkFieldName;
	}

	/**
	 * @return the noteLines
	 */
	public String[] getNoteLines() {
		return noteLines;
	}

	/**
	 * @return the pluginInfo
	 */
	public HashVO getPluginInfo() {
		return pluginInfo;
	}

	/**
	 * @return the show_order
	 */
	public int getShow_order() {
		return show_order;
	}

	/**
	 * @return the timeBucket
	 */
	public int getTimeBucket() {
		return timeBucket;
	}

	/**
	 * @return the xFiledName
	 */
	public String getxFieldName() {
		return xFieldName;
	}

	/**
	 * @return the yFieldName
	 */
	public String getyFieldName() {
		return yFieldName;
	}

	/**
	 * @return the yTital
	 */
	protected String getyTital() {
		return yTital;
	}

	/**
	 * @param beginTime
	 *            the beginTime to set
	 */
	public void setBeginTime(UFDateTime beginTime) {
		this.beginTime = beginTime;
	}

	/**
	 * @param caption
	 *            the caption to set
	 */
	public void setCaption(String caption) {
		this.caption = caption;
	}

	/**
	 * @param chart_high
	 *            the chart_high to set
	 */
	public void setChart_high(int chart_high) {
		this.chart_high = chart_high;
	}

	/**
	 * @param chart_title
	 *            the chart_title to set
	 */
	public void setChart_title(String chart_title) {
		this.chart_title = chart_title;
	}

	/**
	 * @param format_type
	 *            the format_type to set
	 */
	public void setChart_type(String format_type) {
		this.chart_type = format_type;
	}

	/**
	 * @param chart_width
	 *            the chart_width to set
	 */
	public void setChart_width(int chart_width) {
		this.chart_width = chart_width;
	}

	/**
	 * @param contentByText
	 *            the contentByText to set
	 */
	public void setContentByText(String contentByText) {
		this.contentByText = contentByText;
	}

	/**
	 * @param contentVos
	 *            the contentVos to set
	 */
	public void setContentVos(HashVO[] contentVos) {
		this.contentVos = contentVos;
	}

	/**
	 * @param endTime
	 *            the endTime to set
	 */
	public void setEndTime(UFDateTime endTime) {
		this.endTime = endTime;
	}

	/**
	 * @param format
	 *            the format to set
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * @param line_name
	 *            the line_name to set
	 */
	public void setLine_name(String line_name) {
		this.line_name = line_name;
	}

	/**
	 * @param linkFieldName
	 *            the linkFieldName to set
	 */
	public void setLinkFieldName(String linkFieldName) {
		this.linkFieldName = linkFieldName;
	}

	/**
	 * @param noteLines
	 *            the noteLines to set
	 */
	public void setNoteLines(String[] noteLines) {
		this.noteLines = noteLines;
	}

	/**
	 * @param pluginInfo
	 *            the pluginInfo to set
	 */
	public void setPluginInfo(HashVO pluginInfo) {
		this.pluginInfo = pluginInfo;
	}

	/**
	 * @param show_order
	 *            the show_order to set
	 */
	public void setShow_order(int show_order) {
		this.show_order = show_order;
	}

	/**
	 * @param timeBucket
	 *            the timeBucket to set
	 */
	public void setTimeBucket(int timeBucket) {
		this.timeBucket = timeBucket;
	}

	/**
	 * @param xFiledName
	 *            the xFiledName to set
	 */
	public void setxFiledName(String xFiledName) {
		this.xFieldName = xFiledName;
	}

	/**
	 * @param yFieldName
	 *            the yFieldName to set
	 */
	public void setyFieldName(String yFieldName) {
		this.yFieldName = yFieldName;
	}

	/**
	 * @param yTital
	 *            the yTital to set
	 */
	protected void setyTital(String yTital) {
		this.yTital = yTital;
	}

	public String getParentNode() {
		return parentNode;
	}

	public void setParentNode(String parentNode) {
		this.parentNode = parentNode;
	}

	/**
	 * @param dataMap
	 *            the dataMap to set
	 */
	public void setDataMap(Map<String, List<HashVO>> dataMap) {
		this.dataMap = dataMap;
	}

}
