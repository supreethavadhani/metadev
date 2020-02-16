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
import java.util.Collection;

import org.simplity.fm.core.Message;
import org.simplity.fm.core.data.FormData;
import org.simplity.fm.core.data.FormDataTable;
import org.simplity.fm.core.data.SchemaData;
import org.simplity.fm.core.data.SchemaDataTable;
import org.simplity.fm.core.http.LoggedInUser;

/**
 * context for a service execution thread. App specific instance is made
 * available to all components that participate in the service execution path
 *
 * @author simplity.org
 *
 */
public interface IServiceContext {

	/**
	 *
	 * @param key
	 * @return object associated with this key, null if no such key, or teh
	 *         value is null
	 */
	Object getValue(String key);

	/**
	 * put an name-value pair in the context
	 *
	 * @param key
	 *            non-null
	 * @param value
	 *            null has same effect as removing it. hence remove not
	 *            provided.
	 */
	void setValue(String key, Object value);

	/**
	 * @return non-null user on whose behalf this service is requested
	 */
	LoggedInUser getUser();

	/**
	 *
	 * @return non-null writer for sending response to the request.
	 */
	Writer getResponseWriter();

	/**
	 *
	 * @return true if all ok. false if at least one error message is added to
	 *         the context;
	 */
	boolean allOk();

	/**
	 *
	 * @param message
	 *            non-null message
	 */
	void addMessage(Message message);

	/**
	 *
	 * @param messages
	 *            non-null messages
	 */
	void addMessages(Collection<Message> messages);

	/**
	 *
	 * @return non-null array all messages added so far. empty if no message
	 *         added so far;
	 */
	Message[] getMessages();

	/**
	 * @return tenantId, if this APP is designed for multi-tenant deployment.
	 *         null if it is not.
	 */
	Object getTenantId();

	/**
	 * messages is not necessarily all errors. Some clients may want to track
	 * errors.
	 *
	 * @return number errors accumulated in the context. Note that the count
	 *         gets reset if the messages are reset
	 */
	int getNbrErrors();

	/**
	 * serialize this data as response. Note that this can be called only once
	 * with success. any subsequent call will result in no action and a return
	 * value of false;
	 *
	 * @param schemaData
	 *            non-null;
	 * @return true if all ok. false if a response is already set.
	 * @throws IOException
	 *             while writing a serialized response based on this data
	 */
	boolean setAsResponse(SchemaData schemaData) throws IOException;

	/**
	 * serialize this data as response. Note that this can be called only once
	 * with success. any subsequent call will result in no action and a return
	 * value of false;
	 *
	 * @param table
	 *            non-null;
	 * @return true if all ok. false if a response is already set.
	 * @throws IOException
	 *             while writing a serialized response based on this data
	 */
	boolean setAsResponse(SchemaDataTable table) throws IOException;

	/**
	 * serialize this data as response. Note that this can be called only once
	 * with success. any subsequent call will result in no action and a return
	 * value of false;
	 *
	 * @param fd
	 *            non-null;
	 * @return true if all ok. false if a response is already set.
	 * @throws IOException
	 *             while writing a serialized response based on this data
	 */
	boolean setAsResponse(FormData fd) throws IOException;

	/**
	 * serialize this data as response. Note that this can be called only once
	 * with success. any subsequent call will result in no action and a return
	 * value of false;
	 *
	 * @param fdt
	 *            non-null;
	 * @return true if all ok. false if a response is already set.
	 * @throws IOException
	 *             while writing a serialized response based on this data
	 */
	boolean setAsResponse(FormDataTable fdt) throws IOException;
}
