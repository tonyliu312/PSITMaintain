package uap.ps.itm.monitor.service.filetransfer;

import java.io.Serializable;

public class FileInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	// 文件名
	private String filename = null;
	// 文件大小
	private long filesize = 0l;
	// 绝对路径.
	private String absFilePath = null;
	// 上次修改时间
	private String last_modify = null;
	// 是否路径.
	private boolean isDIR = false;
	//相对路径
	private String  relativePath=null;
	
	public boolean isDIR() {
		return isDIR;
	}

	public void setDIR(boolean isDIR) {
		this.isDIR = isDIR;
	}

	public String getAbsFilePath() {
		return absFilePath;
	}

	public void setAbsFilePath(String absFilePath) {
		this.absFilePath = absFilePath;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public long getFilesize() {
		return filesize;
	}

	public void setFilesize(long filesize) {
		this.filesize = filesize;
	}

	public String getLast_modify() {
		return last_modify;
	}

	public void setLast_modify(String last_modify) {
		this.last_modify = last_modify;
	}
	public String toString(){
		if(filename.equals(".."))
			return filename;
		return (isDIR()?"<dir>":"")+filename+"      "+filesize+"      "+last_modify;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}
}
