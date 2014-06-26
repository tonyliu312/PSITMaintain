/**
 * 
 */
package uap.ps.itm.npr.service.plugin;

import java.util.Comparator;

import uap.ps.itm.monitor.pub.HashVO;

/**
 * @author liuzy
 * 
 */
public class Top10DefComparetor implements Comparator<HashVO> {

	private String column = null;

	private boolean isMsgCol = false;

	public Top10DefComparetor(String columnName, boolean isMsgCol) {
		this.column = columnName;
		this.isMsgCol = isMsgCol;
	}


	public int compare(HashVO o1, HashVO o2) {
		long num1 = 0;
		long num2 = 0;
		if (isMsgCol) {
			num1 = o1.getMsgLongValueByField(this.column,null);
			num2 = o2.getMsgLongValueByField(this.column,null);
		} else {
			num1 = Long.valueOf(o1.getStringValue(this.column));
			num2 = Long.valueOf(o2.getStringValue(this.column));
		}
		if (num1 < num2)
			return 1;
		else if (num1 > num2)
			return -1;
		return 0;

	}

}
