/*
 * Copyright (c) 2019 simplity.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.simplity.fm.core.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is the entry point for <code>Agent</code> from a web-server (http-server
 * or servlet container like Tomcat or Jetty)
 * <br/>
 * <br/>
 * Our design is to have just one entry point for a web-app. We do not use REST
 * path standards. Instead, a service oriented approach is used , more like an
 * RPC. Name of service is expected as a header field. We use the the paradigm
 * <bold>response = serve(serviceName, request)</bold>
 * 
 * @author simplity.org
 * 
 */
@WebServlet(value = { "/a" })
public class Servlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	protected static final int STATUS_METHOD_NOT_ALLOWED = 405;

	@Override
	public void init() throws ServletException {
		super.init();
		/*
		 * we will certainly have something to do in the future..
		 */
	}

	@Override
	public void destroy() {
		super.destroy();
		/*
		 * we will certainly have something to do in the future..
		 */
	}

	/**
	 * we expect OPTIONS method only as a pre-flight request in a CORS
	 * environment. We have a ready response
	 */
	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Agent.getAgent().setOptions(req, resp);
	}

	/*
	 * we allow get, post and options. Nothing else
	 */
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setStatus(STATUS_METHOD_NOT_ALLOWED);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setStatus(STATUS_METHOD_NOT_ALLOWED);
	}

	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setStatus(STATUS_METHOD_NOT_ALLOWED);
	}

	@Override
	protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setStatus(STATUS_METHOD_NOT_ALLOWED);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Agent.getAgent().serve(req, resp, true);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Agent.getAgent().serve(req, resp, false);
	}
}
