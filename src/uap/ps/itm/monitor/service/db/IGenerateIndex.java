package uap.ps.itm.monitor.service.db;

import uap.ps.itm.monitor.RemoteException;
import uap.ps.itm.monitor.pub.HashVO;

/**
 *
 * <code>IGenerateIndex<code>
 * <strong></strong>
 * <p>ËµÃ÷£º
 * <li></li>
 * </p>
 * @since NC6.3
 * @version 2013-6-3 ÏÂÎç04:16:30
 * @author tangxx
 */

public interface IGenerateIndex {

	public HashVO[] generateIndex(DBExecutor excutor) throws RemoteException;


}
