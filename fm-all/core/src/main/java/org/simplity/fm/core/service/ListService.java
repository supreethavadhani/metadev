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

import org.simplity.fm.core.ComponentProvider;
import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.serialize.IInputObject;
import org.simplity.fm.core.serialize.ISerializer;
import org.simplity.fm.core.validn.IValueList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * handles request to get drop-down values for a field, typically from a client
 *
 * @author simplity.org
 *
 */
public class ListService implements IService {
	private static final ListService instance = new ListService();
	protected static final Logger logger = LoggerFactory.getLogger(ListService.class);

	/**
	 *
	 * @return non-null instance
	 */
	public static ListService getInstance() {
		return instance;
	}

	private ListService() {
		// privatised for a singleton pattern
	}

	@Override
	public String getId() {
		return Conventions.App.SERVICE_LIST;
	}

	@Override
	public void serve(final IServiceContext ctx, final IInputObject payload) throws Exception {
		final String listName = payload.getString("list");
		if (listName == null || listName.isEmpty()) {
			ctx.addMessage(Message.newError("list is required for listService"));
			return;
		}
		final IValueList list = ComponentProvider.getProvider().getValueList(listName);
		if (list == null) {
			ctx.addMessage(Message.newError("list " + listName + " is not configured"));
			return;
		}
		String key = null;
		if (list.isKeyBased()) {
			key = payload.getString("key");
			if (key == null || key.isEmpty()) {
				ctx.addMessage(Message.newError("list " + listName + " is key based. key is missing in the request"));
				return;
			}
		}
		final Object[][] result = list.getList(key, ctx);
		if (result == null) {
			ctx.addMessage(Message.newError("Error while getting values for list " + listName + " for key " + key));
			return;
		}
		if (result.length == 0) {
			logger.warn("List {} has no values for key {}. sending an empty response", listName, key);
		}
		writeOut(ctx.getSerializer(), result);
	}

	private static void writeOut(final ISerializer writer, final Object[][] rows) {
		writer.beginObject();
		writer.name(Conventions.Http.TAG_LIST);
		writer.beginArray();
		for (final Object[] row : rows) {
			writer.beginObject();

			writer.name("value");
			writer.primitiveObject(row[0]);

			writer.name("text");
			writer.value(row[1].toString());

			writer.endObject();
		}
		writer.endArray();
		writer.endObject();
	}
}
