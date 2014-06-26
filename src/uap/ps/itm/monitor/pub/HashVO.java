package uap.ps.itm.monitor.pub;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;

public class HashVO extends nc.vo.pub.CircularlyAccessibleValueObject {
	private static final long serialVersionUID = 1L;

	private final Hashtable<String, Object> m_hData = new Hashtable<String, Object>();

	private final Hashtable<String, Object> m_hShow = new Hashtable<String, Object>();

	// sorted fld.
	ArrayList<String> sortedFld = new ArrayList<String>();

	private String root = "record";

	private String fldPrefix = "$$";

	public HashVO() {
		super();
	}

	@Override
	public Object getAttributeValue(String attributeName) {
		return m_hData.get(attributeName.toLowerCase());
	}

	@Override
	public void setAttributeValue(String key, Object value) {
		if (key == null)
			return;
		if (value == null) {
			m_hData.remove(key.toLowerCase());
			m_hShow.remove(key.toLowerCase());
			sortedFld.remove(key.toLowerCase());
		} else {
			m_hData.put(key.toLowerCase(), value);
			m_hShow.put(key.toLowerCase(), true);
			setFld(key.toLowerCase());
		}
		// // zhaogb 2011-08-26 修改原因：当value==null时，表中不显示
		// m_hData.put(key.toLowerCase(), value);
		// setFld(key.toLowerCase());
	}

	public void setHideAttributeValue(String key, Object value) {
		if (key == null)
			return;
		if (value == null) {
			m_hData.remove(key.toLowerCase());
			m_hShow.remove(key.toLowerCase());
			sortedFld.remove(key.toLowerCase());
		} else {
			m_hData.put(key.toLowerCase(), value);
			m_hShow.put(key.toLowerCase(), false);
			// setFld(key.toLowerCase());
		}
		// // zhaogb 2011-08-26 修改原因：当value==null时，表中不显示
		// m_hData.put(key.toLowerCase(), value);
		// setFld(key.toLowerCase());
	}

	private void setFld(String fld) {
		for (int i = 0; i < sortedFld.size(); i++) {
			if (sortedFld.get(i).equals(fld))
				return;
		}
		sortedFld.add(fld);
	}

	@Override
	public void validate() {
		return;
	}

	@Override
	public java.lang.String[] getAttributeNames() {
		if (sortedFld != null && sortedFld.size() > 0)
			return sortedFld.toArray(new String[sortedFld.size()]);
		Enumeration<String> en = m_hData.keys();
		Vector<String> v = new Vector<String>();
		while (en.hasMoreElements()) {
			v.add(en.nextElement());
		}
		String[] arsKey = new String[v.size()];
		v.copyInto(arsKey);
		return arsKey;
	}

	public java.lang.String[] getShowAttributeNames() {
		if (sortedFld != null && sortedFld.size() > 0)
			return sortedFld.toArray(new String[sortedFld.size()]);
		Enumeration<String> en = m_hShow.keys();
		Vector<String> v = new Vector<String>();
		while (en.hasMoreElements()) {
			String key = en.nextElement();
			boolean show = (Boolean) m_hShow.get(key);
			if (show)
				v.add(key);
		}
		String[] arsKey = new String[v.size()];
		v.copyInto(arsKey);
		return arsKey;
	}

	public Boolean getBooleanValue(String attrname) {
		Object o = getAttributeValue(attrname);
		if (o == null)
			return false;
		if (o instanceof Boolean)
			return (Boolean) o;
		return new Boolean(o.toString());

	}

	public UFDouble getDoubleValue(String attrname) {
		Object o = getAttributeValue(attrname);
		if (o == null) {
			setAttributeValue(attrname, new UFDouble(0));
			return getDoubleValue(attrname);
		} else if (o instanceof UFDouble)
			return (UFDouble) o;
		else
			return new UFDouble(o.toString());
	}

	@Override
	public java.lang.String getEntityName() {
		return null;
	}

	public Integer getIntegerValue(String attrname) {
		Object o = getAttributeValue(attrname);
		if (Toolkit.isEmpty(o))
			return new Integer(0);
		if (o instanceof Integer)
			return (Integer) o;
		return new Integer(o.toString());
	}

	public String getStringValue(String attrname) {
		if (attrname == null)
			return "";
		Object o = getAttributeValue(attrname);
		return o == null ? "" : o.toString();

	}

	public UFDate getUFDateValue(String attrname) {
		Object o = getAttributeValue(attrname);
		if (o == null)
			return null;
		if (o instanceof UFDate)
			return (UFDate) o;
		return new UFDate(o.toString());
	}

	@Override
	public String toString() {
		if (Toolkit.isEmpty(sortedFld))
			return "";
		StringBuffer sb = new StringBuffer();
		String value = null;
		for (int i = 0; i < sortedFld.size(); i++) {
			value = getStringValue(sortedFld.get(i));
			if (sortedFld.get(i).toLowerCase().equals("password"))
				value = "***";
			if (Toolkit.isEmpty(value))
				continue;
			value = value.trim();
			sb.append(sortedFld.get(i) + "=" + value);
			if (i != sortedFld.size() - 1)
				sb.append(";");
		}
		return sb.toString();
	}

	/**
	 * 新补丁管理系统界面展示
	 *
	 * @return
	 * @date 2013-1-9 上午09:49:46
	 * @since NC6.3
	 */
	public String toString1() {
		if (Toolkit.isEmpty(sortedFld))
			return "";
		StringBuffer sb = new StringBuffer();
		String value = null;
		for (int i = 0; i < sortedFld.size(); i++) {
			value = getStringValue(sortedFld.get(i));
			if (sortedFld.get(i).toLowerCase().equals("password"))
				value = "***";
			if (Toolkit.isEmpty(value))
				continue;
			value = value.trim();
			sb.append(sortedFld.get(i) + "=" + value);
			if (i != sortedFld.size() - 1)
				sb.append(";");
			sb.append("\n");
		}
		return sb.toString();
	}

	public String toXml0() {
		String[] flds = getAttributeNames();
		StringBuffer sb = new StringBuffer();
		sb.append("<" + getRootName()).append(">\n");
		String value = null;
		for (int i = 0; i < flds.length; i++) {
			value = getStringValue(sortedFld.get(i));
			if (Toolkit.isEmpty(value))
				continue;
			value = value.trim();
			sb.append("\t");
			sb.append("<").append(flds[i]).append(">");
			sb.append(value);
			sb.append("</").append(flds[i]).append(">");
			sb.append("\n");
		}
		sb.append("</");
		sb.append(getRootName());
		sb.append(">");
		return sb.toString();
	}

	public String toXml() {
		String[] flds = getAttributeNames();
		StringBuffer sb = new StringBuffer();
		sb.append("<" + getRootName());
		String value = null;
		for (int i = 0; i < flds.length; i++) {
			value = getStringValue(sortedFld.get(i));
			if (Toolkit.isEmpty(value))
				continue;
			value = value.trim();
			if (!value.startsWith("\""))
				value = "\"" + value + "\"";
			sb.append("\t");
			sb.append(flds[i].replace("%", "-"));
			sb.append("=" + value);
		}
		sb.append(">\n</");
		sb.append(getRootName());
		sb.append(">");
		return sb.toString();
	}

	public String getRootName() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public void setFldPrefix(String fldPrefix) {
		this.fldPrefix = fldPrefix;
	}

	public String toString0() {
		if (Toolkit.isEmpty(sortedFld))
			return null;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < sortedFld.size(); i++) {
			sb.append(fldPrefix).append(sortedFld.get(i));
			sb.append("=").append(getStringValue(sortedFld.get(i)));
			sb.append("\t");
		}
		return sb.toString();
	}

	public String toSql(String tablename) {
		StringBuffer sb = new StringBuffer();
		sb.append("insert into ").append(tablename);
		sb.append("(");
		StringBuffer sbValues = new StringBuffer();
		for (int i = 0; i < sortedFld.size(); i++) {
			sb.append(sortedFld.get(i));
			String value = getStringValue(sortedFld.get(i));
			if (value != null) {
				value = value.replace("'", "''");
				value = value.replace("for update", "for ''update");
			}
			sbValues.append("\'").append(value);
			sbValues.append("\'");
			if (i != sortedFld.size() - 1) {
				sb.append(",");
				sbValues.append(",");
			}
		}
		sb.append(") values (").append(sbValues.toString()).append(")");
		String sql = sb.toString();
		// System.out.println(sql);
		return sql;
	}

	public String[] toSqlArray(String tablename, String splitAttrib) {
		int len = 640 * 1000;
		String sVal = getStringValue(splitAttrib);
		if (sVal == null) {
			return new String[] { toSql(tablename) };
		}
		int ss = sVal.length() / len;
		if (ss * len < sVal.length()) {
			ss++;
		}

		String[] sqls = new String[ss];
		for (int k = 0; k < ss; k++) {
			String s = sVal.substring(
					k * len,
					(k + 1) * len < sVal.length() ? (k + 1) * len : sVal
							.length());
			StringBuffer sb = new StringBuffer();
			sb.append("insert into ").append(tablename);
			sb.append("(");
			StringBuffer sbValues = new StringBuffer();
			for (int i = 0; i < sortedFld.size(); i++) {

				sb.append(sortedFld.get(i));
				String value = null;
				if (sortedFld.get(i).equalsIgnoreCase(splitAttrib)) {
					value = s;
				} else {
					value = getStringValue(sortedFld.get(i));
				}
				if (value != null) {
					value = value.replace("'", "''");
					value = value.replace("for update", "for ''update");
				}
				sbValues.append("\'").append(value);
				sbValues.append("\'");
				if (i != sortedFld.size() - 1) {
					sb.append(",");
					sbValues.append(",");
				}
			}
			sb.append(") values (").append(sbValues.toString()).append(")");
			String sql = sb.toString();
			sqls[k] = sql;
		}

		return sqls;
	}

	public static final void main(String[] args) {
		HashVO vo = new HashVO();
		vo.setAttributeValue("test", "test1");
		vo.setAttributeValue("test1", "test2");
		System.out.println(vo.toSql("testtable"));
	}

	public void trimFld(String[] targetFlds, String addToFldName) {
		String[] attrs = getAttributeNames();
		String msg = (addToFldName == null ? "" : getStringValue(addToFldName));
		for (int i = 0; i < attrs.length; i++) {
			boolean isStardardFld = false;
			for (int j = 0; j < targetFlds.length; j++) {
				if (Toolkit.isEqual(attrs[i], targetFlds[j])) {
					isStardardFld = true;
					break;
				}
			}
			if (!isStardardFld) {
				msg += "$" + attrs[i] + " " + getStringValue(attrs[i]);
				setAttributeValue(attrs[i], null);
			}
		}
		if (!Toolkit.isEmpty(addToFldName))
			setAttributeValue(addToFldName, msg);
	}

	public boolean isEqual(HashVO voTarget) {
		if (voTarget == null)
			return false;
		for (int i = 0; i < sortedFld.size(); i++) {
			if (!Toolkit.isEqual(getAttributeValue(sortedFld.get(i)),
					voTarget.getAttributeValue(sortedFld.get(i)))) {
				return false;
			}
		}
		return true;
	}

	public String getMsgStringValueByField(String fieldName, String token) {
		return getMsgPart(fieldName, token);
	}

	public int getMsgIntValueByField(String fieldName, String token)
			throws NumberFormatException {
		String retValue = getMsgPart(fieldName, token);

		if (Toolkit.isEmpty(retValue)) {
			System.out.println("获取msg字符串中" + fieldName + "字段时发生错误");
			// return Integer.MIN_VALUE;
		}
		return Integer.valueOf(retValue);
	}

	private String getMsgPart(String fieldName, String token) {
		if (Toolkit.isEmpty(token))
			token = ";";
		if (";".equals(token))
			return this.getMsgPartTokenSemicolon(fieldName);
		else if ("msg".equals(fieldName))
			return this.getMsgPartFieldMsg();
		else if ("|".equals(token))
			return this.getMsgPartTokenStick(fieldName);
		return null;
	}

	public Long getMsgLongValueByField(String fieldName, String token)
			throws NumberFormatException {
		String retValue = getMsgPart(fieldName, token);

		if (Toolkit.isEmpty(retValue))
			return 0L;

		// if (Toolkit.isEmpty(retValue)) {
		// System.out.println("获取msg字符串中" + fieldName + "字段时发生错误");
		// return Long.MIN_VALUE;
		// }
		return Long.valueOf(retValue);
	}

	private String getMsgPartTokenStick(String fieldName) {
		String token = "|";
		fieldName = token + fieldName;
		String msg = this.getStringValue("msg");
		if (Toolkit.isEmpty(msg))
			return null;
		int begin = msg.indexOf(fieldName) + 1;
		if (begin == 0)
			return null;
		int end = msg.indexOf(token, begin);
		if (end == -1)
			end = msg.length();
		String keyValue = msg.substring(begin, end);
		begin = keyValue.indexOf("=") + 1;
		if (begin == 0)
			return null;
		return keyValue.substring(begin, keyValue.length());
	}

	private String getMsgPartTokenSemicolon(String fieldName) {
		String token = ";";
		String msg = this.getStringValue("msg");
		if (Toolkit.isEmpty(msg))
			return null;
		int begin = msg.indexOf(fieldName);
		if (begin < 0)
			return null;
		int end = msg.indexOf(token, begin);
		if (end == -1)
			end = msg.length();
		String keyValue = msg.substring(begin, end);
		begin = keyValue.indexOf("=") + 1;
		if (begin == 0)
			return null;
		return keyValue.substring(begin, keyValue.length());
	}

	private String getMsgPartFieldMsg() {
		String token = "|costtime";
		String fieldName = "msg";
		String msg = this.getStringValue(fieldName);
		if (Toolkit.isEmpty(msg))
			return null;
		int begin = 0;
		int end = msg.indexOf(token, begin);
		if (end == -1)
			end = msg.length();
		return msg.substring(begin, end);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nc.vo.pub.ValueObject#clone()
	 */
	@Override
	public Object clone() {
		HashVO vo = new HashVO();
		for (String filed : this.getAttributeNames())
			vo.setAttributeValue(filed, this.getAttributeValue(filed));
		return vo;
	}
}
