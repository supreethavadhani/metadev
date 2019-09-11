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
package org.simplity.fm.example;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.simplity.fm.core.http.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * serves as the main class as well as the handler
 * 
 * @author simplity.org
 *
 */
public class JettyServer extends AbstractHandler {
	private static final Logger logger = LoggerFactory.getLogger(JettyServer.class);
	private static final int STATUS_METHOD_NOT_ALLOWED = 405;

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String method = baseRequest.getMethod().toUpperCase();
		logger.info("Request path:{} and method {}", baseRequest.getPathInfo(), method);
		long start = System.currentTimeMillis();
		Agent agent = Agent.getAgent();
		agent.setOptions(baseRequest, response);
		
		if (method.equals("POST") || method.equals("GET")) {
			agent.serve(baseRequest, response, true);
		} else if (method.equals("OPTIONS")) {
			logger.info("Got a pre-flight request. responding generously.. ");
		} else {
			logger.error("Rejected a request with method {}", baseRequest.getMethod());
			response.setStatus(STATUS_METHOD_NOT_ALLOWED);
		}
		
		logger.info("Responded in {}ms", System.currentTimeMillis() - start);
		baseRequest.setHandled(true);
	}

	/**
	 * start jetty server on port 8080. To be extended to get run-time parameter
	 * for port, and error handling if port is in-use etc..
	 * <br/>
	 * Simply invoke this as java app to run the server (of course the class
	 * path etc.. are to be taken care of)
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		/*
		 * our App bootstrapp
		 */
		Bootstrapper.bootstrap();
		Server server = new Server(8080);
		server.setHandler(new JettyServer());
		server.start();
		server.join();
	}
}
