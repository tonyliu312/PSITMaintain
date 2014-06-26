/**
 * 
 */
package uap.ps.itm.npr.vo;

import java.io.Serializable;

/**
 * @author liuzy
 * 
 */
public class XPRFileVO implements Serializable {

	private static final long serialVersionUID = 469080382256429798L;

	private FileVO filevo = null;

	private String nprName = null;

	private String relativePath = null;

	/**
	 * @return the filevo
	 */
	public FileVO getFilevo() {
		return filevo;
	}

	/**
	 * @param filevo
	 *            the filevo to set
	 */
	public void setFilevo(FileVO filevo) {
		this.filevo = filevo;
	}

	/**
	 * @return the nprName
	 */
	public String getNprName() {
		return nprName;
	}

	/**
	 * @param nprName
	 *            the nprName to set
	 */
	public void setNprName(String nprName) {
		this.nprName = nprName;
	}

	/**
	 * @return the relativePath
	 */
	public String getRelativePath() {
		return relativePath;
	}

	/**
	 * @param relativePath
	 *            the relativePath to set
	 */
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

}
