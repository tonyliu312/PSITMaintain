package uap.ps.itm.pub.servlet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;
import uap.ps.itm.monitor.pub.Domain;
import uap.ps.itm.monitor.pub.HashVO;
import uap.ps.itm.monitor.pub.LogTool;
import uap.ps.itm.monitor.pub.Toolkit;
import uap.ps.itm.monitor.service.center.ServerEnv;
import uap.ps.itm.pub.adapter.NMCServiceAdapter;
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
		// ¶ÁÊý¾Ý
		// ObjectInputStream input = new
		// ObjectInputStream(req.getInputStream());
		// Object oIn = null;
		// try {
		// oIn = input.readObject();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		String serviceName = req.getParameter(IServletConst.SERVICE_NAME);
		if (Toolkit.isEmpty(serviceName))
			return;
		String method = req.getParameter(IServletConst.METHOD);
		if (Toolkit.isEmpty(method))
			return;
		String param = req.getParameter(IServletConst.PARAM);

		RequestInfo reqinfo = null;
		// if (oIn instanceof Hashtable) {
		// request = getRequestInfo((Hashtable) oIn);
		// } else {
		// request = (RequestInfo) oIn;
		// }

		Class<?>[] parameterTypes = null;
		Object[] parameterValues = null;
		reqinfo = new RequestInfo();
		reqinfo.setServiceName(serviceName);
		reqinfo.setMethodName(method);
		reqinfo.setParametertypes(parameterTypes);
		reqinfo.setParameter(parameterValues);

		ServerEnv.getInstance().setRequestInfo(reqinfo);
		getInvoke().invoke(reqinfo, resp.getOutputStream());

		JSONObject resultJSON = new JSONObject();
		// resultJSON.accumulate(IServletConst.RESULT_SUCCESS,
		// IServletConst.RESULT_SUCCESS_VAL);
		// resultJSON.accumulate(IServletConst.RESULT_TYPE,
		// IServletConst.RESULT_STRING);
		// resultJSON.accumulate(IServletConst.RESULT_VALUE, "OK");

		resp.setContentType("application/x-json");
		PrintWriter out = resp.getWriter();
		out.println(resultJSON.toString());
		out.flush();
		out.close();

		HashVO vo = new HashVO();
		vo.setAttributeValue("ts", Toolkit.getCurTime());
		vo.setAttributeValue("remoteAddr", req.getRemoteAddr());
		vo.setAttributeValue("username", reqinfo.getUsername());
		vo.setAttributeValue("serivce", reqinfo.getServiceName());
		vo.setAttributeValue("method", reqinfo.getMethodName());
		getLogger().info(vo.toString0());

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
