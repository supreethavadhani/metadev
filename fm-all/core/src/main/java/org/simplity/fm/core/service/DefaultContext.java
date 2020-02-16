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

package org.simplity.fm.core.service;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.simplity.fm.core.Message;
import org.simplity.fm.core.MessageType;
import org.simplity.fm.core.data.FormData;
import org.simplity.fm.core.data.FormDataTable;
import org.simplity.fm.core.data.SchemaData;
import org.simplity.fm.core.data.SchemaDataTable;
import org.simplity.fm.core.http.LoggedInUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonWriter;

/**
 * Simple implementation of service context. Application can use this, extend it
 * ignore it!!
 *
 * @author simplity.org
 *
 */
public class DefaultContext implements IServiceContext {
	protected static Logger logger = LoggerFactory.getLogger(DefaultContext.class);
	protected List<Message> messages = new ArrayList<>();
	protected int nbrErrors = 0;
	protected Writer responseWriter;
	protected LoggedInUser loggedInUser;
	protected Object tenantId;
	protected boolean responseSet;
	/*
	 * created on need-basis because we expect this to be sued sparingly..
	 */
	protected Map<String, Object> objects;

	/**
	 *
	 * @param loggedInUser
	 * @param responseWriter
	 */
	public DefaultContext(final LoggedInUser loggedInUser, final Writer responseWriter) {
		this.responseWriter = responseWriter;
		this.loggedInUser = loggedInUser;
	}

	/**
	 * MUST be executed before this context is used in case this APP is designed
	 * for multi-tenant deployment
	 *
	 * @param tenantId
	 *            the tenantId to set
	 */
	public void setTenantId(final Object tenantId) {
		this.tenantId = tenantId;
	}

	@Override
	public LoggedInUser getUser() {
		return this.loggedInUser;
	}

	@Override
	public Writer getResponseWriter() {
		return this.responseWriter;
	}

	@Override
	public boolean allOk() {
		return this.nbrErrors == 0;
	}

	@Override
	public void addMessage(final Message message) {
		if (message == null) {
			return;
		}
		if (message.messageType == MessageType.Error) {
			this.nbrErrors++;
		}
		this.messages.add(message);
	}

	@Override
	public Message[] getMessages() {
		return this.messages.toArray(new Message[0]);
	}

	@Override
	public void addMessages(final Collection<Message> msgs) {
		for (final Message msg : msgs) {
			this.addMessage(msg);
		}
	}

	@Override
	public Object getTenantId() {
		return this.tenantId;
	}

	@Override
	public void setValue(final String key, final Object value) {
		if (this.objects == null) {
			this.objects = new HashMap<>();
		}
		this.objects.put(key, value);
	}

	@Override
	public Object getValue(final String key) {
		if (this.objects == null) {
			return null;
		}
		return this.objects.get(key);
	}

	@Override
	public int getNbrErrors() {
		return this.nbrErrors;
	}

	@Override
	public boolean setAsResponse(final SchemaData data) throws IOException {
		if (this.responseSet) {
			logger.error("A response is already set. Can not write data from {} again.", data.getClass().getName());
			return false;
		}
		try (final JsonWriter writer = new JsonWriter(this.responseWriter)) {
			writer.beginObject();
			data.serializeFields(writer);
			writer.endObject();
		}
		this.responseSet = true;
		return true;
	}

	@Override
	public boolean setAsResponse(final SchemaDataTable data) throws IOException {
		if (this.responseSet) {
			logger.error("A response is already set. Can not write data from {} again.", data.getClass().getName());
			return false;
		}
		try (final JsonWriter writer = new JsonWriter(this.responseWriter)) {
			data.serializeRows(writer);
		}
		this.responseSet = true;
		return true;
	}

	@Override
	public boolean setAsResponse(final FormData fd) throws IOException {
		if (this.responseSet) {
			logger.error("A response is already set. Can not write data from {} again.", fd.getClass().getName());
			return false;
		}
		try (final JsonWriter writer = new JsonWriter(this.responseWriter)) {
			writer.beginObject();
			fd.serializeFields(writer);
			writer.endObject();
		}
		this.responseSet = true;
		return true;
	}

	@Override
	public boolean setAsResponse(final FormDataTable data) throws IOException {
		if (this.responseSet) {
			logger.error("A response is already set. Can not write data from {} again.", data.getClass().getName());
			return false;
		}
		try (final JsonWriter writer = new JsonWriter(this.responseWriter)) {
			data.serializeRows(writer);
		}
		this.responseSet = true;
		return true;
	}
}
