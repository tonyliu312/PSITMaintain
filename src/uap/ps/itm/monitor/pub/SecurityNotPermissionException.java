package uap.ps.itm.monitor.pub;

import uap.ps.itm.monitor.RemoteException;

public class SecurityNotPermissionException extends RemoteException {

    private static final long serialVersionUID = 1L;
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "没有操作权限，请和管理员联系！";
    }

    @Override
    public String getMessage() {
        // TODO Auto-generated method stub
        return "没有操作权限，请和管理员联系！";
    }
    public SecurityNotPermissionException(String msg){
        super(msg);
    }
    
}
