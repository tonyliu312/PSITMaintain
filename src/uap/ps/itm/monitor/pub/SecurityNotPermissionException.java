package uap.ps.itm.monitor.pub;

import uap.ps.itm.monitor.RemoteException;

public class SecurityNotPermissionException extends RemoteException {

    private static final long serialVersionUID = 1L;
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "û�в���Ȩ�ޣ���͹���Ա��ϵ��";
    }

    @Override
    public String getMessage() {
        // TODO Auto-generated method stub
        return "û�в���Ȩ�ޣ���͹���Ա��ϵ��";
    }
    public SecurityNotPermissionException(String msg){
        super(msg);
    }
    
}
