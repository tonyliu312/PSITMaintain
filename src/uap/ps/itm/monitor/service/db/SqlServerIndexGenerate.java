package uap.ps.itm.monitor.service.db;

import java.util.ArrayList;
import java.util.List;

import uap.ps.itm.monitor.RemoteException;
import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.IConfigConst;
import uap.ps.itm.monitor.pub.Toolkit;

/**
 *
 * <code>SqlServerIndexGenerate<code>
 * <strong></strong>
 * <p>ËµÃ÷£º
 * <li></li>
 * </p>
 * @since NC6.3
 * @version 2013-6-3 ÏÂÎç04:20:21
 * @author tangxx
 */

public class SqlServerIndexGenerate implements IGenerateIndex {

	@Override
	public HashVO[] generateIndex(DBExecutor excutor) throws RemoteException {
		String sql = "select upper(object_name(a.object_id)) tablename,upper(a.name) indexname,upper(c.name) indexcolumns"
				+ " from sys.indexes a,sys.index_columns b,sys.columns c"
				+ " where a.object_id=b.objecT_id"
				+ " and a.index_id=b.index_id"
				+ " and c.object_id=b.object_id and c.column_id=b.column_id"
				+ " order by  object_name(a.object_id),a.name,b.index_column_id";

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
