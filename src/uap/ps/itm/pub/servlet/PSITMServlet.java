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
			// String userStr = readJSONString(request);// 得到requestContext
			// JSONObject jsonObj = JSONObject.fromObject(userStr);//
			// 转换成JSONObject
			// System.out.println(jsonObj.getInt("userId"));//
			// 得到JSONObject的userId值
			// System.out.println(jsonObj.getString("name"));

			String serviceName = request
					.getParameter(IServletConst.SERVICE_NAME);
			String method = request.getParameter(IServletConst.METHOD);
			String param = request.getParameter(IServletConst.PARAM);

			JSONObject resultJSON = new JSONObject();// 构建一个JSONObject
			resultJSON.accumulate(IServletConst.RESULT_SUCCESS,
					IServletConst.RESULT_SUCCESS_VAL);
			resultJSON.accumulate(IServletConst.RESULT_TYPE,
					IServletConst.RESULT_STRING);
			resultJSON.accumulate(IServletConst.RESULT_VALUE, "OK");
			// resultJSON.accumulate("errNum", 1);
			// resultJSON.accumulate("errInfo", "成功");

			response.setContentType("application/x-json");// 需要设置ContentType
			// 为"application/x-json"

			PrintWriter out = response.getWriter();
			out.println(resultJSON.toString());// 向客户端输出JSONObject字符串
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}