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

import java.io.Writer;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.JsonUtil;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.ValueLists;
import org.simplity.fm.core.validn.IValueList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * handles request to get drop-down values for a field, typically from a client
 * @author simplity.org
 *
 */
public class ListService implements IService{
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
		//privatised for a singleton pattern
	}

	@Override
	public void serve(IserviceContext ctx, ObjectNode payload) throws Exception {
		
		String listName = ctx.getInputValue("list");
		if(listName == null) {
			ctx.addMessage(Message.newError("list is requred for listService"));
			return;
		}
		IValueList list = ValueLists.getList(listName);
		if(list == null) {
			ctx.addMessage(Message.newError("list " + listName + " is not configured"));
			return;
		}
		String key = null;
		if(list.isKeyBased()) {
			key = ctx.getInputValue("key");
			if(key == null) {
				ctx.addMessage(Message.newError("list " + listName + " is key based. key is missing in the request"));
				return;
			}
		}
		String[][] result = list.getList(key);
		if(result == null) {
			ctx.addMessage(Message.newError("list " + listName + " did not return any values for key "+ key));
			return;
		}
		
		@SuppressWarnings("resource")
		Writer writer = ctx.getResponseWriter();
		writer.write("{\"");
		writer.write(Conventions.Http.TAG_LIST);
		writer.write("\":");
		JsonUtil.write(writer, result);
		writer.write("}");
	}
}
