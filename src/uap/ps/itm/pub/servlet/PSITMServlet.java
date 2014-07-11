package uap.ps.itm.pub.servlet;

import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import nc.monitor.pub.HashVO;
import nc.monitor.pub.LogTool;
import nc.monitor.pub.Toolkit;
import nc.monitor.service.center.ServerEnv;
import nc.monitor.servlet.Invoker;
import nc.monitor.servlet.RequestInfo;
import nc.monitor.ui.pub.Domain;
import net.sf.json.JSONObject;
import uap.ps.itm.pub.consts.IServletConst;

public class PSITMServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	Invoker invoke = null;
	private static Logger logger = null;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		invoke(req, resp);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		invoke(req, resp);
	}

	public void invoke(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		RequestInfo request = null;
		resp.setContentType("application/x-json");
		request = this.getRequestInfo4JSON(req);
		request.setServer2Server(true);
		ServerEnv.getInstance().setRequestInfo(request);

		getInvoke().invoke(request, resp.getOutputStream());

		// JSONObject resultJSON = new JSONObject();
		// resultJSON.accumulate(IServletConst.RESULT_SUCCESS,
		// IServletConst.RESULT_SUCCESS_VAL);
		// resultJSON.accumulate(IServletConst.RESULT_TYPE,
		// IServletConst.RESULT_STRING);
		// resultJSON.accumulate(IServletConst.RESULT_VALUE, "OK");
		//
		//
		//
		// String str = new PSITMService().loadhistory();
		// PrintWriter out = resp.getWriter();
		// out.println(str);
		// out.flush();
		// out.close();

		HashVO vo = new HashVO();
		vo.setAttributeValue("ts", Toolkit.getCurTime());
		vo.setAttributeValue("remoteAddr", req.getRemoteAddr());
		vo.setAttributeValue("username", request.getUsername());
		vo.setAttributeValue("serivce", request.getServiceName());
		vo.setAttributeValue("method", request.getMethodName());
		getLogger().info(vo.toString0());

	}

	private RequestInfo getRequestInfo4JSON(HttpServletRequest req) {
		String serviceName = req.getParameter(IServletConst.SERVICE_NAME);
		if (Toolkit.isEmpty(serviceName))
			return null;
		String method = req.getParameter(IServletConst.METHOD_NAME);
		if (Toolkit.isEmpty(method))
			return null;
		String param = req.getParameter(IServletConst.PARAM);

		RequestInfo request = new RequestInfo();
		request.setServiceName(serviceName);
		request.setMethodName(method);

		return request;
	}

	private RequestInfo getRequestInfo4Ajax(String aoData) {
		JSONObject jsonObj = JSONObject.fromObject(aoData);
		String serviceName = jsonObj.getString(IServletConst.SERVICE_NAME);
		if (Toolkit.isEmpty(serviceName))
			return null;
		String method = jsonObj.getString(IServletConst.METHOD_NAME);
		if (Toolkit.isEmpty(method))
			return null;
		String param = jsonObj.getString(IServletConst.PARAM);

		RequestInfo request = new RequestInfo();
		request.setServiceName(serviceName);
		request.setMethodName(method);

		return request;
	}

	private Logger getLogger() {
		synchronized (this) {
			if (logger == null) {
				logger = LogTool.createLogger("access", "access", "remotecall");
			}
		}
		return logger;
	}

	private RequestInfo getRequestInfo(Hashtable h) {
		RequestInfo request = new RequestInfo();
		String servicename = getStringvalue("servicename", h);
		String methodname = getStringvalue("methodname", h);
		Class[] parametertype = (Class[]) h.get("parametertypes");
		Object[] parametervalue = (Object[]) h.get("parameter");
		String username = getStringvalue("username", h);
		String password = getStringvalue("password", h);
		request.setMethodName(methodname);
		request.setParameter(parametervalue);
		request.setParametertypes(parametertype);
		request.setServer2Server(false);
		request.setServiceName(servicename);
		Domain d = new Domain();
		d.setUsername(username);
		d.setPassword(password);
		request.setDomain(d);
		return request;

	}

	public Invoker getInvoke() {
		if (invoke == null) {
			invoke = new Invoker();
		}
		return invoke;
	}

	private String getStringvalue(String key, Hashtable h) {
		if (key == null)
			return null;
		Object o = h.get(key);
		if (o == null)
			return null;
		if (o instanceof String)
			return (String) o;

		return o.toString();
	}
}
