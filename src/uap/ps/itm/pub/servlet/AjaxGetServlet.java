package uap.ps.itm.pub.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AjaxGetServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8761777455072846839L;

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter out = response.getWriter();

		// String username = request.getParameter("name");
		// String password =request.getParameter("password");
		//
		// if("zhangsan".equals(username)){
		// //�˴���out.print����out.println
		// out.print("������");//out.print()������ݽ��������jspҳ����callback������
		// }else{
		// out.print("����ʹ��");
		// }
		out.flush();
		out.close();
	}
}
