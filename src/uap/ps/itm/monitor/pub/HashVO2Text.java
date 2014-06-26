package uap.ps.itm.monitor.pub;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class HashVO2Text {
	public static void writeToFile(String filename, HashVO[] vos,String prefix) {
		if (Toolkit.isEmpty(filename))
			return;
		if (Toolkit.isEmpty(vos))
			return;
		File file = new File(filename);
		try {
			if (!file.exists())
				file.createNewFile();
			writeToFile(file, vos, prefix);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeToFile(File file, HashVO[] vos,String prefix) {
		  if (Toolkit.isEmpty(vos))
	            return;
	        DataOutputStream out = null;
	        try {
	            if (!file.exists())
	                file.createNewFile();
	            out = new DataOutputStream(new FileOutputStream(file));
	            out.write(getText(vos, prefix).getBytes());
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            if (out != null) {
	                try {
	                    out.close();
	                } catch (Exception e) {

	                }
	            }
	        }
	}

	public static String getText(HashVO[] vos, String prefix) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < vos.length; i++) {
            vos[i].setFldPrefix(prefix);
            sb.append(vos[i].toString0());
            sb.append("\n");
        }
        return sb.toString();
	}
	public static final void main(String[] args){
		HashVO vo=new HashVO();
		StringBuffer sb=new StringBuffer();
		long l=13323234333313l;
		sb.append("$costtime="+l);
		vo.setAttributeValue("msg", sb.toString());
		writeToFile("c:\\tttt.txt", new HashVO[]{vo}, "$");
	}
}
