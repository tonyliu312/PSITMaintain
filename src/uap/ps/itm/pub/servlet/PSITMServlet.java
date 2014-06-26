package uap.ps.itm.pub.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;
import uap.ps.itm.pub.consts.IServletConst;


public class PSITMServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5651506731436930288L;
	

	/**
	 * Constructor of the object.
	 */
	public PSITMServlet() {
		super();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			String serviceName = request
					.getParameter(IServletConst.SERVICE_NAME);
			String method = request.getParameter(IServletConst.METHOD);
			String param = request.getParameter(IServletConst.PARAM);

			JSONObject resultJSON = new JSONObject();
//			resultJSON.accumulate(IServletConst.RESULT_SUCCESS,
//					IServletConst.RESULT_SUCCESS_VAL);
//			resultJSON.accumulate(IServletConst.RESULT_TYPE,
//					IServletConst.RESULT_STRING);
//			resultJSON.accumulate(IServletConst.RESULT_VALUE, "OK");
			
			

			response.setContentType("application/x-json");
			PrintWriter out = response.getWriter();
			out.println(resultJSON.toString());
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}