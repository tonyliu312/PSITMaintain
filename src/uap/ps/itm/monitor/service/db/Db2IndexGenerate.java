package uap.ps.itm.monitor.service.db;

import uap.ps.itm.monitor.RemoteException;
import uap.ps.itm.monitor.pub.HashVO;

/**
 *
 * <code>Db2IndexGenerate<code>
 * <strong></strong>
 * <p>ËµÃ÷£º
 * <li></li>
 * </p>
 * @since NC6.3
 * @version 2013-6-3 ÏÂÎç04:22:55
 * @author tangxx
 */

public class Db2IndexGenerate implements IGenerateIndex {

	@Override
	public HashVO[] generateIndex(DBExecutor excutor) throws RemoteException {
		String sql = "SELECT B.TABNAME tablename ,B.INDNAME indexname,replace(substr(B.COLNAMES,2),'+',',') indexcolumns"
				+ " FROM SYSCAT.TABLES A,SYSCAT.INDEXES B"
				+ " WHERE A.TABSCHEMA=B.TABSCHEMA"
				+ " AND A.TABNAME=B.TABNAME"
				+ " AND A.TYPE='T'"
				+ " AND B.INDSCHEMA=CURRENT_SCHEMA order by B.TABNAME,B.INDNAME ";
		try {
			HashVO[] vos = excutor.queryVOBySql(sql);
			return vos;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		}
	}

}
