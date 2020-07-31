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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.MessageType;
import org.simplity.fm.core.UserContext;
import org.simplity.fm.core.app.ApplicationError;
import org.simplity.fm.core.data.DbTable;
import org.simplity.fm.core.data.Field;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.data.RecordOverride;
import org.simplity.fm.core.serialize.ISerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple implementation of service context. Application can use this, extend it
 * ignore it!!
 *
 * @author simplity.org
 *
 */
public class DefaultContext implements IServiceContext {
	protected static Logger logger = LoggerFactory.getLogger(DefaultContext.class);

	protected final ISerializer serializer;
	protected final UserContext currentUtx;
	protected final Object userId;
	protected final List<Message> messages = new ArrayList<>();

	protected int nbrErrors = 0;
	protected UserContext newUtx;
	protected Object tenantId;
	protected boolean responseSet;
	/*
	 * created on need-basis because we expect this to be used sparingly..
	 */
	protected Map<String, Object> objects;

	/**
	 *
	 * @param session
	 * @param serializer
	 */
	public DefaultContext(final UserContext session, final ISerializer serializer) {
		this.serializer = serializer;
		this.currentUtx = session;
		/*
		 * apps may use an internal id instead. And that id can be part of the
		 * session
		 */
		if (session == null) {
			this.userId = null;
		} else {
			this.userId = session.getUserId();
		}
	}

	/**
	 * MUST be executed before this context is used in case this APP is designed
	 * for multi-tenant deployment
	 *
	 * @param tenantId
	 *            the tenantId to set
	 */
	protected void setTenantId(final Object tenantId) {
		this.tenantId = tenantId;
	}

	@Override
	public Object getUserId() {
		return this.userId;
	}

	@Override
	public ISerializer getSerializer() {
		return this.serializer;
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
	public void setAsResponse(final Record record) {
		if (this.responseSet) {
			throw new ApplicationError("Cannot set " + record.fetchName()
					+ " as response-record. A response is already set or the serializer is already in use.");
		}
		this.serializer.beginObject();
		this.serializer.fields(record);
		this.serializer.endObject();
		this.responseSet = true;
	}

	@Override
	public void setAsResponse(final Field[] fields, final Object[][] values) {
		if (this.responseSet) {
			throw new ApplicationError(
					"Cannot set fields  as response. A response is already set or the serializer is already in use.");
		}

		this.serializer.beginObject();
		this.serializer.name(Conventions.Http.TAG_LIST);
		this.serializer.beginArray();

		if (values != null && values.length > 0) {
			this.serializer.arrayElements(fields, values);
		}

		this.serializer.endArray();
		this.serializer.endObject();
		this.responseSet = true;
	}

	/**
	 * set this data table as the response. Note that the response would be like
	 * {"list":[{}...]} that is the standard response for a list/filter request
	 *
	 * @param table
	 */
	@Override
	public void setAsResponse(final DbTable<?> table) {
		if (this.responseSet) {
			throw new ApplicationError(
					"Cannot set a dbTable as response-record. A response is already set or the serializer is already in use.");
		}
		logger.info("Setting table as list inside the response object");
		this.serializer.beginObject();
		this.serializer.name(Conventions.Http.TAG_LIST);
		this.serializer.beginArray();

		if (table != null) {
			table.forEach(record -> {
				this.serializer.beginObject();
				this.serializer.fields(record);
				this.serializer.endObject();
			});
		}

		this.serializer.endArray();
		this.serializer.endObject();
		this.responseSet = true;
	}

	@Override
	public void setAsResponse(final Record header, final String childName, final DbTable<?> lines) {
		if (this.responseSet) {
			throw new ApplicationError(
					"Cannot set a dbTable as response-record. A response is already set or the serializer is already in use.");
		}
		this.serializer.beginObject();
		this.serializer.fields(header);
		this.serializer.name(childName);
		this.serializer.beginArray();

		if (lines != null && lines.length() > 0) {
			lines.forEach(record -> {
				this.serializer.beginObject();
				this.serializer.fields(record);
				this.serializer.endObject();
			});
		}

		this.serializer.endArray();
		this.serializer.endObject();
		this.responseSet = true;
	}

	@Override
	public void setAsResponse(final Record header, final String childName, final List<? extends Record> lines) {
		if (this.responseSet) {
			throw new ApplicationError(
					"Cannot set a dbTable as response-record. A response is already set or the serializer is already in use.");
		}
		this.serializer.beginObject();
		this.serializer.fields(header);
		this.serializer.name(childName);
		this.serializer.beginArray();

		if (lines != null && lines.size() > 0) {
			lines.forEach(record -> {
				this.serializer.beginObject();
				this.serializer.fields(record);
				this.serializer.endObject();
			});
		}
		this.serializer.endArray();
		this.serializer.endObject();
		this.responseSet = true;
	}

	@Override
	public UserContext getCurrentUserContext() {
		return this.currentUtx;
	}

	@Override
	public UserContext getNewUserContext() {
		return this.newUtx;
	}

	@Override
	public void setNewUserContext(final UserContext utx) {
		this.newUtx = utx;

	}

	@Override
	public String getRecordOverrideId(final String recordName) {
		return this.getCurrentUserContext().getRecordOverrideId(recordName);
	}

	@Override
	public RecordOverride getRecordOverride(final String recordName) {
		return this.getCurrentUserContext().getRecordOverride(recordName);
	}

	@Override
	public String getFormOverrideId(final String formName) {
		return this.getCurrentUserContext().getFormOverrideId(formName);
	}

}
