package uap.ps.itm.pub.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class PSITMServletFilter implements Filter {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		PSITMResponseWrapper wrapper = new PSITMResponseWrapper(
				(HttpServletResponse) response);
		chain.doFilter(request, wrapper);
		byte[] bData = wrapper.getResponseData();
		ByteArrayInputStream bais = new ByteArrayInputStream(bData);
		ObjectInputStream ois = new ObjectInputStream(bais);
		String json = null;
		try {
			Object obj = ois.readObject();
			json = String.valueOf(obj);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PrintWriter writer = response.getWriter();
		writer.println(json);
		writer.flush();
		writer.close();
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}

}