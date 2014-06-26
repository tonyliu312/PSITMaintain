package uap.ps.itm.monitor.service.db;

import uap.ps.itm.monitor.pub.IConfigConst;

/**
 *
 * <code>GenerateIndexFactory<code>
 * <strong></strong>
 * <p>ËµÃ÷£º
 * <li></li>
 * </p>
 * @since NC6.3
 * @version 2013-6-3 ÏÂÎç04:18:04
 * @author tangxx
 */

public class GenerateIndexFactory {

	private static IGenerateIndex indexGenerate = null;


	public static IGenerateIndex createGenerateIndex(int type) {
		if (IConfigConst.ORACLE == type) {
			indexGenerate = new OracleIndexGenerate();
		} else if (IConfigConst.SQLSERVER == type) {
			indexGenerate = new SqlServerIndexGenerate();
		} else if (IConfigConst.DB2 == type) {
			indexGenerate = new Db2IndexGenerate();
		}
		return indexGenerate;

	}


}
