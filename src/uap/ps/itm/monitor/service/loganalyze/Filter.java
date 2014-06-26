package uap.ps.itm.monitor.service.loganalyze;

import java.io.Serializable;

import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Toolkit;

public class Filter implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final String GREATER = ">";

	private static final String LESS = "<";

	private static final String EQ = "=";

	private static final String GREATANDEQ = ">=";

	private static final String LESSANDEQ = "<=";

	private static final String LIKE = "like";

	private String fld;

	private String oper;

	private String conValue;

	public Filter(String fld, String oper, String srcconValue) {
		this.fld = fld;
		this.oper = oper;
		if (srcconValue.startsWith("\""))
			this.conValue = srcconValue.substring(1, srcconValue.length() - 1);
		else
			this.conValue = srcconValue;
		this.conValue=conValue.toLowerCase();
	}

	public boolean filter(HashVO vo) {
		if (oper.equals(GREATER)) {
			return greater(vo);
		} else if (oper.equals(LESS)) {
			return less(vo);
		} else if (oper.equals(EQ)) {
			return equal(vo);
		} else if (oper.equals(GREATANDEQ)) {
			return greatandequal(vo);
		} else if (oper.equals(LESSANDEQ)) {
			return lessandequal(vo);
		} else if (oper.equals(LIKE)) {
			return like(vo);
		} else {
			return false;
		}
	}

	private boolean like(HashVO vo) {
		String value = vo.getStringValue(fld);
		if (value == null)
			return false;
		return value.toLowerCase().indexOf(conValue.toLowerCase()) > -1;
	}

	private boolean lessandequal(HashVO vo) {
		String value = vo.getStringValue(fld);
		if (value == null)
			return false;
		value = value.toLowerCase();
		String dvalue = trimquote(value);
		String dconValue = trimquote(conValue);
		if (isNum(dvalue) && isNum(dconValue))
			return compare(getdouble(dvalue), getdouble(dconValue)) <= 0;
		return value.compareTo(conValue) <= 0;
	}

	private boolean greatandequal(HashVO vo) {
		String value = vo.getStringValue(fld);
		if (value == null)
			return false;
		value = value.toLowerCase();
		String dvalue = trimquote(value);
		String dconValue = trimquote(conValue);
		if (isNum(dvalue) && isNum(dconValue))
			return compare(getdouble(dvalue), getdouble(dconValue)) >= 0;
		return value.compareTo(conValue) >= 0;

	}

	private String trimquote(String value) {
		if (value != null && value.startsWith("\""))
			return value.substring(1, value.length() - 1);
		return value;
	}

	private boolean less(HashVO vo) {
		String value = vo.getStringValue(fld);
		if (value == null)
			return false;
		value = value.toLowerCase();
		String dvalue = trimquote(value);
		String dconValue = trimquote(conValue);
		if (isNum(dvalue) && isNum(dconValue))
			return compare(getdouble(dvalue), getdouble(dconValue)) < 0;

		return value.compareTo(conValue) < 0;

	}

	private boolean greater(HashVO vo) {
		String value = vo.getStringValue(fld);
		if (value == null)
			return false;
		value = value.toLowerCase();
		String dvalue = trimquote(value);
		String dconValue = trimquote(conValue);
		if (isNum(dvalue) && isNum(dconValue)) {
			return compare(getdouble(dvalue), getdouble(dconValue)) > 0;
		}
		return value.compareTo(conValue) > 0;

	}

	private boolean equal(HashVO vo) {
		String value = vo.getStringValue(fld);
		if (value == null)
			return false;
		value = value.toLowerCase();
		return value.compareTo(conValue) == 0;
	}

	private double getdouble(String src) {
		if (Toolkit.isEmpty(src))
			return 0.0;
		return Double.valueOf(src);
	}

	public boolean isNum(String src) {
		return Toolkit.isNum(src);

	}

	private int compare(double d1, double d2) {
		if (d1 == d2)
			return 0;
		if (d1 > d2)
			return 1;
		if (d2 < d2)
			return -1;
		return -1;
	}

	public String getFld() {
		return fld;
	}

	public String getOper() {
		return oper;
	}

	public String getValue() {
		return conValue;
	}

}
