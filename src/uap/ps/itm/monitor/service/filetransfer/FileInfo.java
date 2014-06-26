package uap.ps.itm.monitor.service.filetransfer;

import java.io.Serializable;

public class FileInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	// �ļ���
	private String filename = null;
	// �ļ���С
	private long filesize = 0l;
	// ����·��.
	private String absFilePath = null;
	// �ϴ��޸�ʱ��
	private String last_modify = null;
	// �Ƿ�·��.
	private boolean isDIR = false;
	//���·��
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
