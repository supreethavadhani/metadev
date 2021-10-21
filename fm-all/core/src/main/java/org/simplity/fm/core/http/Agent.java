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
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.UserContext;
import org.simplity.fm.core.app.App;
import org.simplity.fm.core.app.IApp;
import org.simplity.fm.core.serialize.ISerializer;
import org.simplity.fm.core.serialize.gson.JsonInputObject;
import org.simplity.fm.core.serialize.gson.JsonSerializer;
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.service.IServiceContext;
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
	/**
	 * various headers that we respond back with
	 */
	public static final String[] HDR_NAMES = { "Access-Control-Allow-Methods", "Access-Control-Allow-Headers",
			"Access-Control-Max-Age", "Connection", "Cache-Control", "Accept" };
	/**
	 * values for the headers
	 */
	public static final String[] HDR_TEXTS = { "POST, GET, OPTIONS",
			"content-type, authorization, " + Conventions.Http.HEADER_SERVICE, "1728000", "Keep-Alive",
			"no-cache, no-store, must-revalidate", "application/json" };

	/**
	 *
	 * @return an instance of the agent
	 */
	public static Agent getAgent() {
		return new Agent();
	}

	private HttpServletRequest req;
	private HttpServletResponse resp;
	private final IApp app = App.getApp();

	private String token;
	private UserContext session;
	private String userId;
	private String serviceName;
	private IService service;
	private JsonObject inputData;
	private IServiceContext ctx;

	/**
	 * response for a pre-flight request
	 *
	 * @param req
	 *
	 * @param resp
	 */
	public void setOptions(final HttpServletRequest req, final HttpServletResponse resp) {
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
	 * @param request
	 * @param response
	 * @throws IOException
	 *             IO exception
	 *
	 */
	public void serve(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		this.req = request;
		this.resp = response;
		/*
		 * process the request header to get service, session and user
		 */
		this.processHeader();
		if (this.serviceName == null) {
			logger.error("requested service {} is not served on this app.", this.serviceName);
			return;
		}

		final StringWriter writer = new StringWriter();
		final ISerializer outputObject = new JsonSerializer(writer);
		this.ctx = this.app.getContextFactory().newContext(this.session, outputObject);

		this.service = this.app.getCompProvider().getService(this.serviceName, this.ctx);
		if (this.service == null) {
			logger.error("No service. Responding with 404");
			this.resp.setStatus(Conventions.Http.STATUS_INVALID_SERVICE);
			return;
		}

		if (this.userId == null) {
			if (this.service.serveGuests() == false) {
				logger.info("No user. Service {} requires an authenticated user.");
				this.resp.setStatus(Conventions.Http.STATUS_AUTH_REQUIRED);
				return;
			}
		} else {
			if (this.app.getAccessController().okToServe(this.service, this.ctx) == false) {
				logger.error("User {} does not have the preveleges for service {}. Responding with 404", this.userId,
						this.service.getId());
				this.resp.setStatus(Conventions.Http.STATUS_INVALID_SERVICE);
				return;
			}
		}

		this.readInput();
		if (this.inputData == null) {
			logger.info("Invalid JSON recd from client ");
			this.resp.setStatus(Conventions.Http.STATUS_INVALID_DATA);
			return;
		}

		/*
		 * we are ready to execute this service.
		 */
		this.app.getRequestLogger().log(this.userId, this.service.getId(), this.inputData.toString());

		try {
			this.service.serve(this.ctx, new JsonInputObject(this.inputData));
			if (this.ctx.allOk()) {
				logger.info("Service returned with All Ok");
			} else {
				logger.error("Service returned with error messages");
			}
		} catch (final Throwable e) {
			logger.error("internal Error", e);
			this.app.getExceptionListener().listen(this.ctx, e);
			this.ctx.addMessage(Message.newError(Message.MSG_INTERNAL_ERROR));
		}
		this.respond(writer.toString());
	}

	private void readInput() {
		if (this.req.getContentLength() == 0) {
			this.inputData = new JsonObject();
		} else {
			try (Reader reader = this.req.getReader()) {
				/*
				 * read it as json
				 */
				final JsonElement node = new JsonParser().parse(reader);
				if (!node.isJsonObject()) {
					return;
				}
				this.inputData = (JsonObject) node;

			} catch (final Exception e) {
				logger.error("Invalid data recd from client {}", e.getMessage());
			}
		}
		this.readQueryString();
	}

	private void respond(final String payload) {
		/*
		 * are we to set a user session?
		 */
		final UserContext seshan = this.ctx.getNewUserContext();
		boolean addToken = false;
		if (seshan != null) {
			if (this.token == null) {
				/*
				 * this is a new session. We have to create a token and send
				 * that to the client in the header as well
				 */
				this.token = UUID.randomUUID().toString();
				this.resp.setHeader(Conventions.Http.HEADER_SERVICE, this.token);
				logger.info("Auth token set to {} ", this.token);
				addToken = true;
			}
			App.getApp().getSessionCache().put(this.token, seshan);
		}
		try (Writer writer = this.resp.getWriter()) {
			writer.write("{\"");
			writer.write(Conventions.Http.TAG_ALL_OK);
			writer.write("\":");
			if (this.ctx.allOk()) {
				writer.write("true");
				if (addToken) {
					writer.write(",\"");
					writer.write(Conventions.Http.TAG_TOKEN);
					writer.write("\":\"");
					writer.write(this.token);
					writer.write('"');
				}
				if (payload != null && payload.isEmpty() == false) {
					writer.write(",\"");
					writer.append(Conventions.Http.TAG_DATA);
					writer.write("\":");
					writer.write(payload);
				}
			} else {
				writer.write("false");
			}
			writeMessage(writer, this.ctx.getMessages());
			writer.write("}");
		} catch (final Exception e) {
			e.printStackTrace();
			try {
				this.resp.sendError(500);
			} catch (final IOException e1) {
				//
			}
		}
	}

	/**
	 * @param writer
	 * @param messages
	 * @throws IOException
	 */
	private static void writeMessage(final Writer writer, final Message[] msgs) throws IOException {
		if (msgs == null || msgs.length == 0) {
			return;
		}
		writer.write(",\"");
		writer.write(Conventions.Http.TAG_MESSAGES);
		writer.write("\":[");
		boolean isFirst = true;
		for (final Message msg : msgs) {
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

	private void processHeader() {
		this.serviceName = this.req.getHeader(Conventions.Http.HEADER_SERVICE);
		if (this.serviceName == null) {
			logger.error("header {} not received. No service", Conventions.Http.HEADER_SERVICE);
			return;
		}

		logger.info("Requested service = {}", this.serviceName);
		this.token = this.req.getHeader(Conventions.Http.HEADER_AUTH);
		if (this.token == null) {
			logger.info("Request received with  no token. Assumed guest request");
		} else {
			this.session = this.app.getSessionCache().get(this.token);
			if (this.session == null) {
				logger.info("Token {} is not valid. possibly timed out. Treating this as a guest request", this.token);
				this.token = null;
			} else {
				this.userId = this.session.getUserId();
				logger.info("Request from authuenticated user {} ", this.userId);
			}
		}
	}

	private void readQueryString() {
		final String qry = this.req.getQueryString();
		if (qry == null) {
			return;
		}

		for (final String part : qry.split("&")) {
			final String[] pair = part.split("=");
			String val;
			if (pair.length == 1) {
				val = "";
			} else {
				val = decode(pair[1]);
			}
			this.inputData.addProperty(pair[0].trim(), val);
		}
	}

	private static String decode(final String text) {
		try {
			return URLDecoder.decode(text, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			/*
			 * we do know that this is supported. so, this is unreachable code.
			 */
			return text;
		}
	}
}
