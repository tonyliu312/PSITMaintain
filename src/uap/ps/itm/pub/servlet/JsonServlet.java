package uap.ps.itm.pub.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

public class JsonServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5651506731436930288L;

	/**
	 * Constructor of the object.
	 */
	public JsonServlet() {
		super();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			String userStr = readJSONString(request);// �õ�requestContext
			JSONObject jsonObj = JSONObject.fromObject(userStr);// ת����JSONObject
//			System.out.println(jsonObj.getInt("userId"));// �õ�JSONObject��userIdֵ
//			System.out.println(jsonObj.getString("name"));
			
			String serviceName = jsonObj.getString("")

			JSONObject resultJSON = new JSONObject();// ����һ��JSONObject
//			resultJSON.accumulate("errNum", 1);
//			resultJSON.accumulate("errInfo", "�ɹ�");

			response.setContentType("application/x-json");// ��Ҫ����ContentType
															// Ϊ"application/x-json"
			PrintWriter out = response.getWriter();
			out.println(resultJSON.toString());// ��ͻ������JSONObject�ַ���
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String readJSONString(HttpServletRequest request) {
		StringBuffer json = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null) {
				json.append(line);
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return json.toString();
	}

}