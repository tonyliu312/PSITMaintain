package uap.ps.itm.pub.servlet;

import java.io.OutputStream;



public interface IHttpServletAdapt {
	public Object invoke(RequestInfo req, OutputStream out) throws Exception;

}
