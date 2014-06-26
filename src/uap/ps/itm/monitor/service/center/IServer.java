package uap.ps.itm.monitor.service.center;


public interface IServer {
public void startServer();
public void stopServer();
public ServiceResource[] getServerResouce(ServiceDesc serviceDesc);
public void refreshEnv();
}
