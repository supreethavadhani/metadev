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
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.simplity.fm.core.ComponentProvider;
import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.service.DefaultContext;
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.service.IserviceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * Agent is the single-point-of-contact to invoke any service on this app.
 * Services are not to be invoked directly (bypassing the Agent) in production.
 * This design provides a simple and clean separation of web and service layer.
 * No code needs to be written for a service in the web layer.
 * 
 * @author simplity.org
 * 
 */
public class Agent {
	private static final Logger logger = LoggerFactory.getLogger(Agent.class);
	private static Agent singleInstance = new Agent();

	/**
	 * 
	 * @return an instance of the agent
	 */
	public static Agent getAgent() {
		return singleInstance;
	}

	/**
	 * TODO: cache manager to be used for session cache. Using a local map for
	 * the time being
	 */
	private Map<String, LoggedInUser> activeUsers = new HashMap<>();

	/**
	 * response for a pre-flight request
	 * 
	 * @param req
	 * 
	 * @param resp
	 */
	public void setOptions(HttpServletRequest req, HttpServletResponse resp) {
		for (int i = 0; i < Conventions.Http.HDR_NAMES.length; i++) {
			resp.setHeader(Conventions.Http.HDR_NAMES[i], Conventions.Http.HDR_TEXTS[i]);
		}
		/*
		 * we have no issue with CORS. We are ready to respond to any client so
		 * long the auth is taken care of
		 */
		resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
	}

	/**
	 * serve an in-bound request.
	 *
	 * @param req
	 *            http request
	 * @param resp
	 *            http response
	 * @param inputDataIsInPayload
	 * @throws IOException
	 *             IO exception
	 *
	 */
	public void serve(HttpServletRequest req, HttpServletResponse resp, boolean inputDataIsInPayload)
			throws IOException {
		logger.info("Started serving request {}", req.getPathInfo());
		LoggedInUser user = this.getUser(req);
		if (user == null) {
			logger.info("No User. Responding with auth required status");
			resp.setStatus(Conventions.Http.STATUS_AUTH_REQUIRED);
			return;
		}

		IService service = this.getService(req);
		if (service == null) {
			resp.setStatus(Conventions.Http.STATUS_INVALID_SERVICE);
			return;
		}

		JsonObject json = this.readContent(req);
		if (json == null) {
			logger.info("Invalid JSON recd from client ");
			resp.setStatus(Conventions.Http.STATUS_INVALID_DATA);
			return;
		}
		this.readQueryString(req, json);

		/*
		 * We allow the service to use output stream, but not input stream. This
		 * is a safety mechanism against possible measures to be taken when
		 * receiving payload from an external source
		 */
		Writer writer = new StringWriter();
		IserviceContext ctx = new DefaultContext(user, writer);
		try {
			service.serve(ctx, json);
			if (ctx.allOk()) {
				logger.info("Service returned with All Ok");
			} else {
				logger.error("Service returned with error messages");
			}
		} catch (Throwable e) {
			e.printStackTrace();
			String msg = e.getMessage();
			logger.error("Internal Error : {}", msg);
			ctx.addMessage(Message.newError(Message.MSG_INTERNAL_ERROR));
		}
		respond(resp, ctx, writer.toString());
	}

	private JsonObject readContent(HttpServletRequest req) {
		if (req.getContentLength() == 0) {
			return new JsonObject();
		}
		try (Reader reader = req.getReader()) {
			/*
			 * read it as json
			 */
			JsonElement node = new JsonParser().parse(reader);
			if (!node.isJsonObject()) {
				return null;
			}
			return (JsonObject) node;
		} catch (Exception e) {
			logger.error("Invalid data recd from client {}", e.getMessage());
			return null;
		}
	}

	private static void respond(HttpServletResponse resp, IserviceContext ctx, String payload) {
		try (Writer writer = resp.getWriter()) {
			writer.write("{\"");
			writer.write(Conventions.Http.TAG_ALL_OK);
			writer.write("\":");
			if (ctx.allOk()) {
				writer.write("true");
				if (payload != null && payload.isEmpty() == false) {
					writer.write(",\"");
					writer.append(Conventions.Http.TAG_DATA);
					writer.write("\":");
					writer.write(payload);
				}
			} else {
				writer.write("false");
			}
			writeMessage(writer, ctx.getMessages());
			writer.write("}");
		} catch (Exception e) {
			e.printStackTrace();
			try {
				resp.sendError(500);
			} catch (IOException e1) {
				//
			}
		}
	}

	/**
	 * @param writer
	 * @param messages
	 * @throws IOException
	 */
	private static void writeMessage(Writer writer, Message[] msgs) throws IOException {
		if (msgs == null || msgs.length == 0) {
			return;
		}
		writer.write(",\"");
		writer.write(Conventions.Http.TAG_MESSAGES);
		writer.write("\":[");
		boolean isFirst = true;
		for (Message msg : msgs) {
			if (msg == null) {
				continue;
			}
			if (isFirst) {
				isFirst = false;
			} else {
				writer.write(",");
			}
			msg.toJson(writer);
		}
		writer.write("]");
	}

	private void readQueryString(HttpServletRequest req, JsonObject json) {
		String qry = req.getQueryString();
		if (qry == null) {
			return;
		}

		for (String part : qry.split("&")) {
			String[] pair = part.split("=");
			String val;
			if (pair.length == 1) {
				val = "";
			} else {
				val = this.decode(pair[1]);
			}
			json.addProperty(pair[0].trim(), val);
		}
	}

	private IService getService(HttpServletRequest req) {
		String serviceName = req.getHeader(Conventions.Http.SERVICE_HEADER);
		if (serviceName == null) {
			logger.info("header {} not received", Conventions.Http.SERVICE_HEADER);

			return null;
		}
		IService service = ComponentProvider.getProvider().getService(serviceName);
		if (service == null) {
			logger.info("{} is not a service", serviceName);
		}
		return service;
	}

	/**
	 * temp method in the absence of real authentication and session. We use
	 * AUthorization token as userId as well
	 * 
	 * @param req
	 * @return
	 */
	private LoggedInUser getUser(HttpServletRequest req) {
		String token = req.getHeader(Conventions.Http.TOKEN_HEADER);
		if (token == null) {
			return null;
		}

		LoggedInUser user = this.activeUsers.get(token);
		if (user == null) {
			/*
			 * we assume that the token is valid when we get called. Hence we
			 * have to create a user. token is used as userId, there by allowing
			 * testing with different users
			 */
			LoggedInUser cu = new LoggedInUser(token, token);
			logger.info("USer {} successfully logged-in", token);
			this.activeUsers.put(token, cu);
			user = cu;
		}
		return user;
	}

	private String decode(String text) {
		try {
			return URLDecoder.decode(text, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			/*
			 * we do know that this is supported. so, this is unreachable code.
			 */
			return text;
		}
	}
}
