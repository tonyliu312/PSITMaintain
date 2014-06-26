/**
 * 
 */
package uap.ps.itm.npr.vo;

import nc.vo.pub.lang.UFDateTime;

/**
 * NPR最终生成的报告VO
 * 
 * @author liuzy
 * 
 */
public class BPRReport {

	private UFDateTime beginTime = null;

	private UFDateTime endTime = null;

	private XPRPluginResult[] pluginResult = null;

	private String destFileName = null;
	
	private String reportTitle = null;

	/**
	 * @return the beginTime
	 */
	public UFDateTime getBeginTime() {
		return beginTime;
	}

	/**
	 * @param beginTime
	 *            the beginTime to set
	 */
	public void setBeginTime(UFDateTime beginTime) {
		this.beginTime = beginTime;
	}

	/**
	 * @return the endTime
	 */
	public UFDateTime getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime
	 *            the endTime to set
	 */
	public void setEndTime(UFDateTime endTime) {
		this.endTime = endTime;
	}

	/**
	 * @return the pluginResult
	 */
	public XPRPluginResult[] getPluginResult() {
		return pluginResult;
	}

	/**
	 * @param pluginResult
	 *            the pluginResult to set
	 */
	public void setPluginResult(XPRPluginResult[] pluginResult) {
		this.pluginResult = pluginResult;
	}

	/**
	 * 获得所有段落的标题
	 * 
	 * @return
	 */
	public String[] getCaptions() {
		String[] captions = new String[this.getPluginResult().length];
		for (int i = 0; i < captions.length; i++)
			captions[i] = this.getPluginResult()[i].getCaption();
		return captions;
	}

	/**
	 * @return the destFileName
	 */
	public String getDestFileName() {
		return destFileName;
	}

	/**
	 * @param destFileName
	 *            the destFileName to set
	 */
	public void setDestFileName(String destFileName) {
		this.destFileName = destFileName;
	}

	public String getReportTitle() {
		return reportTitle;
	}

	public void setReportTitle(String reportTitle) {
		this.reportTitle = reportTitle;
	}

}
