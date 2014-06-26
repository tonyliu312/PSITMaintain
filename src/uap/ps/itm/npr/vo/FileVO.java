package uap.ps.itm.npr.vo;

import java.io.Serializable;

public class FileVO implements Serializable {
	
	private static final long serialVersionUID = -4687247128472254706L;
	
	/**
	 * 文件长度
	 */
	private int fileSize;

	/**
	 * 文件名
	 */
	private String name;
	
	/**
	 * 内容
	 */
	private byte[] content;
	
	/**
	 * 是否备份
	 */
	private boolean backup;
	
	/**
	 * id
	 */
	private String id;
	
	private int lastModified;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public boolean isBackup() {
		return backup;
	}

	public void setBackup(boolean backup) {
		this.backup = backup;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getLastModified() {
		return lastModified;
	}

	public void setLastModified(int lastModified) {
		this.lastModified = lastModified;
	}

		

}
