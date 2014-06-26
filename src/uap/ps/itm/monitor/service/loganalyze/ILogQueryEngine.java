package uap.ps.itm.monitor.service.loganalyze;

import java.io.File;

import uap.ps.itm.monitor.pub.HashVO;

public interface ILogQueryEngine {
    public HashVO[] query(File file,Filter[] filter) throws Exception;

}
