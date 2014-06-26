package uap.ps.itm.monitor.service.db;

import java.util.ArrayList;
import java.util.List;

import uap.ps.itm.monitor.RemoteException;
import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.IConfigConst;
import uap.ps.itm.monitor.pub.Toolkit;

/**
 *
 * <code>OracleIndexGenerate<code>
 * <strong></strong>
 * <p>ËµÃ÷£º
 * <li></li>
 * </p>
 * @since NC6.3
 * @version 2013-6-3 ÏÂÎç04:25:15
 * @author tangxx
 */

public class OracleIndexGenerate implements IGenerateIndex {

	@Override
	public HashVO[] generateIndex(DBExecutor excutor) throws RemoteException {

		String sql = "select a.table_name tablename, b.index_name indexname, b.column_name indexcolumns"
				+ "  from user_tables a, user_ind_columns b"
				+ "  where a.temporary = 'N'"
				+ "  and a.table_name = b.table_name"
				+ "  order by a.table_name, b.index_name, b.column_position";
		try {
			HashVO[] vos = excutor.queryVOBySql(sql);
			return dealVos(vos);
		} catch (Exception e) {
			throw new RemoteException(e.getMessage());
		}
	}

	private HashVO[] dealVos(HashVO[] vos) {
		List<HashVO> list = new ArrayList<HashVO>();
		String curtablename = null;
		String curindexname = null;
		String curcolumnames = null;
		for (int i = 0; i < vos.length; i++) {
			String tablename = vos[i].getStringValue(IConfigConst.INDEXTABLE);
			String indexname = vos[i].getStringValue(IConfigConst.INDEXNAME);
			String columnname = vos[i]
					.getStringValue(IConfigConst.INDEXCOLUMNS);
			if (Toolkit.isEmpty(curtablename)) {
				curtablename = tablename;
				curindexname = indexname;
				curcolumnames = columnname;
				continue;
			} else if (curtablename.equals(tablename)
					&& indexname.equals(curindexname)) {
				String columnames = curcolumnames;
				columnames += ",";
				columnames += columnname;
				curcolumnames = columnames;
				continue;
			} else {
				HashVO vo = new HashVO();
				vo.setAttributeValue(IConfigConst.INDEXTABLE, curtablename);
				vo.setAttributeValue(IConfigConst.INDEXNAME, curindexname);
				vo.setAttributeValue(IConfigConst.INDEXCOLUMNS, curcolumnames);
				list.add(vo);
				curtablename = tablename;
				curindexname = indexname;
				curcolumnames = columnname;

			}

		}
		HashVO vo = new HashVO();
		vo.setAttributeValue(IConfigConst.INDEXTABLE, curtablename);
		vo.setAttributeValue(IConfigConst.INDEXNAME, curindexname);
		vo.setAttributeValue(IConfigConst.INDEXCOLUMNS, curcolumnames);
		list.add(vo);
		return list.size() == 0 ? null : list.toArray(new HashVO[0]);

	}

}
