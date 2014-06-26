package uap.ps.itm.monitor.service.loganalyze;

import java.util.ArrayList;

import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.Toolkit;

public abstract class AbstraceEngine implements ILogQueryEngine{
    public  HashVO[] filter(HashVO[] srcVos,Filter[] filter){
        if(filter==null||filter.length==0)
            return srcVos;
        if(Toolkit.isEmpty(srcVos))
            return srcVos;
        ArrayList<HashVO> al=new ArrayList<HashVO>();
        HashVO votmp=null;
        boolean bcheck=false;
        for(int i=0;i<srcVos.length;i++){
            votmp=srcVos[i];
            bcheck=true;
            for(int j=0;j<filter.length;j++){
                if(!filter[j].filter(votmp)){
                    bcheck=false;
                    break;
                }
            }
            if(bcheck)
                al.add(votmp);
        }
        return al.toArray(new HashVO[al.size()]);
    }

}
