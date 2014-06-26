package uap.ps.itm.monitor.service.center;

import java.io.Serializable;

public class ServiceResource implements Serializable {
    private static final long serialVersionUID = 1L;
    private String host = null;
    private String port = null;
    private String serviceName = null;
    private String displayName = null;
    private String requestUrl = null;
    private String requestClassName = null;
    private String userName = null;
    private String passWord = null;
    private String instanceName = null;
    private ServiceDesc serviceDesc=null;
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestClassName() {
        return requestClassName;
    }

    public void setRequestClassName(String requestClassName) {
        this.requestClassName = requestClassName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public ServiceDesc getServiceDesc() {
        return serviceDesc;
    }

    public void setServiceDesc(ServiceDesc serviceDesc) {
        this.serviceDesc = serviceDesc;
    }
    public String toString(){
    	return getDisplayName();
    }
}
