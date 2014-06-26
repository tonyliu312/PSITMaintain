package uap.ps.itm.monitor.service.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import nc.vo.pub.lang.UFDouble;
import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Toolkit;

public class DBExecutor {
	private String sConnStr = null;
	private String username = null;
	private String password = null;
	private String dbDesc = null;
	private static final int MAXROWS = 100000;

	public DBExecutor(String sDBDriver, String sConnStr, String username,
			String password) {
		try {
			this.sConnStr = sConnStr;
			this.username = username;
			this.password = password;
			Class.forName(sDBDriver);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public Connection getConn() throws Exception {
		Connection con = DriverManager.getConnection(sConnStr, username,
				password);
		con.setAutoCommit(true);
		return con;
	}

	synchronized public void executeUpdate(String sql, List<List<?>> params)
			throws Exception {
		Connection con = null;
		try {
			con = getConn();
			PreparedStatement ptmt = con.prepareStatement(sql);
			for (List<?> param : params) {
				for (int i = 0; i < param.size(); i++) {
					Object val = param.get(i);
					if (val instanceof String) {
						ptmt.setString(i + 1, (String) val);
					} else if (val instanceof Integer) {
						ptmt.setInt(i + 1, (Integer) val);
					}
				}
				ptmt.addBatch();
			}
			ptmt.executeBatch();
		} catch (Exception ex) {
			throw ex;
		} finally {
			try {
				con.close();
			} catch (Exception e) {
			}
		}
	}

	public void executeUpdate(String sql) throws Exception {
		Connection con = null;
		try {
			con = getConn();
			Statement stmt = con.createStatement();
			stmt.execute(sql);
		} catch (Exception ex) {
			// ex.printStackTrace();
			// System.err.println(ex.getMessage());
			throw ex;
		} finally {
			try {
				con.close();
			} catch (Exception e) {
			}
		}

	}

	public HashVO[] queryVOBySql(String sql) throws Exception {
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		Vector<HashVO> vResult = new Vector<HashVO>();
		try {
			con = getConn();
			stmt = con.createStatement();
			rs = stmt.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			HashVO voTmp = null;
			int irownum = 0;
			while (rs.next()) {
				voTmp = new HashVO();
				for (int i = 1; i < rsmd.getColumnCount() + 1; i++) {
					Object o = rs.getObject(i);
					Object value = null;
					if (rsmd.getColumnType(i) == Types.DECIMAL
							|| rsmd.getColumnType(i) == Types.DOUBLE
							|| rsmd.getColumnType(i) == Types.DOUBLE
							|| rsmd.getColumnType(i) == Types.FLOAT) {
						value = (o == null ? new UFDouble("0") : new UFDouble(
								o.toString()));
					} else if (rsmd.getColumnType(i) == Types.NUMERIC) {
						if (o != null && o.toString().indexOf(".") < 0
								&& o.toString().length() < 3) {
							value = new Integer(rs.getInt(i));
						} else {
							value = (o == null ? new UFDouble("0")
									: new UFDouble(o.toString()));
						}
					} else if (rsmd.getColumnType(i) == Types.INTEGER
							|| rsmd.getColumnType(i) == Types.SMALLINT) {
						value = new Integer(rs.getInt(i));
					} else if (rsmd.getColumnType(i) == Types.DATE) {
						// value = new UFDate(rs.getDate(i));
						value = rs.getString(i);
					} else {
						value = rs.getString(i);
					}
					voTmp.setAttributeValue(rsmd.getColumnName(i), value);

				}
				vResult.add(voTmp);
				irownum++;
				if (irownum == MAXROWS) {
					break;
				}
			}
		} catch (Exception ex) {
			// ex.printStackTrace();
			throw ex;
		} finally {
			try {
				if (con != null)
					con.close();
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs = null;
			} catch (Exception ex) {
			}
		}
		HashVO[] vos = new HashVO[vResult.size()];
		vResult.copyInto(vos);
		return vos;
	}

	public Object exec(String sql) throws Exception {
		sql = sql.trim();
		if (sql.toLowerCase().startsWith("select")) {
			return queryVOBySql(sql);
		} else if (sql.startsWith("call ")) {
			callProc(sql);
		} else {
			executeUpdate(sql);
		}
		return "exec over!";
	}

	public void assignClientTask() {
		String sql = "select cuserid from sm_user where cuserid not in (select cuserid from pub_clienttask_deliver)";
		String sql1 = "select pk_task from pub_clienttask_def where taskclass = 'nc.monitor.client.plugin.TestMain'";
		String insertSql = "insert into pub_clienttask_deliver (pk_taskdeliver, pk_task, cuserid, exemode, locktag) values (?,?,?,'repeated','N')";
		String updataSql = "update pub_clienttask_deliver set locktag  = 'N' where locktag = 'Y'";
		try {

			HashVO[] vos = queryVOBySql(sql);
			if (!Toolkit.isEmpty(vos)) {
				HashVO pkTaskVO = queryVOBySql(sql1)[0];
				String pkTask = pkTaskVO.getStringValue("pk_task");
				List<List<?>> params = new ArrayList<List<?>>();
				int i = 0;
				long ts = System.currentTimeMillis();
				for (HashVO vo : vos) {
					List<Object> param = new ArrayList<Object>();
					param.add(ts + "_" + i++);
					param.add(pkTask);
					param.add(vo.getStringValue("cuserid"));
					params.add(param);
				}
				executeUpdate(insertSql, params);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			this.executeUpdate(updataSql);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void callProc(String sql) throws Exception {
		Connection con = null;
		try {
			con = getConn();
			CallableStatement call = con.prepareCall(sql);
			call.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				con.close();
			} catch (Exception e) {

			}
		}
	}

	public String getDbDesc() {
		return dbDesc;
	}

	public void setDbDesc(String dbDesc) {
		this.dbDesc = dbDesc;
	}

	public static final void main(String[] args) {
		DBExecutor exe = new DBExecutor("com.ibm.db2.jcc.DB2Driver",
				"jdbc:db2://127.0.0.1:50000/db2test", "administrator",
				"iloveyou");
		try {
			exe.exec("alter table test add column ff varchar(10)");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
