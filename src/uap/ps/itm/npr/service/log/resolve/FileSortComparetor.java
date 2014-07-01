package uap.ps.itm.npr.service.log.resolve;

import java.io.File;
import java.util.Comparator;

public class FileSortComparetor implements Comparator<File> {


	public int compare(File o1, File o2) {
		long file1Time = o1.lastModified();
		long file2Time = o2.lastModified();
		if (file1Time > file2Time)
			return 1;
		else if (file1Time < file2Time)
			return -1;
		return 0;
	}
}